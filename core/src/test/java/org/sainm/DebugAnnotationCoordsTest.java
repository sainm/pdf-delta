package org.sainm;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.junit.jupiter.api.Test;
import org.sainm.report.TextDiffRenderer;

public class DebugAnnotationCoordsTest {

    
    static final float PAGE_W = 841.9f; 
    static final float PAGE_H = 595.3f;
    static final float MARGIN = 20f;
    static final float GAP    = 8f;
    static final float COL_W  = (PAGE_W - MARGIN * 2 - GAP) / 2;
    static final float DPI    = 150f;

    @Test
    void printAnnotationCoords() throws Exception {
        byte[] pdfA = PersonalInfoPdfFactory.generateV1();
        byte[] pdfB = PersonalInfoPdfFactory.generateV2();

        float srcWa, srcHa;
        try (var doc = org.apache.pdfbox.Loader.loadPDF(pdfA)) {
            srcWa = doc.getPage(0).getMediaBox().getWidth();
            srcHa = doc.getPage(0).getMediaBox().getHeight();
        }

        float imgH   = PAGE_H - MARGIN * 2;
        float scaleX = COL_W / srcWa;
        float scaleY = imgH / srcHa;

        System.out.printf("PAGE: w=%.1f h=%.1f%n", PAGE_W, PAGE_H);
        System.out.printf("COL_W=%.1f imgH=%.1f scaleX=%.4f scaleY=%.4f%n",
            COL_W, imgH, scaleX, scaleY);
        System.out.printf("Left col X range: %.1f ~ %.1f%n", MARGIN, MARGIN + COL_W);
        System.out.printf("Right col X range: %.1f ~ %.1f%n", MARGIN + COL_W + GAP, MARGIN + COL_W + GAP + COL_W);
        System.out.printf("Y range: %.1f ~ %.1f%n", MARGIN, MARGIN + imgH);

        var linesA = new TextDiffRenderer().extractLines(pdfA);
        var linesB = new TextDiffRenderer().extractLines(pdfB);
        var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
        var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        System.out.println("\n=== RUNS for first CHANGE lines ===");
        for (int i = 2; i <= 4; i++) {
            var line = linesA.get(i);
            System.out.printf("A[%d] runs:%n", i);
            for (var r : line.runs())
                System.out.printf("  run: x=%.1f w=%.1f [%s]%n", r.x(), r.w(), r.text());
        }
        if (linesB.size() > 4) {
            for (int i = 2; i <= 4; i++) {
                var line = linesB.get(i);
                System.out.printf("B[%d] runs:%n", i);
                for (var r : line.runs())
                    System.out.printf("  run: x=%.1f w=%.1f [%s]%n", r.x(), r.w(), r.text());
            }
        }
        for (var delta : patch.getDeltas()) {
            int ai = delta.getSource().getPosition();
            int bi = delta.getTarget().getPosition();
            System.out.printf("[%s]%n", delta.getType());

            if (delta.getType() == DeltaType.DELETE || delta.getType() == DeltaType.CHANGE) {
                for (int i = 0; i < delta.getSource().getLines().size(); i++) {
                    var line = linesA.get(ai + i);
                    float bx = MARGIN + line.x() * scaleX;
                    float by = MARGIN + (line.y() - line.h()) * scaleY;
                    float bw = line.w() * scaleX;
                    float bh = line.h() * scaleY;
                    System.out.printf("  A[%d] line: x=%.1f y=%.1f w=%.1f h=%.1f  =>  box: x=%.1f y=%.1f w=%.1f h=%.1f  [%s]%n",
                        ai+i, line.x(), line.y(), line.w(), line.h(), bx, by, bw, bh, line.text());
                }
            }
            if (delta.getType() == DeltaType.INSERT || delta.getType() == DeltaType.CHANGE) {
                for (int i = 0; i < delta.getTarget().getLines().size(); i++) {
                    var line = linesB.get(bi + i);
                    float bx = MARGIN + COL_W + GAP + line.x() * scaleX;
                    float by = MARGIN + (line.y() - line.h()) * scaleY;
                    float bw = line.w() * scaleX;
                    float bh = line.h() * scaleY;
                    System.out.printf("  B[%d] line: x=%.1f y=%.1f w=%.1f h=%.1f  =>  box: x=%.1f y=%.1f w=%.1f h=%.1f  [%s]%n",
                        bi+i, line.x(), line.y(), line.w(), line.h(), bx, by, bw, bh, line.text());
                }
            }
        }
    }
}
