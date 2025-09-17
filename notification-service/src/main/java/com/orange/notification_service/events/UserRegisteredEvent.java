package com.orange.notification_service.events;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserRegisteredEvent implements Serializable {
    private String email;
    private String firstName;
    private String otp;
}
