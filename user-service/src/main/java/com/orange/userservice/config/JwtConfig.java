package com.orange.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private int expMinutes;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpMinutes() {
        return expMinutes;
    }

    public void setExpMinutes(int expMinutes) {
        this.expMinutes = expMinutes;
    }

    public int getExpSeconds() {
        return expMinutes * 60;
    }
}
