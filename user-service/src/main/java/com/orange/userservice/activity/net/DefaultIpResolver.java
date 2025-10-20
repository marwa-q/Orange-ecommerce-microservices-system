package com.orange.userservice.activity.net;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DefaultIpResolver implements IpResolver {

    @Override
    public String resolveClientIp(HttpServletRequest request) {
        // Prefer X-Forwarded-For chain (first IP = original client)
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = Arrays.stream(xff.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElse(null);
            if (first != null) return first;
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real.trim();
        return request.getRemoteAddr();
    }
}
