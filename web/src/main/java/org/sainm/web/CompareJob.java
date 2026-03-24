package org.sainm.web;

import org.sainm.model.CompareResult;

import java.time.Instant;

public final class CompareJob {
    public enum Status { PENDING, RUNNING, DONE, FAILED }

    private final String jobId;
    private volatile Status status = Status.PENDING;
    private volatile double progress = 0.0;
    private volatile CompareResult result;
    private volatile String errorMessage;
    private final Instant createdAt = Instant.now();
    private volatile Instant completedAt;

    public CompareJob(String jobId) { this.jobId = jobId; }

    public String getJobId() { return jobId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
    public CompareResult getResult() { return result; }
    public void setResult(CompareResult result) { this.result = result; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
