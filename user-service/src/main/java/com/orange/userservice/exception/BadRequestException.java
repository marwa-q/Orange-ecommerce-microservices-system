package com.orange.userservice.exception;

public class BadRequestException extends RuntimeException {
    private final String messageKey;

    public BadRequestException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
