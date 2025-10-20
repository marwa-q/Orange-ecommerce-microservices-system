package com.orange.userservice.security;

import com.orange.userservice.user.repo.UserRepository;
import com.orange.userservice.user.entity.UserStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService uds, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        // If no Authorization header or not Bearer continue
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }
        String token = header.substring(7);

        try {
            // Check expired + not already authenticated
            if (!jwtUtil.isExpired(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwtUtil.getEmail(token);

                var user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("JWT rejected: user not found [{}]", email);
                    chain.doFilter(req, res);
                    return;
                }

                if (user.getStatus() == UserStatus.BLOCKED) {
                    log.warn("JWT rejected: user [{}] is BLOCKED", email);
                    chain.doFilter(req, res);
                    return;
                }

                UserDetails details = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                details,
                                null,
                                details.getAuthorities()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("JWT accepted, user [{}] authenticated", email);
            }
        } catch (Exception ex) {
            // Do not ignore exceptions silently
            log.warn("JWT processing failed: {}", ex.getMessage(), ex);
            // Let Spring Security handle 401/403 if needed
        }

        chain.doFilter(req, res);
    }

}
