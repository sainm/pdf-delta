package org.sainm.document;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentStructurerTest {
    @Test void producesLogicalDocumentFromPages() {
        var pages = List.of(
            pageWithText(1, "Introduction paragraph text here."),
            pageWithText(2, "Another paragraph.")
        );
        var structurer = new DocumentStructurer();
        var doc = structurer.structure(pages, CompareOptions.defaults());

        assertThat(doc.getParagraphs()).isNotEmpty();
        assertThat(doc.getPageMap()).isNotNull();
    }

    @Test void pageMapResolvesLogicalToPhysical() {
        var pages = List.of(pageWithText(1, "Hello World."));
        var doc = new DocumentStructurer().structure(pages, CompareOptions.defaults());
        var para = doc.getParagraphs().get(0);
        var loc = doc.getPageMap().resolve(LogicalLocation.ofParagraph(para.getParagraphId()));
        assertThat(loc.page()).isEqualTo(1);
    }

    private PageContent pageWithText(int pageNum, String text) {
        var block = new TextBlock();
        block.setText(text);
        block.setBbox(new BoundingBox(50, 100, 400, 15, pageNum));
        var page = new PageContent();
        page.setPageNumber(pageNum);
        page.setType(PageType.TEXT);
        page.setWidth(595);
        page.setHeight(842);
        page.setTextBlocks(new ArrayList<>(List.of(block)));
        return page;
    }
}
