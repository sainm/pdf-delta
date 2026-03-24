package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class TableBlock {
    private List<List<TableCell>> cells = new ArrayList<>(); 
    private List<String> headerRow = new ArrayList<>();
    private BoundingBox bbox;
    private boolean hasExplicitBorder;
    private boolean isNested;

    public List<List<TableCell>> getCells() { return cells; }
    public void setCells(List<List<TableCell>> cells) { this.cells = cells; }
    public List<String> getHeaderRow() { return headerRow; }
    public void setHeaderRow(List<String> headerRow) { this.headerRow = headerRow; }
    public BoundingBox getBbox() { return bbox; }
    public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
    public boolean isHasExplicitBorder() { return hasExplicitBorder; }
    public void setHasExplicitBorder(boolean hasExplicitBorder) { this.hasExplicitBorder = hasExplicitBorder; }
    public boolean isNested() { return isNested; }
    public void setNested(boolean nested) { isNested = nested; }
}
