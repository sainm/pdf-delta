package org.sainm.ocr.remote;

import org.sainm.exception.OcrProviderUnavailableException;
import org.sainm.model.OcrOptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RemoteOcrProviderTest {

    @Test void providerIdIsRemote() {
        assertThat(new RemoteOcrProvider().providerId()).isEqualTo("remote");
    }

    @Test void priorityIs50() {
        assertThat(new RemoteOcrProvider().priority()).isEqualTo(50);
    }

    @Test void selectsPaddleAdapterByDefault() {
        var provider = new RemoteOcrProvider();
        var options = OcrOptions.defaults();
        options.setRemoteEndpoint("http://localhost:8000");

        assertThatCode(() -> {
            var adapter = provider.selectAdapter(options);
            assertThat(adapter.adapterId()).isEqualTo("paddle");
        }).doesNotThrowAnyException();
    }

    @Test void throwsWhenNoAdapterAvailable() {
        var provider = new RemoteOcrProvider();
        var options = OcrOptions.defaults();

        assertThatThrownBy(() -> provider.selectAdapter(options))
            .isInstanceOf(OcrProviderUnavailableException.class)
            .hasMessageContaining("No remote OCR adapter available");
    }
}
