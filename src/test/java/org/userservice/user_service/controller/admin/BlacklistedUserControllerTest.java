package org.userservice.user_service.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.userservice.user_service.exception.UnauthorizedAccessException;
import org.userservice.user_service.exception.UserNotFoundException;
import org.userservice.user_service.service.blacklist.BlacklistedUserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistedUserControllerTest {

    @Mock
    private BlacklistedUserService blacklistedUserService;

    @InjectMocks
    private BlacklistedController blacklistedController;

    private final Long existingUserId = 1L;
    private final Long nonExistingUserId = 999L;

    @BeforeEach
    void setup() {
        // Can initialize common setup here if needed
    }

    // --- Blacklist User ---

    @Test
    void testBlacklistUser_Success() {
        doNothing().when(blacklistedUserService).blacklistUser(existingUserId);

        var response = blacklistedController.blacklistUser(existingUserId);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("blacklisted"));
        verify(blacklistedUserService).blacklistUser(existingUserId);
    }

    @Test
    void testBlacklistUser_UserNotFound() {
        doThrow(new UserNotFoundException("User not found"))
                .when(blacklistedUserService).blacklistUser(nonExistingUserId);

        assertThrows(UserNotFoundException.class, () ->
                blacklistedController.blacklistUser(nonExistingUserId));
        verify(blacklistedUserService).blacklistUser(nonExistingUserId);
    }

    @Test
    void testBlacklistUser_Unauthorized() {
        doThrow(new UnauthorizedAccessException("Unauthorized"))
                .when(blacklistedUserService).blacklistUser(existingUserId);

        assertThrows(UnauthorizedAccessException.class, () ->
                blacklistedController.blacklistUser(existingUserId));
        verify(blacklistedUserService).blacklistUser(existingUserId);
    }

    // --- Unblock User ---

    @Test
    void testUnblockUser_Success() {
        doNothing().when(blacklistedUserService).unblockUser(existingUserId);

        var response = blacklistedController.unblockUser(existingUserId);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("unblocked"));
        verify(blacklistedUserService).unblockUser(existingUserId);
    }

    @Test
    void testUnblockUser_UserNotFound() {
        doThrow(new UserNotFoundException("User not found"))
                .when(blacklistedUserService).unblockUser(nonExistingUserId);

        assertThrows(UserNotFoundException.class, () ->
                blacklistedController.unblockUser(nonExistingUserId));
        verify(blacklistedUserService).unblockUser(nonExistingUserId);
    }

    @Test
    void testUnblockUser_Unauthorized() {
        doThrow(new UnauthorizedAccessException("Unauthorized"))
                .when(blacklistedUserService).unblockUser(existingUserId);

        assertThrows(UnauthorizedAccessException.class, () ->
                blacklistedController.unblockUser(existingUserId));
        verify(blacklistedUserService).unblockUser(existingUserId);
    }

    // --- Additional edge cases for coverage ---

    @Test
    void testBlacklistUser_WithZeroId() {
        doNothing().when(blacklistedUserService).blacklistUser(0L);

        var response = blacklistedController.blacklistUser(0L);
        assertEquals(200, response.getStatusCodeValue());
        verify(blacklistedUserService).blacklistUser(0L);
    }

    @Test
    void testUnblockUser_WithZeroId() {
        doNothing().when(blacklistedUserService).unblockUser(0L);

        var response = blacklistedController.unblockUser(0L);
        assertEquals(200, response.getStatusCodeValue());
        verify(blacklistedUserService).unblockUser(0L);
    }

    @Test
    void testBlacklistUser_CallsLogger() {
        // Logger calls are not usually tested, but we ensure method is called
        doNothing().when(blacklistedUserService).blacklistUser(existingUserId);
        blacklistedController.blacklistUser(existingUserId);
        verify(blacklistedUserService, times(1)).blacklistUser(existingUserId);
    }

    @Test
    void testUnblockUser_CallsLogger() {
        doNothing().when(blacklistedUserService).unblockUser(existingUserId);
        blacklistedController.unblockUser(existingUserId);
        verify(blacklistedUserService, times(1)).unblockUser(existingUserId);
    }
}
