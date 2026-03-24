package org.sainm.model;

public final class ImageBlock {
    private BoundingBox bbox;
    private String altText;
    private byte[] imageData;

    public BoundingBox getBbox() { return bbox; }
    public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
}
