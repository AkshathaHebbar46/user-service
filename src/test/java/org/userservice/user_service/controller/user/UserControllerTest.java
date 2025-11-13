package org.userservice.user_service.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.exception.GlobalExceptionHandler;
import org.userservice.user_service.service.UserService;
import org.userservice.user_service.validator.AuthValidator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthValidator authValidator;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ================= CREATE USER =================
    @Test
    void testCreateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO("JohnDoe", "john@example.com", "password123", 25);
        UserResponseDTO response = new UserResponseDTO(1L, "JohnDoe", "john@example.com", 25, LocalDateTime.now());

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("JohnDoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService, times(1)).createUser(any(UserRequestDTO.class));
    }

    @Test
    void testCreateUser_ServiceThrowsException() throws Exception {
        UserRequestDTO request = new UserRequestDTO("JohnDoe", "john@example.com", "password123", 25);
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(userService, times(1)).createUser(any(UserRequestDTO.class));
    }

    // ================= GET USER =================
    @Test
    void testGetUser_Authorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(true);

        UserResponseDTO response = new UserResponseDTO(userId, "JohnDoe", "john@example.com", 25, LocalDateTime.now());
        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("JohnDoe"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUser_NotAuthorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(false);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(userId);
    }

    // ================= UPDATE USER =================
    @Test
    void testUpdateUser_Authorized() throws Exception {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO("JaneDoe", "jane@example.com", "password123", 30);
        UserResponseDTO response = new UserResponseDTO(userId, "JaneDoe", "jane@example.com", 30, LocalDateTime.now());

        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(true);
        when(userService.updateUser(eq(userId), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("JaneDoe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService, times(1)).updateUser(eq(userId), any(UserRequestDTO.class));
    }

    @Test
    void testUpdateUser_NotAuthorized() throws Exception {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO("JaneDoe", "jane@example.com", "password123", 30);
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(false);

        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(eq(userId), any(UserRequestDTO.class));
    }

    // ================= DELETE USER =================
    @Test
    void testDeleteUser_Authorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(true);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_NotAuthorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(false);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(userId);
    }
}
