package org.sainm.extractor;

import org.sainm.model.CompareOptions;
import org.sainm.model.PageContent;
import org.sainm.model.PdfSource;

import java.util.List;

interface ContentExtractor {
    List<PageContent> extract(PdfSource source, CompareOptions options);
}
