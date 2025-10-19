package com.orange.notification_service.service;

import com.orange.notification_service.entity.Notification;
import com.orange.notification_service.mail.EmailSender;
import com.orange.notification_service.mail.MailTemplateRenderer;
import com.orange.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class WelcomeEmailService {

    private static final Logger log = LoggerFactory.getLogger(WelcomeEmailService.class);

    private final EmailSender emailSender;
    private final MailTemplateRenderer renderer;
    private final NotificationRepository notificationRepository;

    public WelcomeEmailService(EmailSender emailSender, MailTemplateRenderer renderer, NotificationRepository notificationRepository) {
        this.emailSender = emailSender;
        this.renderer = renderer;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Send a welcome email to a newly registered user.
     *
     * Business decisions:
     * - Template name = "welcome" (maps to mail-templates/welcome.html)
     * - Subject is set here (can be localized later)
     * - This method is synchronous; we can make it async later with @Async or by sending to a message bus
     */
    public void sendWelcomeEmail(String toEmail, String fullName, String otp, UUID userId) {
        Map<String, Object> model = Map.of(
                "fullName", fullName != null ? fullName : "",
                "otp", otp != null ? otp : "#"
        );

        String html = renderer.render("welcome", model);
        String subject = "Welcome to Orange e-shop — verify your email";

        try {
            emailSender.sendHtml(toEmail, subject, html);

            // Save notification record
            try {
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setMessage(html);
                notificationRepository.save(notification);
                log.info("Welcome notification record saved for user {}", userId);
            } catch (Exception e) {
                log.error("Failed to save welcome notification record for user {}: {}", userId, e.getMessage());
                // Don't throw exception to avoid breaking email sending
            }

            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception ex) {
            // handle failures according to your policy later (retry, dead-letter, audit DB)
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage(), ex);
            throw (RuntimeException) ex;
        }
    }
}
