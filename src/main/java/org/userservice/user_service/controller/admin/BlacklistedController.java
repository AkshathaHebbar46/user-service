package org.userservice.user_service.controller.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.service.blacklist.BlacklistedUserService;

@RestController
@RequestMapping("/admin/users/blacklist")
public class BlacklistedController {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistedController.class);

    private final BlacklistedUserService blacklistedUserService;

    public BlacklistedController(BlacklistedUserService blacklistedUserService) {
        this.blacklistedUserService = blacklistedUserService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> blacklistUser(@PathVariable Long userId) {
        blacklistedUserService.blacklistUser(userId);
        logger.info("User {} and all wallets have been blacklisted", userId);
        return ResponseEntity.ok("User " + userId + " and all wallets blacklisted.");
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        blacklistedUserService.unblockUser(userId);
        logger.info("User {} and all wallets have been unblocked", userId);
        return ResponseEntity.ok("User " + userId + " and all wallets unblocked.");
    }
}
