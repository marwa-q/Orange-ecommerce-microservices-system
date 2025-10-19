package com.orange.product_service.product.service;

import com.orange.product_service.category.service.CategoryService;
import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.category.entity.Category;
import com.orange.product_service.event.LowStockEvent;
import com.orange.product_service.service.LowStockEventPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.product_service.product.dto.AddTagsToProductRequest;
import com.orange.product_service.product.dto.AddVariantRequest;
import com.orange.product_service.product.dto.CreateProductRequest;
import com.orange.product_service.product.dto.DeleteProductRequest;
import com.orange.product_service.product.dto.ProductDto;
import com.orange.product_service.product.dto.ProductPageDto;
import com.orange.product_service.product.dto.ProductVariant;
import com.orange.product_service.product.dto.RemoveTagsFromProductRequest;
import com.orange.product_service.product.dto.RemoveVariantRequest;
import com.orange.product_service.product.dto.UpdateProductRequest;
import com.orange.product_service.product.dto.UpdateVariantRequest;
import com.orange.product_service.product.entity.Product;
import com.orange.product_service.product.repo.ProductRepository;
import com.orange.product_service.tag.entity.Tag;
import com.orange.product_service.tag.repo.TagRepository;
import com.orange.product_service.tag.service.TagService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final MessageSource messageSource;
    private final LowStockEventPublisher lowStockEventPublisher;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, CategoryService categoryService,
                          MessageSource messageSource, LowStockEventPublisher lowStockEventPublisher,
                          TagRepository tagRepository, TagService tagService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.messageSource = messageSource;
        this.lowStockEventPublisher = lowStockEventPublisher;
        this.tagService = tagService;
        this.objectMapper = new ObjectMapper();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "deletedProducts"}, allEntries = true)
    public ApiResponse<ProductDto> createProduct(CreateProductRequest request , Locale locale) {
        try {
            Product product = new Product();
            Category category = categoryService.findByUuid(request.categoryId());
            product.setName(request.name());
            product.setPrice(request.price());
            product.setImage(request.image());
            product.setStock(request.stock());
            product.setCategory(category);

            Product savedProduct = productRepository.save(product);
            
            // Check for low stock after creating product
            checkAndPublishLowStockEvent(savedProduct);
            
            ProductDto productDto = convertToDto(savedProduct);

            String msg = messageSource.getMessage("product.created.success" , null , locale);
            return ApiResponse.success(msg, productDto);
        } catch (RuntimeException e) {
            // Handle category not found or other runtime exceptions
            String msg = messageSource.getMessage("product.created.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            String msg = messageSource.getMessage("product.created.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Update product function
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "deletedProducts", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> updateProduct(UpdateProductRequest request , Locale locale) {
        try {
            // Check if product exists
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            // Only update category if categoryId is provided
            if (request.categoryId() != null) {
                Category category = categoryService.findByUuid(request.categoryId());
                product.setCategory(category);
            }
            
            // Update other fields if provided
            if (request.name() != null)
                product.setName(request.name());
            if (request.price() != null)
                product.setPrice(request.price());
            if (request.image() != null)
                product.setImage(request.image());
            if (request.stock() != null)
                product.setStock(request.stock());

            Product savedProduct = productRepository.save(product);
            
            // Check for low stock after updating product
            checkAndPublishLowStockEvent(savedProduct);
            
            ProductDto productDto = convertToDto(savedProduct);

            String msg = messageSource.getMessage("product.updated.success" , null , locale);
            return ApiResponse.success(msg, productDto);
        } catch (RuntimeException e) {
            // Handle category not found or other runtime exceptions
            String msg = messageSource.getMessage("product.updated.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            String msg = messageSource.getMessage("product.updated.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get paginated active products
    @Cacheable(value = "products", key = "#page + '_' + #size")
    public ApiResponse<ProductPageDto> getAllProducts(Locale locale, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending()); // you can sort by any field
            Page<Product> productPage = productRepository.findActiveProducts(pageable);

            List<ProductDto> productDtos = productPage.getContent()
                    .stream()
                    .map(this::convertToDto)
                    .toList();

            // Create ProductPageDto with pagination information
            ProductPageDto productPageDto = new ProductPageDto(
                    productDtos,
                    productPage.getNumber(),
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getSize(),
                    productPage.hasNext(),
                    productPage.hasPrevious(),
                    productPage.isFirst(),
                    productPage.isLast()
            );

            String msg = messageSource.getMessage("product.list.success", null, locale);
            return ApiResponse.success(msg, productPageDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get paginated deleted products
    @Cacheable(value = "deletedProducts", key = "#page + '_' + #size")
    public ApiResponse<List<ProductDto>> getDeletedProducts(Locale locale, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending()); // you can sort by any field
            Page<Product> productPage = productRepository.findDeletedProducts(pageable);

            List<ProductDto> productDtos = productPage.getContent()
                    .stream()
                    .map(this::convertToDto)
                    .toList();

            String msg = messageSource.getMessage("product.list.success", null, locale);
            return ApiResponse.success(msg, productDtos);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get products by category
    @Cacheable(value = "productsByCategory", key = "#categoryUuid + '_' + #page + '_' + #size")
    public ApiResponse<ProductPageDto> getProductsByCategory(UUID categoryUuid, Locale locale, int page, int size) {
        try {
            // Verify category exists
            Category category = categoryService.findByUuid(categoryUuid);
            if (category == null) {
                String msg = messageSource.getMessage("category.not.found", null, locale);
                return ApiResponse.failure(msg);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<Product> productPage = productRepository.findByCategoryUuid(categoryUuid, pageable);

            List<ProductDto> productDtos = productPage.getContent()
                    .stream()
                    .map(this::convertToDto)
                    .toList();

            // Create ProductPageDto with pagination information
            ProductPageDto productPageDto = new ProductPageDto(
                    productDtos,
                    productPage.getNumber(),
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getSize(),
                    productPage.hasNext(),
                    productPage.hasPrevious(),
                    productPage.isFirst(),
                    productPage.isLast()
            );

            String msg = messageSource.getMessage("product.list.success", null, locale);
            return ApiResponse.success(msg, productPageDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get product by ID (public endpoint)
    @Cacheable(value = "productDetails", key = "#productId")
    public ApiResponse<ProductDto> getProductById(UUID productId, Locale locale) {
        try {
            Product product = productRepository.findByUuid(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            
            // Check if product is not deleted
            if (product.getIsDeleted()) {
                String msg = messageSource.getMessage("product.details.failure", null, locale);
                return ApiResponse.failure(msg);
            }
            
            ProductDto productDto = convertToDto(product);
            String msg = messageSource.getMessage("product.details.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.details.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Soft delete product
    @CacheEvict(value = {"products", "productsByCategory", "deletedProducts", "productDetails"}, allEntries = true)
    public ApiResponse<Void> softDeleteProduct(DeleteProductRequest request, Locale locale) {
        try{
            Product product = productRepository.findByUuid(request.productId()).orElseThrow();

            product.setIsDeleted(true);
            productRepository.save(product);

            String msg = messageSource.getMessage("product.deleted.success", null, locale);
            return ApiResponse.success(msg, null);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.deleted.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Activate product
    @CacheEvict(value = {"products", "productsByCategory", "deletedProducts", "productDetails"}, allEntries = true)
    public ApiResponse<Void> activateProduct(DeleteProductRequest request, Locale locale) {
        try{
            Product product = productRepository.findByUuid(request.productId()).orElseThrow();

            product.setIsDeleted(false);
            productRepository.save(product);

            String msg = messageSource.getMessage("product.activated.success", null, locale);
            return ApiResponse.success(msg, null);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.activated.failure" , null , locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Add tags to product
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> addTagsToProduct(AddTagsToProductRequest request, Locale locale) {
        try {
            // Find the product
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            // Check if product is not deleted
            if (product.getIsDeleted()) {
                String msg = messageSource.getMessage("product.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Find all tags
            List<Tag> tags = tagService.findByUuidIn(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
                String msg = messageSource.getMessage("product.tag.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Add tags to product
            for (Tag tag : tags) {
                if (!product.getTags().contains(tag)) {
                    product.getTags().add(tag);
                    tag.getProducts().add(product);
                }
            }
            
            Product savedProduct = productRepository.save(product);
            ProductDto productDto = convertToDto(savedProduct);
            
            String msg = messageSource.getMessage("product.tag.add.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.tag.add.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Remove tags from product
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> removeTagsFromProduct(RemoveTagsFromProductRequest request, Locale locale) {
        try {
            // Find the product
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            // Check if product is not deleted
            if (product.getIsDeleted()) {
                String msg = messageSource.getMessage("product.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Find all tags
            List<Tag> tags = tagService.findByUuidIn(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
                String msg = messageSource.getMessage("product.tag.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Remove tags from product
            for (Tag tag : tags) {
                product.getTags().remove(tag);
                tag.getProducts().remove(product);
            }
            
            Product savedProduct = productRepository.save(product);
            ProductDto productDto = convertToDto(savedProduct);
            
            String msg = messageSource.getMessage("product.tag.remove.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.tag.remove.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Check and publish low stock event if stock is below threshold
    private void checkAndPublishLowStockEvent(Product product) {
        final int LOW_STOCK_THRESHOLD = 10;
        
        if (product.getStock() != null && product.getStock() < LOW_STOCK_THRESHOLD) {
            LowStockEvent event = new LowStockEvent(
                product.getUuid(),
                product.getName(),
                product.getStock(),
                LOW_STOCK_THRESHOLD,
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getName() : "Unknown"
            );
            
            lowStockEventPublisher.publishLowStockEvent(event);
        }
    }

    // Variant Management Methods
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> addVariant(AddVariantRequest request, Locale locale) {
        try {
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            List<ProductVariant> variants = getVariantsFromProduct(product);
            
            // Create new variant
            ProductVariant newVariant = new ProductVariant();
            newVariant.setVariantId(java.util.UUID.randomUUID().toString());
            newVariant.setName(request.name());
            newVariant.setAttributes(request.attributes());
            newVariant.setPrice(request.price());
            newVariant.setStock(request.stock());
            newVariant.setSku(request.sku());
            newVariant.setImage(request.image());
            newVariant.setIsActive(request.isActive() != null ? request.isActive() : true);
            
            variants.add(newVariant);
            
            // Save variants back to product
            product.setVariantsJson(objectMapper.writeValueAsString(variants));
            Product savedProduct = productRepository.save(product);
            
            ProductDto productDto = convertToDto(savedProduct);
            String msg = messageSource.getMessage("product.variant.add.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.variant.add.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> updateVariant(UpdateVariantRequest request, Locale locale) {
        try {
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            List<ProductVariant> variants = getVariantsFromProduct(product);
            
            // Find and update variant
            boolean variantFound = false;
            for (ProductVariant variant : variants) {
                if (variant.getVariantId().equals(request.variantId())) {
                    if (request.name() != null) variant.setName(request.name());
                    if (request.attributes() != null) variant.setAttributes(request.attributes());
                    if (request.price() != null) variant.setPrice(request.price());
                    if (request.stock() != null) variant.setStock(request.stock());
                    if (request.sku() != null) variant.setSku(request.sku());
                    if (request.image() != null) variant.setImage(request.image());
                    if (request.isActive() != null) variant.setIsActive(request.isActive());
                    variantFound = true;
                    break;
                }
            }
            
            if (!variantFound) {
                String msg = messageSource.getMessage("product.variant.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Save variants back to product
            product.setVariantsJson(objectMapper.writeValueAsString(variants));
            Product savedProduct = productRepository.save(product);
            
            ProductDto productDto = convertToDto(savedProduct);
            String msg = messageSource.getMessage("product.variant.update.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.variant.update.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> removeVariant(RemoveVariantRequest request, Locale locale) {
        try {
            Product product = productRepository.findByUuid(request.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId()));
            
            List<ProductVariant> variants = getVariantsFromProduct(product);
            
            // Remove variant
            boolean removed = variants.removeIf(variant -> variant.getVariantId().equals(request.variantId()));
            
            if (!removed) {
                String msg = messageSource.getMessage("product.variant.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Save variants back to product
            product.setVariantsJson(objectMapper.writeValueAsString(variants));
            Product savedProduct = productRepository.save(product);
            
            ProductDto productDto = convertToDto(savedProduct);
            String msg = messageSource.getMessage("product.variant.remove.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.variant.remove.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }
    
    // Helper method to get variants from product
    private List<ProductVariant> getVariantsFromProduct(Product product) {
        try {
            if (product.getVariantsJson() == null || product.getVariantsJson().trim().isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return objectMapper.readValue(product.getVariantsJson(), new TypeReference<List<ProductVariant>>() {});
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    // Set stock for a product
    @CacheEvict(value = {"products", "productsByCategory", "productDetails"}, allEntries = true)
    public ApiResponse<ProductDto> setStock(UUID productId, int quantity, String action, Locale locale) {
        try {
            // Find the product
            Product product = productRepository.findByUuid(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            
            // Check if product is not deleted
            if (product.getIsDeleted()) {
                String msg = messageSource.getMessage("product.not.found", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Validate operation type
            if (!"increase".equalsIgnoreCase(action) && !"decrease".equalsIgnoreCase(action)) {
                String msg = messageSource.getMessage("product.stock.invalid.operation", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Validate quantity
            if (quantity <= 0) {
                String msg = messageSource.getMessage("product.stock.invalid.quantity", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Get current stock
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            int effectiveQuantityChange = "increase".equalsIgnoreCase(action) ? quantity : -quantity;
            int newStock = currentStock + effectiveQuantityChange;
            
            // Check if decreasing stock would result in negative stock
            if (newStock < 0) {
                String msg = messageSource.getMessage("product.stock.insufficient", null, locale);
                return ApiResponse.failure(msg);
            }
            
            // Update stock
            product.setStock(newStock);
            Product savedProduct = productRepository.save(product);
            
            // Check for low stock after updating
            checkAndPublishLowStockEvent(savedProduct);
            
            ProductDto productDto = convertToDto(savedProduct);
            
            String msg = messageSource.getMessage("product.stock.updated.success", null, locale);
            return ApiResponse.success(msg, productDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("product.stock.updated.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    public ApiResponse<String> getProductNameById(UUID id) {

        Optional<String> productName = productRepository.findNameByUuid(id);

        if(productName.isEmpty()){
            return new ApiResponse<String>(false, "Failed", null);
        }
        String name = productName.get();
        return new ApiResponse<String>(true, "Success", name);
    }

    // Convert entity to dto
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setUuid(product.getUuid());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImage(product.getImage());
        dto.setStock(product.getStock());
        dto.setIsDeleted(product.getIsDeleted());
        dto.setViewCount(product.getViewCount());
        dto.setRate(product.getRate());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Convert category
        if (product.getCategory() != null) {
            dto.setCategory(new ProductDto.CategorySummaryDto(
                    product.getCategory().getUuid(),
                    product.getCategory().getName()
            ));
        }
        
        // Convert tags
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(product.getTags().stream()
                    .map(tag -> new ProductDto.TagSummaryDto(
                            tag.getUuid(),
                            tag.getName()
                    ))
                    .toList());
        }
        
        // Convert reviews
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            dto.setReviews(product.getReviews().stream()
                    .map(review -> new ProductDto.ReviewSummaryDto(
                            review.getUuid(),
                            review.getUserId(),
                            review.getRate()
                    ))
                    .toList());
        }
        
        // Convert variants
        dto.setVariants(getVariantsFromProduct(product));
        
        return dto;
    }
}
