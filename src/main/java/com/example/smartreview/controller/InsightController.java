package com.example.smartreview.controller;

import com.example.smartreview.dto.ProductInsightsResponse;
import com.example.smartreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class InsightController {

    private final ReviewService reviewService;

    @GetMapping("/{productId}/insights")
    public ResponseEntity<ProductInsightsResponse> getInsights(@PathVariable String productId) {
        ProductInsightsResponse insights = reviewService.getProductInsights(productId);
        return ResponseEntity.ok(insights);
    }
}
