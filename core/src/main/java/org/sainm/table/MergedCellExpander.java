package org.sainm.table;

import org.sainm.model.TableCell;

import java.util.ArrayList;
import java.util.List;

final class MergedCellExpander {
    public List<List<TableCell>> expand(List<List<TableCell>> cells, int rows, int cols) {
        TableCell[][] grid = new TableCell[rows][cols];
        for (var row : cells) {
            for (var cell : row) {
                for (int r = cell.getRow(); r < cell.getRow() + cell.getRowSpan() && r < rows; r++) {
                    for (int c = cell.getCol(); c < cell.getCol() + cell.getColSpan() && c < cols; c++) {
                        grid[r][c] = cell;
                    }
                }
            }
        }
        List<List<TableCell>> result = new ArrayList<>();
        for (var row : grid) result.add(new ArrayList<>(List.of(row)));
        return result;
    }
}
