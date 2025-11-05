package com.orange.userservice.varify.controller;

import com.orange.userservice.common.dto.ApiResponse;
import com.orange.userservice.user.entity.User;
import com.orange.userservice.user.service.UserService;
import com.orange.userservice.varify.service.VarifyEmailService;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/varify")
@RestController
public class VarifyEmailController {

    private final VarifyEmailService varifyService;
    private final UserService userService;

    public VarifyEmailController(VarifyEmailService varifyService, UserService userService) {
        this.varifyService = varifyService;
        this.userService = userService;
    }

    @PostMapping("/email")
    public ApiResponse<Void> varifyEmail(@RequestParam(value = "username") String userName , @RequestParam(value = "otp") String otp) {
        try {
            varifyService.verifyOtp(userName , otp);
            return ApiResponse.success("email.verified.success");
        } catch (BadRequestException e) {
            return ApiResponse.failure(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("email.verification.failed");
        }
    }

    @PostMapping("/request-otp")
    public ApiResponse<Void> sendOtp(@RequestParam(value = "username") String username) {
        try {
            User user = userService.findbyEmail(username).orElseThrow();
            varifyService.sendRequestedOtp(user);
            return ApiResponse.success("otp.sent.success");
        } catch (Exception e) {
            return ApiResponse.failure("otp.send.failed");
        }
    }
}
