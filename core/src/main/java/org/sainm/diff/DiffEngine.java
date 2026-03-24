package org.sainm.diff;

import com.github.difflib.DiffUtils;
import org.sainm.model.AlignedPair;
import org.sainm.model.BlockType;
import org.sainm.model.CharDiffSegment;
import org.sainm.model.CompareOptions;
import org.sainm.model.DiffItem;
import org.sainm.model.DiffSeverity;
import org.sainm.model.DiffType;
import org.sainm.model.TableCellDiff;
import org.sainm.model.TableColumnDiff;
import org.sainm.model.TableDiffDetail;
import org.sainm.model.TableRowDiff;

import java.util.*;

public final class DiffEngine {
    private static final double ROW_MATCH_THRESHOLD = 0.62;
    private final SeverityClassifier classifier = new SeverityClassifier();

    public List<DiffItem> diff(List<AlignedPair> pairs, CompareOptions options) {
        List<DiffItem> result = new ArrayList<>();
        for (var pair : pairs) {
            switch (pair.type()) {
                case EQUAL -> {}
                case ADD -> result.add(DiffItem.builder()
                    .itemId(UUID.randomUUID().toString())
                    .type(DiffType.ADD)
                    .original(null)
                    .revised(pair.blockB().getNormalizedText())
                    .confidence(pair.alignmentScore())
                    .blockType(pair.blockB().getBlockType())
                    .tableDiff(pair.blockB().getBlockType() == BlockType.TABLE
                        ? buildTableDiff(null, pair.blockB().getNormalizedText())
                        : null)
                    .build());
                case DELETE -> result.add(DiffItem.builder()
                    .itemId(UUID.randomUUID().toString())
                    .type(DiffType.DELETE)
                    .original(pair.blockA().getNormalizedText())
                    .revised(null)
                    .confidence(pair.alignmentScore())
                    .blockType(pair.blockA().getBlockType())
                    .tableDiff(pair.blockA().getBlockType() == BlockType.TABLE
                        ? buildTableDiff(pair.blockA().getNormalizedText(), null)
                        : null)
                    .build());
                case MODIFY -> {
                    String orig = pair.blockA().getNormalizedText();
                    String rev = pair.blockB().getNormalizedText();
                    var item = DiffItem.builder()
                        .itemId(UUID.randomUUID().toString())
                        .type(DiffType.MODIFY)
                        .original(orig)
                        .revised(rev)
                        .confidence(pair.alignmentScore())
                        .blockType(pair.blockA().getBlockType())
                        .charDiff(computeCharDiff(orig != null ? orig : "", rev != null ? rev : ""))
                        .tableDiff(pair.blockA().getBlockType() == BlockType.TABLE ? buildTableDiff(orig, rev) : null)
                        .build();
                    result.add(item);
                }
            }
        }
        
        List<DiffItem> classified = result.stream()
            .map(item -> DiffItem.builder()
                .itemId(item.getItemId())
                .type(item.getType())
                .original(item.getOriginal())
                .revised(item.getRevised())
                .confidence(item.getConfidence())
                .blockType(item.getBlockType())
                .charDiff(item.getCharDiff())
                .tableDiff(item.getTableDiff())
                .severity(classifier.classify(item))
                .build())
            .toList();

        if (!options.getReportLevels().isEmpty()) {
            return classified.stream()
                .filter(i -> options.getReportLevels().contains(i.getSeverity()))
                .toList();
        }
        return classified;
    }

    private List<CharDiffSegment> computeCharDiff(String a, String b) {
        var charsA = Arrays.asList(a.split(""));
        var charsB = Arrays.asList(b.split(""));
        var patch = DiffUtils.diff(charsA, charsB);
        List<CharDiffSegment> segments = new ArrayList<>();
        int ai = 0;
        for (var delta : patch.getDeltas()) {
            
            while (ai < delta.getSource().getPosition()) {
                segments.add(new CharDiffSegment(charsA.get(ai), CharDiffSegment.SegmentType.EQUAL));
                ai++;
            }
            for (var ch : delta.getSource().getLines())
                segments.add(new CharDiffSegment(ch, CharDiffSegment.SegmentType.DELETE));
            for (var ch : delta.getTarget().getLines())
                segments.add(new CharDiffSegment(ch, CharDiffSegment.SegmentType.INSERT));
            ai += delta.getSource().size();
        }
        while (ai < charsA.size()) {
            segments.add(new CharDiffSegment(charsA.get(ai), CharDiffSegment.SegmentType.EQUAL));
            ai++;
        }
        return segments;
    }

