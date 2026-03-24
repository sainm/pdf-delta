package org.sainm.ocr.paddle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaddleOcrProviderTest {

    @Test
    void providerIdIsPaddle() {
        var provider = new PaddleOcrProvider();
        assertThat(provider.providerId()).isEqualTo("paddle");
    }

    @Test
    void priorityIs100() {
        var provider = new PaddleOcrProvider();
        assertThat(provider.priority()).isEqualTo(100);
    }

    @Test
    void isAvailableReturnsFalseWithoutNativeLib() {
        
        var provider = new PaddleOcrProvider();
        assertThat(provider.isAvailable()).isFalse();
    }
}
