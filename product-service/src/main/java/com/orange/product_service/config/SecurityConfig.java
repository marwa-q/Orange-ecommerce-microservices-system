package com.orange.product_service.config;

import com.orange.product_service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/health/**",
                                "/api/test/**",
                                "/api/auth-test/**",
                                "/actuator/**",
                                "/api/reviews/test/header",
                                "/api/products/*",  // Allow public access to product details by ID
                                "/api/reviews/product/*",  // Allow public access to product reviews
                                "/api/reviews/statistics/*",  // Allow public access to review statistics
                                "/api/reviews/debug/*",  // Allow public access to debug endpoint
                                "/api/reviews/*"  // Allow public access to review details
                        ).permitAll()
                        .requestMatchers("/api/products/create", "/api/products/update", "/api/products/delete", "/api/products/list", "/api/products/add-variant", "/api/products/update-variant", "/api/products/remove-variant", "/api/tags/**", "/api/reviews/create", "/api/reviews/update", "/api/reviews/delete", "/api/reviews/user").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
