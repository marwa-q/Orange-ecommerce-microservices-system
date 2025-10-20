package com.orange.userservice.varify.dto;

import jakarta.validation.constraints.NotBlank;

public record VarifyRequest (
        @NotBlank String OTP,
        @NotBlank String username
){ }
