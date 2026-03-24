package org.sainm.model;

public final class TableColumnDiff {
    private int originalColumnIndex = -1;
    private int revisedColumnIndex = -1;
    private DiffType type;
    private String originalHeader;
    private String revisedHeader;

    public int getOriginalColumnIndex() { return originalColumnIndex; }
    public void setOriginalColumnIndex(int originalColumnIndex) { this.originalColumnIndex = originalColumnIndex; }
    public int getRevisedColumnIndex() { return revisedColumnIndex; }
    public void setRevisedColumnIndex(int revisedColumnIndex) { this.revisedColumnIndex = revisedColumnIndex; }
    public DiffType getType() { return type; }
    public void setType(DiffType type) { this.type = type; }
    public String getOriginalHeader() { return originalHeader; }
    public void setOriginalHeader(String originalHeader) { this.originalHeader = originalHeader; }
    public String getRevisedHeader() { return revisedHeader; }
    public void setRevisedHeader(String revisedHeader) { this.revisedHeader = revisedHeader; }
}
