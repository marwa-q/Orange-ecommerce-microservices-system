package com.orange.userservice.common.dto;

import com.orange.userservice.util.RequestUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
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
        log.info("Trying to resolve message");
        String lang = RequestUtils.getHeaderValue("Accept-Language", "en");
        Locale locale = Locale.forLanguageTag(lang);
        if (messageSource == null) {
            log.info("Message source is null");
            return key;
        }
        try {
            String resolved = messageSource.getMessage(key, null, key, locale);
            log.info("Message resolved successfully {}", resolved);
            return resolved;
        } catch (Exception ex) {
            log.info("Failed to resolve message , error: {}", ex.getMessage());
            return key;
        }
    }
}