package com.orange.userservice.user.service;

import com.orange.userservice.security.JwtUtil;
import com.orange.userservice.common.dto.ApiResponse;
import com.orange.userservice.user.dto.MeResponse;
import com.orange.userservice.user.dto.ResetPasswordRequest;
import com.orange.userservice.user.dto.UpdateProfileRequest;
import com.orange.userservice.user.entity.User;
import com.orange.userservice.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.orange.userservice.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final JwtUtil jwtUtil;


    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, MessageSource messageSource, JwtUtil jwtUtil) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.jwtUtil = jwtUtil;
    }

    @Value("${app.media.avatar-dir}")
    private String avatarDir; // storage/avatars

    public ApiResponse<MeResponse> getMe(UUID userId) {
        User u = userRepo.findByUuid(userId).orElseThrow();
        MeResponse response = new MeResponse(
                u.getUuid(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole(),
                u.getStatus(),
                u.getPhone(),
                u.getAvatar(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
        return ApiResponse.success(response);
    }

    @Transactional
    public ApiResponse<MeResponse> updateMe(UUID userId, UpdateProfileRequest req, MultipartFile avatar) {
        User u = userRepo.findByUuid(userId).orElseThrow();

        if (req.firstName() != null) u.setFirstName(req.firstName());
        if (req.lastName() != null) u.setLastName(req.lastName());
        if (req.phone() != null) u.setPhone(req.phone());

        if (avatar != null && !avatar.isEmpty()) {
            // allow-list content types
            final Map<String, String> extByType = Map.of(
                    "image/jpeg", ".jpg",
                    "image/png",  ".png",
                    "image/webp", ".webp"
            );
            String ct = avatar.getContentType();
            if (ct == null || !extByType.containsKey(ct)) {
                return ApiResponse.failure("avatar.image.format.invalid");
            }

            String newName = userId + "-" + UUID.randomUUID() + extByType.get(ct);
            Path avatarRoot = Paths.get(avatarDir).toAbsolutePath().normalize();
            Path target = avatarRoot.resolve(newName);

            try {
                Files.createDirectories(avatarRoot);
                Files.copy(avatar.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return ApiResponse.failure("avatar.store.failed");
            }

            // delete previous avatar if it’s one we own
            deleteLocalIfOwned(u.getAvatar(), avatarRoot);

            // URL that your ResourceHandler maps to the above directory
            String publicUrl = "/media/avatars/" + newName;
            u.setAvatar(publicUrl);
        }

        u.setUpdatedAt(Instant.now());
        userRepo.save(u);
        MeResponse response = getMe(userId).getData();
        return ApiResponse.success(response);
    }


    @Transactional
    public ApiResponse<Void> resetPassword(String userName, ResetPasswordRequest request) {
        try {
            User user = userRepo.findByEmail(userName)
                    .orElse(null);

            if (user == null) {
                return ApiResponse.failure("user.not.found");
            }

            // verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

                return ApiResponse.failure("password.current.invalid");
            }

            // prevent reusing the same password
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                return ApiResponse.failure("password.new.same.as.current");
            }

            // set new password
            String hashed = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(hashed);
            userRepo.save(user);

            return ApiResponse.success("password.reset.success", null);

        } catch (Exception ex) {
            // fallback error handling
            return ApiResponse.failure("server.error");
        }

    }




    // For user avatar
    private void deleteLocalIfOwned(String url, Path avatarRoot) {
        if (url == null || !url.startsWith("/media/avatars/")) return;
        String fileName = url.substring("/media/avatars/".length());
        Path prev = avatarRoot.resolve(fileName).normalize();
        try {
            if (prev.startsWith(avatarRoot)) {
                Files.deleteIfExists(prev);
            }
        } catch (IOException ignored) {}
    }


    public Optional<User> findbyEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public String getEmailByUserId(UUID id) {
        return userRepo.findEmailByUuid(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
}
