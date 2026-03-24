package org.sainm.ocr.paddle;

import org.junit.jupiter.api.Test;
import org.sainm.ocr.paddle.model.OcrResult;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostProcessorTest {

    private PostProcessor createProcessor() {
        return new PostProcessor(List.of(
                "",    
                "a",   
                "b",   
                "c",   
                "d",   
                " "    
        ));
    }

    @Test
    void ctcDecodeSkipsBlanksAndRepeats() {
        var pp = createProcessor();
        
        
        int vocabSize = 6;
        int seqLen = 6;
        float[] output = new float[seqLen * vocabSize];

        
        output[0] = 1.0f;
        
        output[1 * vocabSize + 1] = 0.9f;
        
        output[2 * vocabSize + 1] = 0.8f;
        
        output[3 * vocabSize + 2] = 0.95f;
        
        output[4 * vocabSize + 0] = 1.0f;
        
        output[5 * vocabSize + 3] = 0.85f;

        OcrResult result = pp.ctcDecode(output, seqLen, vocabSize);
        assertThat(result.text()).isEqualTo("abc");
        assertThat(result.score()).isBetween(0.8f, 1.0f);
    }

    @Test
    void ctcDecodeEmptySequence() {
        var pp = createProcessor();
        
        int vocabSize = 6;
        int seqLen = 3;
        float[] output = new float[seqLen * vocabSize];
        output[0] = 1.0f;
        output[vocabSize] = 1.0f;
        output[2 * vocabSize] = 1.0f;

        OcrResult result = pp.ctcDecode(output, seqLen, vocabSize);
        assertThat(result.text()).isEmpty();
        assertThat(result.score()).isZero();
    }

    @Test
    void dbPostProcessFindsRegions() {
        var pp = createProcessor();
        int h = 32, w = 32;
        float[] output = new float[h * w];
        
        for (int y = 10; y < 22; y++) {
            for (int x = 5; x < 25; x++) {
                output[y * w + x] = 0.8f;
            }
        }

        List<float[][]> boxes = pp.dbPostProcess(output, h, w, 1.0f);
        assertThat(boxes).isNotEmpty();
        
        for (float[][] box : boxes) {
            assertThat(box.length).isEqualTo(4);
            for (float[] pt : box) {
                assertThat(pt).hasSize(2);
            }
        }
    }

    @Test
    void dbPostProcessEmptyOutput() {
        var pp = createProcessor();
        float[] output = new float[32 * 32]; 
        List<float[][]> boxes = pp.dbPostProcess(output, 32, 32, 1.0f);
        assertThat(boxes).isEmpty();
    }

    @Test
    void cropRegionReturnsValidImage() {
        var source = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
        float[][] box = {{10, 10}, {50, 10}, {50, 30}, {10, 30}};
        BufferedImage cropped = PostProcessor.cropRegion(source, box);
        assertThat(cropped.getWidth()).isEqualTo(40);
        assertThat(cropped.getHeight()).isEqualTo(20);
    }

    @Test
    void toBoundingBoxConvertsCorrectly() {
        float[][] box = {{10, 20}, {110, 20}, {110, 50}, {10, 50}};
        var bbox = PostProcessor.toBoundingBox(box, 1);
        assertThat(bbox.x()).isEqualTo(10.0);
        assertThat(bbox.y()).isEqualTo(20.0);
        assertThat(bbox.width()).isEqualTo(100.0);
        assertThat(bbox.height()).isEqualTo(30.0);
        assertThat(bbox.page()).isEqualTo(1);
    }
}

