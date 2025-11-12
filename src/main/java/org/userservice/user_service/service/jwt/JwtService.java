package org.userservice.user_service.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.userservice.user_service.config.JwtUtil;

@Service
public class JwtService {

    private final JwtUtil jwtUtil;

    public JwtService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String generateToken(String email, Long userId, String role) {
        return jwtUtil.generateToken(email, userId, role);
    }

    public String extractEmail(String token) {
        return jwtUtil.extractUsername(token);
    }

    public Long extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    public String extractRole(String token) {
        return jwtUtil.extractRole(token);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && jwtUtil.validateToken(token);
    }

}
