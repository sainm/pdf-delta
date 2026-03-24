package org.sainm.extractor;

import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
