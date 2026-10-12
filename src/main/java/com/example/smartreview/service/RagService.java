package com.example.smartreview.service;

import com.example.smartreview.dto.AnalysisResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.rag.top-k:6}")
    private int topK;

    @Value("${app.rag.similarity-threshold:0.72}")
    private double similarityThreshold;

    @CircuitBreaker(name = "llmService", fallbackMethod = "fallbackAnalyze")
    @Retry(name = "llmService")
    public AnalysisResponse analyze(String reviewText, String productId) {
        long start = System.currentTimeMillis();

        // 1. Retrieve similar reviews / context
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(reviewText)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .filterExpression("productId == '" + productId + "'")
                        .build()
        );

        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        if (context.isBlank()) {
            context = "No similar historical reviews found for this product.";
        }

        // 2. Build prompt
        String systemPrompt = """
                You are an expert product review analyst.
                Analyze the new customer review using the provided historical context.
                Always respond with valid JSON only, no markdown, no extra text.
                JSON schema:
                {
                  "sentiment": "POSITIVE | NEGATIVE | NEUTRAL | MIXED",
                  "sentimentScore": 0.0 to 1.0,
                  "keyTopics": ["topic1", "topic2"],
                  "summary": "one or two sentence summary",
                  "actionableInsights": ["insight1", "insight2"],
                  "confidence": 0.0 to 1.0
                }
                """;

        String userPrompt = """
                Historical Context:
                %s

                New Review to analyze:
                %s
                """.formatted(context, reviewText);

        // 3. Call LLM
        ChatClient chatClient = chatClientBuilder.build();
        String rawResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // 4. Parse response
        Map<String, Object> parsed = parseJson(rawResponse);

        long duration = System.currentTimeMillis() - start;

        return AnalysisResponse.builder()
                .productId(productId)
                .sentiment((String) parsed.getOrDefault("sentiment", "NEUTRAL"))
                .sentimentScore(toDouble(parsed.get("sentimentScore")))
                .keyTopics(toStringList(parsed.get("keyTopics")))
                .summary((String) parsed.getOrDefault("summary", ""))
                .actionableInsights(toStringList(parsed.get("actionableInsights")))
                .confidence(toDouble(parsed.get("confidence")))
                .processingTimeMs(duration)
                .build();
    }

    // Fallback when circuit is open or LLM fails
    private AnalysisResponse fallbackAnalyze(String reviewText, String productId, Throwable t) {
        log.warn("RAG analysis fallback triggered for product {}: {}", productId, t.getMessage());
        return AnalysisResponse.builder()
                .productId(productId)
                .sentiment("NEUTRAL")
                .sentimentScore(0.5)
                .keyTopics(List.of("Analysis temporarily unavailable"))
                .summary("We could not analyze this review right now. Please try again later.")
                .actionableInsights(Collections.emptyList())
                .confidence(0.0)
                .processingTimeMs(0L)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        try {
            // Clean possible markdown code fences
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", raw, e);
            return Map.of(
                    "sentiment", "NEUTRAL",
                    "sentimentScore", 0.5,
                    "keyTopics", List.of(),
                    "summary", "Could not parse analysis",
                    "actionableInsights", List.of(),
                    "confidence", 0.0
            );
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.5;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.5;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of(value.toString());
    }
}
