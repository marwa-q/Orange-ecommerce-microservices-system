package com.orange.notification_service.web;

import com.orange.notification_service.service.WelcomeEmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final WelcomeEmailService welcomeEmailService;

    public AdminController(WelcomeEmailService welcomeEmailService) {
        this.welcomeEmailService = welcomeEmailService;
    }

    public static record SendWelcomeRequest(@Email String to,
                                            @NotBlank String fullName,
                                            String verificationLink) {}

    /**
     * POST /api/admin/send-welcome
     * Body: { "to":"user@example.com", "fullName":"Marwa", "verificationLink":"https://..." }
     */
    @PostMapping("/send-welcome")
    public ResponseEntity<Void> sendWelcome(@RequestBody @Valid SendWelcomeRequest req) {
        welcomeEmailService.sendWelcomeEmail(req.to(), req.fullName(), req.verificationLink());
        return ResponseEntity.ok().build();
    }
}
