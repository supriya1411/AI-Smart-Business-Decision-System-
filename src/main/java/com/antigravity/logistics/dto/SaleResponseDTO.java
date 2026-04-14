package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SaleResponseDTO {
    private Long saleId;
    private Long productId;
    private Integer quantitySold;
    private LocalDate saleDate;
    private BigDecimal revenue;
    private String channel;
}
