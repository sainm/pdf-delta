package org.sainm.ocr.remote;

import org.sainm.spi.OcrProvider;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class SpiLoadTest {

    @Test void ocrProviderSpiRegistered() {
        var providers = ServiceLoader.load(OcrProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.providerId().equals("remote"))
            .toList();

        assertThat(providers).hasSize(1);
        assertThat(providers.get(0)).isInstanceOf(RemoteOcrProvider.class);
    }
}
