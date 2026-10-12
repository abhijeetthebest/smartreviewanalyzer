package com.example.smartreview.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductInsightsResponse {

    private String productId;
    private long totalReviewsAnalyzed;
    private Map<String, Double> overallSentiment; // positive, neutral, negative percentages
    private List<String> topPositiveTopics;
    private List<String> topNegativeTopics;
    private List<TrendingIssue> trendingIssues;
    private Double averageRating;

    @Data
    @Builder
    public static class TrendingIssue {
        private String topic;
        private long mentionCount;
        private String trend; // INCREASING, STABLE, DECREASING
    }
}
