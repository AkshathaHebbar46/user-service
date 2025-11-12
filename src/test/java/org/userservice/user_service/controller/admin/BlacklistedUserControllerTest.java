package org.userservice.user_service.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.userservice.user_service.exception.GlobalExceptionHandler;
import org.userservice.user_service.exception.UnauthorizedAccessException;
import org.userservice.user_service.service.blacklist.BlacklistedUserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BlacklistedUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BlacklistedUserService blacklistedUserService;

    @InjectMocks
    private BlacklistedUserController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testBlacklistUser() throws Exception {
        Long userId = 10L;

        mockMvc.perform(post("/admin/users/blacklist/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User 10 and all wallets blacklisted."));

        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void testUnblockUser() throws Exception {
        Long userId = 20L;

        mockMvc.perform(post("/admin/users/blacklist/{userId}/unblock", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User 20 and all wallets unblocked."));

        verify(blacklistedUserService, times(1)).unblockUser(userId);
    }

    @Test
    void testBlacklistWallet() throws Exception {
        Long walletId = 101L;

        mockMvc.perform(post("/admin/users/blacklist/wallet/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("Wallet 101 blacklisted."));

        verify(blacklistedUserService, times(1)).blacklistWallet(walletId);
    }

    @Test
    void testUnblockWallet() throws Exception {
        Long walletId = 202L;

        mockMvc.perform(post("/admin/users/blacklist/wallet/{walletId}/unblock", walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("Wallet 202 unblocked."));

        verify(blacklistedUserService, times(1)).unblockWallet(walletId);
    }

    @Test
    void testBlacklistUser_ServiceThrowsException() throws Exception {
        Long userId = 30L;
        doThrow(new RuntimeException("Database error")).when(blacklistedUserService).blacklistUser(userId);

        mockMvc.perform(post("/admin/users/blacklist/{userId}", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void testUnblockUser_ServiceThrowsException() throws Exception {
        Long userId = 40L;
        doThrow(new RuntimeException("Database error")).when(blacklistedUserService).unblockUser(userId);

        mockMvc.perform(post("/admin/users/blacklist/{userId}/unblock", userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(blacklistedUserService, times(1)).unblockUser(userId);
    }

    @Test
    void testBlacklistWallet_ServiceThrowsException() throws Exception {
        Long walletId = 303L;
        doThrow(new RuntimeException("Database error")).when(blacklistedUserService).blacklistWallet(walletId);

        mockMvc.perform(post("/admin/users/blacklist/wallet/{walletId}", walletId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(blacklistedUserService, times(1)).blacklistWallet(walletId);
    }

    @Test
    void testUnblockWallet_ServiceThrowsException() throws Exception {
        Long walletId = 404L;
        doThrow(new RuntimeException("Database error")).when(blacklistedUserService).unblockWallet(walletId);

        mockMvc.perform(post("/admin/users/blacklist/wallet/{walletId}/unblock", walletId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));

        verify(blacklistedUserService, times(1)).unblockWallet(walletId);
    }

    @Test
    void testBlacklistUser_Unauthorized() throws Exception {
        Long userId = 505L;
        doThrow(new UnauthorizedAccessException("Not allowed")).when(blacklistedUserService).blacklistUser(userId);

        mockMvc.perform(post("/admin/users/blacklist/{userId}", userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not allowed"));

        verify(blacklistedUserService, times(1)).blacklistUser(userId);
    }

    @Test
    void testUnblockWallet_Unauthorized() throws Exception {
        Long walletId = 606L;
        doThrow(new UnauthorizedAccessException("Forbidden")).when(blacklistedUserService).unblockWallet(walletId);

        mockMvc.perform(post("/admin/users/blacklist/wallet/{walletId}/unblock", walletId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));

        verify(blacklistedUserService, times(1)).unblockWallet(walletId);
    }
}
