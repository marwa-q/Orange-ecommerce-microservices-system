package com.orange.order_service.common.dto;

import com.orange.order_service.util.RequestUtils;
import org.springframework.context.MessageSource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public static <T> ApiResponse<T> success(String ky, T data) {
        String resolved = resolveMessage(ky);
        return new ApiResponse<T>(true, resolved, data);
    }

    public static <T> ApiResponse<T> success(String ky) {
        String resolved = resolveMessage(ky);
        return new ApiResponse<T>(true, resolved, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        String resolved = resolveMessage("success");
        return new ApiResponse<T>(true, resolved, data);
    }

    public static <T> ApiResponse<T> error(String ky) {
        String resolved = resolveMessage(ky);
        return new ApiResponse<T>(false, resolved, null);
    }

    public static <T> ApiResponse<T> failure(String ky) {
        String resolved = resolveMessage(ky);
        return new ApiResponse<T>(false, resolved, null);
    }

    public boolean isSuccess() {
        return success;
    }

    private static String resolveMessage(String key) {
        String lang = RequestUtils.getHeaderValue("Accept-Language", "en");
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