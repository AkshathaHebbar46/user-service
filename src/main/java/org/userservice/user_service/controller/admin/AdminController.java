package org.userservice.user_service.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.user.UserUpdateRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.exception.UnauthorizedAccessException;
import org.userservice.user_service.validator.AuthValidator;
import org.userservice.user_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final AuthValidator authValidator;

    public AdminController(UserService userService, AuthValidator authValidator) {
        this.userService = userService;
        this.authValidator = authValidator;
    }

    /** Get all users */
    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized access",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(HttpServletRequest request) {
        String token = authValidator.extractToken(request);
        if (!authValidator.isAdmin(token)) {
            logger.warn("Unauthorized attempt to access all users");
            throw new UnauthorizedAccessException("You are not authorized to access this resource");
        }

        List<UserResponseDTO> users = userService.getAllUsers();
        logger.info("Admin retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    /** Get a single user by ID */
    @Operation(summary = "Get user by ID", description = "Retrieve a single user's details by their ID (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId,
                                                       HttpServletRequest request) {
        String token = authValidator.extractToken(request);
        if (!authValidator.isAdmin(token)) {
            logger.warn("Unauthorized attempt to access user with ID {}", userId);
            throw new UnauthorizedAccessException("You are not authorized to access this resource");
        }

        UserResponseDTO user = userService.getUserById(userId);
        logger.info("Admin retrieved user with ID {}", userId);
        return ResponseEntity.ok(user);
    }

    /** Update user details */
    @Operation(summary = "Update user", description = "Update user details by admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long userId,
                                                      @RequestBody UserUpdateRequestDTO dto,
                                                      HttpServletRequest request) {
        String token = authValidator.extractToken(request);
        if (!authValidator.isAdmin(token)) {
            logger.warn("Unauthorized attempt to update user with ID {}", userId);
            throw new UnauthorizedAccessException("You are not authorized");
        }

        UserResponseDTO updatedUser = userService.updateUserByAdmin(userId, dto);
        logger.info("Admin updated user with ID {}", userId);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user", description = "Delete a user by admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted user",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Unauthorized access",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        String token = authValidator.extractToken(request);
        if (!authValidator.isAdmin(token)) {
            logger.warn("Unauthorized attempt to delete user with ID {}", userId);
            throw new UnauthorizedAccessException("You are not authorized");
        }

        userService.deleteUserByAdmin(userId, token);
        logger.info("Admin deleted user with ID {}", userId);
        return ResponseEntity.noContent().build();
    }
}
