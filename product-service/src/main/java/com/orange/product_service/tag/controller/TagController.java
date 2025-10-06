package com.orange.product_service.tag.controller;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.tag.dto.CreateTagRequest;
import com.orange.product_service.tag.dto.TagDto;
import com.orange.product_service.tag.service.TagService;
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

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/tags")
@Tag(name = "Tag Controller", description = "Tag management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new tag", description = "Create a new tag (Admin only)")
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Valid @RequestBody CreateTagRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<TagDto> response = tagService.createTag(request, locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tags", description = "Get list of all tags (Admin only)")
    public ResponseEntity<ApiResponse<List<TagDto>>> getAllTags(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {
        
        Locale locale = Locale.forLanguageTag(language);
        ApiResponse<List<TagDto>> response = tagService.getAllTags(locale);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}