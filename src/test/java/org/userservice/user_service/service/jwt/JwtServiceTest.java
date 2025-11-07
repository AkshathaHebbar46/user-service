package org.userservice.user_service.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.userservice.user_service.service.jwt.JwtService;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Use a test secret; normally HS256 needs 32+ bytes
    private final String testSecret = "abcdefghijklmnopqrstuvwxyz123456";
    private final long expirationMs = 1000L * 60 * 60; // 1 hour

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        // Use reflection to set @Value fields
        try {
            var secretField = JwtService.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtService, testSecret);

            var expField = JwtService.class.getDeclaredField("expirationMs");
            expField.setAccessible(true);
            expField.set(jwtService, expirationMs);
        } catch (Exception e) {
            fail("Failed to set JwtService fields via reflection");
        }
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "test@example.com";
        Long userId = 42L;
        String role = "USER";

        String token = jwtService.generateToken(email, userId, role);
        assertNotNull(token);

        Jws<Claims> claimsJws = jwtService.validateToken(token);
        assertEquals(email, claimsJws.getBody().getSubject());
        assertEquals(userId, claimsJws.getBody().get("userId", Long.class));
        assertEquals(role, claimsJws.getBody().get("role", String.class));
    }

    @Test
    void testExtractEmail() {
        String email = "extract@example.com";
        String token = jwtService.generateToken(email, 1L, "ADMIN");

        String extracted = jwtService.extractEmail(token);
        assertEquals(email, extracted);
    }

    @Test
    void testExtractUserIdAndRole() {
        Long userId = 123L;
        String role = "MANAGER";
        String token = jwtService.generateToken("user@example.com", userId, role);

        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        // Temporarily set a very short expiration
        try {
            var expField = JwtService.class.getDeclaredField("expirationMs");
            expField.setAccessible(true);
            expField.set(jwtService, 10L); // 10ms
        } catch (Exception e) {
            fail("Failed to set expirationMs");
        }

        String token = jwtService.generateToken("exp@example.com", 1L, "USER");

        // Wait for token to expire
        TimeUnit.MILLISECONDS.sleep(20);

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtService.validateToken(token));
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(io.jsonwebtoken.JwtException.class,
                () -> jwtService.validateToken(invalidToken));
    }
}
