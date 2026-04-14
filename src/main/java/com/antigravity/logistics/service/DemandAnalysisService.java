package com.antigravity.logistics.service;

import com.antigravity.logistics.ai.AiServiceClient;
import com.antigravity.logistics.dto.DemandAnalyticsDTO;
import com.antigravity.logistics.model.entity.Product;
import com.antigravity.logistics.repository.ProductRepository;
import com.antigravity.logistics.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DemandAnalysisService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final AiServiceClient aiServiceClient;

    @Transactional(readOnly = true)
    public DemandAnalyticsDTO analyse(Long productId, int windowWeeks) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        LocalDate startDate = LocalDate.now().minusWeeks(windowWeeks);
        List<Object[]> weeklyData = saleRepository.findWeeklySalesForProduct(productId, startDate);

        // Process data
        double currentWeekQty = 0;
        double priorWeeksSum = 0;
        int priorWeeksCount = 0;
        List<Integer> history = new ArrayList<>();

        if (!weeklyData.isEmpty()) {
            currentWeekQty = ((Number) weeklyData.get(0)[1]).doubleValue(); // most recent week (ordered desc)

            for (int i = 1; i < Math.min(5, weeklyData.size()); i++) { // prior 4 weeks
                priorWeeksSum += ((Number) weeklyData.get(i)[1]).doubleValue();
                priorWeeksCount++;
            }

            for (Object[] row : weeklyData) {
                history.add(0, ((Number) row[1]).intValue()); // reverse chron order for history
            }
        }

        double velocity = 0.0;
        if (priorWeeksCount > 0) {
            double avgPrior = priorWeeksSum / priorWeeksCount;
            velocity = avgPrior > 0 ? (currentWeekQty / avgPrior) : currentWeekQty;
        }

        String trend = "STABLE";
        if (velocity > 1.3) trend = "RISING";
        else if (velocity < 0.7) trend = "DECLINING";

        // Call AI for forecast
        Integer forecastedUnits = aiServiceClient.getDemandForecast(productId, history);
        if (forecastedUnits == null) {
            // Rule-based fallback
            double seasonalIndex = 1.05; // stub
            forecastedUnits = (int) (velocity * currentWeekQty * seasonalIndex);
        }

        return DemandAnalyticsDTO.builder()
                .productId(productId)
                .productName(product.getName())
                .periodWeeks(windowWeeks)
                .currentVelocity(velocity)
                .trend(trend)
                .forecastedUnits(forecastedUnits)
                .build();
    }
}
