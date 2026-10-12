package com.example.smartreview.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AnalysisResponse {

    private UUID reviewId;
    private String productId;
    private String sentiment;
    private Double sentimentScore;
    private List<String> keyTopics;
    private String summary;
    private List<String> actionableInsights;
    private Double confidence;
    private Long processingTimeMs;
}
