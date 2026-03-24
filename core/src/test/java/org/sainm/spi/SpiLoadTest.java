package org.sainm.spi;

import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class SpiLoadTest {
    @Test void ocrProviderSpiInterface() {
        var loader = ServiceLoader.load(OcrProvider.class);
        assertThat(loader).isNotNull();
        assertThat(loader.stream().count()).isEqualTo(0);
    }
}
