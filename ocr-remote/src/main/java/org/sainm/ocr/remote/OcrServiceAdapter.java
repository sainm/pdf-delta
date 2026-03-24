package org.sainm.ocr.remote;

import org.sainm.exception.OcrException;
import org.sainm.model.OcrOptions;
import org.sainm.model.TextBlock;

import java.awt.image.BufferedImage;
import java.util.List;

public interface OcrServiceAdapter {
    List<TextBlock> recognize(BufferedImage image, OcrOptions options) throws OcrException;
    String adapterId();
    boolean isAvailable(OcrOptions options);
}
