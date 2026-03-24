package org.sainm.table;

import org.sainm.model.TextBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class CellClusterer {

    public record ClusterResult(List<List<TextBlock>> grid) {
        public int rowCount() { return grid.size(); }
        public int colCount() { return grid.isEmpty() ? 0 : grid.get(0).size(); }
        public TextBlock cellAt(int row, int col) { return grid.get(row).get(col); }
    }

    public static ClusterResult cluster(List<TextBlock> blocks, double tolerance) {
        List<List<TextBlock>> rows = clusterByY(blocks, tolerance);
        List<Double> colCenters = extractColCenters(rows, tolerance);
        return buildGrid(rows, colCenters, tolerance);
    }

    public static ClusterResult clusterForOcrTable(List<TextBlock> blocks, double tolerance) {
        List<List<TextBlock>> rows = clusterByY(blocks, tolerance);
        if (rows.isEmpty()) {
            return new ClusterResult(List.of());
        }

        int maxRowSize = rows.stream().mapToInt(List::size).max().orElse(0);
        if (maxRowSize < 2) {
            return new ClusterResult(List.of());
        }

        int minTableRowSize = Math.max(2, maxRowSize / 2);
        List<List<TextBlock>> tableRows = rows.stream()
                .filter(row -> row.size() >= minTableRowSize)
                .toList();
        if (tableRows.isEmpty()) {
            return new ClusterResult(List.of());
        }

        List<Double> colCenters = tableRows.stream()
                .max(Comparator.comparingInt(List::size))
                .stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparingDouble(b -> b.getBbox().x()))
                .map(CellClusterer::centerX)
                .toList();
        return buildGrid(tableRows, colCenters, tolerance);
    }

    private static List<List<TextBlock>> clusterByY(List<TextBlock> blocks, double tol) {
        var sorted = blocks.stream()
            .sorted(Comparator.comparingDouble(b -> b.getBbox().y()))
            .toList();
        List<List<TextBlock>> rows = new ArrayList<>();
        List<TextBlock> current = new ArrayList<>();
        double lastY = Double.MIN_VALUE;
        for (TextBlock b : sorted) {
            double cy = b.getBbox().y() + b.getBbox().height() / 2;
            if (!current.isEmpty() && Math.abs(cy - lastY) > tol) {
                rows.add(current);
                current = new ArrayList<>();
            }
            current.add(b);
            lastY = cy;
        }
        if (!current.isEmpty()) rows.add(current);
        return rows;
    }

    private static List<Double> extractColCenters(List<List<TextBlock>> rows, double tol) {
        List<Double> centers = new ArrayList<>();
        for (var row : rows) {
            for (var b : row) {
                double cx = b.getBbox().x() + b.getBbox().width() / 2;
                boolean found = false;
                for (int i = 0; i < centers.size(); i++) {
                    if (Math.abs(centers.get(i) - cx) <= tol) {
                        centers.set(i, (centers.get(i) + cx) / 2);
                        found = true;
                        break;
                    }
                }
                if (!found) centers.add(cx);
            }
        }
        centers.sort(Double::compareTo);
        return centers;
    }

    private static ClusterResult buildGrid(List<List<TextBlock>> rows, List<Double> colCenters, double tol) {
        List<List<TextBlock>> grid = new ArrayList<>();
        for (var row : rows) {
            TextBlock[] rowArr = new TextBlock[colCenters.size()];
            for (var b : row) {
                double cx = centerX(b);
                int col = 0;
                double minDist = Double.MAX_VALUE;
                for (int i = 0; i < colCenters.size(); i++) {
                    double dist = Math.abs(colCenters.get(i) - cx);
                    if (dist < minDist) { minDist = dist; col = i; }
                }
                rowArr[col] = b;
            }
            grid.add(Arrays.asList(rowArr));
        }
        return new ClusterResult(grid);
    }

    private static double centerX(TextBlock block) {
        return block.getBbox().x() + block.getBbox().width() / 2;
    }
}
