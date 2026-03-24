package org.sainm.report;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.DeltaType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.sainm.model.CompareResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;




public final class TextDiffRenderer {

    private static final float PAGE_W  = PDRectangle.A4.getHeight();
    private static final float PAGE_H  = PDRectangle.A4.getWidth();
    private static final float MARGIN  = 40f;
    private static final float GAP     = 12f;
    private static final float COL_W   = (PAGE_W - MARGIN * 2 - GAP) / 2;
    private static final float LINE_H  = 14f;
    private static final float FONT_SZ = 8.5f;

    
    private static final List<String> SYSTEM_FONT_CANDIDATES = List.of(
        
        "/mnt/c/Windows/Fonts/NotoSansJP-VF.ttf",
        "/mnt/c/Windows/Fonts/msgothic.ttc",
        
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        
        "/Library/Fonts/Arial Unicode.ttf",
        "/System/Library/Fonts/Helvetica.ttc"
    );

    static java.io.InputStream resolveFontStream() {
        
        var stream = TextDiffRenderer.class.getResourceAsStream("/fonts/NotoSansJP.ttf");
        if (stream != null) return stream;
        
        for (var path : SYSTEM_FONT_CANDIDATES) {
            var f = new File(path);
            if (f.exists()) {
                try { return new java.io.FileInputStream(f); }
                catch (java.io.FileNotFoundException ignored) {}
            }
        }
        throw new org.sainm.exception.RenderException(
            "No usable font found. Add a font to src/main/resources/fonts/NotoSansJP.ttf");
    }

    public record Run(String text, float x, float y, float w, float h) {}
    public record TextLine(String text, float x, float y, float w, float h, List<Run> runs) {
        public TextLine(String text, float x, float y, float w, float h) {
            this(text, x, y, w, h, List.of());
        }
    }

    byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        List<TextLine> linesA = extractLines(pdfA);
        List<TextLine> linesB = extractLines(pdfB);

        float srcW = pdfWidth(pdfA);

        var textsA = linesA.stream().map(TextLine::text).toList();
        var textsB = linesB.stream().map(TextLine::text).toList();
        var patch  = DiffUtils.diff(textsA, textsB);

        record Row(TextLine a, TextLine b, DeltaType type) {}
        List<Row> rows = new ArrayList<>();

        int ai = 0, bi = 0;
        for (var delta : patch.getDeltas()) {
            int srcPos = delta.getSource().getPosition();
            while (ai < srcPos) {
                rows.add(new Row(linesA.get(ai), linesB.get(bi), DeltaType.EQUAL));
                ai++; bi++;
            }
            var srcLines = delta.getSource().getLines();
            var tgtLines = delta.getTarget().getLines();
            if (delta.getType() == DeltaType.CHANGE) {
                int maxLen = Math.max(srcLines.size(), tgtLines.size());
                for (int i = 0; i < maxLen; i++) {
                    TextLine la = i < srcLines.size() ? linesA.get(ai + i) : null;
                    TextLine lb = i < tgtLines.size() ? linesB.get(bi + i) : null;
                    if (la != null && lb != null && similarity(la.text(), lb.text()) < 0.4f) {
                        rows.add(new Row(la, null, DeltaType.DELETE));
                        rows.add(new Row(null, lb, DeltaType.INSERT));
                    } else {
                        rows.add(new Row(la, lb, DeltaType.CHANGE));
                    }
                }
            } else if (delta.getType() == DeltaType.DELETE) {
                for (int i = 0; i < srcLines.size(); i++)
                    rows.add(new Row(linesA.get(ai + i), null, DeltaType.DELETE));
            } else {
                for (int i = 0; i < tgtLines.size(); i++)
                    rows.add(new Row(null, linesB.get(bi + i), DeltaType.INSERT));
            }
            ai += srcLines.size();
            bi += tgtLines.size();
        }
        while (ai < linesA.size() && bi < linesB.size()) {
            rows.add(new Row(linesA.get(ai), linesB.get(bi), DeltaType.EQUAL));
            ai++; bi++;
        }

