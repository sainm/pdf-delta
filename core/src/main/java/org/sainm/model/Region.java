package org.sainm.model;

public record Region(int page, double x, double y, double width, double height, RegionType type) {
    public enum RegionType { HEADER, FOOTER, WATERMARK, CUSTOM }
}
