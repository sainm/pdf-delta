package org.sainm.model;

import java.util.ArrayList;
import java.util.List;

public final class LogicalDocument {
    private List<PageContent> pages = new ArrayList<>();
    private int totalPages;
    private List<LogicalParagraph> paragraphs = new ArrayList<>();
    private List<LogicalTable> tables = new ArrayList<>();
    private PageMap pageMap;

    public List<PageContent> getPages() { return pages; }
    public void setPages(List<PageContent> pages) { this.pages = pages; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public List<LogicalParagraph> getParagraphs() { return paragraphs; }
    public void setParagraphs(List<LogicalParagraph> paragraphs) { this.paragraphs = paragraphs; }
    public List<LogicalTable> getTables() { return tables; }
    public void setTables(List<LogicalTable> tables) { this.tables = tables; }
    public PageMap getPageMap() { return pageMap; }
    public void setPageMap(PageMap pageMap) { this.pageMap = pageMap; }
}
