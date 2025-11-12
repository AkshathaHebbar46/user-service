package org.userservice.user_service.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.exception.UnauthorizedAccessException;
import org.userservice.user_service.validator.AuthValidator;
import org.userservice.user_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AuthValidator authValidator;

    public AdminController(UserService userService, AuthValidator authValidator) {
        this.userService = userService;
        this.authValidator = authValidator;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(HttpServletRequest request) {
        // Extract token from header
        String token = authValidator.extractToken(request);

        // Check admin access
        if (!authValidator.isAdmin(token)) {
            throw new UnauthorizedAccessException("You are not authorized to access this resource");
        }

        // Fetch and return all users
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
