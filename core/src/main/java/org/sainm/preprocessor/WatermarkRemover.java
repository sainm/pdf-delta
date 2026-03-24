package org.sainm.preprocessor;

import org.sainm.model.*;
import org.sainm.spi.Preprocessor;

final class WatermarkRemover implements Preprocessor {
    private static final double WATERMARK_CONFIDENCE_THRESHOLD = 0.5;
    private static final double WATERMARK_AREA_RATIO = 0.3;

    @Override public int order() { return 10; }
    @Override public boolean supports(PageContent page) { return true; }

    @Override
    public PageContent process(PageContent page, CompareOptions options) {
        if (!options.isIgnoreWatermark()) return page;
        var filtered = new PageContent(page);
        filtered.setTextBlocks(page.getTextBlocks().stream()
            .filter(b -> !isWatermark(b, page))
            .toList());
        return filtered;
    }

    private boolean isWatermark(TextBlock block, PageContent page) {
        if (block.getBbox() == null) return false;
        double pageArea = page.getWidth() * page.getHeight();
        if (pageArea == 0) return false;
        double blockArea = block.getBbox().width() * block.getBbox().height();
        return block.getConfidence() < WATERMARK_CONFIDENCE_THRESHOLD &&
               blockArea / pageArea > WATERMARK_AREA_RATIO;
    }
}
