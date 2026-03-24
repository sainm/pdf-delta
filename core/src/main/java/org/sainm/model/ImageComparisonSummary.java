package org.sainm.model;

public final class ImageComparisonSummary {
    private int imageLikePages;
    private int visualDiffPages;
    private int visualDiffItems;
    private int partialSuccessPages;

    public int getImageLikePages() { return imageLikePages; }
    public void setImageLikePages(int imageLikePages) { this.imageLikePages = imageLikePages; }
    public int getVisualDiffPages() { return visualDiffPages; }
    public void setVisualDiffPages(int visualDiffPages) { this.visualDiffPages = visualDiffPages; }
    public int getVisualDiffItems() { return visualDiffItems; }
    public void setVisualDiffItems(int visualDiffItems) { this.visualDiffItems = visualDiffItems; }
    public int getPartialSuccessPages() { return partialSuccessPages; }
    public void setPartialSuccessPages(int partialSuccessPages) { this.partialSuccessPages = partialSuccessPages; }
}
