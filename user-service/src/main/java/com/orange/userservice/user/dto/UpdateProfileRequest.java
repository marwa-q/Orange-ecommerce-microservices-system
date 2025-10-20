package com.orange.userservice.user.dto;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone
) {}
