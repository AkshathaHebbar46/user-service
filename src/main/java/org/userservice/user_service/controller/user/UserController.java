package org.userservice.user_service.controller.user;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.dto.response.wallet.WalletResponseDTO;
import org.userservice.user_service.service.user.UserService;
import org.userservice.user_service.service.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Create a new user (public)
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        logger.info("Received request to create new user: {}", request.username());
        UserResponseDTO response = userService.createUser(request);
        logger.info("User created successfully with userId={}", response.id());
        return ResponseEntity.status(201).body(response);
    }

    // Get all users (only ADMIN)
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Admin fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID (ADMIN or same user)
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        Long requesterUserId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role) && !requesterUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Fetching user by id={}", userId);
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    // Update user (ADMIN or same user)
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequestDTO request,
            HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        Long requesterUserId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role) && !requesterUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Updating user id={} with new data", userId);
        UserResponseDTO response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    // Delete user (ADMIN or same user)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        Long requesterUserId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role) && !requesterUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Deleting user with id={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // Get all wallets for a user (ADMIN or same user)
    @GetMapping("/{userId}/wallets")
    public ResponseEntity<List<WalletResponseDTO>> getUserWallets(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long requesterUserId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (!"ADMIN".equals(role) && !requesterUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Fetching wallets for user id={}", userId);
        List<WalletResponseDTO> wallets = userService.getUserWallets(authHeader, userId);
        return ResponseEntity.ok(wallets);
    }
}
