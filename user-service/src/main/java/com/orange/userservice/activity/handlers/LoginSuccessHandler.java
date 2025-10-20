package com.orange.userservice.activity.handlers;

import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.web.RequestMetadataHolder;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginActivityRecorder recorder;

    public LoginSuccessHandler(LoginActivityRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var md = RequestMetadataHolder.get();
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        // FIX: Use Authentication.getName() which resolves to username; avoids serializing principal classes
        String username = authentication != null ? authentication.getName() : null;
        String sessionId = null;
        HttpSession session = request.getSession(false);
        if (session != null) sessionId = session.getId();
        recorder.recordLoginEvent(new LoginActivityRecorder.Record(
                UUID.randomUUID(),
                Optional.empty(), // userId can be enriched asynchronously if needed
                username,
                System.nanoTime(),
                System.nanoTime(),
                md != null && md.ip() != null ? md.ip() : ip,
                md != null ? md.userAgent() : ua,
                md != null ? md.sessionId() : sessionId,
                md != null ? md.traceId() : null,
                LoginOutcome.SUCCESS,
                null
        ));
    }
}
