package org.userservice.user_service.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.userservice.user_service.service.blacklist.BlacklistedUserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlacklistedUserControllerTest {

    @Mock
    private BlacklistedUserService blacklistedUserService;

    @InjectMocks
    private BlacklistedController blacklistedUserController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void blacklistUser_shouldReturnOk_whenServiceSucceeds() {
        Long userId = 1L;

        // no need to mock void method, but can do nothing
        doNothing().when(blacklistedUserService).blacklistUser(userId);

        ResponseEntity<String> response = blacklistedUserController.blacklistUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User 1 and all wallets blacklisted.", response.getBody());

        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void unblockUser_shouldReturnOk_whenServiceSucceeds() {
        Long userId = 1L;

        doNothing().when(blacklistedUserService).unblockUser(userId);

        ResponseEntity<String> response = blacklistedUserController.unblockUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User 1 and all wallets unblocked.", response.getBody());

        verify(blacklistedUserService, times(1)).unblockUser(userId);
    }

    @Test
    void blacklistUser_shouldThrowException_whenServiceThrows() {
        Long userId = 2L;

        doThrow(new IllegalArgumentException("User not found")).when(blacklistedUserService).blacklistUser(userId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> blacklistedUserController.blacklistUser(userId));

        assertEquals("User not found", exception.getMessage());
        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void unblockUser_shouldThrowException_whenServiceThrows() {
        Long userId = 2L;

        doThrow(new IllegalStateException("Cannot unblock inactive user")).when(blacklistedUserService).unblockUser(userId);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> blacklistedUserController.unblockUser(userId));

        assertEquals("Cannot unblock inactive user", exception.getMessage());
        verify(blacklistedUserService, times(1)).unblockUser(userId);
    }

    @Test
    void blacklistUser_shouldHandleNullUserId() {
        Long userId = null;

        doThrow(new IllegalArgumentException("User ID cannot be null")).when(blacklistedUserService).blacklistUser(userId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> blacklistedUserController.blacklistUser(userId));

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void unblockUser_shouldHandleNullUserId() {
        Long userId = null;

        doThrow(new IllegalArgumentException("User ID cannot be null")).when(blacklistedUserService).unblockUser(userId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> blacklistedUserController.unblockUser(userId));

        assertEquals("User ID cannot be null", exception.getMessage());
        verify(blacklistedUserService, times(1)).unblockUser(userId);
    }
}
