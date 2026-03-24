package org.sainm.model;

public final class TableRowDiff {
    private int rowIndex;
    private DiffType type;
    private String original;
    private String revised;

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }
    public DiffType getType() { return type; }
    public void setType(DiffType type) { this.type = type; }
    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }
    public String getRevised() { return revised; }
    public void setRevised(String revised) { this.revised = revised; }
}
