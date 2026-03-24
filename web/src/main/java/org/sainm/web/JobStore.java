package org.sainm.web;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Component
public class JobStore {
    private static final int MAX_CONCURRENT = 10;
    private static final Duration TTL = Duration.ofMinutes(30);
    private final ConcurrentHashMap<String, CompareJob> jobs = new ConcurrentHashMap<>();
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT);

    public CompareJob create() {
        var job = new CompareJob(UUID.randomUUID().toString());
        jobs.put(job.getJobId(), job);
        return job;
    }

    public Optional<CompareJob> get(String jobId) { return Optional.ofNullable(jobs.get(jobId)); }

    public boolean tryAcquire() { return semaphore.tryAcquire(); }
    public void release() { semaphore.release(); }

    @Scheduled(fixedDelay = 60_000)
    public void evictExpired() {
        var cutoff = Instant.now().minus(TTL);
        jobs.entrySet().removeIf(e -> e.getValue().getCreatedAt().isBefore(cutoff));
    }
}
