package org.sainm.pipeline;

import org.sainm.alignment.AlignmentEngine;
import org.sainm.diff.DiffEngine;
import org.sainm.document.DocumentStructurer;
import org.sainm.extractor.PdfExtractorFactory;
import org.sainm.model.CompareOptions;
import org.sainm.model.CompareRequest;
import org.sainm.model.CompareResult;
import org.sainm.model.DiffItem;
import org.sainm.model.DiffSeverity;
import org.sainm.model.DiffSummary;
import org.sainm.model.ImageComparisonSummary;
import org.sainm.model.LogicalDocument;
import org.sainm.model.PageContent;
import org.sainm.model.PageComparisonSummary;
import org.sainm.model.PageType;
import org.sainm.model.VisualDiffItem;
import org.sainm.normalizer.NormalizerChain;
import org.sainm.preprocessor.PreprocessorChain;
import org.sainm.spi.LogicalBlock;
import org.sainm.table.TableReconstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PdfComparator {
    private CompareProgressListener progressListener;

    private final PreprocessorChain preprocessor;
    private final NormalizerChain normalizer;
    private final TableReconstructor tableReconstructor;
    private final DocumentStructurer structurer;
    private final AlignmentEngine alignmentEngine;
    private final DiffEngine diffEngine;
    private final ImagePageComparator imagePageComparator;

    public PdfComparator() {
        this.preprocessor = new PreprocessorChain(List.of());
        this.normalizer = NormalizerChain.fromSpi();
        this.tableReconstructor = new TableReconstructor();
        this.structurer = new DocumentStructurer();
        this.alignmentEngine = new AlignmentEngine();
        this.diffEngine = new DiffEngine();
        this.imagePageComparator = new ImagePageComparator();
    }

    public CompareResult compare(CompareRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        var options = request.options();
        byte[] sourceBytesA = PdfSourceHelper.toBytes(request.sourceA());
        byte[] sourceBytesB = PdfSourceHelper.toBytes(request.sourceB());

        
        var factory = PdfExtractorFactory.fromSpi();
        var pagesA = factory.extract(new org.sainm.model.PdfSource.Bytes(sourceBytesA), options);
        var pagesB = factory.extract(new org.sainm.model.PdfSource.Bytes(sourceBytesB), options);

        
        var processedA = process(pagesA, options);
        var processedB = process(pagesB, options);

        
        var docA = structurer.structure(processedA, options);
        var docB = structurer.structure(processedB, options);

        
        var blocksA = toBlocks(docA);
        var blocksB = toBlocks(docB);

        
        var pairs = alignmentEngine.align(blocksA, blocksB, options);

        
        var items = diffEngine.diff(pairs, options);
        var visualDiffItems = imagePageComparator.compare(sourceBytesA, sourceBytesB, pagesA, pagesB, options);
        var ocrFailedPages = collectOcrFailedPages(pagesA, pagesB);
        var pageSummaries = buildPageSummaries(pagesA, pagesB, visualDiffItems);
        var warnings = buildWarnings(ocrFailedPages);

        
        var result = new CompareResult();
        result.setJobId(UUID.randomUUID().toString());
        result.setOptionsUsed(options);
        result.setItems(items);
        result.setSummary(buildSummary(items));
        result.setVisualDiffItems(visualDiffItems);
        result.setPageSummaries(pageSummaries);
        result.setImageComparisonSummary(buildImageSummary(pageSummaries, visualDiffItems));
        result.setOcrFailedPages(ocrFailedPages);
        result.setWarnings(warnings);
        result.setSourceBytesA(sourceBytesA);
        result.setSourceBytesB(sourceBytesB);
        return result;
    }

    private List<PageContent> process(List<PageContent> pages, CompareOptions options) {
        List<PageContent> result = new ArrayList<>();
        for (var page : pages) {
            var processed = preprocessor.process(page, options);
            normalizer.applyToPage(processed, options);
            var tables = tableReconstructor.reconstruct(processed, options);
            processed.setTableBlocks(tables);
            normalizer.applyToPage(processed, options);
            result.add(processed);
        }
        return result;
    }

    private List<LogicalBlock> toBlocks(LogicalDocument doc) {
        List<LogicalBlock> blocks = new ArrayList<>();
        for (var p : doc.getParagraphs()) blocks.add(new ParagraphBlock(p));
        for (var t : doc.getTables()) blocks.add(new TableLogicalBlock(t));
        return blocks;
    }

    private DiffSummary buildSummary(List<DiffItem> items) {
        return new DiffSummary(
            items.size(),
            (int) items.stream().filter(i -> i.getSeverity() == DiffSeverity.CRITICAL).count(),
            (int) items.stream().filter(i -> i.getSeverity() == DiffSeverity.MAJOR).count(),
            (int) items.stream().filter(i -> i.getSeverity() == DiffSeverity.MINOR).count(),
            (int) items.stream().filter(i -> i.getSeverity() == DiffSeverity.INFO).count()
        );
    }

    private List<Integer> collectOcrFailedPages(List<PageContent> pagesA, List<PageContent> pagesB) {
        int pageCount = Math.min(pagesA.size(), pagesB.size());
        List<Integer> failed = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            if (pagesA.get(i).getType() == PageType.OCR_FAILED || pagesB.get(i).getType() == PageType.OCR_FAILED) {
                failed.add(i + 1);
            }
        }
        return failed;
    }

    private List<PageComparisonSummary> buildPageSummaries(List<PageContent> pagesA, List<PageContent> pagesB,
                                                           List<VisualDiffItem> visualDiffItems) {
        int pageCount = Math.min(pagesA.size(), pagesB.size());
        List<PageComparisonSummary> summaries = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            var pageA = pagesA.get(i);
            var pageB = pagesB.get(i);
            int pageNumber = i + 1;
            var summary = new PageComparisonSummary();
            summary.setPageNumber(pageNumber);
            summary.setPageType(pageA.getType() == PageType.TEXT ? pageB.getType() : pageA.getType());
            boolean ocrAttempted = pageA.getType() != PageType.TEXT || pageB.getType() != PageType.TEXT;
            boolean ocrSucceeded = pageA.getType() != PageType.OCR_FAILED && pageB.getType() != PageType.OCR_FAILED;
            summary.setOcrAttempted(ocrAttempted);
            summary.setOcrSucceeded(ocrSucceeded);
            summary.setHasTextDiffs(false);
            summary.setHasVisualDiffs(visualDiffItems.stream().anyMatch(item -> item.getPageNumber() == pageNumber));
            summary.setStatus(ocrSucceeded ? "OK" : "PARTIAL_SUCCESS");
            summaries.add(summary);
        }
        return summaries;
    }

    private List<String> buildWarnings(List<Integer> ocrFailedPages) {
        if (ocrFailedPages.isEmpty()) return List.of();
        return List.of("OCR failed on pages: " + ocrFailedPages);
    }

    private ImageComparisonSummary buildImageSummary(List<PageComparisonSummary> pageSummaries,
                                                     List<VisualDiffItem> visualDiffItems) {
        var summary = new ImageComparisonSummary();
        summary.setImageLikePages((int) pageSummaries.stream()
            .filter(page -> page.getPageType() != PageType.TEXT)
            .count());
        summary.setVisualDiffPages((int) visualDiffItems.stream()
            .map(VisualDiffItem::getPageNumber)
            .distinct()
            .count());
        summary.setVisualDiffItems(visualDiffItems.size());
        summary.setPartialSuccessPages((int) pageSummaries.stream()
            .filter(page -> !"OK".equals(page.getStatus()))
            .count());
        return summary;
    }

    public void setProgressListener(CompareProgressListener listener) {
        this.progressListener = listener;
    }
}
