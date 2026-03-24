package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class PageContent {
    private int pageNumber;
    private PageType type;
    private List<TextBlock> textBlocks = new ArrayList<>();
    private List<TableBlock> tableBlocks = new ArrayList<>();
    private List<ImageBlock> imageBlocks = new ArrayList<>();
    private double width, height;

    public PageContent() {}

    public PageContent(PageContent other) {
        this.pageNumber = other.pageNumber;
        this.type = other.type;
        this.textBlocks = new ArrayList<>(other.textBlocks);
        this.tableBlocks = new ArrayList<>(other.tableBlocks);
        this.imageBlocks = new ArrayList<>(other.imageBlocks);
        this.width = other.width;
        this.height = other.height;
    }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public PageType getType() { return type; }
    public void setType(PageType type) { this.type = type; }
    public List<TextBlock> getTextBlocks() { return textBlocks; }
    public void setTextBlocks(List<TextBlock> textBlocks) { this.textBlocks = textBlocks; }
    public List<TableBlock> getTableBlocks() { return tableBlocks; }
    public void setTableBlocks(List<TableBlock> tableBlocks) { this.tableBlocks = tableBlocks; }
    public List<ImageBlock> getImageBlocks() { return imageBlocks; }
    public void setImageBlocks(List<ImageBlock> imageBlocks) { this.imageBlocks = imageBlocks; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}
