package com.example.smartreview.repository;

import com.example.smartreview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);

    long countByProductId(String productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.rating IS NOT NULL")
    Double findAverageRatingByProductId(@Param("productId") String productId);

    @Query("SELECT r.sentiment, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.sentiment IS NOT NULL GROUP BY r.sentiment")
    List<Object[]> countBySentimentAndProductId(@Param("productId") String productId);
}
