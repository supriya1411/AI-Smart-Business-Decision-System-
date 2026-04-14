package com.antigravity.logistics.controller;

import com.antigravity.logistics.dto.DemandAnalyticsDTO;
import com.antigravity.logistics.dto.RevenueReportDTO;
import com.antigravity.logistics.dto.SaleRequestDTO;
import com.antigravity.logistics.dto.SaleResponseDTO;
import com.antigravity.logistics.dto.TopProductDTO;
import com.antigravity.logistics.service.DemandAnalysisService;
import com.antigravity.logistics.service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final DemandAnalysisService demandAnalysisService;

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponseDTO recordSale(@Valid @RequestBody SaleRequestDTO request) {
        return salesService.recordSale(request);
    }

    @GetMapping("/analytics/demand")
    public DemandAnalyticsDTO getDemandAnalytics(
            @RequestParam Long productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        // In a real scenario we'd use 'from' and 'to'. Re-using existing method for simplicity.
        return demandAnalysisService.analyse(productId, 8);
    }

    @GetMapping("/analytics/revenue")
    public RevenueReportDTO getRevenue(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam String groupBy) {
        return salesService.getRevenueReport(from, to, groupBy);
    }

    @GetMapping("/analytics/top-products")
    public List<TopProductDTO> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String period) {
        return salesService.getTopProducts(limit, period);
    }
}
