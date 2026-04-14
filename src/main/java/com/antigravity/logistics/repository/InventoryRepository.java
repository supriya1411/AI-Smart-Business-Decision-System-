package com.antigravity.logistics.repository;

import com.antigravity.logistics.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p WHERE i.quantityOnHand < p.restockThreshold")
    List<Inventory> findAllLowStock();

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p WHERE i.quantityOnHand < p.restockThreshold AND i.warehouseZone = :zone")
    List<Inventory> findAllLowStockByZone(@Param("zone") String zone);
}
