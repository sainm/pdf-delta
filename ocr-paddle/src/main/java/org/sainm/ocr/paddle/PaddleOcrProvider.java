package org.sainm.ocr.paddle;

import org.sainm.exception.OcrException;
import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;
import org.sainm.ocr.paddle.ffi.PaddleBindings;
import org.sainm.spi.OcrProvider;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;






public class PaddleOcrProvider implements OcrProvider {

    private static final String DEFAULT_MODEL_DIR =
            System.getProperty("user.home") + "/.pdf-compare/models/PP-OCRv4";

    private volatile PaddleOcrEngine engine;

    @Override
    public List<TextBlock> recognize(BufferedImage image, OcrOptions options) {
        var eng = getOrCreateEngine(options);
        return eng.recognize(image, 0, options.getMinConfidence()).stream()
                .sorted(Comparator
                        .comparingDouble((TextBlock block) -> block.getBbox() != null ? block.getBbox().y() : Double.MAX_VALUE)
                        .thenComparingDouble(block -> block.getBbox() != null ? block.getBbox().x() : Double.MAX_VALUE))
                .toList();
    }

    @Override
    public String providerId() {
        return "paddle";
    }

    @Override
    public boolean isAvailable() {
        return PaddleBindings.isAvailable();
    }

    @Override
    public int priority() {
        return 100;
    }

    private PaddleOcrEngine getOrCreateEngine(OcrOptions options) {
        if (engine != null) return engine;
        synchronized (this) {
            if (engine != null) return engine;
            String dir = options.getModelDir();
            Path modelDir = (dir != null && !dir.isBlank())
                    ? Path.of(dir)
                    : Path.of(DEFAULT_MODEL_DIR);
            engine = PaddleOcrEngine.create(modelDir, normalizedLanguage(options.getLanguage()));
            return engine;
        }
    }

    private boolean hasModels(Path modelDir, String language) {
        var spec = PaddleOcrEngine.modelSpec(language);
        return Files.isDirectory(modelDir)
                && Files.exists(modelDir.resolve(spec.detDir()).resolve("inference.pdmodel"))
                && Files.exists(modelDir.resolve(spec.recDir()).resolve("inference.pdmodel"))
                && Files.exists(modelDir.resolve(spec.dictFile()));
    }

    private String normalizedLanguage(String language) {
        if (language == null || language.isBlank()) return "zh";
        String value = language.strip().toLowerCase();
        if (value.equals("ja") || value.equals("jpn") || value.equals("japan") || value.equals("jp")) return "ja";
        return "zh";
    }
}
