package com.antigravity.logistics.service;

import com.antigravity.logistics.dto.InventoryResponseDTO;
import com.antigravity.logistics.dto.LowStockAlertDTO;
import com.antigravity.logistics.model.entity.Inventory;
import com.antigravity.logistics.model.entity.Product;
import com.antigravity.logistics.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void decrementStock(Long productId, Integer quantitySold) {
        inventoryRepository.findByProductId(productId).ifPresent(inventory -> {
            int newQuantity = inventory.getQuantityOnHand() - quantitySold;
            inventory.setQuantityOnHand(Math.max(0, newQuantity));
            inventoryRepository.save(inventory);
            checkStock(productId, inventory);
        });
    }

    public void checkStock(Long productId, Inventory inventory) {
        Product product = inventory.getProduct();
        if (inventory.getQuantityOnHand() < product.getRestockThreshold()) {
            log.info("Stock low for product id: {}. Current: {}, Threshold: {}",
                    productId, inventory.getQuantityOnHand(), product.getRestockThreshold());
            // An event could be emitted here if spring-event application was fully robust
        }
    }

    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional(readOnly = true)
    public void periodicStockSweep() {
        log.info("Running periodic stock sweep");
        List<Inventory> lowStocks = inventoryRepository.findAllLowStock();
        lowStocks.forEach(i -> checkStock(i.getProduct().getId(), i));
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertDTO> getAlertsByZone(String zone) {
        List<Inventory> alerts = (zone != null && !zone.isEmpty()) ?
                inventoryRepository.findAllLowStockByZone(zone) :
                inventoryRepository.findAllLowStock();

        return alerts.stream().map(i -> LowStockAlertDTO.builder()
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .currentStock(i.getQuantityOnHand())
                .threshold(i.getProduct().getRestockThreshold())
                .warehouseZone(i.getWarehouseZone())
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId).map(i -> 
            InventoryResponseDTO.builder()
                    .inventoryId(i.getId())
                    .productId(productId)
                    .productName(i.getProduct().getName())
                    .quantityOnHand(i.getQuantityOnHand())
                    .warehouseZone(i.getWarehouseZone())
                    .lowStock(i.getQuantityOnHand() < i.getProduct().getRestockThreshold())
                    .lastUpdated(i.getLastUpdated())
                    .build()
        ).orElseThrow(() -> new RuntimeException("Inventory not found for product id: " + productId));
    }
}
