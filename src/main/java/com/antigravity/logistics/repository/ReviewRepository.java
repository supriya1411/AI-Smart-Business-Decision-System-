package com.antigravity.logistics.repository;

import com.antigravity.logistics.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductId(Long productId);

    @Query("SELECT AVG(r.sentimentScore) FROM Review r WHERE r.product.id = :productId AND r.sentimentScore IS NOT NULL")
    Double getAverageSentimentScore(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
}
