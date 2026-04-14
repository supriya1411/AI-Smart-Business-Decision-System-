package com.antigravity.logistics.service;

import com.antigravity.logistics.dto.SaleRequestDTO;
import com.antigravity.logistics.dto.SaleResponseDTO;
import com.antigravity.logistics.model.entity.Product;
import com.antigravity.logistics.model.entity.Sale;
import com.antigravity.logistics.repository.ProductRepository;
import com.antigravity.logistics.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SalesService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional
    public SaleResponseDTO recordSale(SaleRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Sale sale = Sale.builder()
                .product(product)
                .quantitySold(request.getQuantitySold())
                .saleDate(LocalDate.now())
                .revenue(request.getRevenue())
                .channel(request.getChannel())
                .build();

        sale = saleRepository.save(sale);
        
        // Asynchronously or synchronously update inventory
        inventoryService.decrementStock(product.getId(), request.getQuantitySold());

        return SaleResponseDTO.builder()
                .saleId(sale.getId())
                .productId(product.getId())
                .quantitySold(sale.getQuantitySold())
                .saleDate(sale.getSaleDate())
                .revenue(sale.getRevenue())
                .channel(sale.getChannel())
                .build();
    }
    @Transactional(readOnly = true)
    public java.util.List<com.antigravity.logistics.dto.TopProductDTO> getTopProducts(int limit, String period) {
        LocalDate startDate = null;
        if (period != null) {
            if ("week".equalsIgnoreCase(period)) startDate = LocalDate.now().minusWeeks(1);
            else if ("month".equalsIgnoreCase(period)) startDate = LocalDate.now().minusMonths(1);
            else if ("year".equalsIgnoreCase(period)) startDate = LocalDate.now().minusYears(1);
        }
        return saleRepository.findTopProducts(startDate, org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public com.antigravity.logistics.dto.RevenueReportDTO getRevenueReport(LocalDate from, LocalDate to, String groupBy) {
        java.util.List<Object[]> results;
        if ("channel".equalsIgnoreCase(groupBy)) {
            results = saleRepository.findRevenueGroupedByChannel(from, to);
        } else {
            results = saleRepository.findRevenueGroupedByDay(from, to);
        }

        java.math.BigDecimal totalRev = java.math.BigDecimal.ZERO;
        java.util.List<com.antigravity.logistics.dto.RevenueReportDTO.RevenueGroup> groups = new java.util.ArrayList<>();
        
        for (Object[] row : results) {
            String groupName = row[0] != null ? row[0].toString() : "Unknown";
            java.math.BigDecimal revenue = (java.math.BigDecimal) row[1];
            totalRev = totalRev.add(revenue);
            groups.add(com.antigravity.logistics.dto.RevenueReportDTO.RevenueGroup.builder()
                    .groupName(groupName)
                    .revenue(revenue)
                    .build());
        }

        return com.antigravity.logistics.dto.RevenueReportDTO.builder()
                .fromDate(from)
                .toDate(to)
                .groupedBy(groupBy)
                .totalRevenue(totalRev)
                .groups(groups)
                .build();
    }
}
