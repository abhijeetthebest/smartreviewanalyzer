package com.example.smartreview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "review_text", nullable = false, columnDefinition = "TEXT")
    private String reviewText;

    private Integer rating;

    private String userId;

    private String source;

    @Column(name = "sentiment")
    private String sentiment;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Column(name = "key_topics", columnDefinition = "TEXT")
    private String keyTopics;          // stored as JSON string for simplicity

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "actionable_insights", columnDefinition = "TEXT")
    private String actionableInsights; // stored as JSON string

    @Column(name = "confidence")
    private Double confidence;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
