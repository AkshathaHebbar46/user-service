package org.userservice.user_service.validator;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.userservice.user_service.service.jwt.JwtService;

@Component
public class AuthValidator {

    private final JwtService jwtService;

    public AuthValidator(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean isAdmin(String token) {
        return "ADMIN".equals(jwtService.extractRole(token));
    }

    public boolean isOwner(String token, Long userId) {
        return jwtService.extractUserId(token).equals(userId);
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return header.substring(7);
    }

    public boolean isAuthorized(HttpServletRequest request, Long targetUserId) {
        String token = extractToken(request);
        String role = jwtService.extractRole(token);
        Long requesterUserId = jwtService.extractUserId(token);
        return "ADMIN".equals(role) || requesterUserId.equals(targetUserId);
    }
}
