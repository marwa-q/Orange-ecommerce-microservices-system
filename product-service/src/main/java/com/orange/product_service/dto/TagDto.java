package com.orange.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDto extends BaseDto {

    @NotBlank(message = "Tag name is required")
    @Size(max = 255, message = "Tag name must not exceed 255 characters")
    private String name;
}
