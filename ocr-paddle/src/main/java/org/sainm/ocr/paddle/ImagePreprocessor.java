package org.sainm.ocr.paddle;

import java.awt.*;
import java.awt.image.BufferedImage;





public final class ImagePreprocessor {

    private static final float[] DET_MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] DET_STD  = {0.229f, 0.224f, 0.225f};

    private ImagePreprocessor() {}

    



    public static PreprocessResult forDetection(BufferedImage image, int maxSide) {
        int origW = image.getWidth();
        int origH = image.getHeight();
        float ratio = Math.min((float) maxSide / origW, (float) maxSide / origH);
        int resizedW = Math.round(origW * ratio);
        int resizedH = Math.round(origH * ratio);
        
        int padW = (resizedW + 31) / 32 * 32;
        int padH = (resizedH + 31) / 32 * 32;

        BufferedImage resized = resize(image, resizedW, resizedH);
        float[] chw = toDetectionNormalizedCHW(resized, padW, padH);
        int[] shape = {1, 3, padH, padW};
        return new PreprocessResult(chw, shape, ratio);
    }

    



    public static PreprocessResult forRecognition(BufferedImage image, int targetH, int maxW) {
        int origW = image.getWidth();
        int origH = image.getHeight();
        float ratio = (float) targetH / origH;
        int resizedW = Math.min(Math.round(origW * ratio), maxW);

        BufferedImage resized = resize(image, resizedW, targetH);
        float[] chw = toRecognitionNormalizedCHW(resized, maxW, targetH);
        int[] shape = {1, 3, targetH, maxW};
        return new PreprocessResult(chw, shape, ratio);
    }

    static BufferedImage resize(BufferedImage src, int w, int h) {
        var dst = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        var g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    



    static float[] toDetectionNormalizedCHW(BufferedImage image, int outW, int outH) {
        int imgW = image.getWidth();
        int imgH = image.getHeight();
        float[] chw = new float[3 * outH * outW];

        for (int y = 0; y < outH; y++) {
            for (int x = 0; x < outW; x++) {
                int idx = y * outW + x;
                if (x < imgW && y < imgH) {
                    int rgb = image.getRGB(x, y);
                    float r = ((rgb >> 16) & 0xFF) / 255.0f;
                    float g = ((rgb >> 8) & 0xFF) / 255.0f;
                    float b = (rgb & 0xFF) / 255.0f;
                    chw[idx]                   = (r - DET_MEAN[0]) / DET_STD[0];
                    chw[outH * outW + idx]     = (g - DET_MEAN[1]) / DET_STD[1];
                    chw[2 * outH * outW + idx] = (b - DET_MEAN[2]) / DET_STD[2];
                } else {
                    chw[idx]                   = (0 - DET_MEAN[0]) / DET_STD[0];
                    chw[outH * outW + idx]     = (0 - DET_MEAN[1]) / DET_STD[1];
                    chw[2 * outH * outW + idx] = (0 - DET_MEAN[2]) / DET_STD[2];
                }
            }
        }
        return chw;
    }

    



    static float[] toRecognitionNormalizedCHW(BufferedImage image, int outW, int outH) {
        int imgW = image.getWidth();
        int imgH = image.getHeight();
        float[] chw = new float[3 * outH * outW];

        for (int y = 0; y < outH; y++) {
            for (int x = 0; x < outW; x++) {
                if (x >= imgW || y >= imgH) {
                    continue;
                }
                int idx = y * outW + x;
                int rgb = image.getRGB(x, y);
                float r = (((rgb >> 16) & 0xFF) / 255.0f - 0.5f) / 0.5f;
                float g = (((rgb >> 8) & 0xFF) / 255.0f - 0.5f) / 0.5f;
                float b = (((rgb) & 0xFF) / 255.0f - 0.5f) / 0.5f;
                chw[idx] = r;
                chw[outH * outW + idx] = g;
                chw[2 * outH * outW + idx] = b;
            }
        }
        return chw;
    }

    public record PreprocessResult(float[] data, int[] shape, float ratio) {}
}
