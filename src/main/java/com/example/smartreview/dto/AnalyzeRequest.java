package com.example.smartreview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeRequest {

    @NotBlank(message = "productId is required")
    private String productId;

    @NotBlank(message = "reviewText is required")
    @Size(min = 10, max = 5000, message = "reviewText must be between 10 and 5000 characters")
    private String reviewText;

    private Integer rating;

    private String userId;
}
