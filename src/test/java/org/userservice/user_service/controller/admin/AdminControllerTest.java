package org.userservice.user_service.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.exception.GlobalExceptionHandler;
import org.userservice.user_service.service.UserService;
import org.userservice.user_service.validator.AuthValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private AuthValidator authValidator;

    @InjectMocks
    private AdminController adminController;

    private UserResponseDTO sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleUser = new UserResponseDTO(
                1L,
                "John Doe",
                "john@example.com",
                25,
                LocalDateTime.now()
        );
    }

    // ================= GET ALL USERS =================
    @Test
    void getAllUsers_ShouldReturnUsers_WhenAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("John Doe"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this resource"));

        verify(userService, never()).getAllUsers();
    }

    // ================= GET USER BY ID =================
    @Test
    void getUserById_ShouldReturnUser_WhenAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(sampleUser);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("John Doe"));
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to access this resource"));
    }

    // ================= UPDATE USER =================
    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenAdmin() throws Exception {
        UserUpdateRequestDTO updateDto = new UserUpdateRequestDTO();
        updateDto.setName("Jane Doe");

        UserResponseDTO updatedUser = new UserResponseDTO(
                1L,
                "Jane Doe",
                "john@example.com",
                25,
                LocalDateTime.now()
        );

        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.updateUserByAdmin(eq(1L), any(UserUpdateRequestDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane Doe"));
    }

    @Test
    void updateUser_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        UserUpdateRequestDTO updateDto = new UserUpdateRequestDTO();
        updateDto.setName("Jane Doe");

        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        mockMvc.perform(patch("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized"));
    }

    // ================= DELETE USER =================
    @Test
    void deleteUser_ShouldReturnNoContent_WhenAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserByAdmin(1L);
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        when(authValidator.extractToken(any())).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized"));

        verify(userService, never()).deleteUserByAdmin(1L);
    }
}
