package com.orange.gateway_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expMinutes}")
    private long expMinutes;

    private SecretKey key;

    // Converts the plain string secret into a real HMAC SecretKey.
    @PostConstruct
    void init() {
        // JJWT 0.11.x requires a real HMAC key, not a short string
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Generate an access token with uid + role claims. */
    public String generateToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);

        return Jwts.builder()
                .setSubject(email) // sub
                .addClaims(Map.of(
                        "uid", userId.toString(),
                        "role", role
                ))
                .setIssuedAt(Date.from(now))     // iat
                .setExpiration(Date.from(exp))   // exp
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Parse and verify signature; returns the JWT claims.
    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key)   //  verify signature with the same HMAC key
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    // Validate structure, signature, and expiration.
    public boolean validate(String token) {
        try {
            Claims c = parseClaims(token);
            Date exp = c.getExpiration();
            return exp == null || exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            return exp != null && exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        String uid = parseClaims(token).get("uid", String.class);
        return uid != null ? UUID.fromString(uid) : null;
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }
}
