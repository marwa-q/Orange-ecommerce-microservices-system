package com.orange.gateway_service.bff.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bff.services")
public class BffProperties {

    private String userBaseUrl;
    private String productBaseUrl;
    private String cartBaseUrl;
    private String orderBaseUrl;
}


