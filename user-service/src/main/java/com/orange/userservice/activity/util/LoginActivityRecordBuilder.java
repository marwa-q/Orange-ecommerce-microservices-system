package com.orange.userservice.activity.util;

import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.web.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;
import java.util.UUID;

/**
 * Builder utility for creating LoginActivityRecorder.Record objects.
 * Centralizes the logic for extracting request metadata and building records.
 */
public final class LoginActivityRecordBuilder {

    private LoginActivityRecordBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Builds a login activity record for a successful login attempt.
     *
     * @param eventUuid the unique identifier for this event
     * @param startTimeNanos the start time in nanoseconds
     * @param endTimeNanos the end time in nanoseconds
     * @param username the username (email) used for login
     * @param userId optional user ID if available
     * @param request the HTTP request
     * @param metadata the request metadata from holder
     * @return a Record for successful login
     */
    public static LoginActivityRecorder.Record buildSuccessRecord(
            UUID eventUuid,
            long startTimeNanos,
            long endTimeNanos,
            String username,
            Optional<UUID> userId,
            HttpServletRequest request,
            RequestMetadata metadata) {

        return buildRecord(
                eventUuid,
                startTimeNanos,
                endTimeNanos,
                username,
                userId,
                request,
                metadata,
                LoginOutcome.SUCCESS,
                null
        );
    }

    /**
     * Builds a login activity record for a failed login attempt.
     *
     * @param eventUuid the unique identifier for this event
     * @param startTimeNanos the start time in nanoseconds
     * @param endTimeNanos the end time in nanoseconds
     * @param username the username (email) used for login attempt
     * @param request the HTTP request
     * @param metadata the request metadata from holder
     * @param failureReason the reason for failure
     * @return a Record for failed login
     */
    public static LoginActivityRecorder.Record buildFailureRecord(
            UUID eventUuid,
            long startTimeNanos,
            long endTimeNanos,
            String username,
            HttpServletRequest request,
            RequestMetadata metadata,
            String failureReason) {

        return buildRecord(
                eventUuid,
                startTimeNanos,
                endTimeNanos,
                username,
                Optional.empty(),
                request,
                metadata,
                LoginOutcome.FAILURE,
                failureReason
        );
    }

    private static LoginActivityRecorder.Record buildRecord(
            UUID eventUuid,
            long startTimeNanos,
            long endTimeNanos,
            String username,
            Optional<UUID> userId,
            HttpServletRequest request,
            RequestMetadata metadata,
            LoginOutcome outcome,
            String failureReason) {

        String ip = extractIp(request, metadata);
        String userAgent = extractUserAgent(request, metadata);
        String sessionId = extractSessionId(request, metadata);
        String traceId = extractTraceId(metadata);

        return new LoginActivityRecorder.Record(
                eventUuid,
                userId,
                username,
                startTimeNanos,
                endTimeNanos,
                ip,
                userAgent,
                sessionId,
                traceId,
                outcome,
                failureReason
        );
    }

    private static String extractIp(HttpServletRequest request, RequestMetadata metadata) {
        if (metadata != null && metadata.ip() != null) {
            return metadata.ip();
        }
        return request != null ? request.getRemoteAddr() : null;
    }

    private static String extractUserAgent(HttpServletRequest request, RequestMetadata metadata) {
        if (metadata != null && metadata.userAgent() != null) {
            return metadata.userAgent();
        }
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private static String extractSessionId(HttpServletRequest request, RequestMetadata metadata) {
        if (metadata != null && metadata.sessionId() != null) {
            return metadata.sessionId();
        }
        if (request != null) {
            HttpSession session = request.getSession(false);
            return session != null ? session.getId() : null;
        }
        return null;
    }

    private static String extractTraceId(RequestMetadata metadata) {
        return metadata != null ? metadata.traceId() : null;
    }
}

