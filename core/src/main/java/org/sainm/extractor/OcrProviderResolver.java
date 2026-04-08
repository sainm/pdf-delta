package org.sainm.extractor;

import org.sainm.exception.OcrProviderUnavailableException;
import org.sainm.model.OcrOptions;
import org.sainm.spi.OcrProvider;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

final class OcrProviderResolver {
    public static OcrProvider resolve(OcrOptions options) {
        var providers = ServiceLoader.load(OcrProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        return resolve(providers, options);
    }

    static OcrProvider resolve(List<OcrProvider> providers, OcrOptions options) {
        var available = providers.stream()
            .filter(OcrProvider::isAvailable)
            .sorted(Comparator.comparingInt(OcrProvider::priority).reversed()
                .thenComparing(OcrProvider::providerId))
            .toList();
        if (available.isEmpty()) throw new OcrProviderUnavailableException("No OCR provider available");
        var providerType = options != null ? options.getProviderType() : OcrOptions.ProviderType.AUTO;
        return switch (providerType) {
            case AUTO -> available.get(0);
            case LOCAL_PADDLE -> findById(available, "paddle", providerType);
            case REMOTE -> findById(available, "remote", providerType);
        };
    }

    private static OcrProvider findById(List<OcrProvider> providers, String providerId, OcrOptions.ProviderType providerType) {
        return providers.stream()
            .filter(provider -> provider.providerId().equals(providerId))
            .findFirst()
            .orElseThrow(() -> new OcrProviderUnavailableException("Requested OCR provider unavailable: " + providerType));
    }
}
