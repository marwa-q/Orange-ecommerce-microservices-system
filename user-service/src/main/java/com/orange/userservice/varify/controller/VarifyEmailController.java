package com.orange.userservice.varify.controller;

import com.orange.userservice.user.entity.User;
import com.orange.userservice.user.service.UserService;
import com.orange.userservice.varify.service.VarifyEmailService;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
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
    public void varifyEmail(@RequestParam(value = "username") String userName , @RequestParam(value = "otp") String otp) throws BadRequestException {
        varifyService.verifyOtp(userName , otp);
    }

    @PostMapping("/request-otp")
    public void sendOtp(@RequestParam(value = "username") String username) {
        User user = userService.findbyEmail(username).orElseThrow();
        varifyService.sendRequestedOtp(user);
    }
}
