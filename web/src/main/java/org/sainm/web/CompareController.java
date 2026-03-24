package org.sainm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sainm.model.*;
import org.sainm.pipeline.CompareProgressListener;
import org.sainm.pipeline.PdfComparator;
import org.sainm.spi.ReportRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compare")
public class CompareController {
    private final JobStore jobStore;

    public CompareController(JobStore jobStore) { this.jobStore = jobStore; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompareResult> syncCompare(
            @RequestPart("fileA") MultipartFile fileA,
            @RequestPart("fileB") MultipartFile fileB,
            @RequestPart(name = "options", required = false) String options) throws Exception {
        var opts = parseOptions(options);
        var request = new CompareRequest(
            new PdfSource.Bytes(fileA.getBytes()),
            new PdfSource.Bytes(fileB.getBytes()), opts);
        return ResponseEntity.ok(new PdfComparator().compare(request));
    }

    @PostMapping(path = "/artifact/{format}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> syncArtifact(
            @RequestPart("fileA") MultipartFile fileA,
            @RequestPart("fileB") MultipartFile fileB,
            @PathVariable String format,
            @RequestPart(name = "options", required = false) String options) throws Exception {
        var opts = parseOptions(options);
        var request = new CompareRequest(
            new PdfSource.Bytes(fileA.getBytes()),
            new PdfSource.Bytes(fileB.getBytes()), opts);
        var result = new PdfComparator().compare(request);
        var renderer = ServiceLoader.load(ReportRenderer.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(r -> r.formatId().equals(format))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok()
            .contentType(mediaTypeFor(format))
            .body(renderer.render(result, opts));
    }

    @PostMapping(path = "/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> asyncCompare(
            @RequestPart("fileA") MultipartFile fileA,
            @RequestPart("fileB") MultipartFile fileB,
            @RequestPart(name = "options", required = false) String options) throws Exception {
        if (!jobStore.tryAcquire()) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many concurrent jobs"));
        }
        var job = jobStore.create();
        byte[] bytesA = fileA.getBytes(), bytesB = fileB.getBytes();
        var opts = parseOptions(options);
        CompletableFuture.runAsync(() -> {
            try {
                job.setStatus(CompareJob.Status.RUNNING);
                var comparator = new PdfComparator();
                var result = comparator.compare(new CompareRequest(
                    new PdfSource.Bytes(bytesA), new PdfSource.Bytes(bytesB), opts));
                job.setResult(result);
                job.setStatus(CompareJob.Status.DONE);
                job.setCompletedAt(Instant.now());
            } catch (Exception e) {
                job.setStatus(CompareJob.Status.FAILED);
                job.setErrorMessage(e.getMessage());
            } finally {
                jobStore.release();
            }
        });
        return ResponseEntity.accepted().body(Map.of(
            "jobId", job.getJobId(),
            "statusUrl", "/api/compare/" + job.getJobId() + "/status"));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<?> status(@PathVariable String jobId) {
        return jobStore.get(jobId)
            .map(job -> {
                var payload = new java.util.LinkedHashMap<String, Object>();
                payload.put("jobId", job.getJobId());
                payload.put("status", job.getStatus());
                payload.put("progress", job.getProgress());
                payload.put("warnings", job.getResult() != null ? job.getResult().getWarnings() : java.util.List.of());
                if (job.getResult() != null && job.getResult().getImageComparisonSummary() != null) {
                    payload.put("imageComparisonSummary", job.getResult().getImageComparisonSummary());
                }
                return ResponseEntity.ok((Object) payload);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{jobId}/report/{format}")
    public ResponseEntity<byte[]> report(@PathVariable String jobId,
                                          @PathVariable String format) {
        return jobStore.get(jobId)
            .filter(j -> j.getStatus() == CompareJob.Status.DONE)
            .map(job -> {
                var renderer = ServiceLoader.load(ReportRenderer.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .filter(r -> r.formatId().equals(format))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                byte[] output = renderer.render(job.getResult(), CompareOptions.defaults());
                return ResponseEntity.ok()
                    .contentType(mediaTypeFor(format))
                    .body(output);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    private MediaType mediaTypeFor(String format) {
        return switch (format) {
            case "json" -> MediaType.APPLICATION_JSON;
            case "html" -> MediaType.TEXT_HTML;
            case "pdf-annotated", "pdf-image-marked" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private CompareOptions parseOptions(String json) {
        if (json == null || json.isBlank()) return CompareOptions.defaults();
        try {
            return new ObjectMapper().readValue(json, CompareOptions.class);
        } catch (Exception e) {
            return CompareOptions.defaults();
        }
    }
}
