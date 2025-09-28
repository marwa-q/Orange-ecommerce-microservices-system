package com.orange.product_service.controller;

import com.orange.product_service.dto.ProductDto;
import com.orange.product_service.entity.Product;
import com.orange.product_service.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Controller", description = "Product management APIs")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of all active products")
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findActiveProducts(pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Increment view count
        product.incrementViewCount();
        productRepository.save(product);
        
        return ResponseEntity.ok(convertToDto(product));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve products filtered by category")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @PathVariable String categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam String name, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/most-viewed")
    @Operation(summary = "Get most viewed products", description = "Retrieve products sorted by view count")
    public ResponseEntity<Page<ProductDto>> getMostViewedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findMostViewedProducts(pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/newest")
    @Operation(summary = "Get newest products", description = "Retrieve products sorted by creation date")
    public ResponseEntity<Page<ProductDto>> getNewestProducts(Pageable pageable) {
        Page<Product> products = productRepository.findNewestProducts(pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated products", description = "Retrieve products sorted by rating")
    public ResponseEntity<Page<ProductDto>> getTopRatedProducts(Pageable pageable) {
        Page<Product> products = productRepository.findTopRatedProducts(pageable);
        Page<ProductDto> productDtos = products.map(this::convertToDto);
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low stock")
    public ResponseEntity<List<ProductDto>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        List<ProductDto> productDtos = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the product service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Product service is running!");
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImage(product.getImage());
        dto.setStock(product.getStock());
        dto.setIsDeleted(product.getIsDeleted());
        dto.setViewCount(product.getViewCount());
        dto.setRate(product.getRate());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        return dto;
    }
}