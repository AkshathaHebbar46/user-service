package org.userservice.user_service.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.exception.UnauthorizedAccessException;
import org.userservice.user_service.exception.UserNotFoundException;
import org.userservice.user_service.service.UserService;
import org.userservice.user_service.validator.AuthValidator;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminController adminController;

    private UserResponseDTO user;

    @BeforeEach
    void setup() {
        user = new UserResponseDTO(1L, "john", "john@example.com", 25, LocalDateTime.now());
    }

    @Test
    void testGetAllUsers_Admin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);

        Page<UserResponseDTO> page = new PageImpl<>(List.of(user));
        when(userService.getUsers(null, null, null, null, 0, 10)).thenReturn(page);

        var response = adminController.getAllUsers(request, null, null, null, null, 0, 10);

        assertEquals(1, response.getBody().getContent().size());
        verify(userService).getUsers(null, null, null, null, 0, 10);
    }

    @Test
    void testGetAllUsers_NotAdmin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class,
                () -> adminController.getAllUsers(request, null, null, null, null, 0, 10));
        verify(userService, never()).getUsers(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testGetUserById_Admin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(user);

        var response = adminController.getUserById(1L, request);

        assertEquals(user, response.getBody());
        verify(userService).getUserById(1L);
    }

    @Test
    void testGetUserById_NotAdmin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> adminController.getUserById(1L, request));
    }

    @Test
    void testUpdateUser_Admin() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.updateUserByAdmin(1L, dto)).thenReturn(user);

        var response = adminController.updateUser(1L, dto, request);

        assertEquals(user, response.getBody());
        verify(userService).updateUserByAdmin(1L, dto);
    }

    @Test
    void testUpdateUser_NotAdmin() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> adminController.updateUser(1L, dto, request));
    }

    @Test
    void testDeleteUser_Admin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);

        var response = adminController.deleteUser(1L, request);

        assertEquals(204, response.getStatusCodeValue());
        verify(userService).deleteUserByAdmin(1L, "token");
    }

    @Test
    void testDeleteUser_NotAdmin() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> adminController.deleteUser(1L, request));
        verify(userService, never()).deleteUserByAdmin(anyLong(), anyString());
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> adminController.getUserById(999L, request));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        when(authValidator.extractToken(request)).thenReturn("token");
        when(authValidator.isAdmin("token")).thenReturn(true);
        when(userService.updateUserByAdmin(999L, dto)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> adminController.updateUser(999L, dto, request));
    }
}
