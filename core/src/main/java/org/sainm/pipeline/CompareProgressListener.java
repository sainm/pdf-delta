package org.sainm.pipeline;

import org.sainm.model.CompareResult;

public interface CompareProgressListener {
    void onProgress(int processedPages, int totalPages, double fraction);
    void onComplete(CompareResult result);
    void onError(Exception e);
}
