package com.orange.cart_service.config;

import com.orange.cart_service.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageSourceConfig {

    @Autowired
    public void wireMessageSource(MessageSource messageSource) {
        ApiResponse.setMessageSource(messageSource);
    }
}


