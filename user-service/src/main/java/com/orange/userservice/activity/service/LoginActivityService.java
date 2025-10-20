package com.orange.userservice.activity.service;

import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.entity.LoginActivity;
import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.repo.LoginActivityRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LoginActivityService implements LoginActivityRecorder {

    private final LoginActivityRepository repo;

    public LoginActivityService(LoginActivityRepository repo) {
        this.repo = repo;
    }

    /**
     * REQUIRED by the LoginActivityRecorder port.
     * Maps the immutable command record to the JPA entity and persists it.
     */
    @Override
    @Async
    public void recordLoginEvent(LoginActivityRecorder.Record cmd) {
        LoginActivity e = new LoginActivity();

        // 1) Identifiers & who
        e.setEventUuid(cmd.eventUuid());
        e.setUserId(cmd.userId().orElse(null));
        e.setUsername(cmd.username());

        // 2) Timing (occurred_at + duration_ms)
        // Store the moment we’re persisting as the event timestamp
        e.setOccurredAt(Instant.now());
        long durationMs = Math.max(0, (cmd.finishedAtNanos() - cmd.startedAtNanos()) / 1_000_000);
        e.setDurationMs((int) Math.min(durationMs, Integer.MAX_VALUE));

        // 3) Request metadata
        e.setIpAddress(cmd.ip());
        e.setUserAgent(truncate(cmd.userAgent(), 255));
        e.setSessionId(cmd.sessionId());
        e.setTraceId(cmd.traceId());

        // 4) Outcome & failure reason
        e.setOutcome(cmd.outcome());
        if (cmd.outcome() == LoginOutcome.FAILURE) {
            e.setFailureReason(truncate(cmd.failureReason(), 255));
        } else {
            e.setFailureReason(null);
        }

        repo.save(e);
    }

    // -------- utilities --------
    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
