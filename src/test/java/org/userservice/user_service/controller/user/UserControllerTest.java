package org.userservice.user_service.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.service.UserService;
import org.userservice.user_service.validator.AuthValidator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== GET USER ====================

    @Test
    void getUser_Authorized_ReturnsUser() {
        Long userId = 1L;
        when(authValidator.isAuthorized(request, userId)).thenReturn(true);

        UserResponseDTO user = new UserResponseDTO(userId, "John Doe", "john@example.com", 25, LocalDateTime.now());
        when(userService.getUserById(userId)).thenReturn(user);

        ResponseEntity<UserResponseDTO> response = userController.getUser(userId, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUser_Unauthorized_Returns403() {
        Long userId = 1L;
        when(authValidator.isAuthorized(request, userId)).thenReturn(false);

        ResponseEntity<UserResponseDTO> response = userController.getUser(userId, request);

        assertEquals(403, response.getStatusCodeValue());
        verify(userService, times(0)).getUserById(any());
    }

    @Test
    void getUser_UserNotFound_ReturnsNullBody() {
        Long userId = 1L;
        when(authValidator.isAuthorized(request, userId)).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(null);

        ResponseEntity<UserResponseDTO> response = userController.getUser(userId, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(null, response.getBody());
    }

    // ==================== PATCH USER ====================

    @Test
    void partiallyUpdateUser_UpdateUsername() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO("Jane Doe", null, null);
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "Jane Doe", "john@example.com", 25, LocalDateTime.now());

        when(userService.patchUpdateUser(1L, patchRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response = userController.partiallyUpdateUser(1L, patchRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }

    @Test
    void partiallyUpdateUser_UpdatePassword() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO(null, "NewPass123", null);
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "John Doe", "john@example.com", 25, LocalDateTime.now());

        when(userService.patchUpdateUser(1L, patchRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response = userController.partiallyUpdateUser(1L, patchRequest);

        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }

    @Test
    void partiallyUpdateUser_UpdateAge() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO(null, null, 30);
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "John Doe", "john@example.com", 30, LocalDateTime.now());

        when(userService.patchUpdateUser(1L, patchRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response = userController.partiallyUpdateUser(1L, patchRequest);

        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }

    @Test
    void partiallyUpdateUser_UpdateAllFields() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO("Jane Doe", "NewPass123", 28);
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "Jane Doe", "john@example.com", 28, LocalDateTime.now());

        when(userService.patchUpdateUser(1L, patchRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response = userController.partiallyUpdateUser(1L, patchRequest);

        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }

    @Test
    void partiallyUpdateUser_NullPatch_ReturnsCurrentUser() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO(null, null, null);
        UserResponseDTO updatedUser = new UserResponseDTO(1L, "John Doe", "john@example.com", 25, LocalDateTime.now());

        when(userService.patchUpdateUser(1L, patchRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response = userController.partiallyUpdateUser(1L, patchRequest);

        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }

    @Test
    void partiallyUpdateUser_UserNotFound_ThrowsException() {
        UserPatchRequestDTO patchRequest = new UserPatchRequestDTO("Jane Doe", null, null);

        when(userService.patchUpdateUser(1L, patchRequest)).thenThrow(new RuntimeException("User not found"));

        try {
            userController.partiallyUpdateUser(1L, patchRequest);
        } catch (RuntimeException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(userService, times(1)).patchUpdateUser(1L, patchRequest);
    }
}
