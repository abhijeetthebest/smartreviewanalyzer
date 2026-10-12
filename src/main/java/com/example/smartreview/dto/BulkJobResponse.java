package com.example.smartreview.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkJobResponse {
    private String jobId;
    private String status;
    private int totalReviews;
    private String statusUrl;
}
