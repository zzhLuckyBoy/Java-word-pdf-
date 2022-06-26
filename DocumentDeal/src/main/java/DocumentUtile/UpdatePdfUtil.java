package DocumentUtile;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * pdf添加文字和水印，自定义位置
 */
public class UpdatePdfUtil {
    public static void main(String[] args) throws Exception {
       URL url = new URL("https://qlxs.oss-cn-hangzhou.aliyuncs.com/2022/IRSAPI/sealpdf/ynzcjcgxmcbb.pdf");
       InputStream inputStream = url.openStream();
        InputStream in =  addText(inputStream,"test","1",100,100);
    }
    /**
     *
     * @param inputPDFFile 文件流
     * @param contentText  添加的文字
     * @param page         添加的第几页
     * @param x            添加位置x坐标
     * @param y            添加位置y坐标
     * @return
     * @throws Exception
     */
    public static InputStream addText(InputStream inputPDFFile, String name,String contentText, Integer x, Integer y)
            throws Exception {
        PdfReader reader = new PdfReader(inputPDFFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader,byteArrayOutputStream );
        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
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

//        stamper.close();
        System.out.println("PDF水印添加完成！");


        //content = stamper.getOverContent(i);// 在内容上方加水印
        content = stamper.getUnderContent(total-1);//在内容下方加水印 page 在第几页加
        content.beginText();

        //字体大小
        content.setFontAndSize(base, 12);
        //content.setTextMatrix(70, 200);
        //内容居中，横纵坐标，偏移量
        content.showTextAligned(Element.ALIGN_LEFT, contentText, x,  y, 0);

        // //添加图片
        // Image image = Image.getInstance("D:\\测试图片.jpg");
        //
        // /*
        //   img.setAlignment(Image.LEFT | Image.TEXTWRAP);
        //   img.setBorder(Image.BOX); img.setBorderWidth(10);
        //   img.setBorderColor(BaseColor.WHITE); img.scaleToFit(100072);//大小
        //   img.setRotationDegrees(-30);//旋转
        //  */
        // //图片的位置（坐标）
        // image.setAbsolutePosition(520, 786);
        // // image of the absolute
        // image.scaleToFit(200, 200);
        // image.scalePercent(15);//依照比例缩放
        // content.addImage(image);

        content.endText();

        stamper.close();
        //关闭打开的原来PDF文件，不执行reader.close()删除不了（必须先执行stamper.close()，否则会报错）
        reader.close();
        //删除原来的PDF文件
        // File targetTemplePDF = new File(inputPDFFilePath);
        // targetTemplePDF.delete();
        inputPDFFile.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
