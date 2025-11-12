package org.userservice.user_service.controller.user;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.validator.AuthValidator;
import org.userservice.user_service.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

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
        logger.info("User created successfully with userId={}", response.id());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId, HttpServletRequest request) {
        if (!authValidator.isAuthorized(request, userId)) {
            return ResponseEntity.status(403).build();
        }
        logger.info("Fetching user by id={}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequestDTO request,
            HttpServletRequest httpRequest) {

        if (!authValidator.isAuthorized(httpRequest, userId)) {
            return ResponseEntity.status(403).build();
        }
        logger.info("Updating user id={} with new data", userId);
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        if (!authValidator.isAuthorized(request, userId)) {
            return ResponseEntity.status(403).build();
        }
        logger.info("Deleting user with id={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}