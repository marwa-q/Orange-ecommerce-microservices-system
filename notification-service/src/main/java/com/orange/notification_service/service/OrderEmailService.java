package com.orange.notification_service.service;

import com.orange.notification_service.entity.Notification;
import com.orange.notification_service.events.OrderPlacedEvent;
import com.orange.notification_service.mail.EmailSender;
import com.orange.notification_service.mail.MailTemplateRenderer;
import com.orange.notification_service.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OrderEmailService {

    private final EmailSender emailSender;
    private final MailTemplateRenderer mailTemplateRenderer;
    private final NotificationRepository notificationRepository;

    public OrderEmailService(EmailSender emailSender, MailTemplateRenderer mailTemplateRenderer, NotificationRepository notificationRepository) {
        this.emailSender = emailSender;
        this.mailTemplateRenderer = mailTemplateRenderer;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Send order summary email to the user
     */
    @Transactional
    public void sendOrderSummaryEmail(OrderPlacedEvent event) {
        try {
            log.info("Preparing order summary email for order: {}", event.getOrderNumber());

            // Get email from the event (already fetched by order service)
            String recipientEmail = event.getUserEmail();

            if(recipientEmail == null || recipientEmail.trim().isEmpty()) {
                throw new RuntimeException("Error: User email not available in order event");
            }
            // Prepare email data
            Map<String, Object> emailData = prepareEmailData(event);

            // Render email template
            String emailContent = mailTemplateRenderer.render("order-summary", emailData);

            // Send the email
            emailSender.sendHtml(
                    recipientEmail,
                    "Order Confirmation - " + event.getOrderNumber(),
                    emailContent
            );

            // Save notification record
            try {
                Notification notification = new Notification();
                notification.setUserId(event.getUserId());
                notification.setMessage(emailContent);
                notificationRepository.save(notification);
                log.info("Notification record saved for user {} and order {}", event.getUserId(), event.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to save notification record for order {}: {}", event.getOrderNumber(), e.getMessage());
                // Don't throw exception to avoid breaking email sending
            }

            log.info("Order summary email sent to {} for order: {}", recipientEmail, event.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send order summary email for order {}: {}",
                    event.getOrderNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order summary email", e);
        }
    }

    /**
     * Prepare data for the email template
     */
    private Map<String, Object> prepareEmailData(OrderPlacedEvent event) {
        Map<String, Object> data = new HashMap<>();

        // Order basic information
        data.put("orderNumber", event.getOrderNumber());
        data.put("orderId", event.getOrderId());
        data.put("orderDate", event.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        data.put("status", event.getStatus());
        data.put("totalAmount", formatCurrency(event.getTotalAmount()));
        data.put("paymentMethod", event.getPaymentMethod());
        data.put("shippingAddress", event.getShippingAddress());
        data.put("giftMessage", event.getGiftMessage());

        // Order items
        data.put("orderItems", event.getOrderItems());
        data.put("itemsCount", event.getOrderItems() != null ? event.getOrderItems().size() : 0);

        // Calculate subtotal for items (if needed)
        BigDecimal itemsSubtotal = event.getOrderItems() != null ?
                event.getOrderItems().stream()
                        .map(OrderPlacedEvent.OrderItemInfo::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) :
                BigDecimal.ZERO;
        data.put("itemsSubtotal", formatCurrency(itemsSubtotal));

        // Additional template data
        data.put("companyName", "Orange E-commerce");
        data.put("supportEmail", "support@orange-ecommerce.com");
        data.put("websiteUrl", "https://orange-ecommerce.com");

        return data;
    }

    /**
     * Format currency for display
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }
}
