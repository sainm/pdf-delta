package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;





public class JapaneseTablePdfFactory {

    private static final String FONT_PATH = "/mnt/c/Windows/Fonts/NotoSansJP-VF.ttf";
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN = 50f;
    private static final float ROW_H = 22f;
    private static final float FONT_SZ = 9f;

    

    
    static final String[] HEADERS_V1 = {"商品コード", "商品名", "単価（円）", "在庫数", "カテゴリ"};

    
    static final String[] HEADERS_V2 = {"商品コード", "商品名", "販売価格（円）", "在庫数", "分類"};

    
    static final String[][] ROWS_V1 = {
        {"A001", "ノートパソコン",   "98,000",  "15", "電子機器"},
        {"A002", "ワイヤレスマウス", "3,500",   "80", "周辺機器"},
        {"A003", "USBハブ",         "2,800",   "50", "周辺機器"},
        {"A004", "モニター",         "45,000",  "10", "電子機器"},
        {"A005", "キーボード",       "5,200",   "60", "周辺機器"},
        {"A006", "Webカメラ",        "8,900",   "25", "周辺機器"},
        {"A007", "外付けSSD",        "12,000",  "30", "ストレージ"},
        {"A008", "プリンター",        "32,000",  "8",  "電子機器"},
    };

    
    static final String[][] ROWS_V2 = {
        {"A001", "ノートパソコン",   "98,000",  "15", "電子機器"},
        {"A002", "ワイヤレスマウス", "3,500",   "80", "周辺機器"},
        
        {"A004", "モニター",         "45,000",  "10", "電子機器"},
        {"A005", "キーボード",       "6,800",   "60", "周辺機器"},  
        {"A006", "Webカメラ",        "8,900",   "25", "周辺機器"},
        {"A007", "外付けSSD",        "12,000",  "45", "ストレージ"}, 
        {"A008", "プリンター",        "32,000",  "8",  "電子機器"},
        {"A009", "スキャナー",        "18,500",  "12", "電子機器"},  
    };

    

    public static byte[] generateV1() throws IOException {
        return generate(HEADERS_V1, ROWS_V1, "商品在庫一覧表 Ver.1.0");
    }

    public static byte[] generateV2() throws IOException {
        return generate(HEADERS_V2, ROWS_V2, "商品在庫一覧表 Ver.2.0");
    }

    

    private static byte[] generate(String[] headers, String[][] rows, String title) throws IOException {
        try (var doc = new PDDocument()) {
            var font = PDType0Font.load(doc, new File(FONT_PATH));
            var page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
            doc.addPage(page);

            
            float[] colW = {70f, 110f, 90f, 60f, 80f};
            float tableW = 0; for (float w : colW) tableW += w;
            float tableX = MARGIN;
            float y = PAGE_H - MARGIN;

            try (var cs = new PDPageContentStream(doc, page)) {
                
                cs.setNonStrokingColor(0f, 0f, 0f);
                cs.beginText();
                cs.setFont(font, 13f);
                cs.newLineAtOffset(tableX, y - 16f);
                cs.showText(title);
                cs.endText();
                y -= 36f;

                
                drawRow(cs, font, headers, colW, tableX, y, true);
                y -= ROW_H;

                
                for (int i = 0; i < rows.length; i++) {
                    boolean shade = i % 2 == 1;
                    if (shade) {
                        cs.setNonStrokingColor(0.96f, 0.96f, 0.96f);
                        cs.addRect(tableX, y - ROW_H, tableW, ROW_H);
                        cs.fill();
                    }
                    drawRow(cs, font, rows[i], colW, tableX, y, false);
                    y -= ROW_H;
                }

                
                cs.setStrokingColor(0.3f, 0.3f, 0.3f);
                cs.setLineWidth(1f);
                float tableTop = PAGE_H - MARGIN - 36f;
                cs.addRect(tableX, y, tableW, tableTop - y);
                cs.stroke();
            }

            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static void drawRow(PDPageContentStream cs, PDType0Font font,
            String[] cells, float[] colW, float x, float y,
            boolean isHeader) throws IOException {
        float cx = x;
        float tableW = 0; for (float w : colW) tableW += w;

        
        if (isHeader) {
            cs.setNonStrokingColor(0.2f, 0.4f, 0.7f);
            cs.addRect(x, y - ROW_H, tableW, ROW_H);
            cs.fill();
        }

        
        cs.setStrokingColor(0.6f, 0.6f, 0.6f);
        cs.setLineWidth(0.4f);
        for (float w : colW) {
            cs.addRect(cx, y - ROW_H, w, ROW_H);
            cs.stroke();
            cx += w;
        }

        
        cx = x;
        for (int i = 0; i < cells.length; i++) {
            float fs = FONT_SZ;
            float tw = font.getStringWidth(cells[i]) / 1000f * fs;
            float tx = cx + Math.max(4f, (colW[i] - tw) / 2f); 
            cs.setNonStrokingColor(isHeader ? 1f : 0f, isHeader ? 1f : 0f, isHeader ? 1f : 0f);
            cs.beginText();
            cs.setFont(font, fs);
            cs.newLineAtOffset(tx, y - ROW_H + 7f);
            cs.showText(cells[i]);
            cs.endText();
            cx += colW[i];
        }
    }
}
