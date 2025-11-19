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
import org.userservice.user_service.service.user_details.CustomUserDetailsService;
import org.userservice.user_service.service.jwt.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        authController = new AuthController(userService, userDetailsService, jwtService, null);

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

    @Test
    void testRegister_EmptyName() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("");
        request.setEmail("john@example.com");
        request.setPassword("pass123");
        request.setAge(25);

        doNothing().when(userService).registerUser(request);

        ResponseEntity<String> response = authController.register(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
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
    void testLogin_InactiveUser() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("inactive@example.com");

        when(userService.login(request)).thenThrow(new IllegalStateException("User account is inactive or blacklisted"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authController.login(request));

        assertTrue(ex.getMessage().contains("inactive or blacklisted"));
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_AdminRole() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("admin@example.com");

        AuthResponseDTO dto = new AuthResponseDTO("admintoken", "ADMIN", 10L);
        when(userService.login(request)).thenReturn(dto);

        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("admintoken", response.getBody().getToken());
        assertEquals("ADMIN", response.getBody().getRole());
        assertEquals(10L, response.getBody().getUserId());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testLogin_JwtTokenReturned() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("jwt@example.com");

        AuthResponseDTO dto = new AuthResponseDTO("jwtTokenXYZ", "USER", 5L);
        when(userService.login(request)).thenReturn(dto);

        ResponseEntity<AuthResponseDTO> response = authController.login(request);

        assertEquals("jwtTokenXYZ", response.getBody().getToken());
        assertEquals(5L, response.getBody().getUserId());
        verify(userService, times(1)).login(request);
    }

    @Test
    void testRegister_CallsUserService() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("callservice@example.com");

        doNothing().when(userService).registerUser(request);

        authController.register(request);

        verify(userService, times(1)).registerUser(request);
    }

}
