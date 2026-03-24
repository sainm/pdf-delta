package org.sainm.preprocessor;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.spi.Preprocessor;

final class RotationCorrector implements Preprocessor {
    @Override public int order() { return 5; }
    @Override public boolean supports(PageContent page) { return true; }

    @Override
    public PageContent process(PageContent page, CompareOptions options) {
        
        return page;
    }
}
