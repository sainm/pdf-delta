package org.sainm.extractor;

import org.sainm.model.*;
import org.sainm.spi.OcrProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImagePdfExtractorTest {
    @Test void delegatesToOcrProvider() throws Exception {
        var mockOcr = mock(OcrProvider.class);
        when(mockOcr.isAvailable()).thenReturn(true);
        when(mockOcr.priority()).thenReturn(10);
        when(mockOcr.providerId()).thenReturn("mock");
        var block = new TextBlock();
        block.setText("OCR Result");
        block.setBbox(new BoundingBox(10, 10, 100, 20, 1));
        when(mockOcr.recognize(any(), any())).thenReturn(List.of(block));

        byte[] pdf = org.sainm.PdfFixtures.singlePageImageOnly();
        var extractor = new ImagePdfExtractor(List.of(mockOcr));
        var pages = extractor.extract(new PdfSource.Bytes(pdf), CompareOptions.defaults());

        assertThat(pages.get(0).getType()).isEqualTo(PageType.IMAGE);
        assertThat(pages.get(0).getTextBlocks()).anySatisfy(b ->
            assertThat(b.getText()).isEqualTo("OCR Result"));
        assertThat(pages.get(0).getTextBlocks().get(0).getSource())
            .isEqualTo(TextBlock.Source.OCR);
    }

    @Test void marksPagesAsOcrFailedWhenNoOcrProviderAvailable() throws Exception {
        var extractor = new ImagePdfExtractor(List.of());
        byte[] pdf = org.sainm.PdfFixtures.singlePageImageOnly();
        var pages = extractor.extract(new PdfSource.Bytes(pdf), CompareOptions.defaults());
        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).getType()).isEqualTo(PageType.OCR_FAILED);
        assertThat(pages.get(0).getImageBlocks()).isNotEmpty();
    }
}
