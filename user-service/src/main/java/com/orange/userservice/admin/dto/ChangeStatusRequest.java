package com.orange.userservice.admin.dto;

import com.orange.userservice.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull UserStatus status
        ) {}
