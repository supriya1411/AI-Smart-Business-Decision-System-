package com.antigravity.logistics.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "text", nullable = false)
    private JsonNode waypoints;

    @Column(name = "total_distance_km", precision = 6, scale = 2)
    private BigDecimal totalDistanceKm;

    @Column(name = "estimated_time_min")
    private Integer estimatedTimeMin;

    @Column(length = 50)
    private String status;
}
