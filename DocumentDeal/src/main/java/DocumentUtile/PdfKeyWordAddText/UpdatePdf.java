package DocumentUtile.PdfKeyWordAddText;

import DocumentUtile.WordToPdf;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.SneakyThrows;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 给pdf根据关键字添加文字  以及添加水印
 */
public class UpdatePdf {

    public static final String BODONIBLACK = "src/main/resources/fonts/STSONG.TTF"; // 宋体

    @SneakyThrows
    public static void main(String[] args) throws IOException {
//        String targetFile = WordToPdf.getWordPath("xmcbb.pdf"); // 输出
        String targetFile = WordToPdf.getWordPath("wordAlter/111.pdf"); // 输出
        String targetFile2 = WordToPdf.getWordPath("wordAlter/222.pdf"); // 输出
        manipulatePdf(targetFile,targetFile2);
        System.out.println(targetFile2);

        File file = new File(targetFile);//模拟文件位置
        if(file!=null && file.exists()){
            FileInputStream is = null;
            try {
                //本地根据文件路径获取文件流
                is = new FileInputStream(file);
                long length = file.length();
                byte[] fileBytes = new byte[(int)length];
                is.read(fileBytes);
                //进行pdf文件修改
                String file1 = pdfFzSqsj(targetFile,fileBytes,"采购用途");
//                if(file1!=null && file1.exists()){
                    System.out.println("修改pdf完成！");
                    System.out.println(file1);
//                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(is!=null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 根据pdf的固定关键字，查找进行pdf相关位置增加文字
     * @param tpeHtcxyw 文件流
     * @param str 关键字
     * @return 修改后的文件
     */
    public static String pdfFzSqsj(String path,byte[] tpeHtcxyw, String str) {
        PdfStamper stamper = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            //获取要写入的申请时间
            String sj  =sdf.format(new Date());
            PdfReader reader = new PdfReader(tpeHtcxyw);
            //创建文件路径
            String fileNamepdfBefore = path;
            String fileNamepdf = path.substring(path.lastIndexOf("/") + 1);
            String filePathAfter = WordToPdf.getWordPath("wordAlter/") + fileNamepdf;
            System.out.println("filePath="+filePathAfter);
            File directory = new File(filePathAfter);
            //如果pdf保存路径不存在，则创建路径
            if (!directory.exists()) {
                directory.mkdirs();
            }
//            String filename = UUID.randomUUID()+"_after.pdf";//修改后文件
//            String filename1 = UUID.randomUUID()+"_before.pdf";//修改前文件 再输出一遍，校验自己在修改之前拿到正确的文件流
            System.out.println("file:"+filePathAfter);
            File file = new File(filePathAfter);//修改后文件
            File file1 = new File(fileNamepdfBefore);//修改前文件 再输出一遍，校验自己在修改之前拿到正确的文件流
            //将写入临时文件
            FileOutputStream  foss = new FileOutputStream (file1);
            foss.write(tpeHtcxyw);
            foss.close();
            //设置字体
            BaseFont baseFont =  BaseFont.createFont(BODONIBLACK, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            Font font = new Font(baseFont,12,Font.NORMAL);
            stamper = new PdfStamper(reader, new FileOutputStream(file));

            //最上面添加遮罩层 遮盖水印方法
            PdfContentByte canvas = stamper.getOverContent(1);
            canvas.saveState();
            canvas.setColorFill(BaseColor.WHITE);
            canvas.rectangle(0, 815, 1000, 20);
            canvas.fill();
            canvas.restoreState();
            System.out.println("去除水印成功！");

            //对于已经固话的pdf回填申请时间
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                PdfContentByte over = stamper.getOverContent(i);
                ColumnText columnText = new ColumnText(over);
                if(i==1){
                    //根据关键字获取关键字位置
                    float[] po =  Html2Pdf.getGzzzb(str,tpeHtcxyw);
                    if(po != null &&  po[0]==1.00f){
                        // 方法setSimpleColumn(float llx, float lly, float urx, float ury)
                        // llx 和 urx  最小的值决定离左边的距离. lly 和 ury 最大的值决定离下边的距离
//                        columnText.setSimpleColumn( po[1]+40f,  po[2]-30f, 600, 0);
                        columnText.setSimpleColumn( po[1]+46f,  po[2]-9f, 500, 0);
                        //将时间文本创建成对象
                        Paragraph elements = new Paragraph(0, new Chunk(new Chunk(sj)));
                        // 设置字体，如果不设置添加的中文将无法显示
                        elements.setFont(font);
                        columnText.addElement(elements);
                        columnText.go();
                        System.out.println("添加文字成功！");
                    } else {
                        System.out.println("未找到关键字，请核查");
                        return null;
                    }
                }
            }
            return filePathAfter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(stamper!=null){
                try {
                    stamper.close();
                } catch (DocumentException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 增加遮盖层
    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
//        PdfContentByte canvas = stamper.getUnderContent(1);
        PdfContentByte canvas = stamper.getOverContent(1);
        canvas.saveState();
        canvas.setColorFill(BaseColor.WHITE);
        canvas.rectangle(0, 815, 1000, 20);
        canvas.fill();
        canvas.restoreState();
        stamper.close();
        reader.close();
    }
    /**
     *添加水印
     */
    public static InputStream addText(InputStream inputPDFFile, String name)
            throws Exception {
        PdfReader reader = new PdfReader(inputPDFFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader,byteArrayOutputStream );
        int total = reader.getNumberOfPages() + 1;
        BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
        PdfGState gs = new PdfGState();

        PdfContentByte content1;
        for (int i = 1; i < total; i++) {
            content1 = stamper.getOverContent(i);
            content1.beginText();
            content1.setGState(gs);
            content1.setColorFill(BaseColor.DARK_GRAY); //水印颜色
            content1.setFontAndSize(base, 56); //水印字体样式和大小
            content1.showTextAligned(Element.ALIGN_CENTER,name, 300, 300, 30); //水印内容和水印位置
            content1.endText();
        }
        gs.setFillOpacity(0.2f);
        System.out.println("PDF水印添加完成！");
        reader.close();
        byteArrayOutputStream.close();
        stamper.close();
//        inputPDFFile.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}

