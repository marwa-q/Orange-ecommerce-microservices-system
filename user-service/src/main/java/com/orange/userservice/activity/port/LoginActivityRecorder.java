package com.orange.userservice.activity.port;

import com.orange.userservice.activity.domain.LoginOutcome;
import java.util.Optional;
import java.util.UUID;

public interface LoginActivityRecorder {

    /**
     * Persist a single login event (one row per attempt with final outcome).
     */
    void recordLoginEvent(Record cmd);

    record Record(
            UUID eventUuid,
            Optional<UUID> userId,
            String username, // email provided to login
            long startedAtNanos,     // to compute duration
            long finishedAtNanos,
            String ip,
            String userAgent,
            String sessionId,
            String traceId,
            LoginOutcome outcome,
            String failureReason     // null if success
    ) { }
}
