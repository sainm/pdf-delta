package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;









public class PersonalInfoPdfFactory {

    private static final String FONT_PATH = "/mnt/c/Windows/Fonts/NotoSansJP-VF.ttf";
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN  = 40f;
    private static final float ROW_H   = 20f;
    private static final float FONT_SZ = 8f;

    static final String[] HEADERS = {"No.", "氏名", "住所", "メールアドレス", "備考"};
    static final float[]  COL_W   = {25f, 80f, 175f, 140f, 95f};

    
    static final String[][] ROWS_V1 = {
        {"1",  "田中 太郎", "東京都新宿区西新宿1-1-1",         "tanaka@example.com",       "営業部"},
        {"2",  "佐藤 花子", "大阪府大阪市北区梅田2-2-2",       "sato.h@example.com",       "人事部"},
        {"3",  "鈴木 一郎", "神奈川県横浜市中区山下町3-3",     "suzuki@example.com",       "正社員"},
        {"4",  "山田 次郎", "愛知県名古屋市中村区名駅4-4-4",   "yamada@example.com",       "契約社員"},
        {"5",  "伊藤 美穂", "福岡県福岡市博多区博多駅前5-5",   "ito.m@example.com",        "経理部"},
        {"6",  "渡辺 健太", "北海道札幌市中央区大通西6-6",     "watanabe@example.com",     "技術部"},
        {"7",  "中村 由美", "宮城県仙台市青葉区一番町7-7-7",   "nakamura.y@example.com",   "総務部"},
        {"8",  "小林 誠",   "広島県広島市中区紙屋町8-8",       "kobayashi@example.com",    "正社員"},
        {"9",  "加藤 さくら","京都府京都市中京区四条通9-9-9",  "kato.s@example.com",       "マーケティング部"},
        {"10", "吉田 浩二", "埼玉県さいたま市大宮区桜木町10",  "yoshida@example.com",      "開発部"},
    };

    
    static final String[][] ROWS_V2 = {
        {"1",  "田中 太郎", "東京都渋谷区道玄坂1-2-3",         "tanaka@example.com",       "営業部"},        
        {"2",  "佐藤 花子", "大阪府大阪市北区梅田2-2-2",       "sato.hanako@newmail.com",  "人事部"},        
        {"3",  "鈴木 一郎", "神奈川県横浜市中区山下町3-3",     "suzuki@example.com",       "部長"},          
        
        {"5",  "伊藤 美穂", "福岡県福岡市博多区博多駅前5-5",   "ito.m@example.com",        "経理部"},
        {"6",  "渡辺 健太", "北海道札幌市中央区大通西6-6",     "watanabe@example.com",     "技術部"},
        {"7",  "中村 由美", "宮城県仙台市青葉区一番町7-7-7",   "nakamura.y@example.com",   "総務部"},
        {"8",  "小林 誠",   "広島県広島市中区紙屋町8-8",       "kobayashi@example.com",    "正社員"},
        {"9",  "加藤 さくら","京都府京都市中京区四条通9-9-9",  "kato.s@example.com",       "マーケティング部"},
        {"10", "吉田 浩二", "埼玉県さいたま市大宮区桜木町10",  "yoshida@example.com",      "開発部"},
        {"11", "新田 美咲", "東京都港区六本木11-11-11",        "nitta.m@example.com",      "新入社員"},      
    };

    public static byte[] generateV1() throws IOException {
        return generate(ROWS_V1, "個人情報一覧表　Ver.1.0");
    }

    public static byte[] generateV2() throws IOException {
        return generate(ROWS_V2, "個人情報一覧表　Ver.2.0");
    }

    private static byte[] generate(String[][] rows, String title) throws IOException {
        try (var doc = new PDDocument()) {
            var font = PDType0Font.load(doc, new File(FONT_PATH));
            var page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
            doc.addPage(page);

            float tableW = 0; for (float w : COL_W) tableW += w;
            float tableX = MARGIN;
            float y = PAGE_H - MARGIN;

            try (var cs = new PDPageContentStream(doc, page)) {
                
                cs.setNonStrokingColor(0f, 0f, 0f);
                cs.beginText();
                cs.setFont(font, 12f);
                cs.newLineAtOffset(tableX, y - 14f);
                cs.showText(title);
                cs.endText();
                y -= 32f;

                
                drawRow(cs, font, HEADERS, tableX, y, true);
                y -= ROW_H;

                
                for (int i = 0; i < rows.length; i++) {
                    if (i % 2 == 1) {
                        cs.setNonStrokingColor(0.96f, 0.96f, 0.98f);
                        cs.addRect(tableX, y - ROW_H, tableW, ROW_H);
                        cs.fill();
                    }
                    drawRow(cs, font, rows[i], tableX, y, false);
                    y -= ROW_H;
                }

                
                cs.setStrokingColor(0.3f, 0.3f, 0.3f);
                cs.setLineWidth(1f);
                float tableTop = PAGE_H - MARGIN - 32f;
                cs.addRect(tableX, y, tableW, tableTop - y);
                cs.stroke();
            }

            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static void drawRow(PDPageContentStream cs, PDType0Font font,
            String[] cells, float x, float y, boolean isHeader) throws IOException {
        float tableW = 0; for (float w : COL_W) tableW += w;
        float cx = x;

        if (isHeader) {
            cs.setNonStrokingColor(0.15f, 0.35f, 0.65f);
            cs.addRect(x, y - ROW_H, tableW, ROW_H);
            cs.fill();
        }

        
        cs.setStrokingColor(0.65f, 0.65f, 0.65f);
        cs.setLineWidth(0.4f);
        for (float w : COL_W) {
            cs.addRect(cx, y - ROW_H, w, ROW_H);
            cs.stroke();
            cx += w;
        }

        
        cx = x;
        for (int i = 0; i < cells.length; i++) {
            float tw = font.getStringWidth(cells[i]) / 1000f * FONT_SZ;
            float tx = cx + (i == 0 ? (COL_W[i] - tw) / 2f : 4f); 
            cs.setNonStrokingColor(isHeader ? 1f : 0f, isHeader ? 1f : 0f, isHeader ? 1f : 0f);
            cs.beginText();
            cs.setFont(font, FONT_SZ);
            cs.newLineAtOffset(tx, y - ROW_H + 6f);
            cs.showText(cells[i]);
            cs.endText();
            cx += COL_W[i];
        }
    }
}