    private TableDiffDetail buildTableDiff(String original, String revised) {
        List<List<String>> rowsA = parseTable(original);
        List<List<String>> rowsB = parseTable(revised);
        if (rowsA.isEmpty() && rowsB.isEmpty()) return null;

        var detail = new TableDiffDetail();
        detail.setOriginalRowCount(rowsA.size());
        detail.setRevisedRowCount(rowsB.size());
        detail.setOriginalColumnCount(maxColumns(rowsA));
        detail.setRevisedColumnCount(maxColumns(rowsB));

        List<String> headerA = rowsA.isEmpty() ? List.of() : rowsA.get(0);
        List<String> headerB = rowsB.isEmpty() ? List.of() : rowsB.get(0);
        ColumnAlignment columnAlignment = alignColumns(headerA, headerB);
        detail.setColumnDiffs(columnAlignment.columnDiffs());

        List<List<String>> alignedRowsA = reorderRows(rowsA, columnAlignment.originalOrder(), columnAlignment.targetColumnCount());
        List<List<String>> alignedRowsB = reorderRows(rowsB, columnAlignment.revisedOrder(), columnAlignment.targetColumnCount());

        List<TableRowDiff> rowDiffs = new ArrayList<>();
        List<TableCellDiff> cellDiffs = new ArrayList<>();
        for (RowAlignment alignment : alignRows(alignedRowsA, alignedRowsB)) {
            int rowIndex = alignment.revisedIndex() >= 0 ? alignment.revisedIndex() : alignment.originalIndex();
            List<String> rowA = alignment.originalIndex() >= 0 ? alignedRowsA.get(alignment.originalIndex()) : null;
            List<String> rowB = alignment.revisedIndex() >= 0 ? alignedRowsB.get(alignment.revisedIndex()) : null;

            if (alignment.type() == DiffType.ADD || alignment.type() == DiffType.DELETE) {
                var rowDiff = new TableRowDiff();
                rowDiff.setRowIndex(rowIndex);
                rowDiff.setType(alignment.type());
                rowDiff.setOriginal(rowA == null ? null : String.join(" | ", rowA));
                rowDiff.setRevised(rowB == null ? null : String.join(" | ", rowB));
                rowDiffs.add(rowDiff);
                continue;
            }

            int maxCols = Math.max(rowA.size(), rowB.size());
            boolean rowChanged = false;
            for (int colIndex = 0; colIndex < maxCols; colIndex++) {
                String cellA = colIndex < rowA.size() ? rowA.get(colIndex) : null;
                String cellB = colIndex < rowB.size() ? rowB.get(colIndex) : null;
                if (Objects.equals(cellA, cellB)) continue;
                rowChanged = true;
                var cellDiff = new TableCellDiff();
                cellDiff.setRowIndex(rowIndex);
                cellDiff.setColumnIndex(colIndex);
                cellDiff.setType(cellA == null ? DiffType.ADD : cellB == null ? DiffType.DELETE : DiffType.MODIFY);
                cellDiff.setOriginal(cellA);
                cellDiff.setRevised(cellB);
                cellDiffs.add(cellDiff);
            }
            if (rowChanged) {
                var rowDiff = new TableRowDiff();
                rowDiff.setRowIndex(rowIndex);
                rowDiff.setType(DiffType.MODIFY);
                rowDiff.setOriginal(String.join(" | ", rowA));
                rowDiff.setRevised(String.join(" | ", rowB));
                rowDiffs.add(rowDiff);
            }
        }
        detail.setRowDiffs(rowDiffs);
        detail.setCellDiffs(cellDiffs);
        return detail;
    }

    private List<RowAlignment> alignRows(List<List<String>> rowsA, List<List<String>> rowsB) {
        int n = rowsA.size();
        int m = rowsB.size();
        double[][] dp = new double[n + 1][m + 1];
        Move[][] move = new Move[n + 1][m + 1];

        dp[0][0] = 0;
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
        Collections.reverse(reversed);
        return reversed;
    }

    private double rowModifyCost(List<String> rowA, List<String> rowB) {
        double similarity = rowSimilarity(rowA, rowB);
        if (similarity < ROW_MATCH_THRESHOLD) {
            return rowDeleteCost(rowA) + rowInsertCost(rowB) + 0.05;
        }
        return 1.0 - similarity;
    }

    private double rowDeleteCost(List<String> row) {
        return Math.max(0.45, nonEmptyRatio(row));
    }

    private double rowInsertCost(List<String> row) {
        return Math.max(0.45, nonEmptyRatio(row));
    }

