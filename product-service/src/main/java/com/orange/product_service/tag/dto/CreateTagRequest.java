package com.orange.product_service.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "Tag name is required")
        @Size(min = 2, max = 255, message = "Tag name must be between 2 and 255 characters")
        String name
) {
}