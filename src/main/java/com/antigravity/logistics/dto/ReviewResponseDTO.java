package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ReviewResponseDTO {
    private Long reviewId;
    private Long productId;
    private String reviewText;
    private Short rating;
    private BigDecimal sentimentScore;
    private LocalDate reviewDate;
}
