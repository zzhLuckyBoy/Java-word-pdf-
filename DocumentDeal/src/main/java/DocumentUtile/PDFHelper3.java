package DocumentUtile;


import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import com.aspose.pdf.devices.PngDevice;
import com.aspose.pdf.devices.Resolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * pdf转其他类型文件
 */

public class PDFHelper3 {

    public static void main(String[] args) throws IOException {
        String targetFile = WordToPdf.getWordPath("wordAlter/111.pdf"); // 输出
        pdf2word(targetFile);
    }


    //转word
    public static String pdf2word(String pdfPath) {
        long old = System.currentTimeMillis();
        FileOutputStream os = null;
        try {
            String targetFile = WordToPdf.getWordPath("wordAlter/"); // 输出
//            String fileNamepdf = pdfPath.substring(pdfPath.lastIndexOf("/") + 1);
            String fileNamepdf=pdfPath.substring(pdfPath.lastIndexOf("/") + 1,pdfPath.lastIndexOf("."))+".docx";
            os = new FileOutputStream(targetFile + fileNamepdf);
            Document doc = new Document(pdfPath);
            doc.save(os, SaveFormat.DocX);
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 Word 共耗时：" + ((now - old) / 1000.0) + "秒");
            return targetFile + fileNamepdf;
        } catch (Exception e) {
            System.out.println("Pdf 转 Word 失败...");
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();// 如果此处出现异常，则out2流也会被关闭
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  null;
    }

    //转ppt
    public static void pdf2ppt(String pdfPath) {
        long old = System.currentTimeMillis();
        try {
            String wordPath=pdfPath.substring(0,pdfPath.lastIndexOf("."))+".ppt";
            FileOutputStream os = new FileOutputStream(wordPath);
            Document doc = new Document(pdfPath);
            doc.save(os, SaveFormat.Pptx);
            os.close();
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 PPT 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Pdf 转 PPT 失败...");
            e.printStackTrace();
        }
    }

    //转excel
    public static void pdf2excel(String pdfPath) {
        long old = System.currentTimeMillis();
        try {
            String wordPath=pdfPath.substring(0,pdfPath.lastIndexOf("."))+".xlsx";
            FileOutputStream os = new FileOutputStream(wordPath);
            Document doc = new Document(pdfPath);
            doc.save(os, SaveFormat.Excel);
            os.close();
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 EXCEL 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Pdf 转 EXCEL 失败...");
            e.printStackTrace();
        }
    }

    //转html
    public static void pdf2Html(String pdfPath) {
        long old = System.currentTimeMillis();
        try {
            String htmlPath=pdfPath.substring(0,pdfPath.lastIndexOf("."))+".html";
            Document doc = new Document(pdfPath);
            doc.save(htmlPath,SaveFormat.Html);
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 HTML 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Pdf 转 HTML 失败...");
            e.printStackTrace();
        }
    }

    //转图片
    public static void pdf2image(String pdfPath) {
        long old = System.currentTimeMillis();
        try {
            Resolution resolution = new Resolution(300);
            String dataDir=pdfPath.substring(0,pdfPath.lastIndexOf("."));
            File imageDir = new File(dataDir+"_images");
            imageDir.mkdirs();
            Document doc = new Document(pdfPath);
            PngDevice pngDevice = new PngDevice(resolution);
            for (int pageCount = 1; pageCount <= doc.getPages().size(); pageCount++) {
                OutputStream imageStream = new FileOutputStream(imageDir+"/"+pageCount+".png");
                pngDevice.process(doc.getPages().get_Item(pageCount), imageStream);
                imageStream.close();
            }
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 PNG 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Pdf 转 PNG 失败...");
            e.printStackTrace();
        }
    }
}
