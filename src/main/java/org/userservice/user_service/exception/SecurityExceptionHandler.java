package org.userservice.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String path = request.getServletPath();

        // Skip Swagger and favicon requests
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/favicon.ico")) {
            return; // Let Spring Security handle access to these endpoints
        }

        logger.warn("Unauthorized access attempt to '{}': {}", path, authException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now(),
                authException.getMessage(),
                path
        );

        response.getWriter().write(json);
    }
}
