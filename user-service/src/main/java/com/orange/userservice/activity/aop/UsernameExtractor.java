package com.orange.userservice.activity.aop;

import java.util.Arrays;

public final class UsernameExtractor {
    private UsernameExtractor(){}

    public static String extract(Object[] args) {
        // case 1: signature like login(String email, String password)
        for (Object arg : args) {
            if (arg instanceof String s && looksLikeEmail(s)) return s;
        }
        // case 2: login(LoginRequest req) with getEmail()
        for (Object arg : args) {
            try {
                var m = arg.getClass().getMethod("getEmail");
                Object val = m.invoke(arg);
                if (val instanceof String s && looksLikeEmail(s)) return s;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static boolean looksLikeEmail(String s) {
        return s != null && s.contains("@");
    }
}
