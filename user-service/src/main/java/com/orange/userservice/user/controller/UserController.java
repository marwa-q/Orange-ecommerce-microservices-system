package com.orange.userservice.user.controller;

import com.orange.userservice.security.JwtUtil;
import com.orange.userservice.user.dto.*;
import com.orange.userservice.user.repo.UserRepository;
import com.orange.userservice.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService service;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

    public UserController(UserService service, UserRepository repo, JwtUtil jwtUtil, MessageSource messageSource) {
        this.service = service;
        this.userRepo = repo;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public MeResponse me(@AuthenticationPrincipal User principal) {
        return service.getMe(resolveUserId(principal.getUsername()));
    }

    @Operation(summary = "Update current user",
            description = "Send JSON + avatar image in multipart/form-data",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UpdateProfileRequest.class))
            ))
    @PutMapping(
            value = "/me",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<MeResponse> updateMe(@AuthenticationPrincipal User principal,
                               @RequestPart(value = "data", required = false) @Valid UpdateProfileRequest req,
                               @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        
        if (req == null) {
            throw new IllegalArgumentException("Profile data is required");
        }
        
        return service.updateMe(resolveUserId(principal.getUsername()), req, avatar);
    }

    private UUID resolveUserId(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUuid();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody ResetPasswordRequest body
    ) {
        String userName = principal.getUsername();
        return service.resetPassword(userName, body);
    }

    @GetMapping("/{userId}/email")
    @Operation(summary = "Get user email by UUID", description = "Retrieve a user's email using their UUID")
    public ResponseEntity<ApiResponse<String>> getUserEmailById(@PathVariable UUID userId) {
        try {
            String email = service.getEmailByUserId(userId);
            ApiResponse<String> response = new ApiResponse<>(true, "User email retrieved successfully", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
