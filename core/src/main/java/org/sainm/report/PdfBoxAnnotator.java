package org.sainm.report;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.sainm.model.DiffItem;
import org.sainm.model.DiffSeverity;
import org.sainm.model.DiffType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;









public final class PdfBoxAnnotator {

    




    public byte[] annotate(byte[] sourcePdf, List<DiffItem> items, boolean useRevised) throws IOException {
        try (var doc = Loader.loadPDF(sourcePdf)) {
            
            Map<Integer, List<TextRun>> pageRuns = extractRuns(doc);

            for (var item : items) {
                String target = useRevised ? item.getRevised() : item.getOriginal();
                if (target == null || target.isBlank() || target.equals("null")) continue;

                
                Match best = findBestMatch(pageRuns, target);
                if (best == null) continue;

                float[] color = boxColor(item.getSeverity(), item.getType());
                drawBox(doc, best, color);
            }

            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    

    private Map<Integer, List<TextRun>> extractRuns(PDDocument doc) throws IOException {
        Map<Integer, List<TextRun>> result = new HashMap<>();
        for (int p = 0; p < doc.getNumberOfPages(); p++) {
            final int pageIdx = p;
            List<TextRun> runs = new ArrayList<>();
            var stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) throws IOException {
                    if (text.isBlank() || positions.isEmpty()) { super.writeString(text, positions); return; }
                    var first = positions.get(0);
                    var last  = positions.get(positions.size() - 1);
                    float x = first.getXDirAdj();
                    float y = first.getYDirAdj();
                    float w = last.getXDirAdj() + last.getWidthDirAdj() - x;
                    float h = first.getHeightDir();
                    runs.add(new TextRun(text.strip(), x, y, Math.max(w, 10), Math.max(h, 8)));
                    super.writeString(text, positions);
                }
            };
            stripper.setStartPage(p + 1);
            stripper.setEndPage(p + 1);
            stripper.getText(doc);
            result.put(pageIdx, runs);
        }
        return result;
    }

    

    private Match findBestMatch(Map<Integer, List<TextRun>> pageRuns, String target) {
        
        String targetNorm = normalize(target);
        Match best = null;
        double bestScore = 0;

        for (var entry : pageRuns.entrySet()) {
            int page = entry.getKey();
            var runs = entry.getValue();

            
            for (var run : runs) {
                String runNorm = normalize(run.text);
                double score = similarity(runNorm, targetNorm);
                if (score > bestScore) {
                    bestScore = score;
                    best = new Match(page, run.x, run.y, run.w, run.h);
                }
            }

            
            for (int size = 2; size <= 4; size++) {
                for (int i = 0; i <= runs.size() - size; i++) {
                    StringBuilder sb = new StringBuilder();
                    float x1 = runs.get(i).x, y1 = runs.get(i).y;
                    float x2 = x1, y2 = y1;
                    for (int j = i; j < i + size; j++) {
                        sb.append(runs.get(j).text).append(" ");
                        x2 = Math.max(x2, runs.get(j).x + runs.get(j).w);
                        y2 = Math.max(y2, runs.get(j).y);
                    }
                    double score = similarity(normalize(sb.toString()), targetNorm);
                    if (score > bestScore) {
                        bestScore = score;
                        best = new Match(page, x1, Math.min(y1, y2),
                                x2 - x1, Math.abs(y2 - y1) + runs.get(i).h);
                    }
                }
            }
        }
        return bestScore > 0.3 ? best : null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[\\s]+", " ").trim();
    }

    
    private double similarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        if (a.equals(b)) return 1.0;
        String shorter = a.length() < b.length() ? a : b;
        String longer  = a.length() < b.length() ? b : a;
        if (longer.contains(shorter)) return (double) shorter.length() / longer.length();
        
        Set<String> wa = new HashSet<>(Arrays.asList(a.split(" ")));
        Set<String> wb = new HashSet<>(Arrays.asList(b.split(" ")));
        long common = wa.stream().filter(wb::contains).count();
        return (2.0 * common) / (wa.size() + wb.size());
    }

    

    private void drawBox(PDDocument doc, Match m, float[] rgb) throws IOException {
        PDPage page = doc.getPage(m.pageIdx);
        float pageH = page.getMediaBox().getHeight();

        
        float pdfY = pageH - m.y;
        float pad = 3f;

        try (var cs = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            cs.setStrokingColor(rgb[0], rgb[1], rgb[2]);
            cs.setLineWidth(1.5f);
            cs.addRect(m.x - pad, pdfY - m.h - pad, m.w + pad * 2, m.h + pad * 2);
            cs.stroke();

            
            cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
            
            cs.addRect(m.x - pad, pdfY - m.h - pad, 3f, m.h + pad * 2);
            cs.fill();
        }
    }

    private float[] boxColor(DiffSeverity severity, DiffType type) {
        if (type == DiffType.ADD)    return new float[]{0.1f, 0.6f, 0.1f};  
        if (type == DiffType.DELETE) return new float[]{0.8f, 0.1f, 0.1f};  
        return switch (severity) {
            case CRITICAL -> new float[]{0.85f, 0.1f, 0.1f};
            case MAJOR    -> new float[]{0.9f,  0.5f, 0.0f};
            case MINOR    -> new float[]{0.8f,  0.8f, 0.0f};
            case INFO     -> new float[]{0.5f,  0.5f, 0.5f};
        };
    }

    

    private record TextRun(String text, float x, float y, float w, float h) {}
    private record Match(int pageIdx, float x, float y, float w, float h) {}
}
