package com.antigravity.logistics.controller;

import com.antigravity.logistics.dto.InventoryResponseDTO;
import com.antigravity.logistics.dto.LowStockAlertDTO;
import com.antigravity.logistics.dto.ProductRequestDTO;
import com.antigravity.logistics.dto.ProductResponseDTO;
import com.antigravity.logistics.model.entity.Product;
import com.antigravity.logistics.repository.ProductRepository;
import com.antigravity.logistics.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductRequestDTO request) {
        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .unitPrice(request.getUnitPrice())
                .restockThreshold(request.getRestockThreshold() != null ? request.getRestockThreshold() : 20)
                .build();
        product = productRepository.save(product);

        return mapToDto(product);
    }

    @GetMapping("/products/{id}")
    public ProductResponseDTO getProduct(@PathVariable Long id) {
        return productRepository.findById(id).map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @GetMapping("/products")
    public Page<ProductResponseDTO> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> productPage = (category != null && !category.isEmpty()) ?
                productRepository.findByCategory(category, pageRequest) :
                productRepository.findAll(pageRequest);

        return productPage.map(this::mapToDto);
    }

    // Inventory Endpoints mapped under Products based on logical grouping
    
    @PutMapping("/inventory/{productId}/stock")
    public InventoryResponseDTO updateStock(@PathVariable Long productId, @RequestBody java.util.Map<String, Object> body) {
        // Simple update delta proxy
        Integer delta = (Integer) body.get("quantity_delta");
        // In a real app we'd map this, but for simplicity:
        inventoryService.decrementStock(productId, -delta); // decrement a negative = increment
        return inventoryService.getInventory(productId);
    }

    @GetMapping("/inventory/{productId}")
    public InventoryResponseDTO getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId);
    }

    @GetMapping("/inventory/alerts")
    public List<LowStockAlertDTO> getAlerts(@RequestParam(required = false) String zone) {
        return inventoryService.getAlertsByZone(zone);
    }

    private ProductResponseDTO mapToDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .restockThreshold(product.getRestockThreshold())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
