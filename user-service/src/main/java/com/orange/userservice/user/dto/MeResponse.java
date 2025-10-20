package com.orange.userservice.user.dto;

import com.orange.userservice.user.entity.Role;
import com.orange.userservice.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role,
        UserStatus status,
        String phone,
        String avatar,
        Instant createdAt,
        Instant updatedAt
) {}
