package com.orange.userservice.activity.service;

import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.entity.LoginActivity;
import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.repo.LoginActivityRepository;
import com.orange.userservice.activity.util.StringTruncator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service implementation for recording login activity events.
 * Handles mapping from command records to JPA entities and persistence.
 */
@Service
public class LoginActivityService implements LoginActivityRecorder {

    private static final int USERNAME_MAX_LENGTH = 100;
    private static final int IP_ADDRESS_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 255;
    private static final int SESSION_ID_MAX_LENGTH = 100;
    private static final int TRACE_ID_MAX_LENGTH = 64;
    private static final int FAILURE_REASON_MAX_LENGTH = 255;

    private final LoginActivityRepository repository;

    public LoginActivityService(LoginActivityRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a login event asynchronously.
     * Maps the immutable command record to the JPA entity and persists it.
     *
     * @param record the login event record containing all event data
     */
    @Override
    @Async
    public void recordLoginEvent(Record record) {
        LoginActivity activity = mapToEntity(record);
        repository.save(activity);
    }

    /**
     * Maps a command record to a JPA entity, ensuring all string fields
     * are truncated to match database column constraints.
     */
    private LoginActivity mapToEntity(Record record) {
        LoginActivity activity = new LoginActivity();

        // Identifiers
        activity.setEventUuid(record.eventUuid());
        activity.setUserId(record.userId().orElse(null));
        activity.setUsername(StringTruncator.truncate(record.username(), USERNAME_MAX_LENGTH));

        // Timing
        activity.setOccurredAt(Instant.now());
        long durationMs = Math.max(0, (record.finishedAtNanos() - record.startedAtNanos()) / 1_000_000);
        activity.setDurationMs((int) Math.min(durationMs, Integer.MAX_VALUE));

        // Request metadata
        activity.setIpAddress(StringTruncator.truncate(record.ip(), IP_ADDRESS_MAX_LENGTH));
        activity.setUserAgent(StringTruncator.truncate(record.userAgent(), USER_AGENT_MAX_LENGTH));
        activity.setSessionId(StringTruncator.truncate(record.sessionId(), SESSION_ID_MAX_LENGTH));
        activity.setTraceId(StringTruncator.truncate(record.traceId(), TRACE_ID_MAX_LENGTH));

        // Outcome
        activity.setOutcome(record.outcome());
        if (record.outcome() == LoginOutcome.FAILURE) {
            activity.setFailureReason(StringTruncator.truncate(record.failureReason(), FAILURE_REASON_MAX_LENGTH));
        } else {
            activity.setFailureReason(null);
        }

        return activity;
    }
}
