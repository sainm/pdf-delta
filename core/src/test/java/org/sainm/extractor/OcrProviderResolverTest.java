package org.sainm.extractor;

import org.junit.jupiter.api.Test;
import org.sainm.exception.OcrProviderUnavailableException;
import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;
import org.sainm.spi.OcrProvider;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcrProviderResolverTest {
    @Test void autoPrefersHighestPriorityAvailableProvider() {
        var options = OcrOptions.defaults();

        var selected = OcrProviderResolver.resolve(List.of(
            provider("remote", true, 50),
            provider("paddle", true, 100)
        ), options);

        assertThat(selected.providerId()).isEqualTo("paddle");
    }

    @Test void remoteSelectionPrefersRemoteProvider() {
        var options = OcrOptions.defaults();
        options.setProviderType(OcrOptions.ProviderType.REMOTE);

        var selected = OcrProviderResolver.resolve(List.of(
            provider("paddle", true, 100),
            provider("remote", true, 50)
        ), options);

        assertThat(selected.providerId()).isEqualTo("remote");
    }

    @Test void localSelectionFailsWhenRequestedProviderIsUnavailable() {
        var options = OcrOptions.defaults();
        options.setProviderType(OcrOptions.ProviderType.LOCAL_PADDLE);

        assertThatThrownBy(() -> OcrProviderResolver.resolve(List.of(
            provider("remote", true, 50),
            provider("paddle", false, 100)
        ), options))
            .isInstanceOf(OcrProviderUnavailableException.class)
            .hasMessageContaining("LOCAL_PADDLE");
    }

    private static OcrProvider provider(String id, boolean available, int priority) {
        return new OcrProvider() {
            @Override
            public List<TextBlock> recognize(BufferedImage image, OcrOptions options) {
                return List.of();
            }

            @Override
            public String providerId() {
                return id;
            }

            @Override
            public boolean isAvailable() {
                return available;
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }
}
