package org.sainm.spi;

import org.sainm.model.TextBlock;

import java.awt.image.BufferedImage;
import java.util.List;

public interface OcrEnhancer {
    List<TextBlock> enhance(List<TextBlock> raw, BufferedImage original, double threshold);
}
