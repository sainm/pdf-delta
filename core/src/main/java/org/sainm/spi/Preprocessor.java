package org.sainm.spi;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;

public interface Preprocessor {
    PageContent process(PageContent page, CompareOptions options);
    int order();
    boolean supports(PageContent page);
}
