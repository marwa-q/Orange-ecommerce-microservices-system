package com.orange.userservice.varify.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class OtpService {
    private static final SecureRandom RNG = new SecureRandom();

    public OtpService() {}

    public static String generate5DigitOtp() {
        int n = RNG.nextInt(100000);
        // Force output in ENGLISH locale so digits are always 0-9
        return String.format(Locale.ENGLISH, "%05d", n);
    }
}
