package org.sainm.preprocessor;

import org.sainm.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatermarkRemoverTest {
    @Test void removesLowOpacityRepeatedText() {
        var remover = new WatermarkRemover();
        var page = new PageContent();
        page.setPageNumber(1);
        page.setType(PageType.TEXT);
        page.setWidth(595);
        page.setHeight(842);
        var watermark = new TextBlock();
        watermark.setText("CONFIDENTIAL");
        watermark.setBbox(new BoundingBox(100, 100, 500, 400, 1)); 
        watermark.setConfidence(0.3);
        var normal = new TextBlock();
        normal.setText("Normal text");
        normal.setBbox(new BoundingBox(50, 200, 200, 15, 1));
        normal.setConfidence(1.0);
        page.setTextBlocks(new ArrayList<>(List.of(watermark, normal)));

        var result = remover.process(page, CompareOptions.defaults());
        assertThat(result.getTextBlocks()).hasSize(1);
        assertThat(result.getTextBlocks().get(0).getText()).isEqualTo("Normal text");
    }

    @Test void skipsWhenIgnoreWatermarkFalse() {
        var remover = new WatermarkRemover();
        var page = new PageContent();
        page.setPageNumber(1);
        page.setType(PageType.TEXT);
        page.setWidth(595);
        page.setHeight(842);
        var watermark = new TextBlock();
        watermark.setText("CONFIDENTIAL");
        watermark.setBbox(new BoundingBox(100, 100, 500, 400, 1));
        watermark.setConfidence(0.3);
        page.setTextBlocks(new ArrayList<>(List.of(watermark)));

        var result = remover.process(page, CompareOptions.defaults().withIgnoreWatermark(false));
        assertThat(result.getTextBlocks()).hasSize(1);
    }
}
