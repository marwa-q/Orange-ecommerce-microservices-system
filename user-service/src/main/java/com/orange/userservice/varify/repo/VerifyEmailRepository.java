package com.orange.userservice.varify.repo;

import com.orange.userservice.varify.entity.VerifyEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerifyEmailRepository extends JpaRepository<VerifyEmail, Long> {
    Optional<VerifyEmail> findByUserIdAndUsedFalse(long userId);
    void deleteByUserId(long userId);
}