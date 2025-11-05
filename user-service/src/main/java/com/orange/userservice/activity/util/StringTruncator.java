package com.orange.userservice.activity.util;

/**
 * Utility class for truncating strings to specified maximum lengths.
 * Used to ensure database column constraints are respected.
 */
public final class StringTruncator {

    private StringTruncator() {
        // Utility class - prevent instantiation
    }

    /**
     * Truncates a string to the specified maximum length.
     *
     * @param value the string to truncate, may be null
     * @param maxLength the maximum length allowed
     * @return the truncated string, or null if input was null
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

