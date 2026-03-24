package org.sainm.ocr.paddle.model;







public record OcrResult(String text, float[][] box, float score) {}
