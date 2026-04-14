package com.antigravity.logistics.repository;

import com.antigravity.logistics.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query(value = "SELECT strftime('%W', sale_date) as weekId, SUM(quantity_sold) as totalQuantity " +
                   "FROM sales WHERE product_id = :productId AND sale_date >= :startDate " +
                   "GROUP BY strftime('%W', sale_date) ORDER BY strftime('%W', sale_date) DESC", nativeQuery = true)
    List<Object[]> findWeeklySalesForProduct(@Param("productId") Long productId, @Param("startDate") LocalDate startDate);

    List<Sale> findByProductId(Long productId);

    @Query("SELECT SUM(s.quantitySold) FROM Sale s WHERE s.product.id = :productId AND s.saleDate >= :startDate AND s.saleDate <= :endDate")
    Integer getTotalQuantitySold(@Param("productId") Long productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT new com.antigravity.logistics.dto.TopProductDTO(s.product.id, s.product.name, SUM(s.quantitySold)) " +
           "FROM Sale s WHERE s.saleDate >= :startDate OR cast(:startDate as date) IS NULL " +
           "GROUP BY s.product.id, s.product.name " +
           "ORDER BY SUM(s.quantitySold) DESC")
    List<com.antigravity.logistics.dto.TopProductDTO> findTopProducts(@Param("startDate") LocalDate startDate, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT s.saleDate, SUM(s.revenue) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate GROUP BY s.saleDate ORDER BY s.saleDate ASC")
    List<Object[]> findRevenueGroupedByDay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s.channel, SUM(s.revenue) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate GROUP BY s.channel ORDER BY SUM(s.revenue) DESC")
    List<Object[]> findRevenueGroupedByChannel(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
