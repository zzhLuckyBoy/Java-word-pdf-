package DocumentUtile;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * docx  word文档替换内容，可循环替换列表
 *
 * @author zzh
 */
public class WordX {

    public void main(String[] args) throws IOException {
        //创建临时文件
        String filepath = WordToPdf.getWordPath("wordAlter");
        String srcFile = WordToPdf.getWordPath("xmcbb.docx"); // 输入
        String targetFile = WordToPdf.getWordPath("wordAlter/xmcbb.docx"); // 输出

        // 获取原文件路径
        Path srcPath = Paths.get(srcFile);
        // 新生成文件路径及文件名
        Path targetPath = Paths.get(targetFile);
        // 复制文件
        FileUtil.copy(srcPath, targetPath);
        // 模拟数据
        Map<String, Object> map = new HashMap<>();
        map.put("date", "2021-12-27");
//        map.put("name1", "ddbb");
        List<Map<String, Object>> table2 = new ArrayList<Map<String, Object>>();
        Map<String, Object> map4 = new HashMap<>();
        map4.put("name", "tom");
        map4.put("number", "sd1234");
        map4.put("address", "上海");

        Map<String, Object> map5 = new HashMap<>();
        map5.put("name", "seven");
        map5.put("number", "sd15678");
        map5.put("address", "北京");

        Map<String, Object> map6 = new HashMap<>();
        map6.put("name", "lisa");
        map6.put("number", "sd9078");
        map6.put("address", "广州");

        table2.add(map4);
        table2.add(map5);
        table2.add(map6);

        Map<String, Object> wordMap = new HashMap<>();
        wordMap.put("parametersMap", map);
//        wordMap.put("table2", table2);
        wordMap.putAll(map);

        InputStream is = new FileInputStream(targetFile);
        WordX wordTemplate = new WordX(is);

        wordTemplate.replaceDocx(wordMap);

        OutputStream os = new FileOutputStream(targetFile);
        wordTemplate.getDocument().write(os);
    }

    private static final Logger log = LoggerFactory.getLogger(WordX.class);

    private XWPFDocument document;

    public XWPFDocument getDocument() {
        return document;
    }

    public void setDocument(XWPFDocument document) {
        this.document = document;
    }

    /**
     * 构造方法初始化 读取模板<br>
     * 1、文本框未处理
     *
     * @param is
     */
    public WordX(InputStream is) throws IOException {
        document = new XWPFDocument(is);
    }

    /**
     * 替换word模板的
     *
     * @param dataMap
     */
    public void replaceDocx(Map<String, Object> dataMap) {

        // 当前表格索引
        int curTab = 0;
        // 所有对象（段落+表格）
        List<IBodyElement> elements = document.getBodyElements();
        for (int i = 0; i < elements.size(); i++) {

            IBodyElement curEle = elements.get(i);
            BodyElementType eleType = curEle.getElementType();
            log.info(" ---- 节点类型：{}", eleType);

            // 段落
            if (BodyElementType.PARAGRAPH.equals(eleType)) {

                // 当前段落
                XWPFParagraph paragraph = curEle.getBody().getParagraphArray(i);
                // 处理段落
                dealParagraph(paragraph, dataMap);

                // 表格
            } else if (BodyElementType.TABLE.equals(eleType)) {

                // 当前表格
                List<XWPFTable> tableList = curEle.getBody().getTables();
                dealTable(tableList.get(curTab), curTab++, dataMap);
            }
        }
    }

    /**
     * 处理表格
     *
     * @param table
     * @param curTab
     * @param dataMap
     */
    private void dealTable(XWPFTable table, int curTab, Map<String, Object> dataMap) {

        if (ObjectUtil.isNull(table)) {
            return;
        }

        // 表格中所有文本
        String tableText = table.getText();
        // 文本含#{xxx}才解析
        if (matcherTab(tableText).find()) {

            log.info("存在需要遍历表格");
            // 第一行2列 第一列 #{foreach}:固定参数，第二列 数据源
            // 例: | #{foreach} | table1 |
            replaceTabForeach(table, dataMap, curTab);

        } else if (matcher(tableText).find()) {
            // 解析${XXX}
            replaceTabCommon(table, dataMap);
        }
    }

    /**
     * 需要遍历的表格
     *
     * @param table
     * @param dataMap
     */
    private void replaceTabForeach(XWPFTable table, Map<String, Object> dataMap, int curTab) {

        List<XWPFTableCell> firstRowCells = table.getRows().get(0).getTableCells();
        if (2 != firstRowCells.size()) {
            throw new RuntimeException(StrUtil.format("文档第{}个表格，若需要解析则第一行需设置为两列，第一列 #{foreach}:固定参数，第二列 数据源", curTab + 1));
        }

        // 数据源
        String dataSourceTag = firstRowCells.get(1).getText();
        if (StrUtil.isBlank(dataSourceTag)) {
            throw new RuntimeException(StrUtil.format("文档第{}个表格，数据源不能为空", curTab + 1));
        }

        // 需要遍历的列表数据
        List<Map<String, Object>> sourceData = (List<Map<String, Object>>) dataMap.get(dataSourceTag);

        // 开始遍历表格
        boolean isForeachTab = false;
        int startForeachIndex = 0;
        for (int i = 0; i < table.getRows().size(); i++) {

            XWPFTableRow row = table.getRow(i);
            // 一行的第一个cell的文本
            String rowFirstCellText = row.getCell(0).getText();
            if (rowFirstCellText.contains("#{foreach}")) {
                isForeachTab = true;
                continue;
            } else if (rowFirstCellText.contains("#{startForeach}")) {

                // #{startForeach} 标签位置
                startForeachIndex = i;
                // 模板行
                XWPFTableRow templateRow = table.getRow(i + 1);
                for (Map<String, Object> map : sourceData) {
                    XWPFTableRow newRow = table.insertNewTableRow(++i);
                    copyRow(newRow, templateRow);
                    replaceRow(newRow, map);
                }
                table.removeRow(i + 1);
            }
        }

        if (isForeachTab) {
            // 删除第一行
            table.removeRow(0);
            table.removeRow(--startForeachIndex);
        }
    }

