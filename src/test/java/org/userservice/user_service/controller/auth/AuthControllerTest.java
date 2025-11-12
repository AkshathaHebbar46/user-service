package org.userservice.user_service.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.userservice.user_service.dto.request.login.AuthRequestDTO;
import org.userservice.user_service.dto.request.register.RegisterRequestDTO;
import org.userservice.user_service.entity.Role;
import org.userservice.user_service.entity.UserEntity;
import org.userservice.user_service.repository.UserRepository;
import org.userservice.user_service.service.jwt.JwtService;
import org.userservice.user_service.service.user_details.CustomUserDetailsService;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

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
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        registerRequest = new RegisterRequestDTO();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password");
        registerRequest.setAge(25);

        loginRequest = new AuthRequestDTO();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password");

        activeUser = new UserEntity();
        activeUser.setId(1L);
        activeUser.setUsername("John Doe");
        activeUser.setEmail("john@example.com");
        activeUser.setPassword("encodedPassword");
        activeUser.setRole(Role.USER);
        activeUser.setActive(true);

        inactiveUser = new UserEntity();
        inactiveUser.setId(2L);
        inactiveUser.setUsername("Jane Doe");
        inactiveUser.setEmail("jane@example.com");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setRole(Role.USER);
        inactiveUser.setActive(false);
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already registered"));
    }

    @Test
    void register_ShouldReturnOk_WhenEmailNotExists() throws Exception {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userDetailsService.encodePassword(registerRequest.getPassword())).thenReturn("encodedPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void login_ShouldReturnForbidden_WhenUserInactive() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(new UsernamePasswordAuthenticationToken("jane@example.com", "password"));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(inactiveUser));

        AuthRequestDTO inactiveLoginRequest = new AuthRequestDTO();
        inactiveLoginRequest.setEmail("jane@example.com");
        inactiveLoginRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inactiveLoginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User account is inactive or blacklisted."));
    }

    @Test
    void login_ShouldReturnToken_WhenUserActive() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(new UsernamePasswordAuthenticationToken("john@example.com", "password"));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));
        when(jwtService.generateToken(activeUser.getEmail(), activeUser.getId(), activeUser.getRole().name()))
                .thenReturn("dummy-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(new UsernamePasswordAuthenticationToken("unknown@example.com", "password"));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthRequestDTO unknownUserRequest = new AuthRequestDTO();
        unknownUserRequest.setEmail("unknown@example.com");
        unknownUserRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertTrue(result.getResolvedException() instanceof IllegalArgumentException);
                    assertEquals("User not found", result.getResolvedException().getMessage());
                });
    }
}
