package org.sainm.report;

import org.sainm.model.CompareResult;

import java.io.IOException;






public final class DiffReportPdfGenerator {

    private ReportMode mode = ReportMode.TEXT_DIFF;

    public DiffReportPdfGenerator mode(ReportMode mode) {
        this.mode = mode;
        return this;
    }

    public byte[] generate(CompareResult result, byte[] pdfA, byte[] pdfB) throws IOException {
        return switch (mode) {
            case TEXT_DIFF    -> new TextDiffRenderer().generate(result, pdfA, pdfB);
            case SIDE_BY_SIDE -> new SideBySideRenderer().generate(result, pdfA, pdfB);
            case SINGLE_PAGE  -> new SinglePageRenderer().generate(result, pdfA, pdfB);
            case DUAL_PAGE    -> new DualPageRenderer().generate(result, pdfA, pdfB);
            case HTML         -> new HtmlDiffRenderer().generate(result, pdfA, pdfB);
            case TEXT_LAYOUT  -> new TextLayoutRenderer().generate(result, pdfA, pdfB);
        };
    }
}
