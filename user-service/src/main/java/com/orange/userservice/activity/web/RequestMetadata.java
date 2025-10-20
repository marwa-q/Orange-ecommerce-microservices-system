package com.orange.userservice.activity.web;

public record RequestMetadata(
        String ip,
        String userAgent,
        String sessionId,
        String traceId
) { }
