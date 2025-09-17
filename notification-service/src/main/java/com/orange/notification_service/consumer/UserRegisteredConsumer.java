package com.orange.notification_service.consumer;

import com.orange.notification_service.config.RabbitConfig;
import com.orange.notification_service.events.UserRegisteredEvent;
import com.orange.notification_service.service.WelcomeEmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserRegisteredConsumer {

    private final WelcomeEmailService emailService;

    public UserRegisteredConsumer(WelcomeEmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.USER_REGISTERED_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("Received UserRegisteredEvent for email: " + event.getEmail());

        // Send welcome message
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName(), event.getOtp());
    }
}
