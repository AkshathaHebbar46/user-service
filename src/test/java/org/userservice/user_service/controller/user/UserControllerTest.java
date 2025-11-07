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
import org.userservice.user_service.dto.response.wallet.WalletResponseDTO;
import org.userservice.user_service.service.UserService;
import org.userservice.user_service.validator.AuthValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    // ================= CREATE USER =================
    @Test
    void testCreateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "JohnDoe",
                "john@example.com",
                "password123",
                25
        );

        UserResponseDTO response = new UserResponseDTO(
                1L,
                "JohnDoe",
                "john@example.com",
                25,
                LocalDateTime.now()
        );

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

    // ================= GET ALL USERS =================
    @Test
    void testGetAllUsers_Admin() throws Exception {
        when(authValidator.extractToken(any(HttpServletRequest.class))).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);

        UserResponseDTO user = new UserResponseDTO(
                1L,
                "JohnDoe",
                "john@example.com",
                25,
                LocalDateTime.now()
        );
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("JohnDoe"))
                .andExpect(jsonPath("$[0].age").value(25));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_NotAdmin() throws Exception {
        when(authValidator.extractToken(any(HttpServletRequest.class))).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    // ================= GET USER BY ID =================
    @Test
    void testGetUser_Authorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(true);

        UserResponseDTO response = new UserResponseDTO(
                userId,
                "JohnDoe",
                "john@example.com",
                25,
                LocalDateTime.now()
        );
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
        UserRequestDTO request = new UserRequestDTO(
                "JaneDoe",
                "jane@example.com",
                "pass1234",
                30
        );

        UserResponseDTO response = new UserResponseDTO(
                userId,
                "JaneDoe",
                "jane@example.com",
                30,
                LocalDateTime.now()
        );

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
        UserRequestDTO request = new UserRequestDTO(
                "JaneDoe",
                "jane@example.com",
                "pass1234",
                30
        );

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

    @Test
    void testGetUserWallets_Authorized() throws Exception {
        Long userId = 1L;

        // Mock authorization
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(true);
        when(authValidator.extractToken(any(HttpServletRequest.class))).thenReturn("token");

        // Correct WalletResponseDTO
        WalletResponseDTO wallet = new WalletResponseDTO(
                1L,    // walletId
                1L,    // userId
                1000.0 // currentBalance
        );

        when(userService.getUserWallets(eq("Bearer token"), eq(userId))).thenReturn(List.of(wallet));

        mockMvc.perform(get("/users/{userId}/wallets", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].walletId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].currentBalance").value(1000.0));

        verify(userService, times(1)).getUserWallets(eq("Bearer token"), eq(userId));
    }


    @Test
    void testGetUserWallets_NotAuthorized() throws Exception {
        Long userId = 1L;
        when(authValidator.isAuthorized(any(HttpServletRequest.class), eq(userId))).thenReturn(false);

        mockMvc.perform(get("/users/{userId}/wallets", userId))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserWallets(anyString(), eq(userId));
    }
}
