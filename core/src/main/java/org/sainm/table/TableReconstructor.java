package org.sainm.table;

import org.sainm.model.*;

import java.util.ArrayList;
import java.util.List;

public final class TableReconstructor {
    private final MergedCellExpander expander = new MergedCellExpander();

    public List<TableBlock> reconstruct(PageContent page, CompareOptions options) {
        List<TableBlock> result = new ArrayList<>();
        if (!page.getTableBlocks().isEmpty()) {
            for (var tb : page.getTableBlocks()) {
                result.add(expandMergedCells(tb));
            }
        } else {
            result.addAll(detectBorderlessTable(page, options));
        }
        return result;
    }

    private TableBlock expandMergedCells(TableBlock tb) {
        int rows = tb.getCells().size();
        int cols = rows > 0 ? tb.getCells().get(0).size() : 0;
        var expanded = expander.expand(tb.getCells(), rows, cols);
        var result = new TableBlock();
        result.setCells(expanded);
        result.setHeaderRow(tb.getHeaderRow());
        result.setBbox(tb.getBbox());
        return result;
    }

    private List<TableBlock> detectBorderlessTable(PageContent page, CompareOptions options) {
        var blocks = page.getTextBlocks();
        if (blocks.size() < 2) return List.of();

        double tol = options.getPositionTolerance();
        var clustered = isImageLikePage(page)
                ? CellClusterer.clusterForOcrTable(blocks, tol)
                : CellClusterer.cluster(blocks, tol);
        if (clustered.rowCount() < 1 || clustered.colCount() < 2) return List.of();

        
        var tb = new TableBlock();
        List<List<TableCell>> cells = new ArrayList<>();
        List<String> headerRow = new ArrayList<>();

        for (int r = 0; r < clustered.rowCount(); r++) {
            List<TableCell> row = new ArrayList<>();
            for (int c = 0; c < clustered.colCount(); c++) {
                var textBlock = clustered.cellAt(r, c);
                var cell = new TableCell();
                cell.setRow(r); cell.setCol(c);
                if (textBlock != null) {
                    cell.setText(textBlock.getText());
                    cell.setBbox(textBlock.getBbox());
                } else {
                    cell.setText("");
                }
                row.add(cell);
                if (r == 0) headerRow.add(cell.getText());
            }
            cells.add(row);
        }
        tb.setCells(cells);
        tb.setHeaderRow(headerRow);
        tb.setBbox(computeBoundingBox(cells, page.getPageNumber()));
        return List.of(tb);
    }

    private boolean isImageLikePage(PageContent page) {
        return page.getType() == PageType.IMAGE
                || page.getType() == PageType.MIXED
                || page.getType() == PageType.OCR_FAILED;
    }

    private BoundingBox computeBoundingBox(List<List<TableCell>> cells, int pageNumber) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        boolean found = false;

        for (List<TableCell> row : cells) {
            for (TableCell cell : row) {
                if (cell.getBbox() == null) continue;
                found = true;
                minX = Math.min(minX, cell.getBbox().x());
                minY = Math.min(minY, cell.getBbox().y());
                maxX = Math.max(maxX, cell.getBbox().x() + cell.getBbox().width());
                maxY = Math.max(maxY, cell.getBbox().y() + cell.getBbox().height());
            }
        }

        if (!found) {
            return null;
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY, pageNumber);
    }
}
