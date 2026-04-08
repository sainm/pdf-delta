package org.sainm.extractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;





final class OcrCache {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<CachedTextBlock>> CACHE_TYPE = new TypeReference<>() {};

    private final Path cacheDir;

    public OcrCache(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Optional<List<TextBlock>> get(String pdfHash, int pageIndex) {
        var file = cacheFile(pdfHash, pageIndex);
        if (!Files.exists(file)) return Optional.empty();
        try {
            return Optional.of(readJson(file));
        } catch (IOException | IllegalArgumentException e) {
            try {
                return Optional.of(readLegacy(file));
            } catch (IOException | IllegalArgumentException legacyError) {
                return Optional.empty();
            }
        }
    }

    private List<TextBlock> readJson(Path file) throws IOException {
        var cachedBlocks = MAPPER.readValue(file.toFile(), CACHE_TYPE);
        var blocks = new ArrayList<TextBlock>(cachedBlocks.size());
        for (var cached : cachedBlocks) {
            blocks.add(cached.toTextBlock());
        }
        return blocks;
    }

    private List<TextBlock> readLegacy(Path file) throws IOException {
        var lines = Files.readAllLines(file);
        var blocks = new ArrayList<TextBlock>();
        for (var line : lines) {
            if (line.isBlank()) continue;
            var parts = splitEscaped(line);
            if (parts.size() < 8) continue;
            var block = new TextBlock();
            block.setText(unescape(parts.get(0)));
            double x = Double.parseDouble(parts.get(1));
            double y = Double.parseDouble(parts.get(2));
            double w = Double.parseDouble(parts.get(3));
            double h = Double.parseDouble(parts.get(4));
            int pg = Integer.parseInt(parts.get(5));
            block.setBbox(new BoundingBox(x, y, w, h, pg));
            block.setConfidence(Double.parseDouble(parts.get(6)));
            block.setSource(TextBlock.Source.valueOf(parts.get(7)));
            blocks.add(block);
        }
        return blocks;
    }

    public void put(String pdfHash, int pageIndex, List<TextBlock> blocks) {
        var file = cacheFile(pdfHash, pageIndex);
        try {
            Files.createDirectories(cacheDir);
            var cachedBlocks = blocks.stream()
                .map(CachedTextBlock::from)
                .toList();
            MAPPER.writeValue(file.toFile(), cachedBlocks);
        } catch (IOException e) {
            
        }
    }

    private Path cacheFile(String pdfHash, int pageIndex) {
        return cacheDir.resolve(pdfHash + "_page_" + pageIndex + ".cache");
    }

    private static List<String> splitEscaped(String line) {
        var parts = new ArrayList<String>();
        var current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (escaped) {
                current.append('\\').append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (escaped) current.append('\\');
        parts.add(current.toString());
        return parts;
    }

    private static String unescape(String s) {
        return s.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\");
    }

    private static final class CachedTextBlock {
        public String text;
        public double x;
        public double y;
        public double width;
        public double height;
        public int page;
        public double confidence;
        public String source;

        static CachedTextBlock from(TextBlock block) {
            var cached = new CachedTextBlock();
            var bbox = block.getBbox();
            cached.text = block.getText();
            cached.x = bbox != null ? bbox.x() : 0;
            cached.y = bbox != null ? bbox.y() : 0;
            cached.width = bbox != null ? bbox.width() : 0;
            cached.height = bbox != null ? bbox.height() : 0;
            cached.page = bbox != null ? bbox.page() : 0;
            cached.confidence = block.getConfidence();
            cached.source = block.getSource() != null ? block.getSource().name() : TextBlock.Source.OCR.name();
            return cached;
        }

        TextBlock toTextBlock() {
            var block = new TextBlock();
            block.setText(text);
            block.setBbox(new BoundingBox(x, y, width, height, page));
            block.setConfidence(confidence);
            block.setSource(TextBlock.Source.valueOf(source));
            return block;
        }
    }
}
