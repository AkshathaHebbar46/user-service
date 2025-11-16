package org.userservice.user_service.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

@Schema(name = "UserPatchRequestDTO", description = "Request payload to partially update user information")
public record UserPatchRequestDTO(
        @Nullable
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Schema(description = "New username of the user", example = "JohnDoe")
        String username,

        @Nullable
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Schema(description = "New password for the user account", example = "NewPassword123")
        String password,

        @Nullable
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 100, message = "Age cannot exceed 100")
        @Schema(description = "New age of the user", example = "30")
        Integer age
) {}
