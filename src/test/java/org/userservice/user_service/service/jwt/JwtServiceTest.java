package org.userservice.user_service.service.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import org.common.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtService jwtService;

    private final String validToken = "valid.jwt.token";
    private final String expiredToken = "expired.jwt.token";
    private final String invalidToken = "invalid.jwt.token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateToken_Success() {
        when(jwtUtil.generateToken("user@example.com", 1L, "USER")).thenReturn(validToken);

        String token = jwtService.generateToken("user@example.com", 1L, "USER");

        assertEquals(validToken, token);
        verify(jwtUtil, times(1)).generateToken("user@example.com", 1L, "USER");
    }

    @Test
    void testValidateToken_Success() {
        when(jwtUtil.validateToken(validToken)).thenReturn(true);

        boolean valid = jwtUtil.validateToken(validToken);

        assertTrue(valid);
        verify(jwtUtil, times(1)).validateToken(validToken);
    }

    @Test
    void testValidateToken_Failure_InvalidToken() {
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        boolean valid = jwtUtil.validateToken(invalidToken);

        assertFalse(valid);
        verify(jwtUtil, times(1)).validateToken(invalidToken);
    }

    @Test
    void testValidateToken_ThrowsExpiredJwtException() {
        when(jwtUtil.validateToken(expiredToken)).thenThrow(ExpiredJwtException.class);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(expiredToken));
        verify(jwtUtil, times(1)).validateToken(expiredToken);
    }

    @Test
    void testExtractEmail_Success() {
        when(jwtUtil.extractUsername(validToken)).thenReturn("user@example.com");

        String email = jwtService.extractEmail(validToken);

        assertEquals("user@example.com", email);
        verify(jwtUtil, times(1)).extractUsername(validToken);
    }

    @Test
    void testExtractEmail_NullToken() {
        when(jwtUtil.extractUsername(null)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> jwtService.extractEmail(null));
        verify(jwtUtil, times(1)).extractUsername(null);
    }

    @Test
    void testExtractUserId_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(42L);

        Long userId = jwtService.extractUserId(validToken);

        assertEquals(42L, userId);
        verify(jwtUtil, times(1)).extractUserId(validToken);
    }

    @Test
    void testExtractRole_Success() {
        when(jwtUtil.extractRole(validToken)).thenReturn("USER");

        String role = jwtService.extractRole(validToken);

        assertEquals("USER", role);
        verify(jwtUtil, times(1)).extractRole(validToken);
    }

    @Test
    void testExtractRole_NullToken() {
        when(jwtUtil.extractRole(null)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> jwtService.extractRole(null));
        verify(jwtUtil, times(1)).extractRole(null);
    }

    @Test
    void testValidateToken_NullToken() {
        when(jwtUtil.validateToken(null)).thenReturn(false);

        boolean valid = jwtUtil.validateToken(null);

        assertFalse(valid);
        verify(jwtUtil, times(1)).validateToken(null);
    }

    @Test
    void testGenerateToken_NullRole() {
        when(jwtUtil.generateToken("user@example.com", 1L, null)).thenReturn(validToken);

        String token = jwtService.generateToken("user@example.com", 1L, null);

        assertEquals(validToken, token);
        verify(jwtUtil, times(1)).generateToken("user@example.com", 1L, null);
    }

    @Test
    void testExtractUserId_InvalidToken() {
        when(jwtUtil.extractUserId(invalidToken)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUserId(invalidToken));
        verify(jwtUtil, times(1)).extractUserId(invalidToken);
    }
}
