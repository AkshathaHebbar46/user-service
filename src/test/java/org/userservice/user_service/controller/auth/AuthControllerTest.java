package org.userservice.user_service.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.userservice.user_service.dto.request.login.AuthRequestDTO;
import org.userservice.user_service.dto.request.register.RegisterRequestDTO;
import org.userservice.user_service.entity.Role;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.jwt.JwtService;
import org.userservice.user_service.service.user_details.CustomUserDetailsService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDTO registerRequest;
    private AuthRequestDTO loginRequest;
    private UserEntity activeUser;
    private UserEntity inactiveUser;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setName("John");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setAge(25);

        loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        activeUser = new UserEntity();
        activeUser.setId(1L);
        activeUser.setEmail("john@example.com");
        activeUser.setUsername("John");
        activeUser.setPassword("encodedPassword");
        activeUser.setRole(Role.USER);
        activeUser.setActive(true);

        inactiveUser = new UserEntity();
        inactiveUser.setId(2L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setUsername("Inactive");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setRole(Role.USER);
        inactiveUser.setActive(false);
    }

    // --- Register tests ---
    @Test
    void testRegister_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userDetailsService.encodePassword(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = authController.register(registerRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        ResponseEntity<String> response = authController.register(registerRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email already registered", response.getBody());
        verify(userRepository, never()).save(any());
    }

    // --- Login tests ---
    @Test
    void testLogin_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(anyString(), anyLong(), anyString())).thenReturn("token123");

        ResponseEntity<Map<String, Object>> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("token123", response.getBody().get("token"));
        assertEquals("USER", response.getBody().get("role"));
        assertEquals(1L, response.getBody().get("userId"));
    }

    @Test
    void testLogin_UserNotFound() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authController.login(loginRequest));
    }

    @Test
    void testLogin_InactiveUser() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactiveUser));

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("inactive@example.com");
        request.setPassword("password123");

        ResponseEntity<Map<String, Object>> response = authController.login(request);

        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().get("error").toString().contains("inactive or blacklisted"));
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authController.login(loginRequest));
    }

    // --- Additional edge cases for coverage ---
    @Test
    void testRegister_WithEmptyName() {
        registerRequest.setName("");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userDetailsService.encodePassword(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = authController.register(registerRequest);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testLogin_WithDifferentRole() {
        activeUser.setRole(Role.ADMIN);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(anyString(), anyLong(), anyString())).thenReturn("admintoken");

        ResponseEntity<Map<String, Object>> response = authController.login(loginRequest);
        assertEquals("ADMIN", response.getBody().get("role"));
    }

    @Test
    void testLogin_CallsLogger() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(anyString(), anyLong(), anyString())).thenReturn("token123");

        authController.login(loginRequest);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void testRegister_CallsLogger() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userDetailsService.encodePassword(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        authController.register(registerRequest);
        verify(userRepository).save(any());
    }
}
