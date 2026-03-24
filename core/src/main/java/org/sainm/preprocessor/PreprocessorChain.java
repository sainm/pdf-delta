package org.sainm.preprocessor;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.spi.Preprocessor;

import java.util.Comparator;
import java.util.List;

public final class PreprocessorChain {
    private final List<Preprocessor> ordered;

    public PreprocessorChain(List<Preprocessor> preprocessors) {
        this.ordered = preprocessors.stream()
            .sorted(Comparator.comparingInt(Preprocessor::order))
            .toList();
    }

    public PageContent process(PageContent page, CompareOptions options) {
        PageContent current = page;
        for (Preprocessor p : ordered) {
            if (p.supports(current)) {
                current = p.process(current, options);
            }
        }
        return current;
    }
}
