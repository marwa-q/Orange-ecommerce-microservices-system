package com.orange.notification_service.mail;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String from;

    public JavaMailEmailSender(JavaMailSender javaMailSender, @Value("${app.mail.from}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml
            javaMailSender.send(message);
        } catch (MailException ex) {
            throw ex;
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to build the email message", ex);
        }
    }
}
