package org.sainm.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.sainm.model.CompareResult;
import org.sainm.report.TextDiffRenderer.TextLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;






final class TextLayoutRenderer {

    private static final float PAGE_W  = PDRectangle.A4.getWidth()  * 2;
    private static final float PAGE_H  = PDRectangle.A4.getHeight();
    private static final float MARGIN  = 20f;
    private static final float FONT_SZ = 7f;
    private static final float LINE_H  = 11f;
    private static final float COL_W   = PDRectangle.A4.getWidth() - MARGIN * 2;

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        var extractor = new TextDiffRenderer();
        var linesA = extractor.extractLines(pdfA);
        var linesB = extractor.extractLines(pdfB);

        var textsA = linesA.stream().map(TextLine::text).toList();
        var textsB = linesB.stream().map(TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        
        DeltaType[] typesA = new DeltaType[linesA.size()];
        DeltaType[] typesB = new DeltaType[linesB.size()];

        for (var delta : patch.getDeltas()) {
            int ai = delta.getSource().getPosition();
            int bi = delta.getTarget().getPosition();
            for (int i = 0; i < delta.getSource().getLines().size(); i++)
                typesA[ai + i] = delta.getType();
            for (int i = 0; i < delta.getTarget().getLines().size(); i++)
                typesB[bi + i] = delta.getType();
        }

        try (var out = new PDDocument()) {
            var font = PDType0Font.load(out, TextDiffRenderer.resolveFontStream());

            
            int totalA = linesA.size();
            int totalB = linesB.size();
            int linesPerPage = (int) ((PAGE_H - MARGIN * 2) / LINE_H);

            int pages = (int) Math.ceil(Math.max(totalA, totalB) / (double) linesPerPage);
            if (pages == 0) pages = 1;

            for (int p = 0; p < pages; p++) {
                var page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
                out.addPage(page);

                try (var cs = new PDPageContentStream(out, page)) {
                    
                    drawLabel(cs, font, "旧版", MARGIN, PAGE_H - MARGIN + 4f);
                    drawLabel(cs, font, "新版", MARGIN + COL_W + MARGIN * 2, PAGE_H - MARGIN + 4f);

                    
                    cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                    cs.setLineWidth(0.5f);
                    cs.moveTo(COL_W + MARGIN * 2, MARGIN);
                    cs.lineTo(COL_W + MARGIN * 2, PAGE_H - MARGIN);
                    cs.stroke();

                    int startA = p * linesPerPage;
                    int startB = p * linesPerPage;

                    
                    for (int i = 0; i < linesPerPage && startA + i < totalA; i++) {
                        int idx = startA + i;
                        float y = PAGE_H - MARGIN - (i + 1) * LINE_H;
                        drawTextLine(cs, font, linesA.get(idx).text(), typesA[idx],
                                MARGIN, y, COL_W, true);
                    }

                    
                    float colRX = COL_W + MARGIN * 3;
                    for (int i = 0; i < linesPerPage && startB + i < totalB; i++) {
                        int idx = startB + i;
                        float y = PAGE_H - MARGIN - (i + 1) * LINE_H;
                        drawTextLine(cs, font, linesB.get(idx).text(), typesB[idx],
                                colRX, y, COL_W, false);
                    }
                }
            }

            var bout = new ByteArrayOutputStream();
            out.save(bout);
            return bout.toByteArray();
        }
    }

    private void drawTextLine(PDPageContentStream cs, PDType0Font font,
            String text, DeltaType type, float x, float y, float w, boolean isLeft) throws IOException {
        
        if (type != null) {
            float[] bg = switch (type) {
                case DELETE -> new float[]{1f, 0.85f, 0.85f};
                case INSERT -> new float[]{0.85f, 1f, 0.85f};
                case CHANGE -> new float[]{1f, 0.93f, 0.8f};
                default     -> null;
            };
            if (bg != null) {
                cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
                cs.addRect(x, y, w, LINE_H - 1f);
                cs.fill();
            }
        }

        
        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.beginText();
        cs.setFont(font, FONT_SZ);
        cs.newLineAtOffset(x + 2f, y + 2f);
        cs.showText(truncate(text, 80));
        cs.endText();

        
        if (type == DeltaType.DELETE && isLeft) {
            cs.setStrokingColor(0.8f, 0.1f, 0.1f);
            cs.setLineWidth(0.7f);
            cs.moveTo(x, y + LINE_H / 2);
            cs.lineTo(x + w, y + LINE_H / 2);
            cs.stroke();
        }
    }

    private void drawLabel(PDPageContentStream cs, PDType0Font font,
            String label, float x, float y) throws IOException {
        cs.setNonStrokingColor(0.2f, 0.2f, 0.6f);
        cs.beginText();
        cs.setFont(font, 9f);
        cs.newLineAtOffset(x, y);
        cs.showText(label);
        cs.endText();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
