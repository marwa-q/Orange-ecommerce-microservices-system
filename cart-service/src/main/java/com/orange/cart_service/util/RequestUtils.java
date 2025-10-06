package com.orange.cart_service.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {

    public static String getHeaderValue(String headerName, String defaultValue) {
        if (headerName == null || headerName.isBlank()) {
            throw new IllegalArgumentException("Header name must not be null or blank");
        }

        try {
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (!(requestAttributes instanceof ServletRequestAttributes attributes)) {
                return defaultValue;
            }

            var request = attributes.getRequest();
            String headerValue = request.getHeader(headerName);

            // Case-insensitive check if header not found (optional)
            if (headerValue == null) {
                var headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        if (headerName.equalsIgnoreCase(name)) {
                            headerValue = request.getHeader(name);
                            break;
                        }
                    }
                }
            }

            if (headerValue == null || headerValue.isBlank()) {
                return defaultValue;
            }

            headerValue = headerValue.trim();
            return headerValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }
}
