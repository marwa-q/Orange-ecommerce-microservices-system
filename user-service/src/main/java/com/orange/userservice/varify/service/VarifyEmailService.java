package com.orange.userservice.varify.service;

import com.orange.userservice.events.UserEventPublisher;
import com.orange.userservice.events.UserRegisteredEvent;
import com.orange.userservice.user.entity.User;
import com.orange.userservice.user.repo.UserRepository;
import com.orange.userservice.varify.entity.VerifyEmail;
import com.orange.userservice.varify.repo.VerifyEmailRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class VarifyEmailService {

    private final VerifyEmailRepository verifyEmailRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;



    public VarifyEmailService(VerifyEmailRepository verifyEmailRepo, UserRepository userRepo, PasswordEncoder passwordEncoder, UserEventPublisher eventPublisher) {
        this.verifyEmailRepo = verifyEmailRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String generateOtp(User user) {

        // Generate OTP
        String otp = OtpService.generate5DigitOtp();
        String otpHash = passwordEncoder.encode(otp);

        // Persist OTP
        VerifyEmail ve = new VerifyEmail();
        ve.setUserId(user.getId());
        ve.setOtpHash(otpHash);

        // Remove old OTPs
        verifyEmailRepo.deleteByUserId(user.getId());
        verifyEmailRepo.save(ve);

        return otp;
    }

    @Transactional
    public void sendRequestedOtp(User user) {

        // Generate otp
        String otp = generateOtp(user);

        // Publish event
        UserRegisteredEvent event = new UserRegisteredEvent(user.getUuid(), user.getFirstName(), user.getEmail(), otp);
        eventPublisher.publishUserRegistered(event);

    }

    // Email varification
    @Transactional
    public void verifyOtp(String username, String otp) throws BadRequestException {

        // Check username
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + username));

        // Check if otp is used
        VerifyEmail ve = verifyEmailRepo.findByUserIdAndUsedFalse(user.getId())
                .orElseThrow(() -> new BadRequestException("OTP Not found for this user"));

        // Check if otp is expired
        if (ve.isExpired()) {
            throw new BadRequestException("OTP expired");
        }

        // Check if otp is valid
        System.out.println(!passwordEncoder.matches(otp, ve.getOtpHash()));
        if (!passwordEncoder.matches(otp, ve.getOtpHash())) {
            // TODO: increment retry counter, rate-limit, maybe lock after N attempts
            throw new BadRequestException("Invalid OTP");
        }

        // success: mark used and flip user flag
        ve.setUsed(true);
        verifyEmailRepo.save(ve);
        user.setEmailVerified(true);
        userRepo.save(user);
    }
}
