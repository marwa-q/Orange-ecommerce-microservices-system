package com.orange.order_service.config;

import com.orange.order_service.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {

    @Autowired
    public void wireMessageSource(MessageSource messageSource) {
        ApiResponse.setMessageSource(messageSource);
    }
}
