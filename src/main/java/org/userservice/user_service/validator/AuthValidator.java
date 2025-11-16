package org.userservice.user_service.validator;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.userservice.user_service.exception.UnauthorizedAccessException;
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

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Missing or invalid Authorization header");
        }
        return header.substring(7);
    }

    public boolean isAuthorized(HttpServletRequest request, Long targetUserId) {
        String token = extractToken(request);
        String role = jwtService.extractRole(token);
        Long requesterUserId = jwtService.extractUserId(token);

        if (!("ADMIN".equals(role) || requesterUserId.equals(targetUserId))) {
            throw new UnauthorizedAccessException("You are not authorized to access this resource");
        }
        return true;
    }
}