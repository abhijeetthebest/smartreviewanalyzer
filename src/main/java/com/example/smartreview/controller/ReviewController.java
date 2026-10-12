package com.example.smartreview.controller;

import com.example.smartreview.dto.*;
import com.example.smartreview.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Simple in-memory job tracker for demo purposes (replace with Redis/DB in production)
    private final Map<String, String> jobStatus = new ConcurrentHashMap<>();

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeSingle(@Valid @RequestBody AnalyzeRequest request) {
        AnalysisResponse response = reviewService.analyzeAndStore(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/bulk")
    public ResponseEntity<BulkJobResponse> analyzeBulk(@Valid @RequestBody BulkAnalyzeRequest request) {
        String jobId = "job_" + UUID.randomUUID().toString().substring(0, 8);
        jobStatus.put(jobId, "PROCESSING");

        // In a real system this would push to Kafka / async executor
        // For demo we just mark as accepted
        // You can enhance this later with @Async or Kafka

        BulkJobResponse response = BulkJobResponse.builder()
                .jobId(jobId)
                .status("ACCEPTED")
                .totalReviews(request.getReviews().size())
                .statusUrl("/api/v1/reviews/jobs/" + jobId)
                .build();

        // Simulate background completion
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                jobStatus.put(jobId, "COMPLETED");
            } catch (InterruptedException ignored) {}
        }).start();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        String status = jobStatus.getOrDefault(jobId, "NOT_FOUND");
        return ResponseEntity.ok(Map.of(
                "jobId", jobId,
                "status", status
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingest(@Valid @RequestBody AnalyzeRequest request) {
        // Simple ingest without full analysis (can call a lighter path)
        AnalysisResponse analysis = reviewService.analyzeAndStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "reviewId", analysis.getReviewId(),
                "status", "INGESTED",
                "message", "Review stored and embedded successfully"
        ));
    }
}
