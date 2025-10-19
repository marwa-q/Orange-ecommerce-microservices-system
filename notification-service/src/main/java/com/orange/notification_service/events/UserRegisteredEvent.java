package com.orange.notification_service.events;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class UserRegisteredEvent implements Serializable {
    private UUID userId;
    private String email;
    private String firstName;
    private String otp;
}
