package org.sainm.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.sainm.model.CompareResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;





final class HtmlDiffRenderer {

    private static final float DPI = 120f;
    private static final float SCALE = DPI / 72f; 

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        var linesA = new TextDiffRenderer().extractLines(pdfA);
        var linesB = new TextDiffRenderer().extractLines(pdfB);

        var textsA = linesA.stream().map(TextDiffRenderer.TextLine::text).toList();
        var textsB = linesB.stream().map(TextDiffRenderer.TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        try (var docA = Loader.loadPDF(pdfA);
             var docB = Loader.loadPDF(pdfB)) {

            var rendA = new PDFRenderer(docA);
            var rendB = new PDFRenderer(docB);
            int pages = Math.max(docA.getNumberOfPages(), docB.getNumberOfPages());

            var sb = new StringBuilder();
            sb.append(htmlHead());

            for (int p = 0; p < pages; p++) {
                float srcHa = p < docA.getNumberOfPages()
                    ? docA.getPage(p).getMediaBox().getHeight() : 842f;
                float srcHb = p < docB.getNumberOfPages()
                    ? docB.getPage(p).getMediaBox().getHeight() : 842f;
                float srcWa = p < docA.getNumberOfPages()
                    ? docA.getPage(p).getMediaBox().getWidth() : 595f;
                float srcWb = p < docB.getNumberOfPages()
                    ? docB.getPage(p).getMediaBox().getWidth() : 595f;

                String imgA = p < docA.getNumberOfPages()
                    ? toBase64(rendA.renderImageWithDPI(p, DPI)) : "";
                String imgB = p < docB.getNumberOfPages()
                    ? toBase64(rendB.renderImageWithDPI(p, DPI)) : "";

                int pxWa = Math.round(srcWa * SCALE);
                int pxHa = Math.round(srcHa * SCALE);
                int pxWb = Math.round(srcWb * SCALE);
                int pxHb = Math.round(srcHb * SCALE);

                sb.append("<div class='page-pair'>\n");
                sb.append("<div class='page-label'>ページ ").append(p + 1).append("</div>\n");
                sb.append("<div class='pages'>\n");

                
                sb.append("<div class='col'><div class='col-label old'>旧版</div>");
                sb.append("<div class='page-wrap' style='width:").append(pxWa).append("px;height:").append(pxHa).append("px'>\n");
                if (!imgA.isEmpty())
                    sb.append("<img src='data:image/png;base64,").append(imgA).append("' width='").append(pxWa).append("' height='").append(pxHa).append("'/>\n");

                
                for (var delta : patch.getDeltas()) {
                    int ai = delta.getSource().getPosition();
                    int bi = delta.getTarget().getPosition();
                    if (delta.getType() == DeltaType.DELETE) {
                        for (int i = 0; i < delta.getSource().getLines().size(); i++)
                            sb.append(annBox(linesA.get(ai + i), srcHa, pxHa, "ann-delete", "削除"));
                    } else if (delta.getType() == DeltaType.CHANGE) {
                        int srcSize = delta.getSource().getLines().size();
                        int tgtSize = delta.getTarget().getLines().size();
                        for (int i = 0; i < srcSize; i++) {
                            var lineA = linesA.get(ai + i);
                            if (i >= tgtSize) {
                                sb.append(annBox(lineA, srcHa, pxHa, "ann-delete", "削除"));
                            } else {
                                sb.append(annChangeRuns(lineA, linesB.get(bi + i), srcHa, pxHa));
                            }
                        }
                    }
                }
                sb.append("</div></div>\n");

                
                sb.append("<div class='col'><div class='col-label new'>新版</div>");
                sb.append("<div class='page-wrap' style='width:").append(pxWb).append("px;height:").append(pxHb).append("px'>\n");
                if (!imgB.isEmpty())
                    sb.append("<img src='data:image/png;base64,").append(imgB).append("' width='").append(pxWb).append("' height='").append(pxHb).append("'/>\n");

                for (var delta : patch.getDeltas()) {
                    int ai = delta.getSource().getPosition();
                    int bi = delta.getTarget().getPosition();
                    if (delta.getType() == DeltaType.INSERT) {
                        for (int i = 0; i < delta.getTarget().getLines().size(); i++)
                            sb.append(annBox(linesB.get(bi + i), srcHb, pxHb, "ann-insert", "追加"));
                    } else if (delta.getType() == DeltaType.CHANGE) {
                        int srcSize = delta.getSource().getLines().size();
                        int tgtSize = delta.getTarget().getLines().size();
                        for (int i = 0; i < tgtSize; i++) {
                            var lineB = linesB.get(bi + i);
                            if (i >= srcSize) {
                                sb.append(annBox(lineB, srcHb, pxHb, "ann-insert", "追加"));
                            } else {
                                sb.append(annChangeRuns(lineB, linesA.get(ai + i), srcHb, pxHb));
                            }
                        }
                    }
                }
                sb.append("</div></div>\n");

                sb.append("</div></div>\n");
            }

            sb.append("</body></html>");
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    private String annBox(TextDiffRenderer.TextLine line, float srcH, int pxH,
            String cls, String label) {
        float scaleY = (float) pxH / srcH;
        float scaleX = scaleY;
        int x = Math.round(line.x() * scaleX) - 1;
        
        
        int y = Math.round((srcH - line.y() - line.h()) * scaleY);
        int w = Math.max(Math.round(line.w() * scaleX) + 2, 30);
        int h = Math.max(Math.round(line.h() * scaleY), 6);
        return String.format(
            "<div class='ann %s' style='left:%dpx;top:%dpx;width:%dpx;height:%dpx' title='%s'></div>\n",
            cls, x, y, w, h, label);
    }

    private String annChangeRuns(TextDiffRenderer.TextLine line, TextDiffRenderer.TextLine other,
            float srcH, int pxH) {
        var runsA = line.runs();
        var runsB = other.runs();
        var textsA = runsA.stream().map(TextDiffRenderer.Run::text).toList();
        var textsB = runsB.stream().map(TextDiffRenderer.Run::text).toList();
        var patch = com.github.difflib.DiffUtils.diff(textsA, textsB);

        java.util.Set<Integer> changedIdx = new java.util.HashSet<>();
        for (var d : patch.getDeltas())
            for (int i = d.getSource().getPosition(); i < d.getSource().getPosition() + d.getSource().size(); i++)
                changedIdx.add(i);

        if (changedIdx.isEmpty() || runsA.isEmpty()) return "";

        float scaleY = (float) pxH / srcH;
        float scaleX = scaleY;
        var sb = new StringBuilder();
        for (int i = 0; i < runsA.size(); i++) {
            if (!changedIdx.contains(i)) continue;
            var run = runsA.get(i);
            int x = Math.round(run.x() * scaleX) - 1;
            int y = Math.round((srcH - run.y() - run.h()) * scaleY);
            int w = Math.max(Math.round(run.w() * scaleX) + 2, 8);
            int h = Math.max(Math.round(run.h() * scaleY), 6);
            sb.append(String.format(
                "<div class='ann ann-change' style='left:%dpx;top:%dpx;width:%dpx;height:%dpx' title='変更'></div>\n",
                x, y, w, h));
        }
        return sb.toString();
    }

    private String toBase64(BufferedImage img) throws IOException {
        var out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private String htmlHead() {
        return """
            <!DOCTYPE html>
            <html lang="ja">
            <head>
            <meta charset="UTF-8"/>
            <title>PDF差分レポート</title>
            <style>
              body { font-family: sans-serif; background:#f5f5f5; margin:0; padding:16px; }
              h1 { font-size:18px; color:#333; }
              .page-pair { margin-bottom:32px; }
              .page-label { font-size:13px; color:#666; margin-bottom:6px; }
              .pages { display:flex; gap:16px; align-items:flex-start; }
              .col { display:flex; flex-direction:column; }
              .col-label { font-size:11px; font-weight:bold; padding:2px 6px; border-radius:3px; margin-bottom:4px; width:fit-content; }
              .col-label.old { background:#fdd; color:#900; }
              .col-label.new { background:#dfd; color:#060; }
              .page-wrap { position:relative; border:1px solid #ccc; background:#fff; }
              .page-wrap img { display:block; }
              .ann { position:absolute; pointer-events:none; border-radius:2px; }
              .ann-delete { border:2px solid rgba(200,30,30,0.85); background:rgba(255,80,80,0.15); }
              .ann-insert { border:2px solid rgba(30,160,30,0.85); background:rgba(80,200,80,0.15); }
              .ann-change { border:2px solid rgba(220,120,0,0.85); background:rgba(255,180,50,0.15); }
            </style>
            </head>
            <body>
            <h1>PDF差分レポート</h1>
            """;
    }
}
