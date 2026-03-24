package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.spi.Normalizer;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class NumberNormalizer implements Normalizer {
    private static final Pattern NUMBER = Pattern.compile("\\d{1,3}(,\\d{3})+(\\.\\d+)?|\\d+\\.\\d+");

    @Override public int order() { return 30; }

    @Override
    public String normalize(String text, CompareOptions options) {
        if (!options.isIgnoreNumberFormat()) return text;
        return NUMBER.matcher(text).replaceAll(m -> {
            String s = m.group().replace(",", "");
            if (s.contains(".")) s = new BigDecimal(s).stripTrailingZeros().toPlainString();
            return s;
        });
    }
}
