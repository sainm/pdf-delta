package org.sainm.extractor;

import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;





final class OcrCache {
    private final Path cacheDir;

    public OcrCache(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    public Optional<List<TextBlock>> get(String pdfHash, int pageIndex) {
        var file = cacheFile(pdfHash, pageIndex);
        if (!Files.exists(file)) return Optional.empty();
        try {
            var lines = Files.readAllLines(file);
            var blocks = new ArrayList<TextBlock>();
            for (var line : lines) {
                if (line.isBlank()) continue;
                var parts = line.split("\\|", -1);
                if (parts.length < 8) continue;
                var block = new TextBlock();
                block.setText(unescape(parts[0]));
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double w = Double.parseDouble(parts[3]);
                double h = Double.parseDouble(parts[4]);
                int pg  = Integer.parseInt(parts[5]);
                block.setBbox(new BoundingBox(x, y, w, h, pg));
                block.setConfidence(Double.parseDouble(parts[6]));
                block.setSource(TextBlock.Source.valueOf(parts[7]));
                blocks.add(block);
            }
            return Optional.of(blocks);
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void put(String pdfHash, int pageIndex, List<TextBlock> blocks) {
        var file = cacheFile(pdfHash, pageIndex);
        try {
            Files.createDirectories(cacheDir);
            var sb = new StringBuilder();
            for (var block : blocks) {
                var bbox = block.getBbox();
                double x = bbox != null ? bbox.x() : 0;
                double y = bbox != null ? bbox.y() : 0;
                double w = bbox != null ? bbox.width() : 0;
                double h = bbox != null ? bbox.height() : 0;
                int pg   = bbox != null ? bbox.page() : 0;
                sb.append(escape(block.getText())).append('|')
                  .append(x).append('|').append(y).append('|')
                  .append(w).append('|').append(h).append('|')
                  .append(pg).append('|')
                  .append(block.getConfidence()).append('|')
                  .append(block.getSource() != null ? block.getSource().name() : TextBlock.Source.OCR.name())
                  .append('\n');
            }
            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            
        }
    }

    private Path cacheFile(String pdfHash, int pageIndex) {
        return cacheDir.resolve(pdfHash + "_page_" + pageIndex + ".cache");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n");
    }

    private static String unescape(String s) {
        return s.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\");
    }
}
