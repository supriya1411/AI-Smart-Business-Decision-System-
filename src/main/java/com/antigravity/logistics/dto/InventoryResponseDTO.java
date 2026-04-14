package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponseDTO {
    private Long inventoryId;
    private Long productId;
    private String productName;
    private Integer quantityOnHand;
    private String warehouseZone;
    private Boolean lowStock;
    private LocalDateTime lastUpdated;
}
