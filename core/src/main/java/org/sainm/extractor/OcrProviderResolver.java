package org.sainm.extractor;

import org.sainm.exception.OcrProviderUnavailableException;
import org.sainm.model.OcrOptions;
import org.sainm.spi.OcrProvider;

import java.util.Comparator;
import java.util.ServiceLoader;

final class OcrProviderResolver {
    public static OcrProvider resolve(OcrOptions options) {
        var providers = ServiceLoader.load(OcrProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(OcrProvider::isAvailable)
            .sorted(Comparator.comparingInt(OcrProvider::priority).reversed()
                .thenComparing(OcrProvider::providerId))
            .toList();
        if (providers.isEmpty()) throw new OcrProviderUnavailableException("No OCR provider available");
        return providers.get(0);
    }
}
