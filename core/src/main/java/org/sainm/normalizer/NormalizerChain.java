package org.sainm.normalizer;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.spi.Normalizer;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public final class NormalizerChain {
    private final List<Normalizer> ordered;

    public NormalizerChain(List<Normalizer> normalizers) {
        this.ordered = normalizers.stream()
            .sorted(Comparator.comparingInt(Normalizer::order))
            .toList();
    }

    public static NormalizerChain defaults() {
        return new NormalizerChain(List.of(
            new WhitespaceNormalizer(),
            new GlyphNormalizer(),
            new NumberNormalizer(),
            new DateNormalizer(),
            new UnitNormalizer()
        ));
    }

    public static NormalizerChain fromSpi() {
        var list = ServiceLoader.load(Normalizer.class)
            .stream().map(ServiceLoader.Provider::get).toList();
        return list.isEmpty() ? defaults() : new NormalizerChain(list);
    }

    public String normalize(String text, CompareOptions options) {
        String result = text;
        for (Normalizer n : ordered) result = n.normalize(result, options);
        return result;
    }

    public void applyToPage(PageContent page, CompareOptions options) {
        page.getTextBlocks().forEach(b ->
            b.setNormalizedText(normalize(b.getText(), options)));
        page.getTableBlocks().forEach(table ->
            table.getCells().forEach(row ->
                row.forEach(cell ->
                    cell.setNormalizedText(normalize(cell.getText(), options)))));
    }
}
