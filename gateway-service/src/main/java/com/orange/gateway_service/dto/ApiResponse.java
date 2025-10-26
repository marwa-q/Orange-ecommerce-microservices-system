package com.orange.gateway_service.dto;

import com.orange.gateway_service.util.RequestUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ServerWebExchange;

import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    private static MessageSource messageSource;

    public static void setMessageSource(MessageSource source) {
        messageSource = source;
    }

    public static <T> ApiResponse<T> success(String ky, T data, ServerWebExchange exchange) {
        String resolved = resolveMessage(ky, exchange);
        return new ApiResponse<T>(true, resolved, data);
    }

    public static <T> ApiResponse<T> success(String ky, ServerWebExchange exchange) {
        String resolved = resolveMessage(ky, exchange);
        return new ApiResponse<T>(true, resolved, null);
    }

    public static <T> ApiResponse<T> success(T data, ServerWebExchange exchange) {
        String resolved = resolveMessage("success", exchange);
        return new ApiResponse<T>(true, resolved, data);
    }

    public static <T> ApiResponse<T> error(String ky, ServerWebExchange exchange) {
        String resolved = resolveMessage(ky, exchange);
        return new ApiResponse<T>(false, resolved, null);
    }

    public static <T> ApiResponse<T> failure(String ky, ServerWebExchange exchange) {
        String resolved = resolveMessage(ky, exchange);
        return new ApiResponse<T>(false, resolved, null);
    }

    public boolean isSuccess() {
        return success;
    }

    private static String resolveMessage(String key, ServerWebExchange exchange) {
        String lang = RequestUtils.getHeaderValue(exchange, "Accept-Language", "en");
        Locale locale = Locale.forLanguageTag(lang);
        if (messageSource == null) {
            return key;
        }
        try {
            return messageSource.getMessage(key, null, key, locale);
        } catch (Exception ignored) {
            return key;
        }
    }
}