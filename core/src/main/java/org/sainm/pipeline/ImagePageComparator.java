package org.sainm.pipeline;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.sainm.model.BoundingBox;
import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.model.PageType;
import org.sainm.model.VisualDiffItem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class ImagePageComparator {
    List<VisualDiffItem> compare(byte[] pdfA, byte[] pdfB,
                                 List<PageContent> pagesA,
                                 List<PageContent> pagesB,
                                 CompareOptions options) {
        if (!options.isEnableVisualDiff()) return List.of();
        try (var docA = Loader.loadPDF(pdfA); var docB = Loader.loadPDF(pdfB)) {
            var rendererA = new PDFRenderer(docA);
            var rendererB = new PDFRenderer(docB);
            int pageCount = Math.min(Math.min(docA.getNumberOfPages(), docB.getNumberOfPages()),
                Math.min(pagesA.size(), pagesB.size()));
            List<VisualDiffItem> items = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                var pageA = pagesA.get(i);
                var pageB = pagesB.get(i);
                if (!isImageLike(pageA, pageB)) continue;
                if (pageA.getType() == PageType.IMAGE && pageB.getType() == PageType.IMAGE && pageTextsDiffer(pageA, pageB)) {
                    continue;
                }
                if (pageA.getType() == PageType.MIXED && pageTextsDiffer(pageA, pageB)) {
                    continue;
                }

                BufferedImage imageA = rendererA.renderImageWithDPI(i, options.getRenderDpi());
                BufferedImage imageB = rendererB.renderImageWithDPI(i, options.getRenderDpi());
                var item = comparePage(imageA, imageB, pageA, options.getVisualDiffThreshold());
                if (item != null) {
                    item.setPageNumber(i + 1);
                    items.add(item);
                }
            }
            return items;
        } catch (IOException e) {
            return List.of();
        }
    }

    private boolean isImageLike(PageContent a, PageContent b) {
        return a.getType() != PageType.TEXT || b.getType() != PageType.TEXT;
    }

    private boolean pageTextsDiffer(PageContent a, PageContent b) {
        return !pageText(a).equals(pageText(b));
    }

    private String pageText(PageContent page) {
        return page.getTextBlocks().stream()
            .map(block -> block.getNormalizedText() != null ? block.getNormalizedText() : block.getText())
            .filter(text -> text != null && !text.isBlank())
            .reduce("", (left, right) -> left + "\n" + right)
            .strip();
    }

    private VisualDiffItem comparePage(BufferedImage imageA, BufferedImage imageB, PageContent pageA, double threshold) {
        int width = Math.min(imageA.getWidth(), imageB.getWidth());
        int height = Math.min(imageA.getHeight(), imageB.getHeight());
        long diffPixels = 0;
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isIgnoredTextPixel(x, y, imageA.getWidth(), imageA.getHeight(), pageA)) continue;
                if (imageA.getRGB(x, y) == imageB.getRGB(x, y)) continue;
                diffPixels++;
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
        double diffRatio = width == 0 || height == 0 ? 0 : (double) diffPixels / (width * (double) height);
        if (diffRatio < threshold || maxX < minX || maxY < minY) return null;

        var item = new VisualDiffItem();
        item.setDiffRatio(diffRatio);
        item.setReason(pageA.getType() == PageType.OCR_FAILED ? "OCR_FAILED_VISUAL_FALLBACK" : "IMAGE_REGION_CHANGED");
        item.setBbox(toPdfBox(minX, minY, maxX, maxY, imageA.getWidth(), imageA.getHeight(), pageA));
        return item;
    }

    private boolean isIgnoredTextPixel(int x, int y, int width, int height, PageContent page) {
        if (page.getType() != PageType.MIXED) return false;
        for (var block : page.getTextBlocks()) {
            var bbox = block.getBbox();
            if (bbox == null) continue;
            double left = bbox.x() / page.getWidth() * width;
            double top = bbox.y() / page.getHeight() * height;
            double right = (bbox.x() + bbox.width()) / page.getWidth() * width;
            double bottom = (bbox.y() + bbox.height()) / page.getHeight() * height;
            if (x >= left && x <= right && y >= top && y <= bottom) return true;
        }
        return false;
    }

    private BoundingBox toPdfBox(int minX, int minY, int maxX, int maxY, int imageWidth, int imageHeight, PageContent page) {
        double x = minX / (double) imageWidth * page.getWidth();
        double y = minY / (double) imageHeight * page.getHeight();
        double w = Math.max(1, (maxX - minX + 1) / (double) imageWidth * page.getWidth());
        double h = Math.max(1, (maxY - minY + 1) / (double) imageHeight * page.getHeight());
        return new BoundingBox(x, y, w, h, page.getPageNumber());
    }
}
