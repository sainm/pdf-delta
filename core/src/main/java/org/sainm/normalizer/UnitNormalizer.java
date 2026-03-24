package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.spi.Normalizer;

public final class UnitNormalizer implements Normalizer {
    @Override public int order() { return 50; }

    @Override
    public String normalize(String text, CompareOptions options) {
        return text.replace("￥", "CNY").replace("RMB", "CNY").replace("¥", "CNY");
    }
}
