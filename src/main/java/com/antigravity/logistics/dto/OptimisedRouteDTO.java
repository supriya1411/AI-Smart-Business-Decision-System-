package com.antigravity.logistics.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OptimisedRouteDTO {
    private Long orderId;
    private JsonNode waypoints;
    private BigDecimal totalDistanceKm;
    private Integer estimatedTimeMin;
}