    /**
     * 替换行
     *
     * @param row
     * @param dataMap
     */
    private void replaceRow(XWPFTableRow row, Map<String, Object> dataMap) {
        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                dealParagraph(paragraph, dataMap);
            }
        }
    }

    /**
     * 复制行
     *
     * @param destRow
     * @param srcRow
     */
    private void copyRow(XWPFTableRow destRow, XWPFTableRow srcRow) {

        for (int i = 0; i < srcRow.getTableCells().size(); i++) {
            destRow.addNewTableCell();
        }
        // 复制样式
        destRow.getCtRow().setTrPr(srcRow.getCtRow().getTrPr());
        for (int i = 0; i < destRow.getTableCells().size(); i++) {
            copyCell(destRow.getCell(i), srcRow.getCell(i));
        }
    }

    /**
     * 复制单元格
     *
     * @param destCell
     * @param srcCell
     */
    private void copyCell(XWPFTableCell destCell, XWPFTableCell srcCell) {
        // 复制样式
        destCell.getCTTc().setTcPr(srcCell.getCTTc().getTcPr());
        for (XWPFParagraph srcParagraph : srcCell.getParagraphs()) {
            copyParagraph(destCell.addParagraph(), srcParagraph);
        }
    }

    /**
     * 复制段落
     *
     * @param destParagraph
     * @param srcParagraph
     */
    private void copyParagraph(XWPFParagraph destParagraph, XWPFParagraph srcParagraph) {
        for (XWPFRun srcRun : srcParagraph.getRuns()) {
            copyRun(destParagraph.createRun(), srcRun);
        }
    }

    /**
     * 复制run
     *
     * @param destRun
     * @param srcRun
     */
    private void copyRun(XWPFRun destRun, XWPFRun srcRun) {
        destRun.getCTR().setRPr(srcRun.getCTR().getRPr());
        destRun.setText(srcRun.text());
    }

    /**
     * 根据<b>dataMap</b>替换表格里的<b>${xxxx}</b>
     *
     * @param table
     * @param dataMap
     */
    private void replaceTabCommon(XWPFTable table, Map<String, Object> dataMap) {
        if (ObjectUtil.isNull(table)) {
            return;
        }

        // 行
        for (XWPFTableRow row : table.getRows()) {
            // 列
            for (XWPFTableCell cell : row.getTableCells()) {
                // 段落
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    dealParagraph(paragraph, dataMap);
                }
            }
        }
    }

    /**
     * 处理段落需填充的字段
     *
     * @param paragraph 需要填充的段落
     * @param dataMap   填充的数据
     */
    public void dealParagraph(XWPFParagraph paragraph, Map<String, Object> dataMap) {

        if (ObjectUtil.isNull(paragraph)) {
            return;
        }

        // 段落文本内容
        String paragraphText = paragraph.getText();
        if (matcher(paragraphText).find()) {
            // 有匹配到需要填充内容
            // 段落所有的run
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                dealRun(paragraph, runs.get(i), i, dataMap);
            }
        }
    }

    /**
     * 处理run
     *
     * @param paragraph 段落
     * @param run       需要处理的run
     * @param i         run位置
     * @param dataMap   填充内容
     */
    public void dealRun(XWPFParagraph paragraph, XWPFRun run, int i, Map<String, Object> dataMap) {

        if (ObjectUtil.isNull(run)) {
            return;
        }

        // run内容
        String runText = run.text();
        // 匹配需要填充的字符
        Matcher matcher = matcher(runText);
        if (matcher.find()) {

            // 替换 没有则替换为空
            runText = matcher.replaceFirst(String.valueOf(dataMap.get(matcher.group(1))));
            // 新增run
            XWPFRun newRun = paragraph.insertNewRun(i);
            newRun.getCTR().setRPr(run.getCTR().getRPr());
            // 设置文本
            newRun.setText(runText);
            // 移除原本run
            paragraph.removeRun(i + 1);
        }
    }

    /**
     * ${XXX} 匹配，非贪心模式，匹配尽可能短的字符串 贪心算法的不足就是注重找局部最优解陷阱，有可能会找不到
     */
    public static final String PATTER = "\\$\\{(.+?)\\}";


    /**
     * #{XXX} 匹配，非贪心模式，匹配尽可能短的字符串
     */
    public static final String PATTER_TAB = "\\#\\{(.+?)\\}";
    /**
     * 段落匹配格式 ${xxx}
     *
     * @param str
     * @return
     */
    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile(PATTER, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(str);
    }

    /**
     * 表格匹配 #{xxx}
     *
     * @param str
     * @return
     */
    private Matcher matcherTab(String str) {
        Pattern pattern = Pattern.compile(PATTER_TAB, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(str);
    }


}
