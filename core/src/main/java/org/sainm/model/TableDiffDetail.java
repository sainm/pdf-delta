package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class TableDiffDetail {
    private java.util.List<TableColumnDiff> columnDiffs = new ArrayList<>();
    private List<TableRowDiff> rowDiffs = new ArrayList<>();
    private List<TableCellDiff> cellDiffs = new ArrayList<>();
    private int originalRowCount;
    private int revisedRowCount;
    private int originalColumnCount;
    private int revisedColumnCount;

    public java.util.List<TableColumnDiff> getColumnDiffs() { return columnDiffs; }
    public void setColumnDiffs(java.util.List<TableColumnDiff> columnDiffs) { this.columnDiffs = columnDiffs; }
    public List<TableRowDiff> getRowDiffs() { return rowDiffs; }
    public void setRowDiffs(List<TableRowDiff> rowDiffs) { this.rowDiffs = rowDiffs; }
    public List<TableCellDiff> getCellDiffs() { return cellDiffs; }
    public void setCellDiffs(List<TableCellDiff> cellDiffs) { this.cellDiffs = cellDiffs; }
    public int getOriginalRowCount() { return originalRowCount; }
    public void setOriginalRowCount(int originalRowCount) { this.originalRowCount = originalRowCount; }
    public int getRevisedRowCount() { return revisedRowCount; }
    public void setRevisedRowCount(int revisedRowCount) { this.revisedRowCount = revisedRowCount; }
    public int getOriginalColumnCount() { return originalColumnCount; }
    public void setOriginalColumnCount(int originalColumnCount) { this.originalColumnCount = originalColumnCount; }
    public int getRevisedColumnCount() { return revisedColumnCount; }
    public void setRevisedColumnCount(int revisedColumnCount) { this.revisedColumnCount = revisedColumnCount; }
}
