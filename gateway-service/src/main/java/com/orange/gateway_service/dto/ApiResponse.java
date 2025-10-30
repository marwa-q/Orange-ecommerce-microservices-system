package com.orange.gateway_service.dto;

import com.orange.gateway_service.util.RequestUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.MessageSource;
import reactor.core.publisher.Mono;

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

    public static <T> Mono<ApiResponse<T>> success(String key, T data) {
        return resolveMessage(key)
                .map(resolved -> new ApiResponse<>(true, resolved, data));
    }

    public static <T> Mono<ApiResponse<T>> success(String key) {
        return resolveMessage(key)
                .map(resolved -> new ApiResponse<>(true, resolved, null));
    }

    public static <T> Mono<ApiResponse<T>> success(T data) {
        return resolveMessage("success")
                .map(resolved -> new ApiResponse<>(true, resolved, data));
    }

    public static <T> Mono<ApiResponse<T>> error(String key) {
        return resolveMessage(key)
                .map(resolved -> new ApiResponse<>(false, resolved, null));
    }

    public static <T> Mono<ApiResponse<T>> failure(String key) {
        return resolveMessage(key)
                .map(resolved -> new ApiResponse<>(false, resolved, null));
    }

    private static Mono<String> resolveMessage(String key) {
        return RequestUtils.getHeaderValue("Accept-Language", "en")
                .map(Locale::forLanguageTag)
                .flatMap(locale -> {
                    if (messageSource == null) {
                        System.out.println("Message source is null");
                        return Mono.just(key);
                    }
                    try {
                        String resolved = messageSource.getMessage(key, null, key, locale);
                        return Mono.just(resolved);
                    } catch (Exception ex) {
                        return Mono.just(ex.getMessage());
                    }
                });
    }
}
