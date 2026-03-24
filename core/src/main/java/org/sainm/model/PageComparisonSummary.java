package org.sainm.model;

public final class PageComparisonSummary {
    private int pageNumber;
    private PageType pageType;
    private boolean ocrAttempted;
    private boolean ocrSucceeded;
    private boolean hasTextDiffs;
    private boolean hasVisualDiffs;
    private String status;

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public PageType getPageType() { return pageType; }
    public void setPageType(PageType pageType) { this.pageType = pageType; }
    public boolean isOcrAttempted() { return ocrAttempted; }
    public void setOcrAttempted(boolean ocrAttempted) { this.ocrAttempted = ocrAttempted; }
    public boolean isOcrSucceeded() { return ocrSucceeded; }
    public void setOcrSucceeded(boolean ocrSucceeded) { this.ocrSucceeded = ocrSucceeded; }
    public boolean isHasTextDiffs() { return hasTextDiffs; }
    public void setHasTextDiffs(boolean hasTextDiffs) { this.hasTextDiffs = hasTextDiffs; }
    public boolean isHasVisualDiffs() { return hasVisualDiffs; }
    public void setHasVisualDiffs(boolean hasVisualDiffs) { this.hasVisualDiffs = hasVisualDiffs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
