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
import java.util.HashSet;
import java.util.Set;





final class DualPageRenderer {

    private static final float DPI = 150f;

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        var linesA = new TextDiffRenderer().extractLines(pdfA);
        var linesB = new TextDiffRenderer().extractLines(pdfB);

        var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
        var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
        var patch  = com.github.difflib.DiffUtils.diff(textsA, textsB);

        try (var docA = Loader.loadPDF(pdfA);
             var docB = Loader.loadPDF(pdfB);
             var out  = new PDDocument()) {

            var rendA = new PDFRenderer(docA);
            var rendB = new PDFRenderer(docB);
            var font  = PDType0Font.load(out, TextDiffRenderer.resolveFontStream());

            int pages = Math.max(docA.getNumberOfPages(), docB.getNumberOfPages());

            for (int p = 0; p < pages; p++) {
                
                if (p < docA.getNumberOfPages()) {
                    float srcW = docA.getPage(p).getMediaBox().getWidth();
                    float srcH = docA.getPage(p).getMediaBox().getHeight();
                    var page = new PDPage(new PDRectangle(srcW, srcH));
                    out.addPage(page);
                    BufferedImage img = rendA.renderImageWithDPI(p, DPI);
                    try (var cs = new PDPageContentStream(out, page)) {
                        cs.drawImage(LosslessFactory.createFromImage(out, img), 0, 0, srcW, srcH);
                        
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.8f);
                        cs.beginText(); cs.setFont(font, 9f);
                        cs.newLineAtOffset(4f, srcH - 12f);
                        cs.showText("【旧版】"); cs.endText();
                    }
                    try (var cs = new PDPageContentStream(out, page,
                            PDPageContentStream.AppendMode.APPEND, true, true)) {
                        for (var delta : patch.getDeltas()) {
                            int ai = delta.getSource().getPosition();
                            int bi = delta.getTarget().getPosition();
                            if (delta.getType() == com.github.difflib.patch.DeltaType.DELETE) {
                                for (int i = 0; i < delta.getSource().getLines().size(); i++)
                                    drawBox(cs, linesA.get(ai + i), new float[]{0.85f, 0.1f, 0.1f}, true);
                            } else if (delta.getType() == com.github.difflib.patch.DeltaType.CHANGE) {
                                int srcSize = delta.getSource().getLines().size();
                                int tgtSize = delta.getTarget().getLines().size();
                                for (int i = 0; i < srcSize; i++) {
                                    var lineA = linesA.get(ai + i);
                                    if (i >= tgtSize) {
                                        drawBox(cs, lineA, new float[]{0.85f, 0.1f, 0.1f}, true);
                                    } else {
                                        drawChangeRuns(cs, lineA, linesB.get(bi + i));
                                    }
                                }
                            }
                        }
                    }
                }

                
                if (p < docB.getNumberOfPages()) {
                    float srcW = docB.getPage(p).getMediaBox().getWidth();
                    float srcH = docB.getPage(p).getMediaBox().getHeight();
                    var page = new PDPage(new PDRectangle(srcW, srcH));
                    out.addPage(page);
                    BufferedImage img = rendB.renderImageWithDPI(p, DPI);
                    try (var cs = new PDPageContentStream(out, page)) {
                        cs.drawImage(LosslessFactory.createFromImage(out, img), 0, 0, srcW, srcH);
                        cs.setNonStrokingColor(0.1f, 0.5f, 0.1f);
                        cs.beginText(); cs.setFont(font, 9f);
                        cs.newLineAtOffset(4f, srcH - 12f);
                        cs.showText("【新版】"); cs.endText();
                    }
                    try (var cs = new PDPageContentStream(out, page,
                            PDPageContentStream.AppendMode.APPEND, true, true)) {
                        for (var delta : patch.getDeltas()) {
                            int ai = delta.getSource().getPosition();
                            int bi = delta.getTarget().getPosition();
                            if (delta.getType() == com.github.difflib.patch.DeltaType.INSERT) {
                                for (int i = 0; i < delta.getTarget().getLines().size(); i++)
                                    drawBox(cs, linesB.get(bi + i), new float[]{0.1f, 0.65f, 0.1f}, false);
                            } else if (delta.getType() == com.github.difflib.patch.DeltaType.CHANGE) {
                                int srcSize = delta.getSource().getLines().size();
                                int tgtSize = delta.getTarget().getLines().size();
                                for (int i = 0; i < tgtSize; i++) {
                                    var lineB = linesB.get(bi + i);
                                    if (i >= srcSize) {
                                        drawBox(cs, lineB, new float[]{0.1f, 0.65f, 0.1f}, false);
                                    } else {
                                        drawChangeRuns(cs, lineB, linesA.get(ai + i));
                                    }
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

    private void drawBox(PDPageContentStream cs, TextDiffRenderer.TextLine line,
            float[] color, boolean strikethrough) throws IOException {
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

    private void drawChangeRuns(PDPageContentStream cs,
            TextDiffRenderer.TextLine line, TextDiffRenderer.TextLine other) throws IOException {
        var runsA = line.runs();
        var runsB = other.runs();
        var textsA = runsA.stream().map(TextDiffRenderer.Run::text).toList();
        var textsB = runsB.stream().map(TextDiffRenderer.Run::text).toList();
        var patch = com.github.difflib.DiffUtils.diff(textsA, textsB);

        Set<Integer> changedIdx = new HashSet<>();
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
}
