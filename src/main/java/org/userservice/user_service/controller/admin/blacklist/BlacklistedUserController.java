package org.userservice.user_service.controller.admin.blacklist;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.service.blacklist.BlacklistedUserService;

@RestController
@RequestMapping("/admin/users/blacklist")
public class BlacklistedUserController {

    private final BlacklistedUserService blacklistedUserService;

    public BlacklistedUserController(BlacklistedUserService blacklistedUserService) {
        this.blacklistedUserService = blacklistedUserService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> blacklistUser(@PathVariable Long userId) {
        blacklistedUserService.blacklistUser(userId);
        return ResponseEntity.ok("User " + userId + " and all wallets blacklisted.");
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        blacklistedUserService.unblockUser(userId);
        return ResponseEntity.ok("User " + userId + " and all wallets unblocked.");
    }

    @PostMapping("/wallet/{walletId}")
    public ResponseEntity<String> blacklistWallet(@PathVariable Long walletId) {
        blacklistedUserService.blacklistWallet(walletId);
        return ResponseEntity.ok("Wallet " + walletId + " blacklisted.");
    }

    @PostMapping("/wallet/{walletId}/unblock")
    public ResponseEntity<String> unblockWallet(@PathVariable Long walletId) {
        blacklistedUserService.unblockWallet(walletId);
        return ResponseEntity.ok("Wallet " + walletId + " unblocked.");
    }
}
