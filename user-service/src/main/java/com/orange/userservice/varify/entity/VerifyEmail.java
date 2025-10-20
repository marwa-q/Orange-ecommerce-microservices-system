package com.orange.userservice.varify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verify_email")
public class VerifyEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        expiresAt = Instant.now().plusSeconds(600); // 10 minutes
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
