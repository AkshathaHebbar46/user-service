package org.userservice.user_service.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "UserRequestDTO", description = "Request payload to create a new user")
public record UserRequestDTO(
        @NotBlank(message = "Name is required")
        @Schema(description = "Username of the new user", example = "JohnDoe", required = true)
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Email address of the new user", example = "johndoe@example.com", required = true)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Schema(description = "Password for the new user account", example = "SecurePass123", required = true)
        String password,

        @NotNull(message = "Age is required")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 100, message = "Age cannot exceed 100")
        @Schema(description = "Age of the new user", example = "25", required = true)
        Integer age
) {}
