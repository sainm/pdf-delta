package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class CompareResult {
    private String jobId;
    private List<DiffItem> items = new ArrayList<>();
    private DiffSummary summary;
    private List<Integer> ocrFailedPages = new ArrayList<>();
    private ImageComparisonSummary imageComparisonSummary;
    private List<VisualDiffItem> visualDiffItems = new ArrayList<>();
    private List<PageComparisonSummary> pageSummaries = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private CompareOptions optionsUsed;
    private byte[] sourceBytesA;
    private byte[] sourceBytesB;

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public List<DiffItem> getItems() { return items; }
    public void setItems(List<DiffItem> items) { this.items = items; }
    public DiffSummary getSummary() { return summary; }
    public void setSummary(DiffSummary summary) { this.summary = summary; }
    public List<Integer> getOcrFailedPages() { return ocrFailedPages; }
    public void setOcrFailedPages(List<Integer> ocrFailedPages) { this.ocrFailedPages = ocrFailedPages; }
    public ImageComparisonSummary getImageComparisonSummary() { return imageComparisonSummary; }
    public void setImageComparisonSummary(ImageComparisonSummary imageComparisonSummary) { this.imageComparisonSummary = imageComparisonSummary; }
    public List<VisualDiffItem> getVisualDiffItems() { return visualDiffItems; }
    public void setVisualDiffItems(List<VisualDiffItem> visualDiffItems) { this.visualDiffItems = visualDiffItems; }
    public List<PageComparisonSummary> getPageSummaries() { return pageSummaries; }
    public void setPageSummaries(List<PageComparisonSummary> pageSummaries) { this.pageSummaries = pageSummaries; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public CompareOptions getOptionsUsed() { return optionsUsed; }
    public void setOptionsUsed(CompareOptions optionsUsed) { this.optionsUsed = optionsUsed; }
    public byte[] getSourceBytesA() { return sourceBytesA; }
    public void setSourceBytesA(byte[] sourceBytesA) { this.sourceBytesA = sourceBytesA; }
    public byte[] getSourceBytesB() { return sourceBytesB; }
    public void setSourceBytesB(byte[] sourceBytesB) { this.sourceBytesB = sourceBytesB; }
}
