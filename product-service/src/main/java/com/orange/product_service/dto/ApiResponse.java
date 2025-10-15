package com.orange.product_service.dto;

import com.orange.product_service.utils.RequestUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, "Success", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<T>(true, message, null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(false, message, null);
    }
    
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<T>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }
}