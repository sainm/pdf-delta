package org.sainm.report;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.sainm.exception.RenderException;
import org.sainm.extractor.PdfExtractorFactory;
import org.sainm.model.BoundingBox;
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareResult;
import org.sainm.model.DiffType;
import org.sainm.model.PageContent;
import org.sainm.model.PdfSource;
import org.sainm.model.TableBlock;
import org.sainm.model.TableCell;
import org.sainm.model.VisualDiffItem;
import org.sainm.normalizer.NormalizerChain;
import org.sainm.spi.ReportRenderer;
import org.sainm.table.TableReconstructor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ImageMarkedPdfRenderer implements ReportRenderer {
    private static final float MARGIN = 20f;
    private static final float GAP = 10f;
    private static final float RENDER_DPI = 150f;
    private static final float MIN_BOX_SIZE = 12f;
    private static final double ROW_MATCH_THRESHOLD = 0.62;

    @Override
    public String formatId() {
        return "pdf-image-marked";
    }

    @Override
    public byte[] render(CompareResult result, CompareOptions options) {
        byte[] sourceBytesA = result.getSourceBytesA();
        byte[] sourceBytesB = result.getSourceBytesB();
        if (sourceBytesA == null || sourceBytesA.length == 0 || sourceBytesB == null || sourceBytesB.length == 0) {
            throw new RenderException("Image-marked PDF report requires both source PDFs");
        }

        try {
            List<PageContent> pagesA = extractAndNormalize(sourceBytesA, options);
            List<PageContent> pagesB = extractAndNormalize(sourceBytesB, options);
            Map<Integer, List<BoxMark>> marksA = new HashMap<>();
            Map<Integer, List<BoxMark>> marksB = new HashMap<>();

            addTableCellMarks(pagesA, pagesB, marksA, marksB);
            addVisualMarks(result.getVisualDiffItems(), marksA, marksB);

            return renderSideBySide(sourceBytesA, sourceBytesB, pagesA, pagesB, marksA, marksB, options);
        } catch (Exception e) {
            throw new RenderException("Failed to render image-marked PDF report", e);
        }
    }

    private List<PageContent> extractAndNormalize(byte[] pdfBytes, CompareOptions options) {
        List<PageContent> pages = PdfExtractorFactory.fromSpi().extract(new PdfSource.Bytes(pdfBytes), options);
        var normalizer = NormalizerChain.fromSpi();
        var reconstructor = new TableReconstructor();
        for (PageContent page : pages) {
            normalizer.applyToPage(page, options);
            page.setTableBlocks(reconstructor.reconstruct(page, options));
            normalizer.applyToPage(page, options);
        }
        return pages;
    }

    private void addTableCellMarks(List<PageContent> pagesA,
                                   List<PageContent> pagesB,
                                   Map<Integer, List<BoxMark>> marksA,
                                   Map<Integer, List<BoxMark>> marksB) {
        int pageCount = Math.max(pagesA.size(), pagesB.size());
        for (int i = 0; i < pageCount; i++) {
            List<TableBlock> tablesA = i < pagesA.size() ? pagesA.get(i).getTableBlocks() : List.of();
            List<TableBlock> tablesB = i < pagesB.size() ? pagesB.get(i).getTableBlocks() : List.of();
            int pageNumber = i + 1;
            int tableCount = Math.max(tablesA.size(), tablesB.size());
            for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
                TableBlock tableA = tableIndex < tablesA.size() ? tablesA.get(tableIndex) : null;
                TableBlock tableB = tableIndex < tablesB.size() ? tablesB.get(tableIndex) : null;
                markTableDiff(pageNumber, tableA, tableB, marksA, marksB);
            }
        }
    }

    private void markTableDiff(int pageNumber,
                               TableBlock tableA,
                               TableBlock tableB,
                               Map<Integer, List<BoxMark>> marksA,
                               Map<Integer, List<BoxMark>> marksB) {
        List<List<TableCell>> rowsA = tableA != null ? tableA.getCells() : List.of();
        List<List<TableCell>> rowsB = tableB != null ? tableB.getCells() : List.of();
        for (RowAlignment alignment : alignRows(rowsA, rowsB)) {
            List<TableCell> rowA = alignment.originalIndex() >= 0 ? rowsA.get(alignment.originalIndex()) : null;
            List<TableCell> rowB = alignment.revisedIndex() >= 0 ? rowsB.get(alignment.revisedIndex()) : null;

            if (alignment.type() == DiffType.ADD) {
                BoundingBox rowBox = rowBoundingBox(rowB);
                if (rowBox != null) {
                    addMark(marksB, pageNumber, rowBox, 0.10f, 0.65f, 0.10f, true);
                }
                continue;
            }
            if (alignment.type() == DiffType.DELETE) {
                BoundingBox rowBox = rowBoundingBox(rowA);
                if (rowBox != null) {
                    addMark(marksA, pageNumber, rowBox, 0.85f, 0.15f, 0.15f, true);
                }
                continue;
            }

            int colCount = Math.max(rowA != null ? rowA.size() : 0, rowB != null ? rowB.size() : 0);
            boolean rowChanged = false;
            for (int col = 0; col < colCount; col++) {
                TableCell cellA = rowA != null && col < rowA.size() ? rowA.get(col) : null;
                TableCell cellB = rowB != null && col < rowB.size() ? rowB.get(col) : null;
                String textA = normalizedCellText(cellA);
                String textB = normalizedCellText(cellB);
                if (Objects.equals(textA, textB)) {
                    continue;
                }
                rowChanged = true;
                if (cellA != null && cellA.getBbox() != null) {
                    addMark(marksA, pageNumber, cellA.getBbox(), 0.95f, 0.55f, 0.05f);
                }
                if (cellB != null && cellB.getBbox() != null) {
                    addMark(marksB, pageNumber, cellB.getBbox(), 0.95f, 0.55f, 0.05f);
                }
            }
            if (!rowChanged) {
                continue;
            }
        }
    }

    private void addVisualMarks(List<VisualDiffItem> visualDiffItems,
                                Map<Integer, List<BoxMark>> marksA,
                                Map<Integer, List<BoxMark>> marksB) {
        if (visualDiffItems == null) {
            return;
        }
        for (VisualDiffItem item : visualDiffItems) {
            addMark(marksA, item.getPageNumber(), item.getBbox(), 0.15f, 0.35f, 1.0f);
            addMark(marksB, item.getPageNumber(), item.getBbox(), 0.15f, 0.35f, 1.0f);
        }
    }

    private void addMark(Map<Integer, List<BoxMark>> marks, int pageNumber, BoundingBox bbox,
                         float r, float g, float b) {
        if (bbox == null) {
            return;
        }
        marks.computeIfAbsent(pageNumber, ignored -> new ArrayList<>()).add(new BoxMark(bbox, r, g, b, false));
    }

    private void addMark(Map<Integer, List<BoxMark>> marks, int pageNumber, BoundingBox bbox,
                         float r, float g, float b, boolean rowLevel) {
        if (bbox == null) {
            return;
        }
        marks.computeIfAbsent(pageNumber, ignored -> new ArrayList<>()).add(new BoxMark(bbox, r, g, b, rowLevel));
    }

    private byte[] renderSideBySide(byte[] pdfA,
                                    byte[] pdfB,
                                    List<PageContent> pagesA,
                                    List<PageContent> pagesB,
                                    Map<Integer, List<BoxMark>> marksA,
                                    Map<Integer, List<BoxMark>> marksB,
                                    CompareOptions options) throws IOException {
        try (PDDocument docA = Loader.loadPDF(pdfA);
             PDDocument docB = Loader.loadPDF(pdfB);
             PDDocument out = new PDDocument()) {
            PDFRenderer rendererA = new PDFRenderer(docA);
            PDFRenderer rendererB = new PDFRenderer(docB);
            int pageCount = Math.max(docA.getNumberOfPages(), docB.getNumberOfPages());
            float renderDpi = options.getRenderDpi() > 0 ? options.getRenderDpi() : RENDER_DPI;
            float pixelToPoint = 72f / renderDpi;

            for (int i = 0; i < pageCount; i++) {
                float srcWa = i < pagesA.size() ? (float) pagesA.get(i).getWidth() : 595f;
                float srcHa = i < pagesA.size() ? (float) pagesA.get(i).getHeight() : 842f;
                float srcWb = i < pagesB.size() ? (float) pagesB.get(i).getWidth() : 595f;
                float srcHb = i < pagesB.size() ? (float) pagesB.get(i).getHeight() : 842f;

                float colW = Math.max(srcWa, srcWb);
                float pageW = MARGIN * 2 + colW * 2 + GAP;
                float pageH = MARGIN * 2 + Math.max(srcHa, srcHb) + 30f;
                float leftX = MARGIN;
                float rightX = MARGIN + colW + GAP;
                float baseY = MARGIN;

                PDPage page = new PDPage(new PDRectangle(pageW, pageH));
                out.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(out, page)) {
                    if (i < docA.getNumberOfPages()) {
                        BufferedImage imageA = rendererA.renderImageWithDPI(i, renderDpi);
                        cs.drawImage(LosslessFactory.createFromImage(out, imageA), leftX, baseY, srcWa, srcHa);
                    }
                    if (i < docB.getNumberOfPages()) {
                        BufferedImage imageB = rendererB.renderImageWithDPI(i, renderDpi);
                        cs.drawImage(LosslessFactory.createFromImage(out, imageB), rightX, baseY, srcWb, srcHb);
                    }
                    cs.beginText();
                    cs.setLeading(14f);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10f);
                    cs.newLineAtOffset(MARGIN, pageH - 14f);
                    cs.showText("Left=A  Right=B  Orange=TABLE/TEXT diff  Blue=VISUAL");
                    cs.endText();
                }

                try (PDPageContentStream cs = new PDPageContentStream(out, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    for (BoxMark mark : marksA.getOrDefault(i + 1, List.of())) {
                        drawBox(cs, leftX, baseY, srcHa, pixelToPoint, mark);
                    }
                    for (BoxMark mark : marksB.getOrDefault(i + 1, List.of())) {
                        drawBox(cs, rightX, baseY, srcHb, pixelToPoint, mark);
                    }
                }
            }

            var outBytes = new ByteArrayOutputStream();
            out.save(outBytes);
            return outBytes.toByteArray();
        }
    }

    private void drawBox(PDPageContentStream cs,
                         float offsetX,
                         float baseY,
                         float sourceHeight,
                         float pixelToPoint,
                         BoxMark mark) throws IOException {
        BoundingBox box = mark.box();
        float x = offsetX + (float) box.x() * pixelToPoint;
        float y = baseY + sourceHeight - ((float) box.y() + (float) box.height()) * pixelToPoint;
        float w = Math.max((float) box.width() * pixelToPoint, MIN_BOX_SIZE);
        float h = Math.max((float) box.height() * pixelToPoint, MIN_BOX_SIZE);
        cs.setStrokingColor(mark.r(), mark.g(), mark.b());
        float lineWidth = mark.rowLevel() ? 3.5f : 2.0f;
        float inset = mark.rowLevel() ? 2.0f : 0.0f;
        cs.setLineWidth(lineWidth);
        if (mark.rowLevel()) {
            cs.setNonStrokingColor(mark.r(), mark.g(), mark.b());
            cs.addRect(x - inset, y - inset, 8f, h + inset * 2);
            cs.fill();
        }
        cs.addRect(x - inset, y - inset, w + inset * 2, h + inset * 2);
        cs.stroke();
    }

    private int rowCount(TableBlock table) {
        return table == null ? 0 : table.getCells().size();
    }

    private int colCount(TableBlock table) {
        if (table == null || table.getCells().isEmpty()) {
            return 0;
        }
        return table.getCells().stream().mapToInt(List::size).max().orElse(0);
    }

    private TableCell cellAt(TableBlock table, int row, int col) {
        if (table == null || row < 0 || row >= table.getCells().size()) {
            return null;
        }
        List<TableCell> rowCells = table.getCells().get(row);
        if (col < 0 || col >= rowCells.size()) {
            return null;
        }
        return rowCells.get(col);
    }

    private String normalizedCellText(TableCell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getNormalizedText() != null) {
            return cell.getNormalizedText();
        }
        return cell.getText();
    }

    private List<RowAlignment> alignRows(List<List<TableCell>> rowsA, List<List<TableCell>> rowsB) {
        int n = rowsA.size();
        int m = rowsB.size();
        double[][] dp = new double[n + 1][m + 1];
        Move[][] move = new Move[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            dp[i][0] = dp[i - 1][0] + rowDeleteCost(rowsA.get(i - 1));
            move[i][0] = Move.DELETE;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = dp[0][j - 1] + rowInsertCost(rowsB.get(j - 1));
            move[0][j] = Move.ADD;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double modify = dp[i - 1][j - 1] + rowModifyCost(rowsA.get(i - 1), rowsB.get(j - 1));
                double delete = dp[i - 1][j] + rowDeleteCost(rowsA.get(i - 1));
                double add = dp[i][j - 1] + rowInsertCost(rowsB.get(j - 1));
                double best = modify;
                Move bestMove = Move.MATCH;
                if (delete < best) {
                    best = delete;
                    bestMove = Move.DELETE;
                }
                if (add < best) {
                    best = add;
                    bestMove = Move.ADD;
                }
                dp[i][j] = best;
                move[i][j] = bestMove;
            }
        }

        List<RowAlignment> reversed = new ArrayList<>();
        int i = n;
        int j = m;
        while (i > 0 || j > 0) {
            Move current = move[i][j];
            if (current == Move.MATCH) {
                reversed.add(new RowAlignment(i - 1, j - 1, DiffType.MODIFY));
                i--;
                j--;
            } else if (current == Move.DELETE) {
                reversed.add(new RowAlignment(i - 1, -1, DiffType.DELETE));
                i--;
            } else {
                reversed.add(new RowAlignment(-1, j - 1, DiffType.ADD));
                j--;
            }
        }
        java.util.Collections.reverse(reversed);
        return reversed;
    }

    private double rowModifyCost(List<TableCell> rowA, List<TableCell> rowB) {
        double similarity = rowSimilarity(rowA, rowB);
        if (similarity < ROW_MATCH_THRESHOLD) {
            return rowDeleteCost(rowA) + rowInsertCost(rowB) + 0.05;
        }
        return 1.0 - similarity;
    }

    private double rowDeleteCost(List<TableCell> row) {
        return Math.max(0.45, nonEmptyRatio(row));
    }

    private double rowInsertCost(List<TableCell> row) {
        return Math.max(0.45, nonEmptyRatio(row));
    }

    private double rowSimilarity(List<TableCell> rowA, List<TableCell> rowB) {
        int maxCols = Math.max(rowA.size(), rowB.size());
        if (maxCols == 0) {
            return 1.0;
        }
        double score = 0;
        for (int col = 0; col < maxCols; col++) {
            TableCell cellA = col < rowA.size() ? rowA.get(col) : null;
            TableCell cellB = col < rowB.size() ? rowB.get(col) : null;
            score += cellSimilarity(normalizedCellText(cellA), normalizedCellText(cellB));
        }
        return score / maxCols;
    }

    private double cellSimilarity(String a, String b) {
        String left = a == null ? "" : a.strip();
        String right = b == null ? "" : b.strip();
        if (left.equals(right)) {
            return 1.0;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }
        int maxLen = Math.max(left.length(), right.length());
        if (maxLen == 0) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 - (double) editDistance(left, right) / maxLen);
    }

    private double nonEmptyRatio(List<TableCell> row) {
        if (row.isEmpty()) {
            return 0.45;
        }
        long nonEmpty = row.stream()
                .map(this::normalizedCellText)
                .filter(text -> text != null && !text.isBlank())
                .count();
        return (double) nonEmpty / row.size();
    }

    private int editDistance(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            dp[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                dp[j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? prev
                        : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    private BoundingBox rowBoundingBox(List<TableCell> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        boolean found = false;
        for (TableCell cell : row) {
            if (cell == null || cell.getBbox() == null) {
                continue;
            }
            found = true;
            minX = Math.min(minX, cell.getBbox().x());
            minY = Math.min(minY, cell.getBbox().y());
            maxX = Math.max(maxX, cell.getBbox().x() + cell.getBbox().width());
            maxY = Math.max(maxY, cell.getBbox().y() + cell.getBbox().height());
        }
        if (!found) {
            return null;
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY, row.get(0).getBbox() != null ? row.get(0).getBbox().page() : 1);
    }

    private record BoxMark(BoundingBox box, float r, float g, float b, boolean rowLevel) {}

    private record RowAlignment(int originalIndex, int revisedIndex, DiffType type) {}

    private enum Move { MATCH, DELETE, ADD }
}
