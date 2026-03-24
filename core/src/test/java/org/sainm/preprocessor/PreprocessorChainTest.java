package org.sainm.preprocessor;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.model.PageType;
import org.sainm.spi.Preprocessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PreprocessorChainTest {
    @Test void executesInOrderAscending() {
        List<Integer> callOrder = new ArrayList<>();
        Preprocessor p1 = new StubPreprocessor(10, callOrder);
        Preprocessor p2 = new StubPreprocessor(1, callOrder);
        var chain = new PreprocessorChain(List.of(p1, p2));
        chain.process(emptyPage(), CompareOptions.defaults());
        assertThat(callOrder).containsExactly(1, 10);
    }

    @Test void skipsUnsupportedPreprocessors() {
        var neverSupports = new Preprocessor() {
            public PageContent process(PageContent p, CompareOptions o) {
                throw new AssertionError("should not be called");
            }
            public int order() { return 1; }
            public boolean supports(PageContent p) { return false; }
        };
        var chain = new PreprocessorChain(List.of(neverSupports));
        assertThatCode(() -> chain.process(emptyPage(), CompareOptions.defaults()))
            .doesNotThrowAnyException();
    }

    private PageContent emptyPage() {
        var p = new PageContent();
        p.setPageNumber(1);
        p.setType(PageType.TEXT);
        p.setWidth(595);
        p.setHeight(842);
        return p;
    }

    static class StubPreprocessor implements Preprocessor {
        private final int order;
        private final List<Integer> callOrder;
        StubPreprocessor(int order, List<Integer> callOrder) {
            this.order = order;
            this.callOrder = callOrder;
        }
        public PageContent process(PageContent p, CompareOptions o) { callOrder.add(order); return p; }
        public int order() { return order; }
        public boolean supports(PageContent p) { return true; }
    }
}
