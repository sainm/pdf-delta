package org.sainm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;







public class ColumnChangePdfFactory {

    private static final String FONT_PATH = "/mnt/c/Windows/Fonts/NotoSansJP-VF.ttf";
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN  = 40f;
    private static final float ROW_H   = 20f;
    private static final float FONT_SZ = 8f;

    
    private static final String[] HEADERS_V1 = {"No.", "氏名", "住所", "メールアドレス", "備考"};
    private static final float[]  COL_W_V1   = {25f, 80f, 175f, 140f, 95f};

    
    private static final String[] HEADERS_V2 = {"No.", "氏名", "部署", "住所", "メールアドレス"};
    private static final float[]  COL_W_V2   = {25f, 80f, 80f, 155f, 175f};

    private static final String[][] ROWS_V1 = {
        {"1",  "田中 太郎", "東京都新宿区西新宿1-1-1",       "tanaka@example.com",     "営業部"},
        {"2",  "佐藤 花子", "大阪府大阪市北区梅田2-2-2",     "sato.h@example.com",     "人事部"},
        {"3",  "鈴木 一郎", "神奈川県横浜市中区山下町3-3",   "suzuki@example.com",     "正社員"},
        {"4",  "山田 次郎", "愛知県名古屋市中村区名駅4-4-4", "yamada@example.com",     "契約社員"},
        {"5",  "伊藤 美穂", "福岡県福岡市博多区博多駅前5-5", "ito.m@example.com",      "経理部"},
        {"6",  "渡辺 健太", "北海道札幌市中央区大通西6-6",   "watanabe@example.com",   "技術部"},
        {"7",  "中村 由美", "宮城県仙台市青葉区一番町7-7-7", "nakamura.y@example.com", "総務部"},
        {"8",  "小林 誠",   "広島県広島市中区紙屋町8-8",     "kobayashi@example.com",  "正社員"},
    };

    
    private static final String[][] ROWS_V2 = {
        {"1",  "田中 太郎", "営業部", "東京都新宿区西新宿1-1-1",       "tanaka@example.com"},
        {"2",  "佐藤 花子", "人事部", "大阪府大阪市北区梅田2-2-2",     "sato.hanako@newmail.com"}, 
        {"3",  "鈴木 一郎", "開発部", "神奈川県横浜市中区山下町3-3",   "suzuki@example.com"},      
        {"4",  "山田 次郎", "契約",   "愛知県名古屋市中村区名駅4-4-4", "yamada@example.com"},
        {"5",  "伊藤 美穂", "経理部", "福岡県福岡市博多区博多駅前5-5", "ito.m@example.com"},
        {"6",  "渡辺 健太", "技術部", "北海道札幌市中央区大通西6-6",   "watanabe@example.com"},
        {"7",  "中村 由美", "総務部", "宮城県仙台市青葉区一番町7-7-7", "nakamura.y@example.com"},
        {"8",  "小林 誠",   "正社員", "広島県広島市中区紙屋町8-8",     "kobayashi@example.com"},
        {"9",  "新田 美咲", "新入社員","東京都港区六本木9-9-9",        "nitta.m@example.com"},     
    };

    
    private static final String[] HEADERS_DEL = {"No.", "氏名", "住所", "メールアドレス"};
    private static final float[]  COL_W_DEL   = {25f, 80f, 175f, 140f};
    private static final String[][] ROWS_DEL = {
        {"1",  "田中 太郎", "東京都新宿区西新宿1-1-1",       "tanaka@example.com"},
        {"2",  "佐藤 花子", "大阪府大阪市北区梅田2-2-2",     "sato.h@example.com"},
        {"3",  "鈴木 一郎", "神奈川県横浜市中区山下町3-3",   "suzuki@example.com"},
        {"4",  "山田 次郎", "愛知県名古屋市中村区名駅4-4-4", "yamada@example.com"},
        {"5",  "伊藤 美穂", "福岡県福岡市博多区博多駅前5-5", "ito.m@example.com"},
        {"6",  "渡辺 健太", "北海道札幌市中央区大通西6-6",   "watanabe@example.com"},
        {"7",  "中村 由美", "宮城県仙台市青葉区一番町7-7-7", "nakamura.y@example.com"},
        {"8",  "小林 誠",   "広島県広島市中区紙屋町8-8",     "kobayashi@example.com"},
    };

    
    private static final String[] HEADERS_ADD = {"No.", "氏名", "部署", "住所", "メールアドレス", "備考"};
    private static final float[]  COL_W_ADD   = {25f, 80f, 80f, 155f, 130f, 95f};
    private static final String[][] ROWS_ADD = {
        {"1",  "田中 太郎", "営業部", "東京都新宿区西新宿1-1-1",       "tanaka@example.com",     "営業部"},
        {"2",  "佐藤 花子", "人事部", "大阪府大阪市北区梅田2-2-2",     "sato.h@example.com",     "人事部"},
        {"3",  "鈴木 一郎", "開発部", "神奈川県横浜市中区山下町3-3",   "suzuki@example.com",     "正社員"},
        {"4",  "山田 次郎", "契約",   "愛知県名古屋市中村区名駅4-4-4", "yamada@example.com",     "契約社員"},
        {"5",  "伊藤 美穂", "経理部", "福岡県福岡市博多区博多駅前5-5", "ito.m@example.com",      "経理部"},
        {"6",  "渡辺 健太", "技術部", "北海道札幌市中央区大通西6-6",   "watanabe@example.com",   "技術部"},
        {"7",  "中村 由美", "総務部", "宮城県仙台市青葉区一番町7-7-7", "nakamura.y@example.com", "総務部"},
        {"8",  "小林 誠",   "正社員", "広島県広島市中区紙屋町8-8",     "kobayashi@example.com",  "正社員"},
    };

