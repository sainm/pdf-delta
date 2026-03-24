package org.sainm.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHierarchyTest {
    @Test void allExceptionsExtendBase() {
        assertThat(PdfParseException.class).hasSuperclass(PdfCompareException.class);
        assertThat(OcrException.class).hasSuperclass(PdfCompareException.class);
        assertThat(OcrProviderUnavailableException.class).hasSuperclass(OcrException.class);
        assertThat(OcrTimeoutException.class).hasSuperclass(OcrException.class);
        assertThat(AlignmentException.class).hasSuperclass(PdfCompareException.class);
        assertThat(RenderException.class).hasSuperclass(PdfCompareException.class);
    }
}
