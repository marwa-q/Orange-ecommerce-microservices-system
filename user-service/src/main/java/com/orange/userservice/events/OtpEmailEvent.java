package com.orange.userservice.events;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OtpEmailEvent implements Serializable {
    private String email;
    private String firstName;
    private String otp;
}