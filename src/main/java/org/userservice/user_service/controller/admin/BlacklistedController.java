package org.userservice.user_service.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Blacklist a user and all their wallets", description = "Marks the specified user and their wallets as blacklisted. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User and wallets blacklisted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<String> blacklistUser(@PathVariable Long userId) {
        blacklistedUserService.blacklistUser(userId);
        logger.info("User {} and all wallets have been blacklisted", userId);
        return ResponseEntity.ok("User " + userId + " and all wallets blacklisted.");
    }

    @Operation(summary = "Unblock a blacklisted user and their wallets", description = "Removes the blacklist status from the specified user and their wallets. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User and wallets unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        blacklistedUserService.unblockUser(userId);
        logger.info("User {} and all wallets have been unblocked", userId);
        return ResponseEntity.ok("User " + userId + " and all wallets unblocked.");
    }
}
