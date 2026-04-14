package com.antigravity.logistics.controller;

import com.antigravity.logistics.dto.ReviewRequestDTO;
import com.antigravity.logistics.dto.ReviewResponseDTO;
import com.antigravity.logistics.dto.ReviewSummaryDTO;
import com.antigravity.logistics.repository.ReviewRepository;
import com.antigravity.logistics.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponseDTO submitReview(@Valid @RequestBody ReviewRequestDTO request) {
        return reviewService.processReview(request);
    }

    @GetMapping("/{productId}/summary")
    public ReviewSummaryDTO getSummary(@PathVariable Long productId) {
        long count = reviewRepository.countByProductId(productId);
        Double avgSentiment = reviewRepository.getAverageSentimentScore(productId);
        
        return ReviewSummaryDTO.builder()
                .productId(productId)
                .totalReviews(count)
                .averageRating(0.0) // Stub
                .averageSentimentScore(avgSentiment)
                .build();
    }
}
