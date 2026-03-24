package org.sainm.preprocessor;

import org.sainm.model.*;
import org.sainm.spi.Preprocessor;

import java.util.Set;

final class RegionFilter implements Preprocessor {
    @Override public int order() { return 100; }
    @Override public boolean supports(PageContent page) { return true; }

    @Override
    public PageContent process(PageContent page, CompareOptions options) {
        if (options.getExcludeRegions().isEmpty()) return page;
        var filtered = new PageContent(page);
        filtered.setTextBlocks(page.getTextBlocks().stream()
            .filter(b -> !isExcluded(b.getBbox(), page, options.getExcludeRegions()))
            .toList());
        filtered.setTableBlocks(page.getTableBlocks().stream()
            .filter(b -> !isExcluded(b.getBbox(), page, options.getExcludeRegions()))
            .toList());
        return filtered;
    }

    private boolean isExcluded(BoundingBox bbox, PageContent page, Set<Region> regions) {
        if (bbox == null) return false;
        return regions.stream().anyMatch(r ->
            (r.page() == -1 || r.page() == page.getPageNumber()) &&
            overlaps(bbox, r, page.getWidth(), page.getHeight()));
    }

    private boolean overlaps(BoundingBox bbox, Region r, double pw, double ph) {
        double rx = r.x() * pw, ry = r.y() * ph;
        double rw = r.width() * pw, rh = r.height() * ph;
        return bbox.x() < rx + rw && bbox.x() + bbox.width() > rx &&
               bbox.y() < ry + rh && bbox.y() + bbox.height() > ry;
    }
}
