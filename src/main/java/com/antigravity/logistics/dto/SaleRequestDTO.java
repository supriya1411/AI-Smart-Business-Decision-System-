package com.antigravity.logistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity sold is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantitySold;

    @NotNull(message = "Revenue is required")
    @Min(value = 0, message = "Revenue cannot be negative")
    private BigDecimal revenue;

    private String channel;
}
