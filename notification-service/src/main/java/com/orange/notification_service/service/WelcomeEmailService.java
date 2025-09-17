package com.orange.notification_service.service;

import com.orange.notification_service.mail.EmailSender;
import com.orange.notification_service.mail.MailTemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WelcomeEmailService {

    private static final Logger log = LoggerFactory.getLogger(WelcomeEmailService.class);

    private final EmailSender emailSender;
    private final MailTemplateRenderer renderer;

    public WelcomeEmailService(EmailSender emailSender, MailTemplateRenderer renderer) {
        this.emailSender = emailSender;
        this.renderer = renderer;
    }

    /**
     * Send a welcome email to a newly registered user.
     *
     * Business decisions:
     * - Template name = "welcome" (maps to mail-templates/welcome.html)
     * - Subject is set here (can be localized later)
     * - This method is synchronous; we can make it async later with @Async or by sending to a message bus
     */
    public void sendWelcomeEmail(String toEmail, String fullName, String otp) {
        Map<String, Object> model = Map.of(
                "fullName", fullName != null ? fullName : "",
                "otp", otp != null ? otp : "#"
        );

        String html = renderer.render("welcome", model);
        String subject = "Welcome to Orange e-shop — verify your email";

        try {
            emailSender.sendHtml(toEmail, subject, html);
            log.info("Welcome email sent to {}", toEmail + otp);
        } catch (Exception ex) {
            // handle failures according to your policy later (retry, dead-letter, audit DB)
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage(), ex);
            throw (RuntimeException) ex;
        }
    }
}
