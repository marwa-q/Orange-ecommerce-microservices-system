package com.orange.userservice.auth.service;

import com.orange.userservice.activity.aop.AuditLogin;
import com.orange.userservice.auth.dto.*;
import com.orange.userservice.config.JwtConfig;
import com.orange.userservice.events.UserEventPublisher;
import com.orange.userservice.events.UserRegisteredEvent;
import com.orange.userservice.exception.EmailNotVerifiedException;
import com.orange.userservice.security.JwtUtil;
import com.orange.userservice.user.entity.*;
import com.orange.userservice.user.repo.*;
import com.orange.userservice.varify.service.VarifyEmailService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;
    private final JwtConfig jwtConfig;
    private final UserEventPublisher eventPublisher;
    private final VarifyEmailService varifyEmail;


    public AuthService(UserRepository userRepo,
                       PasswordEncoder encoder, AuthenticationManager authManager, JwtUtil jwt, JwtConfig jwtConfig, UserEventPublisher eventPublisher, VarifyEmailService varifyEmail) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.jwtConfig = jwtConfig;
        this.eventPublisher = eventPublisher;
        this.varifyEmail = varifyEmail;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User u = new User();
        u.setUuid(UUID.randomUUID());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        u.setRole(Role.USER);
        u.setStatus(UserStatus.ACTIVE);

        u = userRepo.save(u);

        // Check if user actually persisted in DB
        if (!userRepo.existsById(u.getId())) {
            throw new IllegalStateException("User registration failed, could not save user");
        }

        // Generate OTP for user
        String otp = varifyEmail.generateOtp(u);

        // Publish event
        UserRegisteredEvent event = new UserRegisteredEvent(u.getUuid(), u.getFirstName(), u.getEmail(), otp);
        eventPublisher.publishUserRegistered(event);

        // Do not throw to avoid transaction rollback; return neutral response
        return new AuthResponse(null, "NONE", 0);
    }


    @AuditLogin
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        var principal = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        User u = userRepo.findByEmail(principal.getUsername()).orElseThrow();
        if (u.getStatus() == UserStatus.BLOCKED) throw new BadCredentialsException("User is blocked");
        if (!u.getEmailVerified()) throw new EmailNotVerifiedException("Please varify your email before trying to login");
        String token = jwt.generateToken(u.getUuid(), u.getEmail(), u.getRole().name());
        return AuthResponse.bearer(token, jwtConfig.getExpSeconds());
    }
}
