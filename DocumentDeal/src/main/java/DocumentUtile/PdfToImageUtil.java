package DocumentUtile;


import cn.hutool.core.util.URLUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * pdf与图片互相转换
 */
public class PdfToImageUtil {

    public static void main(String[] args) {
        try {
            //图片转pdf
            String pdf = "D://test11111.pdf";
            String urls = "https://qlxs.oss-cn-hangzhou.aliyuncs.com/2022/IRSAPI/sealpdf/ynzcjcgxmcbb.pdf";
            URL url = new URL("https://qlxs.oss-cn-hangzhou.aliyuncs.com/2022/IRSAPI/img/test1111.jpg");
            InputStream inputStream = url.openStream();
            jpgToPdf(inputStream,pdf);
//            String filepathpdf = OssUtils.getSavePathSeal("ygzPdf") + "test.pdf";
//            OssUtils.ossUpload(filepathpdf, inputStream);

            //pdf转图片
//            pdfbox(urls);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 使用pdfbox将jpg转成pdf
     * @param jpgStream jpg输入流
     * @param pdfPath pdf文件存储路径
     * @throws IOException IOException
     */
    public static void jpgToPdf(InputStream jpgStream, String pdfPath) throws IOException {

        PDDocument pdDocument = new PDDocument();
        BufferedImage image = ImageIO.read(jpgStream);

        PDPage pdPage = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
        pdDocument.addPage(pdPage);
        PDImageXObject pdImageXObject = LosslessFactory.createFromImage(pdDocument, image);
        PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage);
        contentStream.drawImage(pdImageXObject, 0, 0, image.getWidth(), image.getHeight());
        contentStream.close();
        pdDocument.save(pdfPath);
        pdDocument.close();
    }

    /**
     * pdf转jpg
     * @throws IOException
     */
    public static String pdfbox(String urls) throws IOException {
        //pdf路径
        URL url = new URL(urls);
        InputStream stream = URLUtil.getStream(url);
        // 加载解析PDF文件
        PDDocument doc = PDDocument.load(stream);
        PDFRenderer pdfRenderer = new PDFRenderer(doc);
        PDPageTree pages = doc.getPages();
        int pageCount = pages.getCount();
        for (int i = 0; i < pageCount; i++) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 200);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bim, "jpg", os);
            byte[] datas = os.toByteArray();
              InputStream is = new ByteArrayInputStream(datas);
            //jpg文件转出路径
            return urls.substring(urls.lastIndexOf("/") + 1);
        }
        return "";
    }

}
