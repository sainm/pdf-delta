package org.sainm.model;

public final class TextBlock {
    public enum Source { EXTRACTED, OCR }

    private String text;
    private String normalizedText;
    private BoundingBox bbox;
    private double confidence = 1.0;
    private Source source = Source.EXTRACTED;

    public TextBlock() {}

    public TextBlock(String text, BoundingBox bbox) {
        this.text = text;
        this.bbox = bbox;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getNormalizedText() { return normalizedText; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
    public BoundingBox getBbox() { return bbox; }
    public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
}
