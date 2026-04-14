package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal unitPrice;
    private Integer restockThreshold;
    private LocalDateTime createdAt;
}
