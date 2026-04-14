package com.antigravity.logistics.repository;

import com.antigravity.logistics.model.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, Long> {
    Optional<DeliveryRoute> findByOrderId(Long orderId);
}
