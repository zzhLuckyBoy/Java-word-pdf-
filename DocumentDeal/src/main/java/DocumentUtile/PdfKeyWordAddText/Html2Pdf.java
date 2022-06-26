package DocumentUtile.PdfKeyWordAddText;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class Html2Pdf {
    /**
     * 定位pdf文件中关键字坐标
     *
     * @param signKey 关键字
     * @param pdf     文件流
     * @return 坐标位置
     */
    public static float[] getGzzzb(String signKey, byte[] pdf) {
        PdfReader reader = null;
        final float[] po = new float[3];
        try {
            reader = new PdfReader(pdf);
            int pageNum = reader.getNumberOfPages();
            final String signKeyWord = signKey;
            for (int page = 1; page <= pageNum; page++) {
                PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(reader);
                pdfReaderContentParser.processContent(page, new RenderListener() {
                    StringBuilder sb = new StringBuilder("");
                    int maxLength = signKeyWord.length();

                    @Override
                    public void renderText(TextRenderInfo textRenderInfo) {
                        // 只适用 单字块文档 以及 关键字整个为一个块的情况
                        // 设置 关键字文本为单独的块，不然会错位
                        boolean isKeywordChunk = textRenderInfo.getText().length() == maxLength;
                        if (isKeywordChunk) {
                            // 文档按照 块 读取
                            sb.delete(0, sb.length());
                            sb.append(textRenderInfo.getText());
                        } else {
                            // 有些文档 单字一个块的情况
                            // 拼接字符串
                            sb.append(textRenderInfo.getText());
                            // 去除首部字符串，使长度等于关键字长度
                            if (sb.length() > maxLength) {
                                sb.delete(0, sb.length() - maxLength);
                            }
                        }
                        // 判断是否匹配上
                        if (signKeyWord.equals(sb.toString())) {
                            // 计算中心点坐标
                            Rectangle2D.Float baseFloat = textRenderInfo.getBaseline()
                                    .getBoundingRectange();
                            Rectangle2D.Float ascentFloat = textRenderInfo.getAscentLine()
                                    .getBoundingRectange();
                            float centreX;
                            float centreY;
                            if (isKeywordChunk) {
                                centreX = baseFloat.x + 5 * ascentFloat.width / 6;
                                centreY = baseFloat.y + (5 * (ascentFloat.y - baseFloat.y) / 6);
                            } else {
                                centreX = baseFloat.x + ascentFloat.width - (5 * maxLength * ascentFloat.width / 6);
                                centreY = baseFloat.y + (5 * (ascentFloat.y - baseFloat.y) / 6);
                            }
                            po[0] = 1.00f;
                            po[1] = centreX + 3;
                            po[2] = centreY;
                            // 匹配完后 清除
                            sb.delete(0, sb.length());
                        }
                    }

                    @Override
                    public void renderImage(ImageRenderInfo arg0) {
                    }

                    @Override
                    public void endTextBlock() {
                    }

                    @Override
                    public void beginTextBlock() {
                    }
                });
            }
            if (po[0] == 1.00f) {
                return po;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }
}


