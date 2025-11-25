package org.userservice.user_service.service.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.common.utils.JwtUtil; // Use the shared JwtUtil

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtUtil jwtUtil;

    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(String email, Long userId, String role) {
        log.info("Generating JWT token for email={}, userId={}, role={}", email, userId, role);
        String token = jwtUtil.generateToken(email, userId, role);
        log.debug("Generated token: {}", token);
        return token;
    }

    public String extractEmail(String token) {
        log.debug("Extracting email from token");
        String email = jwtUtil.extractUsername(token);
        log.info("Extracted email: {}", email);
        return email;
    }

    public Long extractUserId(String token) {
        log.debug("Extracting userId from token");
        Long userId = jwtUtil.extractUserId(token);
        log.info("Extracted userId: {}", userId);
        return userId;
    }

    public String extractRole(String token) {
        log.debug("Extracting role from token");
        String role = jwtUtil.extractRole(token);
        log.info("Extracted role: {}", role);
        return role;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Validating token for userDetails={}", userDetails.getUsername());
        final String email = extractEmail(token);
        boolean valid = email.equals(userDetails.getUsername()) && jwtUtil.validateToken(token);
        log.info("Token valid: {}", valid);
        return valid;
    }
}
