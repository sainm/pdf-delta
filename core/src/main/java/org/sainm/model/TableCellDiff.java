package org.sainm.model;

public final class TableCellDiff {
    private int rowIndex;
    private int columnIndex;
    private DiffType type;
    private String original;
    private String revised;

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public int getColumnIndex() { return columnIndex; }
    public void setColumnIndex(int columnIndex) { this.columnIndex = columnIndex; }
    public DiffType getType() { return type; }
    public void setType(DiffType type) { this.type = type; }
    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }
    public String getRevised() { return revised; }
    public void setRevised(String revised) { this.revised = revised; }
}
