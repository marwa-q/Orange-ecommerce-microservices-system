package com.orange.userservice.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "current.password.required")
    private String currentPassword;

    @NotBlank(message = "new.password.required")
    @Size(min = 8, message = "new.password.size")
    private String newPassword;

    @NotBlank(message = "confirm.password.required")
    private String confirmNewPassword;

    public ResetPasswordRequest() {}

    public String getCurrentPassword() {
        return currentPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    @AssertTrue(message = "password.confirm.mismatch")
    private boolean isPasswordConfirmed() {
        if (newPassword == null || confirmNewPassword == null) return true; // other validators handle nulls
        return newPassword.equals(confirmNewPassword);
    }
}
