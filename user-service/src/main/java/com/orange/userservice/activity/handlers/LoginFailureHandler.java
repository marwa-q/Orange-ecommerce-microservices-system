package com.orange.userservice.activity.handlers;

import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.web.RequestMetadataHolder;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import java.io.IOException;

public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginActivityRecorder recorder;

    public LoginFailureHandler(LoginActivityRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        var md = RequestMetadataHolder.get();
        String username = request.getParameter("username");
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");
        recorder.recordLoginEvent(new LoginActivityRecorder.Record(
                UUID.randomUUID(),
                Optional.empty(),
                username,
                System.nanoTime(),
                System.nanoTime(),
                md != null && md.ip() != null ? md.ip() : ip,
                md != null ? md.userAgent() : ua,
                md != null ? md.sessionId() : null,
                md != null ? md.traceId() : null,
                LoginOutcome.FAILURE,
                exception.getClass().getSimpleName()
        ));
    }
}