    private double rowSimilarity(List<String> rowA, List<String> rowB) {
        int maxCols = Math.max(rowA.size(), rowB.size());
        if (maxCols == 0) {
            return 1.0;
        }
        double score = 0;
        for (int col = 0; col < maxCols; col++) {
            String a = col < rowA.size() ? rowA.get(col) : null;
            String b = col < rowB.size() ? rowB.get(col) : null;
            score += cellSimilarity(a, b);
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

    private double nonEmptyRatio(List<String> row) {
        if (row.isEmpty()) {
            return 0.45;
        }
        long nonEmpty = row.stream().filter(cell -> cell != null && !cell.isBlank()).count();
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

    private List<List<String>> reorderRows(List<List<String>> rows, List<Integer> order, int targetColumnCount) {
        if (rows.isEmpty()) return rows;
        List<List<String>> reordered = new ArrayList<>();
        for (List<String> row : rows) {
            List<String> newRow = new ArrayList<>();
            for (int i = 0; i < targetColumnCount; i++) {
                int sourceIndex = i < order.size() ? order.get(i) : -1;
                newRow.add(sourceIndex >= 0 && sourceIndex < row.size() ? row.get(sourceIndex) : null);
            }
            reordered.add(newRow);
        }
        return reordered;
    }

    private ColumnAlignment alignColumns(List<String> headerA, List<String> headerB) {
        if (headerA.isEmpty() && headerB.isEmpty()) {
            return new ColumnAlignment(List.of(), List.of(), List.of(), 0);
        }

        Map<String, Integer> posA = new LinkedHashMap<>();
        for (int i = 0; i < headerA.size(); i++) posA.putIfAbsent(normalizeHeader(headerA.get(i)), i);
        Map<String, Integer> posB = new LinkedHashMap<>();
        for (int i = 0; i < headerB.size(); i++) posB.putIfAbsent(normalizeHeader(headerB.get(i)), i);

        List<Integer> orderA = new ArrayList<>();
        List<Integer> orderB = new ArrayList<>();
        List<TableColumnDiff> diffs = new ArrayList<>();
        Set<String> used = new LinkedHashSet<>();

        for (int i = 0; i < headerA.size(); i++) {
            String header = normalizeHeader(headerA.get(i));
            if (posB.containsKey(header)) {
                int bIndex = posB.get(header);
                orderA.add(i);
                orderB.add(bIndex);
                used.add(header);
                if (bIndex != i) {
                    var diff = new TableColumnDiff();
                    diff.setType(DiffType.MODIFY);
                    diff.setOriginalColumnIndex(i);
                    diff.setRevisedColumnIndex(bIndex);
                    diff.setOriginalHeader(headerA.get(i));
                    diff.setRevisedHeader(headerB.get(bIndex));
                    diffs.add(diff);
                }
            } else {
                orderA.add(i);
                orderB.add(-1);
                var diff = new TableColumnDiff();
                diff.setType(DiffType.DELETE);
                diff.setOriginalColumnIndex(i);
                diff.setOriginalHeader(headerA.get(i));
                diffs.add(diff);
            }
        }

        for (int i = 0; i < headerB.size(); i++) {
            String header = normalizeHeader(headerB.get(i));
            if (used.contains(header) || posA.containsKey(header)) continue;
            orderA.add(-1);
            orderB.add(i);
            var diff = new TableColumnDiff();
            diff.setType(DiffType.ADD);
            diff.setRevisedColumnIndex(i);
            diff.setRevisedHeader(headerB.get(i));
            diffs.add(diff);
        }
        return new ColumnAlignment(orderA, orderB, diffs, Math.max(orderA.size(), orderB.size()));
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.strip().toLowerCase(Locale.ROOT);
    }

    private record ColumnAlignment(
        List<Integer> originalOrder,
        List<Integer> revisedOrder,
        List<TableColumnDiff> columnDiffs,
        int targetColumnCount
    ) {}

    private record RowAlignment(int originalIndex, int revisedIndex, DiffType type) {}

    private enum Move { MATCH, DELETE, ADD }

    private List<List<String>> parseTable(String tableText) {
        if (tableText == null || tableText.isBlank()) return List.of();
        List<List<String>> rows = new ArrayList<>();
        for (String row : tableText.split("\\R")) {
            if (row.isBlank()) continue;
            List<String> cells = Arrays.stream(row.split("\\t", -1))
                .map(String::strip)
                .toList();
            rows.add(cells);
        }
        return rows;
    }

    private int maxColumns(List<List<String>> rows) {
        return rows.stream().mapToInt(List::size).max().orElse(0);
    }
}
