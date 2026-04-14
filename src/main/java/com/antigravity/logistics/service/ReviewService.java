package com.antigravity.logistics.service;

import com.antigravity.logistics.ai.AiServiceClient;
import com.antigravity.logistics.dto.ReviewRequestDTO;
import com.antigravity.logistics.dto.ReviewResponseDTO;
import com.antigravity.logistics.model.entity.Product;
import com.antigravity.logistics.model.entity.Review;
import com.antigravity.logistics.repository.ProductRepository;
import com.antigravity.logistics.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AiServiceClient aiServiceClient;

    @Transactional
    public ReviewResponseDTO processReview(ReviewRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Double sentimentData = aiServiceClient.getSentiment(request.getReviewText(), request.getProductId());
        BigDecimal sentimentScore = sentimentData != null ? BigDecimal.valueOf(sentimentData) : null;

        Review review = Review.builder()
                .product(product)
                .reviewText(request.getReviewText())
                .rating(request.getRating())
                .sentimentScore(sentimentScore)
                .reviewDate(LocalDate.now())
                .build();

        review = reviewRepository.save(review);

        return ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .productId(product.getId())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .sentimentScore(review.getSentimentScore())
                .reviewDate(review.getReviewDate())
                .build();
    }
}
