package org.sainm.document;

import org.sainm.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DocumentStructurer {
    private final CrossPageTableMerger tableMerger = new CrossPageTableMerger();

    public LogicalDocument structure(List<PageContent> pages, CompareOptions options) {
        var doc = new LogicalDocument();
        var pageMap = new PageMap();

        doc.setTables(mergeTables(pages, pageMap));
        doc.setParagraphs(mergeParagraphs(pages, pageMap));
        doc.setPageMap(pageMap);
        doc.setPages(pages);
        doc.setTotalPages(pages.size());
        return doc;
    }

    private List<LogicalTable> mergeTables(List<PageContent> pages, PageMap pageMap) {
        
        List<LogicalTable> result = new ArrayList<>();
        List<CrossPageTableMerger.PageTableEntry> group = new ArrayList<>();
        String lastHeader = null;

        for (var page : pages) {
            for (var tb : page.getTableBlocks()) {
                String header = tb.getHeaderRow() != null ? String.join(",", tb.getHeaderRow()) : "";
                if (!group.isEmpty() && !header.equals(lastHeader)) {
                    result.add(flushGroup(group, pageMap));
                    group = new ArrayList<>();
                }
                group.add(new CrossPageTableMerger.PageTableEntry(page.getPageNumber(), tb));
                lastHeader = header;
            }
        }
        if (!group.isEmpty()) result.add(flushGroup(group, pageMap));
        return result;
    }

    private LogicalTable flushGroup(List<CrossPageTableMerger.PageTableEntry> group, PageMap pageMap) {
        var logical = tableMerger.merge(group);
        for (var loc : logical.getPageLocations()) {
            pageMap.put(LogicalLocation.ofTableCell(logical.getTableId(), 0, 0), loc);
        }
        return logical;
    }

    private List<LogicalParagraph> mergeParagraphs(List<PageContent> pages, PageMap pageMap) {
        List<LogicalParagraph> result = new ArrayList<>();
        LogicalParagraph current = null;
        for (var page : pages) {
            if (isImageLikeTablePage(page)) {
                continue;
            }
            for (var block : page.getTextBlocks()) {
                if (current != null && isTruncated(current.getText())) {
                    current.appendText(block.getText());
                    current.addLocation(new PhysicalLocation(page.getPageNumber(), block.getBbox()));
                } else {
                    current = new LogicalParagraph(UUID.randomUUID().toString(), block);
                    pageMap.put(LogicalLocation.ofParagraph(current.getParagraphId()),
                        new PhysicalLocation(page.getPageNumber(), block.getBbox()));
                    result.add(current);
                }
            }
        }
        return result;
    }

    private boolean isImageLikeTablePage(PageContent page) {
        return !page.getTableBlocks().isEmpty()
                && (page.getType() == PageType.IMAGE
                || page.getType() == PageType.MIXED
                || page.getType() == PageType.OCR_FAILED);
    }

    private boolean isTruncated(String text) {
        if (text == null || text.isBlank()) return false;
        char last = text.charAt(text.length() - 1);
        return last != '。' && last != '.' && last != '？' && last != '?' && last != '！' && last != '!';
    }
}
