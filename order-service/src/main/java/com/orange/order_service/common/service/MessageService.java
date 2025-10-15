package com.orange.order_service.common.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key) {
        return getMessage(key, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }

    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, key, locale);
    }
}
