package com.antigravity.logistics.service;

import com.antigravity.logistics.dto.DecisionResponseDTO;
import com.antigravity.logistics.dto.DemandAnalyticsDTO;
import com.antigravity.logistics.model.entity.Inventory;
import com.antigravity.logistics.repository.InventoryRepository;
import com.antigravity.logistics.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DecisionEngine {

    private final InventoryRepository inventoryRepository;
    private final DemandAnalysisService demandAnalysisService;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public DecisionResponseDTO evaluate(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        DemandAnalyticsDTO demand = demandAnalysisService.analyse(productId, 8);
        Double avgSentiment = reviewRepository.getAverageSentimentScore(productId);
        long reviewCount = reviewRepository.countByProductId(productId);

        int currentStock = inventory.getQuantityOnHand();
        int threshold = inventory.getProduct().getRestockThreshold();
        String trend = demand.getTrend();
        double velocity = demand.getCurrentVelocity();
        // Assuming flat margin logic for demonstration
        double margin = 0.35; 

        List<DecisionResponseDTO.DecisionItem> decisions = new ArrayList<>();

        // Rule 1 & 2: Stock vs Trend
        if (currentStock < threshold) {
            if ("RISING".equals(trend)) {
                decisions.add(buildItem("RESTOCK_URGENT", "HIGH", "Stock " + currentStock + " < threshold " + threshold + " and trend is RISING"));
            } else {
                decisions.add(buildItem("RESTOCK", "MEDIUM", "Stock " + currentStock + " < threshold " + threshold));
            }
        }

        // Rule 3: Sentiment
        if (avgSentiment != null && avgSentiment < 3.0 && reviewCount > 5) {
            decisions.add(buildItem("REVIEW_QUALITY", "MEDIUM", "Avg sentiment " + String.format("%.1f", avgSentiment) + " < 3.0"));
        }

        // Rule 4 & 5: Velocity & Margin
        if (velocity < 0.7 && margin > 0.3) {
            decisions.add(buildItem("REDUCE_PRICE", "LOW", "Velocity " + String.format("%.2f", velocity) + " < 0.7 with healthy margin"));
        } else if (velocity > 1.5 && currentStock >= threshold) {
            decisions.add(buildItem("INCREASE_FORECAST", "LOW", "Sales velocity high with stock healthy"));
        }

        return DecisionResponseDTO.builder()
                .productId(productId)
                .productName(inventory.getProduct().getName())
                .timestamp(LocalDateTime.now())
                .stockStatus(currentStock < threshold ? "LOW" : "HEALTHY")
                .currentStock(currentStock)
                .restockThreshold(threshold)
                .demandTrend(trend)
                .salesVelocity(velocity)
                .averageSentiment(avgSentiment)
                .decisions(decisions)
                .routeOptimization(DecisionResponseDTO.RouteOptimization.builder()
                        .recommended(true)
                        .suggestedZones(List.of(inventory.getWarehouseZone()))
                        .build())
                .build();
    }

    private DecisionResponseDTO.DecisionItem buildItem(String action, String priority, String reason) {
        return DecisionResponseDTO.DecisionItem.builder()
                .action(action)
                .priority(priority)
                .reason(reason)
                .build();
    }
}
