package org.sainm.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.sainm.model.CompareResult;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;







final class SideBySideRenderer {

    private static final float MARGIN = 20f;
    private static final float GAP    = 8f;
    private static final float DPI    = 150f;

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        var linesA = new TextDiffRenderer().extractLines(pdfA);
        var linesB = new TextDiffRenderer().extractLines(pdfB);

        var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
        var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        try (var docA = Loader.loadPDF(pdfA);
             var docB = Loader.loadPDF(pdfB);
             var out  = new PDDocument()) {

            int pages = Math.max(docA.getNumberOfPages(), docB.getNumberOfPages());
            var rendA = new PDFRenderer(docA);
            var rendB = new PDFRenderer(docB);

            for (int p = 0; p < pages; p++) {
                float srcWa = p < docA.getNumberOfPages() ? docA.getPage(p).getMediaBox().getWidth()  : 595f;
                float srcHa = p < docA.getNumberOfPages() ? docA.getPage(p).getMediaBox().getHeight() : 842f;
                float srcWb = p < docB.getNumberOfPages() ? docB.getPage(p).getMediaBox().getWidth()  : 595f;
                float srcHb = p < docB.getNumberOfPages() ? docB.getPage(p).getMediaBox().getHeight() : 842f;

                
                float pageH = Math.max(srcHa, srcHb) + MARGIN * 2;
                float colW  = Math.max(srcWa, srcWb);
                float pageW = colW * 2 + MARGIN * 2 + GAP;

                float imgHa = srcHa, imgHb = srcHb;
                float scaleXa = colW / srcWa, scaleYa = imgHa / srcHa;
                float scaleXb = colW / srcWb, scaleYb = imgHb / srcHb;
                float colL = MARGIN, colR = MARGIN + colW + GAP;

                var page = new PDPage(new PDRectangle(pageW, pageH));
                out.addPage(page);

                
                try (var cs = new PDPageContentStream(out, page)) {
                    if (p < docA.getNumberOfPages()) {
                        BufferedImage imgA = rendA.renderImageWithDPI(p, DPI);
                        cs.drawImage(LosslessFactory.createFromImage(out, imgA), colL, MARGIN, colW, imgHa);
                    }
                    if (p < docB.getNumberOfPages()) {
                        BufferedImage imgB = rendB.renderImageWithDPI(p, DPI);
                        cs.drawImage(LosslessFactory.createFromImage(out, imgB), colR, MARGIN, colW, imgHb);
                    }
                    
                    cs.setStrokingColor(0.6f, 0.6f, 0.6f);
                    cs.setLineWidth(0.5f);
                    cs.moveTo(colL + colW + GAP / 2, pageH - MARGIN / 2);
                    cs.lineTo(colL + colW + GAP / 2, MARGIN / 2);
                    cs.stroke();
                }

                
                try (var cs = new PDPageContentStream(out, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {

                    for (var delta : patch.getDeltas()) {
                        int ai = delta.getSource().getPosition();
                        int bi = delta.getTarget().getPosition();

                        switch (delta.getType()) {
                            case DELETE -> {
                                for (int i = 0; i < delta.getSource().getLines().size(); i++) {
                                    var line = linesA.get(ai + i);
                                    drawDeleteBox(cs, line, colL, scaleXa, scaleYa);
                                }
                            }
                            case INSERT -> {
                                for (int i = 0; i < delta.getTarget().getLines().size(); i++) {
                                    var line = linesB.get(bi + i);
                                    drawInsertBox(cs, line, colR, scaleXb, scaleYb);
                                }
                            }
                            case CHANGE -> {
                                int srcSize = delta.getSource().getLines().size();
                                int tgtSize = delta.getTarget().getLines().size();
                                for (int i = 0; i < srcSize; i++) {
                                    var lineA = linesA.get(ai + i);
                                    if (i >= tgtSize) {
                                        
                                        drawDeleteBox(cs, lineA, colL, scaleXa, scaleYa);
                                    } else {
                                        drawChangeBox(cs, lineA, linesB.get(bi + i), colL, scaleXa, scaleYa);
                                    }
                                }
                                for (int i = 0; i < tgtSize; i++) {
                                    var lineB = linesB.get(bi + i);
                                    if (i >= srcSize) {
                                        
                                        drawInsertBox(cs, lineB, colR, scaleXb, scaleYb);
                                    } else {
                                        drawChangeBox(cs, lineB, linesA.get(ai + i), colR, scaleXb, scaleYb);
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

    
    private void drawDeleteBox(PDPageContentStream cs, TextDiffRenderer.TextLine line,
            float colX, float scaleX, float scaleY) throws IOException {
        float x = colX + line.x() * scaleX;
        float y = MARGIN + line.y() * scaleY;
        float w = Math.max(line.w() * scaleX, 20f);
        float h = Math.max(line.h() * scaleY, 6f);

        cs.setStrokingColor(0.85f, 0.1f, 0.1f);
        cs.setLineWidth(1f);
        cs.addRect(x, y, w, h); cs.stroke();

        
        cs.setLineWidth(0.7f);
        cs.moveTo(x, y + h / 2);
        cs.lineTo(x + w, y + h / 2);
        cs.stroke();
    }

    
    private void drawInsertBox(PDPageContentStream cs, TextDiffRenderer.TextLine line,
            float colX, float scaleX, float scaleY) throws IOException {
        float x = colX + line.x() * scaleX;
        float y = MARGIN + line.y() * scaleY;
        float w = Math.max(line.w() * scaleX, 20f);
        float h = Math.max(line.h() * scaleY, 6f);

        cs.setStrokingColor(0.1f, 0.65f, 0.1f);
        cs.setLineWidth(1f);
        cs.addRect(x, y, w, h); cs.stroke();
    }

    
    private void drawChangeBox(PDPageContentStream cs,
            TextDiffRenderer.TextLine line, TextDiffRenderer.TextLine other,
            float colX, float scaleX, float scaleY) throws IOException {

        List<TextDiffRenderer.Run> runsA = line.runs();
        List<TextDiffRenderer.Run> runsB = other.runs();

        List<String> textsA = runsA.stream().map(TextDiffRenderer.Run::text).toList();
        List<String> textsB = runsB.stream().map(TextDiffRenderer.Run::text).toList();
        var patch = DiffUtils.diff(textsA, textsB);

        Set<Integer> changedIdx = new HashSet<>();
        for (var d : patch.getDeltas())
            for (int i = d.getSource().getPosition(); i < d.getSource().getPosition() + d.getSource().size(); i++)
                changedIdx.add(i);

        if (changedIdx.isEmpty() || runsA.isEmpty()) {
            
            return;
        }

        cs.setStrokingColor(0.9f, 0.45f, 0.0f);
        cs.setLineWidth(1f);
        for (int i = 0; i < runsA.size(); i++) {
            if (!changedIdx.contains(i)) continue;
            var run = runsA.get(i);
            float pad = 1f;
            float rx = colX + run.x() * scaleX - pad;
            float ry = MARGIN + run.y() * scaleY;
            float rw = Math.max(run.w() * scaleX + pad * 2, 8f);
            float rh = Math.max(run.h() * scaleY, 6f);
            cs.addRect(rx, ry, rw, rh); cs.stroke();
        }
    }

    private BufferedImage blankImage() {
        var img = new BufferedImage(595, 842, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, 595, 842);
        g.dispose();
        return img;
    }
}
