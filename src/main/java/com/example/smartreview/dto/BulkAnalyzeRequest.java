package com.example.smartreview.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkAnalyzeRequest {

    @NotBlank
    private String productId;

    @NotEmpty
    @Size(max = 100, message = "Maximum 100 reviews allowed per request")
    @Valid
    private List<ReviewItem> reviews;

    @Data
    public static class ReviewItem {
        @NotBlank
        private String reviewText;
        private Integer rating;
        private String userId;
    }
}
