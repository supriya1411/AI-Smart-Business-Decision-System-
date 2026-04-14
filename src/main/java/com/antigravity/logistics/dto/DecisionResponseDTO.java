package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DecisionResponseDTO {
    private Long productId;
    private String productName;
    private LocalDateTime timestamp;
    private String stockStatus;
    private Integer currentStock;
    private Integer restockThreshold;
    private String demandTrend;
    private Double salesVelocity;
    private Double averageSentiment;
    private List<DecisionItem> decisions;
    private RouteOptimization routeOptimization;

    @Data
    @Builder
    public static class DecisionItem {
        private String action;
        private String priority;
        private String reason;
    }

    @Data
    @Builder
    public static class RouteOptimization {
        private Boolean recommended;
        private List<String> suggestedZones;
    }
}
