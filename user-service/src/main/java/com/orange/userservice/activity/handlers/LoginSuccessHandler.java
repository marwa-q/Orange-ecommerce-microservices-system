package com.orange.userservice.activity.handlers;

import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.port.UserLookupPort;
import com.orange.userservice.activity.util.LoginActivityRecordBuilder;
import com.orange.userservice.activity.web.RequestMetadataHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for successful authentication events.
 * Records login activity when a user successfully authenticates.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginActivityRecorder recorder;
    private final UserLookupPort userLookupPort;

    public LoginSuccessHandler(LoginActivityRecorder recorder, UserLookupPort userLookupPort) {
        this.recorder = recorder;
        this.userLookupPort = userLookupPort;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var metadata = RequestMetadataHolder.get();
        String username = extractUsername(authentication);
        long currentTime = System.nanoTime();
        
        // Try to resolve user ID from username (email)
        Optional<UUID> userId = username != null 
            ? userLookupPort.findUserIdByEmail(username) 
            : Optional.empty();

        var record = LoginActivityRecordBuilder.buildSuccessRecord(
                UUID.randomUUID(),
                currentTime, // Note: Ideally this should be request start time, but we don't have it here
                currentTime,
                username,
                userId,
                request,
                metadata
        );

        recorder.recordLoginEvent(record);
    }

    private String extractUsername(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
