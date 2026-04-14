package com.antigravity.logistics.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RouteRequestDTO {

    private Long orderId;

    @NotEmpty(message = "Deliveries/Waypoints cannot be empty")
    private List<String> deliveryZones;
}
