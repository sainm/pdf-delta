package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.spi.Normalizer;

public final class WhitespaceNormalizer implements Normalizer {
    @Override public int order() { return 10; }

    @Override
    public String normalize(String text, CompareOptions options) {
        if (!options.isIgnoreWhitespace()) return text;
        return text.replace('\u3000', ' ').replaceAll("\\s+", " ").strip();
    }
}
