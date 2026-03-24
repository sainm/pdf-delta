package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.spi.Normalizer;

import java.util.Map;

public final class GlyphNormalizer implements Normalizer {
    private static final Map<Character, Character> MAP = Map.of(
        '己', '已',
        '０', '0', '１', '1', '２', '2', '３', '3', '４', '4',
        'Ｏ', 'O', 'ｌ', 'l'
    );

    @Override public int order() { return 20; }

    @Override
    public String normalize(String text, CompareOptions options) {
        var sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) sb.append(MAP.getOrDefault(c, c));
        return sb.toString();
    }
}
