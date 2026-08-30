package com.smartattendance.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> attendanceBuckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.capacity:5}")
    private int capacity;

    @Value("${app.rate-limit.tokens-per-minute:5}")
    private int tokensPerMinute;

    @Value("${app.rate-limit.attendance-capacity:3}")
    private int attendanceCapacity;

    @Value("${app.rate-limit.attendance-tokens-per-minute:3}")
    private int attendanceTokensPerMinute;

    private Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(tokensPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private Bucket createAttendanceBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(attendanceCapacity)
                        .refillIntervally(attendanceTokensPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(request.getMethod()) && path.endsWith("/api/v1/auth/login")) {
            Bucket bucket = loginBuckets.computeIfAbsent(getClientIP(request), key -> createLoginBucket());
            if (!bucket.tryConsume(1)) {
                sendRateLimitError(request, response, "Rate limit exceeded. Maximum 5 requests per minute allowed.");
                return;
            }
        } else if ("POST".equalsIgnoreCase(request.getMethod()) && path.endsWith("/api/v1/attendance/mark")) {
            Bucket bucket = attendanceBuckets.computeIfAbsent(getClientIP(request), key -> createAttendanceBucket());
            if (!bucket.tryConsume(1)) {
                sendRateLimitError(request, response, "Too many attendance requests. Please wait before retrying.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendRateLimitError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now(), message, request.getRequestURI()));
    }
}
