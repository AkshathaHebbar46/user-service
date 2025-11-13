package org.userservice.user_service.controller.user;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.validator.AuthValidator;
import org.userservice.user_service.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AuthValidator authValidator;

    public UserController(UserService userService, AuthValidator authValidator) {
        this.userService = userService;
        this.authValidator = authValidator;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        logger.info("Received request to create new user: {}", request.username());
        UserResponseDTO response = userService.createUser(request);
        logger.info("User created successfully: userId={}, email={}", response.id(), response.email());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId, HttpServletRequest request) {
        if (!authValidator.isAuthorized(request, userId)) {
            logger.warn("Unauthorized access attempt to fetch user: userId={}", userId);
            return ResponseEntity.status(403).build();
        }
        logger.info("Fetching user by id={}", userId);
        UserResponseDTO user = userService.getUserById(userId);
        logger.info("Fetched user successfully: userId={}", userId);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> partiallyUpdateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserPatchRequestDTO patchRequest
    ) {
        logger.info("Received patch update request for userId={}: {}", userId, patchRequest);
        UserResponseDTO updatedUser = userService.patchUpdateUser(userId, patchRequest);
        logger.info("User updated successfully: userId={}", userId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        if (!authValidator.isAuthorized(request, userId)) {
            logger.warn("Unauthorized access attempt to delete user: userId={}", userId);
            return ResponseEntity.status(403).build();
        }
        logger.info("Deleting user with id={}", userId);
        userService.deleteUser(userId);
        logger.info("User deleted successfully: userId={}", userId);
        return ResponseEntity.noContent().build();
    }
}
