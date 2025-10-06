package com.orange.product_service.tag.service;

import com.orange.product_service.dto.ApiResponse;
import com.orange.product_service.tag.dto.CreateTagRequest;
import com.orange.product_service.tag.dto.TagDto;
import com.orange.product_service.tag.entity.Tag;
import com.orange.product_service.tag.repo.TagRepository;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    private final MessageSource messageSource;

    public TagService(TagRepository tagRepository, MessageSource messageSource) {
        this.tagRepository = tagRepository;
        this.messageSource = messageSource;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TagDto> createTag(CreateTagRequest request, Locale locale) {
        try {
            // Check if tag with same name already exists
            if (tagRepository.findByName(request.name()).isPresent()) {
                String msg = messageSource.getMessage("tag.create.duplicate", null, locale);
                return ApiResponse.failure(msg + ": " + request.name());
            }
            
            Tag tag = new Tag();
            tag.setName(request.name());
            
            Tag savedTag = tagRepository.save(tag);
            TagDto tagDto = convertToDto(savedTag);
            
            String msg = messageSource.getMessage("tag.create.success", null, locale);
            return ApiResponse.success(msg, tagDto);
        } catch (Exception e) {
            String msg = messageSource.getMessage("tag.create.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    // List tags
    public ApiResponse<List<TagDto>> getAllTags(Locale locale) {
        try {
            List<Tag> tags = tagRepository.findAll();
            List<TagDto> tagDtos = tags.stream()
                    .map(this::convertToDto)
                    .toList();

            String msg = messageSource.getMessage("tag.list.success", null, locale);
            return ApiResponse.success(msg, tagDtos);
        } catch (Exception e) {
            String msg = messageSource.getMessage("tag.list.failure", null, locale);
            return ApiResponse.failure(msg + ": " + e.getMessage());
        }
    }

    private TagDto convertToDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setUuid(tag.getUuid());
        dto.setName(tag.getName());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        
        // Convert products if needed
        if (tag.getProducts() != null && !tag.getProducts().isEmpty()) {
            dto.setProducts(tag.getProducts().stream()
                    .map(product -> new TagDto.ProductSummaryDto(
                            product.getUuid(),
                            product.getName(),
                            product.getPrice().toString()
                    ))
                    .toList());
        }
        
        return dto;
    }

    public Tag findTagByUuid(UUID id) {
        return tagRepository.findByUuid(id).orElseThrow();
    }

    public List<Tag> findByUuidIn(List<UUID> list) {
        return tagRepository.findByUuidIn(list);
    }
}