        try (var doc = new PDDocument()) {
            int rowsPerPage = (int) ((PAGE_H - MARGIN * 2) / LINE_H);
            List<List<Row>> pages = new ArrayList<>();
            for (int i = 0; i < rows.size(); i += rowsPerPage)
                pages.add(rows.subList(i, Math.min(i + rowsPerPage, rows.size())));
            if (pages.isEmpty()) pages.add(List.of());

            var font = PDType0Font.load(doc, resolveFontStream());
            float colL = MARGIN;
            float colR = MARGIN + COL_W + GAP;
            float scaleX = COL_W / srcW;

            for (var pageRows : pages) {
                var page = new PDPage(new PDRectangle(PAGE_W, PAGE_H));
                doc.addPage(page);

                try (var cs = new PDPageContentStream(doc, page)) {
                    float y = PAGE_H - MARGIN;
                    for (var row : pageRows) {
                        float[] bg = rowBg(row.type());
                        cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
                        cs.addRect(colL, y - LINE_H, COL_W, LINE_H); cs.fill();
                        cs.setNonStrokingColor(bg[3], bg[4], bg[5]);
                        cs.addRect(colR, y - LINE_H, COL_W, LINE_H); cs.fill();

                        if (row.a() != null) {
                            float tx = colL + row.a().x() * scaleX;
                            float fs = Math.min(FONT_SZ, Math.max(row.a().h() * 0.85f, 5f));
                            cs.setNonStrokingColor(0f, 0f, 0f);
                            cs.beginText();
                            cs.setFont(font, fs);
                            cs.newLineAtOffset(Math.min(tx, colL + COL_W - 10), y - LINE_H + 3f);
                            cs.showText(truncate(row.a().text(), 90));
                            cs.endText();
                        }
                        if (row.b() != null) {
                            float tx = colR + row.b().x() * scaleX;
                            float fs = Math.min(FONT_SZ, Math.max(row.b().h() * 0.85f, 5f));
                            cs.setNonStrokingColor(0f, 0f, 0f);
                            cs.beginText();
                            cs.setFont(font, fs);
                            cs.newLineAtOffset(Math.min(tx, colR + COL_W - 10), y - LINE_H + 3f);
                            cs.showText(truncate(row.b().text(), 90));
                            cs.endText();
                        }

                        cs.setStrokingColor(0.9f, 0.9f, 0.9f);
                        cs.setLineWidth(0.2f);
                        cs.moveTo(colL, y - LINE_H);
                        cs.lineTo(PAGE_W - MARGIN, y - LINE_H);
                        cs.stroke();
                        y -= LINE_H;
                    }
                    cs.setStrokingColor(0.6f, 0.6f, 0.6f);
                    cs.setLineWidth(0.6f);
                    cs.moveTo(colR - GAP / 2f, PAGE_H - MARGIN / 2);
                    cs.lineTo(colR - GAP / 2f, MARGIN / 2);
                    cs.stroke();
                }

                try (var cs = new PDPageContentStream(doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    float y = PAGE_H - MARGIN;
                    for (var row : pageRows) {
                        if (row.type() != DeltaType.EQUAL) {
                            float[] colorL = boxColor(row.type());
                            float[] colorR = row.type() == DeltaType.INSERT
                                ? new float[]{0.1f, 0.65f, 0.1f} : colorL;
                            if (row.type() == DeltaType.CHANGE && row.a() != null && row.b() != null) {
                                float txL = Math.min(colL + row.a().x() * scaleX, colL + COL_W - 10);
                                float txR = Math.min(colR + row.b().x() * scaleX, colR + COL_W - 10);
                                float fsL = Math.min(FONT_SZ, Math.max(row.a().h() * 0.85f, 5f));
                                float fsR = Math.min(FONT_SZ, Math.max(row.b().h() * 0.85f, 5f));
                                drawWordBoxes(cs, font, fsL, fsR, row.a().text(), row.b().text(),
                                    txL, txR, y, colorL, colorR);
                            } else {
                                float pad = 0.5f;
                                if (row.a() != null) {
                                    cs.setStrokingColor(colorL[0], colorL[1], colorL[2]);
                                    cs.setLineWidth(2f);
                                    cs.addRect(colL + pad, y - LINE_H + pad, COL_W - pad * 2, LINE_H - pad * 2);
                                    cs.stroke();
                                }
                                if (row.b() != null) {
                                    cs.setStrokingColor(colorR[0], colorR[1], colorR[2]);
                                    cs.setLineWidth(2f);
                                    cs.addRect(colR + pad, y - LINE_H + pad, COL_W - pad * 2, LINE_H - pad * 2);
                                    cs.stroke();
                                }
                            }
                        }
                        y -= LINE_H;
                    }
                }
            }

            var out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public List<TextLine> extractLines(byte[] pdf) throws IOException {
        List<Run> runs = new ArrayList<>();

        try (var doc = Loader.loadPDF(pdf)) {
            for (int p = 0; p < doc.getNumberOfPages(); p++) {
                final float pageH = doc.getPage(p).getMediaBox().getHeight();
                var stripper = new PDFTextStripper() {
                    @Override
                    protected void writeString(String text, List<TextPosition> pos) throws IOException {
                        if (text.isBlank() || pos.isEmpty()) { super.writeString(text, pos); return; }
                        var f = pos.get(0);
                        var l = pos.get(pos.size() - 1);
                        float pdfY = pageH - f.getY();
                        runs.add(new Run(text.strip(), f.getX(), pdfY,
                            l.getX() + l.getWidth() - f.getX(), f.getHeight()));
                        super.writeString(text, pos);
                    }
                };
                stripper.setStartPage(p + 1);
                stripper.setEndPage(p + 1);
                stripper.getText(doc);
            }
        }

        runs.sort(Comparator.comparingDouble(Run::y).reversed().thenComparingDouble(Run::x));
        List<TextLine> lines = new ArrayList<>();
        List<Run> current = new ArrayList<>();
        float lastY = Float.MIN_VALUE;

        for (var run : runs) {
            if (!current.isEmpty() && Math.abs(run.y() - lastY) > 3f) {
                lines.add(mergeRuns(current));
                current = new ArrayList<>();
            }
            current.add(run);
            lastY = run.y();
        }
        if (!current.isEmpty()) lines.add(mergeRuns(current));
        return lines;
    }

    private TextLine mergeRuns(List<Run> runs) {
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, lineY = 0, lineH = 0;
        StringBuilder sb = new StringBuilder();
        for (var r : runs) {
            if (r.x() < minX) { minX = r.x(); lineY = r.y(); }
            if (r.x() + r.w() > maxX) maxX = r.x() + r.w();
            if (r.h() > lineH) lineH = r.h();
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(r.text());
        }
        return new TextLine(sb.toString().strip(), minX, lineY, maxX - minX, lineH, List.copyOf(runs));
    }


    private float pdfHeight(byte[] pdf) throws IOException {
        try (var doc = Loader.loadPDF(pdf)) { return doc.getPage(0).getMediaBox().getHeight(); }
    }

    private float pdfWidth(byte[] pdf) throws IOException {
        try (var doc = Loader.loadPDF(pdf)) { return doc.getPage(0).getMediaBox().getWidth(); }
    }

    private float[] rowBg(DeltaType t) {
        return switch (t) {
            case DELETE -> new float[]{1f, 0.95f, 0.95f,  0.98f, 0.98f, 0.98f};
            case INSERT -> new float[]{0.98f, 0.98f, 0.98f,  0.95f, 1f, 0.95f};
            case CHANGE -> new float[]{1f, 0.97f, 0.93f,  0.93f, 0.97f, 1f};
            default     -> new float[]{1f, 1f, 1f,  1f, 1f, 1f};
        };
    }

    private float[] boxColor(DeltaType t) {
        return switch (t) {
            case DELETE -> new float[]{0.85f, 0.1f, 0.1f};
            case INSERT -> new float[]{0.1f, 0.65f, 0.1f};
            case CHANGE -> new float[]{0.9f, 0.45f, 0.0f};
            default     -> new float[]{0.5f, 0.5f, 0.5f};
        };
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max - 1) + "…" : (s == null ? "" : s);
    }

    private void drawWordBoxes(PDPageContentStream cs, PDType0Font font, float fsL, float fsR,
            String textA, String textB, float startX_L, float startX_R, float y,
            float[] colorL, float[] colorR) throws IOException {
        List<String> wordsA = Arrays.asList(textA.split(" ", -1));
        List<String> wordsB = Arrays.asList(textB.split(" ", -1));
        var patch = DiffUtils.diff(wordsA, wordsB);
        Set<Integer> changedA = new HashSet<>(), changedB = new HashSet<>();
        for (var delta : patch.getDeltas()) {
            for (int i = delta.getSource().getPosition(); i < delta.getSource().getPosition() + delta.getSource().size(); i++) changedA.add(i);
            for (int i = delta.getTarget().getPosition(); i < delta.getTarget().getPosition() + delta.getTarget().size(); i++) changedB.add(i);
        }
        drawChangedWordBoxes(cs, font, fsL, wordsA, changedA, startX_L, y, colorL);
        drawChangedWordBoxes(cs, font, fsR, wordsB, changedB, startX_R, y, colorR);
    }

    private void drawChangedWordBoxes(PDPageContentStream cs, PDType0Font font, float fs,
            List<String> words, Set<Integer> changed, float startX, float y, float[] color) throws IOException {
        float x = startX;
        float spaceW = font.getStringWidth(" ") / 1000f * fs;
        float pad = 1f;
        List<float[]> spans = new ArrayList<>();
        float spanStart = -1, spanEnd = -1;
        for (int i = 0; i < words.size(); i++) {
            float ww = font.getStringWidth(words.get(i)) / 1000f * fs;
            if (changed.contains(i)) {
                if (spanStart < 0) spanStart = x - pad;
                spanEnd = x + ww + pad;
            } else {
                if (spanStart >= 0) { spans.add(new float[]{spanStart, spanEnd}); spanStart = -1; }
            }
            x += ww + spaceW;
        }
        if (spanStart >= 0) spans.add(new float[]{spanStart, spanEnd});
        cs.setStrokingColor(color[0], color[1], color[2]);
        cs.setLineWidth(1.5f);
        for (var span : spans) {
            cs.addRect(span[0], y - LINE_H + pad, span[1] - span[0], LINE_H - pad * 2);
            cs.stroke();
        }
    }

    private float similarity(String a, String b) {
        if (a == null || b == null) return 0f;
        if (a.equals(b)) return 1f;
        var setA = bigrams(a); var setB = bigrams(b);
        if (setA.isEmpty() && setB.isEmpty()) return 1f;
        if (setA.isEmpty() || setB.isEmpty()) return 0f;
        long intersection = setA.stream().filter(setB::contains).count();
        return (2f * intersection) / (setA.size() + setB.size());
    }

    private Set<String> bigrams(String s) {
        Set<String> set = new HashSet<>();
        String lower = s.toLowerCase();
        for (int i = 0; i < lower.length() - 1; i++) set.add(lower.substring(i, i + 2));
        return set;
    }
}
