package org.userservice.user_service.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.userservice.user_service.dto.request.user.UserPatchRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.validator.AuthValidator;
import org.userservice.user_service.service.UserService;

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

    @Operation(summary = "Get user by ID", description = "Fetches the details of a user by their ID. Requires authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
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

    @Operation(summary = "Partially update user", description = "Updates specified fields of a user. Only non-null fields in the request are updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
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
}
