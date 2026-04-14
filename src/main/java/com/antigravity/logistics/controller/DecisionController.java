package com.antigravity.logistics.controller;

import com.antigravity.logistics.dto.DecisionResponseDTO;
import com.antigravity.logistics.dto.OptimisedRouteDTO;
import com.antigravity.logistics.dto.RouteRequestDTO;
import com.antigravity.logistics.service.DecisionEngine;
import com.antigravity.logistics.service.RouteOptimizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionEngine decisionEngine;
    private final RouteOptimizationService routeOptimizationService;

    @GetMapping("/decisions/{productId}")
    public DecisionResponseDTO getDecision(@PathVariable Long productId) {
        return decisionEngine.evaluate(productId);
    }

    @GetMapping("/decisions/bulk")
    public List<DecisionResponseDTO> getBulkDecisions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String zone) {
        // Stub implementation, would map product categories to loop DecisionEngine
        return List.of();
    }

    @PostMapping("/routes/optimize")
    public OptimisedRouteDTO optimizeRoute(@Valid @RequestBody RouteRequestDTO request) {
        return routeOptimizationService.optimizeRoute(request);
    }
}
