package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryDTO {
    private Long productId;
    private Long totalReviews;
    private Double averageRating;
    private Double averageSentimentScore;
}
