package com.orange.product_service.product.controller;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.product.dto.AddTagsToProductRequest;
import com.orange.product_service.product.dto.AddVariantRequest;
import com.orange.product_service.product.dto.CreateProductRequest;
import com.orange.product_service.product.dto.DeleteProductRequest;
import com.orange.product_service.product.dto.ProductDto;
import com.orange.product_service.product.dto.ProductPageDto;
import com.orange.product_service.product.dto.RemoveTagsFromProductRequest;
import com.orange.product_service.product.dto.RemoveVariantRequest;
import com.orange.product_service.product.dto.UpdateProductRequest;
import com.orange.product_service.product.dto.UpdateVariantRequest;
import com.orange.product_service.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Controller", description = "Product management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new product", description = "Create a new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.createProduct(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Update product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.updateProduct(request, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete product", description = "Soft delete product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> softDeleteProduct(
            @Valid @RequestBody DeleteProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<Void> response = productService.softDeleteProduct(request, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate product", description = "Activate product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> activateProduct(
            @Valid @RequestBody DeleteProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<Void> response = productService.activateProduct(request, locale);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Get all products", description = "Get paginated list of all products with page information")
    public ResponseEntity<ApiResponse<ProductPageDto>> getAllProducts(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductPageDto> response = productService.getAllProducts(locale, page, size);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list-deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get deleted products", description = "Get list of deleted products (Admin only)")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getDeletedProducts(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<List<ProductDto>> response = productService.getDeletedProducts(locale, page, size);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/category/{categoryUuid}")
    @Operation(summary = "Get products by category", description = "Get paginated list of products filtered by category")
    public ResponseEntity<ApiResponse<ProductPageDto>> getProductsByCategory(
            @PathVariable UUID categoryUuid,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductPageDto> response = productService.getProductsByCategory(categoryUuid, locale, page, size);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Public endpoint for product details (no authentication required)
    @GetMapping("/{productId}")
    @Operation(summary = "Get product details", description = "Get product details by ID (Public endpoint)")
    public ResponseEntity<ApiResponse<ProductDto>> getProductDetails(
            @PathVariable UUID productId,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.getProductById(productId, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/add-tags")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add tags to product", description = "Add tags to a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> addTagsToProduct(
            @Valid @RequestBody AddTagsToProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.addTagsToProduct(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/remove-tags")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove tags from product", description = "Remove tags from a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> removeTagsFromProduct(
            @Valid @RequestBody RemoveTagsFromProductRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.removeTagsFromProduct(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/add-variant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add variant to product", description = "Add a new variant to a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> addVariant(
            @Valid @RequestBody AddVariantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.addVariant(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-variant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product variant", description = "Update an existing product variant (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> updateVariant(
            @Valid @RequestBody UpdateVariantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.updateVariant(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/remove-variant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove product variant", description = "Remove a variant from a product (Admin only)")
    public ResponseEntity<ApiResponse<ProductDto>> removeVariant(
            @Valid @RequestBody RemoveVariantRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<ProductDto> response = productService.removeVariant(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}
