package com.orange.userservice.exception;

// Custom exception for email verification
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("Email not varified, please check your email.");
    }

    // Constructor with custom message
    public EmailNotVerifiedException(String message) {
        super(message);
    }

    // Constructor with cause
    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with cause only
    public EmailNotVerifiedException(Throwable cause) {
        super(cause);
    }
}
