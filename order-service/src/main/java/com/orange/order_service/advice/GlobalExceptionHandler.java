package com.orange.order_service.advice;

import com.orange.order_service.common.service.MessageService;
import com.orange.order_service.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, Locale locale) {
        String msg = messageService.getMessage("error.generic", locale);
        return ResponseEntity.badRequest().body(ApiResponse.failure(msg + " : " + ex.getMessage()));
    }
}
