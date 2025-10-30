package com.orange.gateway_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayAppProperties {

    private List<String> publicPaths;
    private RateLimiting rateLimiting;
    private Map<String, List<String>> roleBasedPaths;
    private Locale locale;

    @Getter
    @Setter
    public static class Locale {
        private String defaultLanguage = "en";
    }

    public static class RateLimiting {
        private int requestsPerMinute;
        private int requestsPerHour;
        private List<String> excludedPaths;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getRequestsPerHour() {
            return requestsPerHour;
        }

        public void setRequestsPerHour(int requestsPerHour) {
            this.requestsPerHour = requestsPerHour;
        }

        public List<String> getExcludedPaths() {
            return excludedPaths;
        }

        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }
}