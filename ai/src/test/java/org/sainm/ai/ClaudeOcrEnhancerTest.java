package org.sainm.ai;

import org.sainm.model.TextBlock;
import org.sainm.model.BoundingBox;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClaudeOcrEnhancerTest {
    private TextBlock textBlock(String text, double confidence) {
        var b = new TextBlock();
        b.setText(text);
        b.setConfidence(confidence);
        b.setBbox(new BoundingBox(0, 0, 100, 15, 1));
        return b;
    }

    @Test void skipsHighConfidenceBlocks() {
        
        var enhancer = new ClaudeOcrEnhancer("fake-key");
        var blocks = List.of(textBlock("clear text", 0.95));
        var image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        var result = enhancer.enhance(blocks, image, 0.8);
        assertThat(result.get(0).getText()).isEqualTo("clear text");
    }
}
