package org.sainm.extractor;

import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OcrCacheTest {
    @Test void cacheHitReturnsCachedResult(@TempDir Path tmpDir) throws Exception {
        var cache = new OcrCache(tmpDir);
        var block = new TextBlock();
        block.setText("cached");
        block.setBbox(new BoundingBox(0, 0, 10, 10, 1));
        var blocks = List.of(block);
        cache.put("sha256abc", 1, blocks);
        var hit = cache.get("sha256abc", 1);
        assertThat(hit).isPresent();
        assertThat(hit.get().get(0).getText()).isEqualTo("cached");
    }

    @Test void cacheMissReturnsEmpty(@TempDir Path tmpDir) {
        var cache = new OcrCache(tmpDir);
        assertThat(cache.get("nonexistent", 1)).isEmpty();
    }

    @Test void cacheRoundTripSupportsPipesAndNewlines(@TempDir Path tmpDir) {
        var cache = new OcrCache(tmpDir);
        var block = new TextBlock();
        block.setText("a|b\nc");
        block.setBbox(new BoundingBox(1, 2, 3, 4, 5));
        block.setConfidence(0.75);
        block.setSource(TextBlock.Source.OCR);

        cache.put("sha256pipe", 2, List.of(block));

        var hit = cache.get("sha256pipe", 2);
        assertThat(hit).isPresent();
        assertThat(hit.get()).singleElement().satisfies(cached -> {
            assertThat(cached.getText()).isEqualTo("a|b\nc");
            assertThat(cached.getConfidence()).isEqualTo(0.75);
            assertThat(cached.getBbox()).isEqualTo(new BoundingBox(1, 2, 3, 4, 5));
        });
    }

    @Test void cacheCanReadLegacyEscapedFormat(@TempDir Path tmpDir) throws Exception {
        var cache = new OcrCache(tmpDir);
        var file = tmpDir.resolve("legacy_page_3.cache");
        Files.writeString(file, "foo\\|bar\\nline|1.0|2.0|3.0|4.0|5|0.8|OCR\n", StandardCharsets.UTF_8);

        var hit = cache.get("legacy", 3);
        assertThat(hit).isPresent();
        assertThat(hit.get()).singleElement().satisfies(cached -> {
            assertThat(cached.getText()).isEqualTo("foo|bar\nline");
            assertThat(cached.getBbox()).isEqualTo(new BoundingBox(1, 2, 3, 4, 5));
            assertThat(cached.getConfidence()).isEqualTo(0.8);
            assertThat(cached.getSource()).isEqualTo(TextBlock.Source.OCR);
        });
    }
}
