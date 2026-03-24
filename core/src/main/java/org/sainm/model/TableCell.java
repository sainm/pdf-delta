package org.sainm.model;

public final class TableCell {
    private String text;
    private String normalizedText;
    private int row, col;
    private int rowSpan = 1, colSpan = 1;
    private BoundingBox bbox;
    private double confidence = 1.0;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getNormalizedText() { return normalizedText; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
    public int getRowSpan() { return rowSpan; }
    public void setRowSpan(int rowSpan) { this.rowSpan = rowSpan; }
    public int getColSpan() { return colSpan; }
    public void setColSpan(int colSpan) { this.colSpan = colSpan; }
    public BoundingBox getBbox() { return bbox; }
    public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
