package org.sainm.preprocessor;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegionFilterTest {
    @Test void removesBlocksInExcludedRegion() {
        var region = new Region(-1, 0.0, 0.0, 1.0, 0.1, Region.RegionType.HEADER);
        var options = CompareOptions.defaults().withExcludeRegions(Set.of(region));
        var filter = new RegionFilter();
        var page = pageWithBlock(block("HEADER TEXT", 50, 5, 200, 8));
        page.setWidth(595);
        page.setHeight(100);
        var result = filter.process(page, options);
        assertThat(result.getTextBlocks()).isEmpty();
    }

    @Test void keepsBlocksOutsideExcludedRegion() {
        var region = new Region(-1, 0.0, 0.0, 1.0, 0.1, Region.RegionType.HEADER);
        var options = CompareOptions.defaults().withExcludeRegions(Set.of(region));
        var filter = new RegionFilter();
        var page = pageWithBlock(block("BODY TEXT", 50, 50, 200, 12));
        page.setWidth(595);
        page.setHeight(100);
        var result = filter.process(page, options);
        assertThat(result.getTextBlocks()).hasSize(1);
    }

    private PageContent pageWithBlock(TextBlock b) {
        var page = new PageContent();
        page.setPageNumber(1);
        page.setType(PageType.TEXT);
        page.setTextBlocks(new java.util.ArrayList<>(List.of(b)));
        return page;
    }

    private TextBlock block(String text, double x, double y, double w, double h) {
        var b = new TextBlock();
        b.setText(text);
        b.setBbox(new BoundingBox(x, y, w, h, 1));
        return b;
    }
}
