package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandAnalyticsDTO {
    private Long productId;
    private String productName;
    private Integer periodWeeks;
    private Double currentVelocity;
    private String trend; // RISING, STABLE, DECLINING
    private Integer forecastedUnits;
}
