package com.orange.userservice.activity.handlers;

import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.util.LoginActivityRecordBuilder;
import com.orange.userservice.activity.web.RequestMetadataHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Handler for failed authentication events.
 * Records login activity when a user authentication attempt fails.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginActivityRecorder recorder;

    public LoginFailureHandler(LoginActivityRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        var metadata = RequestMetadataHolder.get();
        String username = request.getParameter("username");
        long currentTime = System.nanoTime();
        String failureReason = exception != null ? exception.getClass().getSimpleName() : "Unknown";

        var record = LoginActivityRecordBuilder.buildFailureRecord(
                UUID.randomUUID(),
                currentTime, // Note: Ideally this should be request start time
                currentTime,
                username,
                request,
                metadata,
                failureReason
        );

        recorder.recordLoginEvent(record);
    }
}
