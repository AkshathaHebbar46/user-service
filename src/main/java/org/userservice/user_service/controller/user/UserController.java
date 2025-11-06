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
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        logger.info("Received request to create new user: {}", request.username());
        UserResponseDTO response = userService.createUser(request); // <-- Service handles wallet REST call
        logger.info("User created successfully with userId={}", response.id());
        return ResponseEntity.status(201).body(response);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        logger.info("Fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        logger.debug("Number of users retrieved: {}", users.size());
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId) {
        logger.info("Fetching user by id={}", userId);
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    // Update user
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long userId,
                                                      @RequestBody UserRequestDTO request) {
        logger.info("Updating user id={} with new data", userId);
        UserResponseDTO response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    // Delete user
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        logger.info("Deleting user with id={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/wallets")
    public ResponseEntity<List<WalletResponseDTO>> getUserWallets(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {

        List<WalletResponseDTO> wallets = userService.getUserWallets(authHeader, userId);
        return ResponseEntity.ok(wallets);
    }


}