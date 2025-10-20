package com.orange.userservice.activity.aop;

import com.orange.userservice.activity.domain.LoginOutcome;
import com.orange.userservice.activity.port.LoginActivityRecorder;
import com.orange.userservice.activity.port.UserLookupPort;
import com.orange.userservice.activity.web.RequestMetadataHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
public class AuthLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthLoggingAspect.class);

    private final LoginActivityRecorder recorder;
    private final UserLookupPort userLookup;

    public AuthLoggingAspect(LoginActivityRecorder recorder, UserLookupPort userLookup) {
        this.recorder = recorder;
        this.userLookup = userLookup;
    }

    @Around("@annotation(com.orange.userservice.activity.aop.AuditLogin)")
    public Object aroundLogin(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        String username = UsernameExtractor.extract(pjp.getArgs());
        var md = RequestMetadataHolder.get();

        UUID eventUuid = UUID.randomUUID();
        try {
            Object result = pjp.proceed();

            record(eventUuid, start, null, username, md, LoginOutcome.SUCCESS);
            log.info("Login SUCCESS for username={} ip={} trace={}",
                    username, md != null ? md.ip() : null, md != null ? md.traceId() : null);
            return result;
        } catch (AuthenticationException ex) {
            // Any Spring Security auth exception => failure
            record(eventUuid, start, ex, username, md, LoginOutcome.FAILURE);
            log.warn("Login FAILURE for username={} reason={} ip={} trace={}",
                    username, ex.getClass().getSimpleName(),
                    md != null ? md.ip() : null, md != null ? md.traceId() : null);
            throw ex;
        } catch (Exception ex) {
            // (kept for clarity, though it’s a subclass of AuthenticationException)
            record(eventUuid, start, ex, username, md, LoginOutcome.FAILURE);
            log.warn("Login FAILURE (bad credentials) for username={} ip={} trace={}",
                    username, md != null ? md.ip() : null, md != null ? md.traceId() : null);
            throw ex;
        } catch (Throwable t) {
            // Not an auth failure: still useful to log, but do NOT store passwords or secrets
            record(eventUuid, start, t, username, md, LoginOutcome.FAILURE);
            log.error("Login ERROR for username={} {} ip={} trace={}",
                    username, t.getClass().getSimpleName(),
                    md != null ? md.ip() : null, md != null ? md.traceId() : null);
            throw t;
        }
    }

    private void record(UUID eventUuid, long start, Throwable t,
                        String username, com.orange.userservice.activity.web.RequestMetadata md,
                        LoginOutcome outcome) {

        long end = System.nanoTime();
        String ip = md != null ? md.ip() : null;
        String ua = md != null ? md.userAgent() : null;
        String sessionId = md != null ? md.sessionId() : null;
        String traceId = md != null ? md.traceId() : null;

        Optional<UUID> userId = Optional.empty();
        if (username != null && outcome == LoginOutcome.SUCCESS) {
            // On success we can reliably resolve the user id
            userId = userLookup.findUserIdByEmail(username);
        }

        String failure = (outcome == LoginOutcome.FAILURE && t != null)
                ? t.getClass().getSimpleName()
                : null;

        recorder.recordLoginEvent(new LoginActivityRecorder.Record(
                eventUuid, userId, username, start, end, ip, ua, sessionId, traceId, outcome, failure
        ));
    }
}
