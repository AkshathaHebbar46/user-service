package org.userservice.user_service.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.userservice.user_service.dto.request.login.AuthRequestDTO;
import org.userservice.user_service.dto.request.register.RegisterRequestDTO;
import org.userservice.user_service.dto.response.auth.AuthResponseDTO;
import org.userservice.user_service.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthController authController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        authController = new AuthController(userService);
    }

    // ================= Register Tests =================

    @Test
    void testRegister_Success() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("pass123");
        request.setAge(25);

        doNothing().when(userService).registerUser(request);

        ResponseEntity<String> response = authController.register(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("exists@example.com");

        doThrow(new IllegalArgumentException("Email already registered"))
                .when(userService).registerUser(request);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.register(request));

        assertEquals("Email already registered", ex.getMessage());
        verify(userService, times(1)).registerUser(request);
    }

    // ================= Login Tests =================

    @Test
    void testLogin_Success() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("pass123");

        AuthResponseDTO dto = new AuthResponseDTO("token123", "USER", 1L);
        when(userService.login(request)).thenReturn(dto);

        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("token123", response.getBody().getToken());
        assertEquals("USER", response.getBody().getRole());
        assertEquals(1L, response.getBody().getUserId());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_InvalidCredentials() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        when(userService.login(request)).thenThrow(new IllegalArgumentException("Bad credentials"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.login(request));

        assertEquals("Bad credentials", ex.getMessage());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_UserNotFound() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("notfound@example.com");

        when(userService.login(request)).thenThrow(new IllegalArgumentException("User not found"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.login(request));

        assertEquals("User not found", ex.getMessage());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testRegister_NullRequest() {
        RegisterRequestDTO request = null;

        doThrow(new IllegalArgumentException("Request cannot be null"))
                .when(userService).registerUser(request);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.register(request));

        assertEquals("Request cannot be null", ex.getMessage());
        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void testRegister_InvalidEmailFormat() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Alice");
        request.setEmail("invalid-email");
        request.setPassword("pass123");
        request.setAge(30);

        doThrow(new IllegalArgumentException("Invalid email format"))
                .when(userService).registerUser(request);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.register(request));

        assertEquals("Invalid email format", ex.getMessage());
        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void testLogin_NullRequest() {
        AuthRequestDTO request = null;

        when(userService.login(request)).thenThrow(new IllegalArgumentException("Request cannot be null"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.login(request));

        assertEquals("Request cannot be null", ex.getMessage());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_BlankPassword() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("bob@example.com");
        request.setPassword("");

        when(userService.login(request)).thenThrow(new IllegalArgumentException("Password cannot be blank"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authController.login(request));

        assertEquals("Password cannot be blank", ex.getMessage());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_NullTokenReturned() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("jane@example.com");
        request.setPassword("pass123");

        AuthResponseDTO dto = new AuthResponseDTO(null, "USER", 2L);
        when(userService.login(request)).thenReturn(dto);

        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        assertNull(response.getBody().getToken());
        assertEquals("USER", response.getBody().getRole());
        assertEquals(2L, response.getBody().getUserId());
        verify(userService, times(1)).login(request);
    }
}
