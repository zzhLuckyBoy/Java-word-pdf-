package DocumentUtile;


import com.aspose.words.Shape;
import com.aspose.words.*;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Date;

/**
 * word转pdf工具类库
 */
public class WordToPdf {

    public static void main(String[] args) throws Exception {
        String pdf = "D://ynzcjcgxmcbb.pdf";
//        String pdf = "C:\\\\Users\\\\zzh\\\\Desktop\\\\益农镇村级采购项目呈报表.docx";
//        pdf2doc(pdf);
//        pdfTohtml(pdf);
//        file2pdf(pdf);
        // 添加水印
//        WordToPdf.wordAddwater(pdf);

        String targetFile = WordToPdf.getWordPath("xmcbb.docx"); // 输出
        file2pdf(targetFile);


    }

    public static String getWordPath(String path) throws IOException {
        File directory = new File("src/main/resources");
        return directory.getCanonicalPath()+ "/word/"+ path;
    }

    /**
     * @param toFilePath 文件夹路径 word转pdf
     */
    public static String file2pdf(String toFilePath) throws Exception {
        String type = toFilePath.substring(toFilePath.lastIndexOf(".") + 1);
        String htmFileName;
        //获取转换成PDF之后文件名
        if ("doc".equals(type)) {
            htmFileName = toFilePath.replace("doc", "pdf");
        } else if ("docx".equals(type)) {
            htmFileName = toFilePath.replace("docx", "pdf");
        } else {
            return null;
        }
        // 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
        //通过转换之后的PDF文件名,创建PDF文件
        File htmlOutputFile = new File(htmFileName);
        //获取文件输出流
        FileOutputStream os = new FileOutputStream(htmlOutputFile);
        //获取Doc文档对象模型
        Document doc = new Document(toFilePath);
        DocumentBuilder builder = new DocumentBuilder(doc);
        builder.setBold(false);
        //         设置纸张大小
//        builder.getPageSetup().setPaperSize(PaperSize.A3);

        //为doc文档添加水印
//        insertWatermarkText(doc, "test效果");
        //将doc文旦转换成PDF文件并输出到之前创建好的pdf文件中
        doc.save(os, SaveFormat.PDF);
        //关闭输出流
        os.close();
        return htmFileName;
    }


    /**
     * url 转file
     * @param path
     * @return
     */
    public  static String getNetUrlHttp(String path) throws IOException {
        //对本地文件命名，path是http的完整路径，主要得到资源的名字
        String newUrl = path;
        newUrl = newUrl.split("[?]")[0];
        String[] bb = newUrl.split("/");
        //得到最后一个分隔符后的名字
        String fileName = bb[bb.length - 1];
        //保存到本地的路径
//        String filePath="e:\\audio\\"+fileName;
        String filePath = WordToPdf.getWordPath("wordAlter/") + fileName; // 输出
        File file = null;

        URL urlfile;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try{
            //判断文件的父级目录是否存在，不存在则创建
            file = new File(filePath);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            try{
                //创建文件
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
            //下载
            urlfile = new URL(newUrl);
            inputStream = urlfile.openStream();
            outputStream = new FileOutputStream(file);

            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead=inputStream.read(buffer,0,8192))!=-1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null != outputStream) {
                    outputStream.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 为word文档添加水印
     *
     * @param doc           word文档模型
     * @param watermarkText 需要添加的水印字段
     * @throws Exception
     */
    public static void insertWatermarkText(Document doc, String watermarkText) throws Exception {
        Shape watermark = new Shape(doc, ShapeType.TEXT_PLAIN_TEXT);
        //水印内容
        watermark.getTextPath().setText(watermarkText);
        //水印字体
        watermark.getTextPath().setFontFamily("宋体");
        //水印宽度
        watermark.setWidth(500);
        //水印高度
        watermark.setHeight(100);
        //旋转水印
        watermark.setRotation(-40);
        //水印颜色
        watermark.getFill().setColor(Color.lightGray);
        watermark.setStrokeColor(Color.lightGray);
        watermark.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
        watermark.setRelativeVerticalPosition(RelativeVerticalPosition.PAGE);
        watermark.setWrapType(WrapType.NONE);
        watermark.setVerticalAlignment(VerticalAlignment.CENTER);
        watermark.setHorizontalAlignment(HorizontalAlignment.CENTER);
        Paragraph watermarkPara = new Paragraph(doc);
        watermarkPara.appendChild(watermark);
        for (Section sect : doc.getSections()) {
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_PRIMARY);
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_FIRST);
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_EVEN);
        }
        System.out.println("Watermark Set");
    }

    /**
     * 在页眉中插入水印
     *
     * @param watermarkPara
     * @param sect
     * @param headerType
     * @throws Exception
     */
    private static void insertWatermarkIntoHeader(Paragraph watermarkPara, Section sect, int headerType) throws Exception {
        HeaderFooter header = sect.getHeadersFooters().getByHeaderFooterType(headerType);
        if (header == null) {
            header = new HeaderFooter(sect.getDocument(), headerType);
            sect.getHeadersFooters().add(header);
        }
        header.appendChild(watermarkPara.deepClone(true));
    }



//    /**
//     * 给pdf文件流添加水印
//     * @param bos   输出文件流
//     * @param input 需要添加水印的文件路径
//     */
//    public static void setWatermark(BufferedOutputStream bos, String input)
//            throws Exception {
//        PdfReader reader = new PdfReader(input);
//        PdfStamper stamper = new PdfStamper(reader, bos);
//        int total = reader.getNumberOfPages() + 1;
//        PdfContentByte content;
//        BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
//        PdfGState gs = new PdfGState();
//
//        // 循环保证每一页 PDF 都能加上水印，有特殊需求也可以改成在固定页码上加水印
//        for (int i = 1; i < total; i++) {
//            // content = stamper.getOverContent(i);// 在内容上方加水印
//            content = stamper.getUnderContent(i);  //在内容下方加水印
//            gs.setFillOpacity(0.2f);
//            content.setGState(gs);
//
//            // 文字水印
//            content.beginText();
//            content.setColorFill(Color.black); // 颜色
//            content.setFontAndSize(base, 15); // 字体和大小
//            content.setTextMatrix(70, 200); // 设置文本矩阵
//            // 设置文字水印的文字内容  绝对值位置 和旋转度数
//            content.showTextAligned(Element.ALIGN_CENTER, "这里是文字水印！！", 150, 830, 0);
//            content.endText(); // 结束文字水印
//
//            // 图片水印
//            Image image = Image.getInstance("C:\\Users\\ca\\Desktop\\stamp.jpg"); // 图片水印位置
//            image.setAbsolutePosition(10, 300); // 设置 绝对位置
//            image.scaleToFit(185, 240);// 设置 图片缩放比例
//            image.setRotationDegrees(0); // 设置旋转角度
//            content.addImage(image); // 添加图片水印
//
//        }
//        stamper.close();
//    }

}