package org.sainm.spi;

import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;

import java.awt.image.BufferedImage;
import java.util.List;

public interface OcrProvider {
    List<TextBlock> recognize(BufferedImage image, OcrOptions options);
    String providerId();
    boolean isAvailable();
    int priority();
}
