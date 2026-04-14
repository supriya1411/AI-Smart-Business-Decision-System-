package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowStockAlertDTO {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer threshold;
    private String warehouseZone;
}
