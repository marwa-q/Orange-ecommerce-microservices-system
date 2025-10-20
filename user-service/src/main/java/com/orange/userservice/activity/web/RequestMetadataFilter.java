package com.orange.userservice.activity.web;

import com.orange.userservice.activity.net.IpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestMetadataFilter extends OncePerRequestFilter {

    private final IpResolver ipResolver;

    public RequestMetadataFilter(IpResolver ipResolver) {
        this.ipResolver = ipResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString().replace("-", "");
        String ip = ipResolver.resolveClientIp(req);
        String ua = req.getHeader("User-Agent");
        String sessionId = req.getSession(false) != null ? req.getSession(false).getId() : null;

        RequestMetadataHolder.set(new RequestMetadata(ip, ua, sessionId, traceId));
        MDC.put("traceId", traceId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("traceId");
            RequestMetadataHolder.clear();
        }
    }
}
