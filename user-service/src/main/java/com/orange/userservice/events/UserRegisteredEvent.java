package com.orange.userservice.events;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserRegisteredEvent {
    private UUID userId;
    private String firstName;
    private String email;
    private String otp;

    // Jackson needs a no-args constructor
    public UserRegisteredEvent() {}

    public UserRegisteredEvent(UUID userId, String firstName, String email, String otp) {
        this.userId = userId;
        this.firstName = firstName;
        this.email = email;
        this.otp = otp;
    }
}
