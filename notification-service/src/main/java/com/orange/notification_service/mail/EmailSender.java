package com.orange.notification_service.mail;

public interface EmailSender {

    void sendHtml(String to, String subject, String htmlContent);
}
