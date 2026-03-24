package org.sainm.model;

public final class VisualDiffItem {
    private int pageNumber;
    private BoundingBox bbox;
    private double diffRatio;
    private String reason;

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public BoundingBox getBbox() { return bbox; }
    public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
    public double getDiffRatio() { return diffRatio; }
    public void setDiffRatio(double diffRatio) { this.diffRatio = diffRatio; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
