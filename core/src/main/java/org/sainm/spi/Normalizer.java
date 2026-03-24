package org.sainm.spi;

import org.sainm.model.CompareOptions;

public interface Normalizer {
    String normalize(String text, CompareOptions options);
    int order();
}
