package com.orange.userservice.activity.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Utility class for extracting username (email) from method arguments.
 * Supports multiple common patterns for login method signatures.
 */
public final class UsernameExtractor {

    private static final Logger log = LoggerFactory.getLogger(UsernameExtractor.class);

    private UsernameExtractor() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts username (email) from method arguments.
     * Supports two patterns:
     * 1. Direct string arguments that look like emails
     * 2. Objects with a getEmail() method
     *
     * @param args the method arguments to search
     * @return the extracted username, or null if not found
     */
    public static String extract(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // Case 1: Direct string arguments (e.g., login(String email, String password))
        Optional<String> emailFromString = findEmailInStrings(args);
        if (emailFromString.isPresent()) {
            return emailFromString.get();
        }

        // Case 2: Objects with getEmail() method (e.g., login(LoginRequest req))
        return findEmailInObjects(args);
    }

    private static Optional<String> findEmailInStrings(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String str && looksLikeEmail(str)) {
                return Optional.of(str);
            }
        }
        return Optional.empty();
    }

    private static String findEmailInObjects(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                var method = arg.getClass().getMethod("getEmail");
                Object value = method.invoke(arg);
                if (value instanceof String str && looksLikeEmail(str)) {
                    return str;
                }
            } catch (NoSuchMethodException e) {
                // Method doesn't exist - continue searching
                continue;
            } catch (Exception e) {
                log.debug("Failed to extract email from object {}: {}", arg.getClass().getSimpleName(), e.getMessage());
                // Continue searching other arguments
            }
        }
        return null;
    }

    private static boolean looksLikeEmail(String value) {
        return value != null && value.contains("@");
    }
}
