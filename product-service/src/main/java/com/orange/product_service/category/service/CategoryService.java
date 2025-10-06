package com.orange.product_service.category.service;

import com.orange.product_service.category.dto.CategoryDto;
import com.orange.product_service.category.dto.CreateCategoryRequest;
import com.orange.product_service.category.dto.DeleteCategoryRequest;
import com.orange.product_service.category.dto.UpdateCategoryRequest;
import com.orange.product_service.category.entity.Category;
import com.orange.product_service.category.repo.CategoryRepository;
import com.orange.product_service.dto.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final MessageSource messageSource;

    public CategoryService(CategoryRepository categoryRepo, MessageSource messageSource) {
        this.categoryRepo = categoryRepo;
        this.messageSource = messageSource;
    }

    // Create category service
    public ApiResponse<CategoryDto> createCategory(CreateCategoryRequest request, Locale locale) {
        try {
            // Check if category with same name already exists
            if (categoryRepo.findByName(request.name()).isPresent()) {
                String msg = messageSource.getMessage("category.create.duplicate", null, locale);
                return ApiResponse.failure(msg + ": " + request.name());
            }
            
            Category category = new Category();
            category.setName(request.name());

            Category savedCategory = categoryRepo.save(category);
            CategoryDto categoryDto = convertToDto(savedCategory);

            String msg = messageSource.getMessage("category.created.success", null, locale);
            return ApiResponse.success(msg, categoryDto);
        } catch (Exception ex) {
            String msg = messageSource.getMessage("category.created.failure", null, locale);
            return ApiResponse.failure(msg + ": " + ex.getMessage());
        }
    }

    // Update category service
    public ApiResponse<CategoryDto> updateCategory(UpdateCategoryRequest request, Locale locale) {
        try {
            // Find the category
            Category category = categoryRepo.findByUuid(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.categoryId()));
            
            // Check if another category with same name already exists
            var existingCategory = categoryRepo.findByName(request.name());
            if (existingCategory.isPresent() && !existingCategory.get().getUuid().equals(request.categoryId())) {
                String msg = messageSource.getMessage("category.create.duplicate", null, locale);
                return ApiResponse.failure(msg + ": " + request.name());
            }
            
            category.setName(request.name());
            Category savedCategory = categoryRepo.save(category);
            CategoryDto categoryDto = convertToDto(savedCategory);

            String msg = messageSource.getMessage("category.updated.success", null, locale);
            return ApiResponse.success(msg, categoryDto);
        } catch (RuntimeException e) {
            String msg = messageSource.getMessage("category.updated.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception ex) {
            String msg = messageSource.getMessage("category.updated.failure", null, locale);
            return ApiResponse.failure(msg + ": " + ex.getMessage());
        }
    }

    // Delete category service
    public ApiResponse<Void> deleteCategory(DeleteCategoryRequest request, Locale locale) {
        try {
            // Find the category
            Category category = categoryRepo.findByUuid(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.categoryId()));
            
            // Check if category has products
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                String msg = messageSource.getMessage("category.delete.has.products", null, locale);
                return ApiResponse.failure(msg + ": " + category.getProducts().size() + " products found");
            }
            
            categoryRepo.delete(category);
            String msg = messageSource.getMessage("category.deleted.success", null, locale);
            return ApiResponse.success(msg, null);
        } catch (RuntimeException e) {
            String msg = messageSource.getMessage("category.deleted.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception ex) {
            String msg = messageSource.getMessage("category.deleted.failure", null, locale);
            return ApiResponse.failure(msg + ": " + ex.getMessage());
        }
    }

    // Get all categories
    public ApiResponse<List<CategoryDto>> getAllCategories(Locale locale) {
        try {
            List<Category> categories = categoryRepo.findAll();
            List<CategoryDto> categoryDtos = categories.stream()
                    .map(this::convertToDto)
                    .toList();
            
            String msg = messageSource.getMessage("category.list.success", null, locale);
            return ApiResponse.success(msg, categoryDtos);
        } catch (Exception e) {
            String msg = messageSource.getMessage("category.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // Get category by ID
    public ApiResponse<CategoryDto> getCategoryById(UUID categoryId, Locale locale) {
        try {
            Category category = categoryRepo.findByUuid(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
            
            CategoryDto categoryDto = convertToDto(category);
            String msg = messageSource.getMessage("category.details.success", null, locale);
            return ApiResponse.success(msg, categoryDto);
        } catch (RuntimeException e) {
            String msg = messageSource.getMessage("category.details.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        } catch (Exception e) {
            String msg = messageSource.getMessage("category.details.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    public Category findByUuid(UUID uuid) {
        return categoryRepo.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Category not found with UUID: " + uuid));
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setUuid(category.getUuid());
        dto.setName(category.getName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Convert products to ProductSummaryDto if needed
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            dto.setProducts(category.getProducts().stream()
                    .map(product -> new CategoryDto.ProductSummaryDto(
                            product.getUuid(),
                            product.getName()
                    ))
                    .toList());
        }
        
        return dto;
    }
}
