package com.example.smartreview.service;

import com.example.smartreview.dto.AnalysisResponse;
import com.example.smartreview.dto.AnalyzeRequest;
import com.example.smartreview.dto.ProductInsightsResponse;
import com.example.smartreview.entity.Review;
import com.example.smartreview.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RagService ragService;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisResponse analyzeAndStore(AnalyzeRequest request) {
        // 1. Run RAG analysis
        AnalysisResponse analysis = ragService.analyze(request.getReviewText(), request.getProductId());

        // 2. Persist review + analysis
        Review review = Review.builder()
                .productId(request.getProductId())
                .reviewText(request.getReviewText())
                .rating(request.getRating())
                .userId(request.getUserId())
                .sentiment(analysis.getSentiment())
                .sentimentScore(analysis.getSentimentScore())
                .keyTopics(toJson(analysis.getKeyTopics()))
                .summary(analysis.getSummary())
                .actionableInsights(toJson(analysis.getActionableInsights()))
                .confidence(analysis.getConfidence())
                .build();

        Review saved = reviewRepository.save(review);

        // 3. Also store in vector store for future retrieval
        Document doc = new Document(
                request.getReviewText(),
                Map.of(
                        "productId", request.getProductId(),
                        "reviewId", saved.getId().toString(),
                        "rating", request.getRating() != null ? request.getRating().toString() : "0",
                        "sentiment", analysis.getSentiment()
                )
        );
        vectorStore.add(List.of(doc));

        analysis.setReviewId(saved.getId());
        return analysis;
    }

    @Cacheable(value = "productInsights", key = "#productId")
    public ProductInsightsResponse getProductInsights(String productId) {
        long total = reviewRepository.countByProductId(productId);
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);

        List<Object[]> sentimentCounts = reviewRepository.countBySentimentAndProductId(productId);
        Map<String, Double> sentimentMap = new HashMap<>();
        long totalWithSentiment = sentimentCounts.stream().mapToLong(r -> (Long) r[1]).sum();

        for (Object[] row : sentimentCounts) {
            String sentiment = (String) row[0];
            long count = (Long) row[1];
            double pct = totalWithSentiment == 0 ? 0 : (count * 100.0) / totalWithSentiment;
            sentimentMap.put(sentiment.toLowerCase(), Math.round(pct * 10.0) / 10.0);
        }

        // Simple topic extraction from stored data (in real system you would use more sophisticated aggregation)
        List<Review> recent = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        Map<String, Long> topicFreq = new HashMap<>();
        for (Review r : recent) {
            List<String> topics = fromJsonList(r.getKeyTopics());
            for (String t : topics) {
                topicFreq.merge(t, 1L, Long::sum);
            }
        }

        List<String> topPositive = topicFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        List<ProductInsightsResponse.TrendingIssue> trending = topicFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> ProductInsightsResponse.TrendingIssue.builder()
                        .topic(e.getKey())
                        .mentionCount(e.getValue())
                        .trend("STABLE")
                        .build())
                .toList();

        return ProductInsightsResponse.builder()
                .productId(productId)
                .totalReviewsAnalyzed(total)
                .overallSentiment(sentimentMap)
                .topPositiveTopics(topPositive)
                .topNegativeTopics(Collections.emptyList()) // can be enhanced
                .trendingIssues(trending)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
