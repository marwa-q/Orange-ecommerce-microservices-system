package com.orange.order_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        String uid = extractAllClaims(token).get("uid", String.class);
        return uid != null ? UUID.fromString(uid) : null;
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // will throw if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
