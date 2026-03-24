package org.sainm.ocr.paddle.ffi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaddleBindingsTest {

    @Test
    void isAvailableReturnsFalseWhenLibNotPresent() {
        
        assertThat(PaddleBindings.isAvailable()).isFalse();
    }

    @Test
    void loadLibraryWithNullPathDoesNotThrow() {
        
        PaddleBindings.loadLibrary(null);
        
        assertThat(PaddleBindings.isAvailable()).isFalse();
    }
}
