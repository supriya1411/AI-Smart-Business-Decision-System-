package com.antigravity.logistics.repository;

import com.antigravity.logistics.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