    public static byte[] buildV1() throws IOException {
        return build("社員情報一覧 v1.0", HEADERS_V1, COL_W_V1, ROWS_V1);
    }

    
    public static byte[] buildDeleteOnly() throws IOException {
        return build("社員情報一覧 (備考列削除)", HEADERS_DEL, COL_W_DEL, ROWS_DEL);
    }

    
    public static byte[] buildAddOnly() throws IOException {
        return build("社員情報一覧 (部署列追加)", HEADERS_ADD, COL_W_ADD, ROWS_ADD);
    }

    public static byte[] buildV2() throws IOException {
        return build("社員情報一覧 v2.0", HEADERS_V2, COL_W_V2, ROWS_V2);
    }

    private static byte[] build(String title, String[] headers, float[] colW, String[][] rows) throws IOException {
        try (var doc = new PDDocument()) {
            var font = PDType0Font.load(doc, new File(FONT_PATH));
            var page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
            doc.addPage(page);

            try (var cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_H - MARGIN;

                
                cs.beginText();
                cs.setFont(font, 12f);
                cs.setNonStrokingColor(0f, 0f, 0f);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(title);
                cs.endText();
                y -= 20f;

                
                drawRow(cs, font, headers, colW, y, true);
                y -= ROW_H;

                
                for (var row : rows) {
                    drawRow(cs, font, row, colW, y, false);
                    y -= ROW_H;
                }
            }

            var bout = new ByteArrayOutputStream();
            doc.save(bout);
            return bout.toByteArray();
        }
    }

    private static void drawRow(PDPageContentStream cs, PDType0Font font,
            String[] cells, float[] colW, float y, boolean isHeader) throws IOException {
        float x = MARGIN;
        float rowH = ROW_H - 2f;

        
        if (isHeader) {
            cs.setNonStrokingColor(0.85f, 0.85f, 0.95f);
            float totalW = 0;
            for (float w : colW) totalW += w;
            cs.addRect(x, y, totalW, rowH);
            cs.fill();
        }

        
        for (int i = 0; i < cells.length && i < colW.length; i++) {
            
            cs.setStrokingColor(0.6f, 0.6f, 0.6f);
            cs.setLineWidth(0.5f);
            cs.addRect(x, y, colW[i], rowH);
            cs.stroke();

            
            cs.setNonStrokingColor(0f, 0f, 0f);
            cs.beginText();
            cs.setFont(font, FONT_SZ);
            cs.newLineAtOffset(x + 3f, y + 5f);
            cs.showText(cells[i]);
            cs.endText();

            x += colW[i];
        }
    }
}
