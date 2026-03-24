package org.sainm.report;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.sainm.model.CompareResult;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;







final class SinglePageRenderer {

    private static final float DPI = 150f;

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        var linesA = new TextDiffRenderer().extractLines(pdfA);
        var linesB = new TextDiffRenderer().extractLines(pdfB);

        try (var docA = Loader.loadPDF(pdfA);
             var out  = new PDDocument()) {

            var rendA = new PDFRenderer(docA);
            var font  = PDType0Font.load(out, TextDiffRenderer.resolveFontStream());

            for (int p = 0; p < docA.getNumberOfPages(); p++) {
                float srcW = docA.getPage(p).getMediaBox().getWidth();
                float srcH = docA.getPage(p).getMediaBox().getHeight();

                var page = new PDPage(new PDRectangle(srcW, srcH));
                out.addPage(page);

                BufferedImage img = rendA.renderImageWithDPI(p, DPI);

                try (var cs = new PDPageContentStream(out, page)) {
                    var pdImg = LosslessFactory.createFromImage(out, img);
                    cs.drawImage(pdImg, 0, 0, srcW, srcH);
                }

                
                var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
                var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
                var patch  = com.github.difflib.DiffUtils.diff(textsA, textsB);

                try (var cs = new PDPageContentStream(out, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {

                    for (var delta : patch.getDeltas()) {
                        int ai = delta.getSource().getPosition();
                        int bi = delta.getTarget().getPosition();

                        switch (delta.getType()) {
                            case DELETE -> {
                                for (int i = 0; i < delta.getSource().getLines().size(); i++) {
                                    var line = linesA.get(ai + i);
                                    drawOverlayBox(cs, line, srcH, new float[]{0.85f, 0.1f, 0.1f}, true);
                                }
                            }
                            case CHANGE -> {
                                int srcSize = delta.getSource().getLines().size();
                                int tgtSize = delta.getTarget().getLines().size();
                                for (int i = 0; i < srcSize; i++) {
                                    var lineA = linesA.get(ai + i);
                                    var lineB = i < tgtSize ? linesB.get(bi + i) : null;
                                    if (lineB == null) {
                                        
                                        drawOverlayBox(cs, lineA, srcH, new float[]{0.85f, 0.1f, 0.1f}, true);
                                    } else {
                                        drawChangeRuns(cs, lineA, lineB);
                                    }
                                }
                            }
                            case INSERT -> {
                                
                                for (int i = 0; i < delta.getTarget().getLines().size(); i++) {
                                    var line = linesB.get(bi + i);
                                    cs.setNonStrokingColor(0.9f, 1f, 0.9f);
                                    cs.addRect(srcW - 120f, srcH - 30f - i * 14f, 115f, 12f);
                                    cs.fill();
                                    cs.setNonStrokingColor(0.1f, 0.5f, 0.1f);
                                    cs.beginText();
                                    cs.setFont(font, 7f);
                                    cs.newLineAtOffset(srcW - 118f, srcH - 40f - i * 14f);
                                    cs.showText("＋ " + truncate(line.text(), 20));
                                    cs.endText();
                                }
                            }
                        }
                    }
                }
            }

            var bout = new ByteArrayOutputStream();
            out.save(bout);
            return bout.toByteArray();
        }
    }

    private void drawChangeRuns(PDPageContentStream cs,
            TextDiffRenderer.TextLine lineA, TextDiffRenderer.TextLine lineB) throws IOException {
        var runsA = lineA.runs();
        var runsB = lineB.runs();
        var textsA = runsA.stream().map(TextDiffRenderer.Run::text).toList();
        var textsB = runsB.stream().map(TextDiffRenderer.Run::text).toList();
        var patch = com.github.difflib.DiffUtils.diff(textsA, textsB);

        java.util.Set<Integer> changedIdx = new java.util.HashSet<>();
        for (var d : patch.getDeltas())
            for (int i = d.getSource().getPosition(); i < d.getSource().getPosition() + d.getSource().size(); i++)
                changedIdx.add(i);

        if (changedIdx.isEmpty() || runsA.isEmpty()) return;

        cs.setStrokingColor(0.9f, 0.45f, 0.0f);
        cs.setLineWidth(1f);
        for (int i = 0; i < runsA.size(); i++) {
            if (!changedIdx.contains(i)) continue;
            var run = runsA.get(i);
            float pad = 1f;
            cs.addRect(run.x() - pad, run.y(), Math.max(run.w() + pad * 2, 8f), Math.max(run.h(), 6f));
            cs.stroke();
        }
    }

    private void drawOverlayBox(PDPageContentStream cs, TextDiffRenderer.TextLine line,
            float srcH, float[] color, boolean strikethrough) throws IOException {
        float x = line.x() - 1;
        float y = line.y();
        float w = Math.max(line.w() + 2, 20f);
        float h = Math.max(line.h(), 6f);

        
        cs.setStrokingColor(color[0], color[1], color[2]);
        cs.setLineWidth(1f);
        cs.addRect(x, y, w, h);
        cs.stroke();

        
        if (strikethrough) {
            cs.setLineWidth(0.7f);
            cs.moveTo(x, y + h / 2);
            cs.lineTo(x + w, y + h / 2);
            cs.stroke();
        }
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max - 1) + "…" : (s == null ? "" : s);
    }
}
