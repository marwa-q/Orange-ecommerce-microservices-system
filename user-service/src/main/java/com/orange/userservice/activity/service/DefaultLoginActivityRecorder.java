package com.orange.userservice.activity.service;

import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.entity.LoginActivity;
import com.orange.userservice.activity.repo.LoginActivityRepository;
import com.orange.userservice.activity.port.LoginActivityRecorder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Primary // FIX: Make this the default LoginActivityRecorder to resolve bean ambiguity
public class DefaultLoginActivityRecorder implements LoginActivityRecorder {

    private final LoginActivityRepository repo;

    public DefaultLoginActivityRecorder(LoginActivityRepository repo) {
        this.repo = repo;
    }

    @Override
    public void recordLoginEvent(Record cmd) {
        LoginActivity e = new LoginActivity();
        e.setEventUuid(cmd.eventUuid());
        e.setUserId(cmd.userId().orElse(null));
        // FIX: Ensure username fits column length (100)
        e.setUsername(truncate(cmd.username(), 100));
        e.setOccurredAt(Instant.now());
        long durationMs = Math.max(0, (cmd.finishedAtNanos() - cmd.startedAtNanos()) / 1_000_000);
        e.setDurationMs((int)Math.min(durationMs, Integer.MAX_VALUE));
        // FIX: Cap all string fields to match DB column sizes
        e.setIpAddress(truncate(cmd.ip(), 45));
        e.setUserAgent(truncate(cmd.userAgent(), 255));
        e.setSessionId(truncate(cmd.sessionId(), 100));
        e.setTraceId(truncate(cmd.traceId(), 64));
        e.setOutcome(cmd.outcome());
        // FIX: Align failure_reason length to 255
        e.setFailureReason(cmd.outcome() == LoginOutcome.FAILURE ? truncate(cmd.failureReason(), 255) : null);
        repo.save(e);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
