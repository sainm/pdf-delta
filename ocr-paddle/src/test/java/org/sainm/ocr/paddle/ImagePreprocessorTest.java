package org.sainm.ocr.paddle;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class ImagePreprocessorTest {

    @Test
    void forDetectionProducesCorrectShape() {
        var image = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
        var result = ImagePreprocessor.forDetection(image, 960);

        assertThat(result.shape()).hasSize(4);
        assertThat(result.shape()[0]).isEqualTo(1);
        assertThat(result.shape()[1]).isEqualTo(3);
        assertThat(result.shape()[2] % 32).isZero();
        assertThat(result.shape()[3] % 32).isZero();

        int expected = result.shape()[0] * result.shape()[1] * result.shape()[2] * result.shape()[3];
        assertThat(result.data()).hasSize(expected);
    }

    @Test
    void forDetectionPreservesAspectRatio() {
        var image = new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR);
        var result = ImagePreprocessor.forDetection(image, 960);

        assertThat(result.ratio()).isLessThanOrEqualTo(1.0f);
        assertThat(result.ratio()).isGreaterThan(0);
    }

    @Test
    void forRecognitionProducesCorrectShape() {
        var image = new BufferedImage(200, 40, BufferedImage.TYPE_3BYTE_BGR);
        var result = ImagePreprocessor.forRecognition(image, 48, 320);

        assertThat(result.shape()).containsExactly(1, 3, 48, 320);
        assertThat(result.data()).hasSize(1 * 3 * 48 * 320);
    }

    @Test
    void detectionNormalizationUsesImageNetStatistics() {
        var image = new BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR);
        var g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, 2, 2);
        g.dispose();

        float[] chw = ImagePreprocessor.toDetectionNormalizedCHW(image, 2, 2);
        assertThat(chw[0]).isBetween(2.0f, 2.5f);
    }

    @Test
    void recognitionNormalizationMatchesPaddleOcrExpectations() {
        var image = new BufferedImage(2, 1, BufferedImage.TYPE_3BYTE_BGR);
        image.setRGB(0, 0, java.awt.Color.WHITE.getRGB());
        image.setRGB(1, 0, java.awt.Color.BLACK.getRGB());

        float[] chw = ImagePreprocessor.toRecognitionNormalizedCHW(image, 4, 1);

        assertThat(chw[0]).isEqualTo(1.0f);
        assertThat(chw[1]).isEqualTo(-1.0f);
        assertThat(chw[2]).isZero();
        assertThat(chw[3]).isZero();
    }

    @Test
    void smallImageHandledGracefully() {
        var image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        var result = ImagePreprocessor.forDetection(image, 960);
        assertThat(result.data()).isNotEmpty();
    }
}
