package org.userservice.user_service.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "UserResponseDTO", description = "Response DTO representing a user")
public record UserResponseDTO(
        @Schema(description = "Unique identifier of the user", example = "123")
        Long id,

        @Schema(description = "Username of the user", example = "john_doe")
        String username,

        @Schema(description = "Email address of the user", example = "john@example.com")
        String email,

        @Schema(description = "Age of the user", example = "25")
        Integer age,

        @Schema(description = "Date and time when the user was created", example = "2025-11-14T10:30:00")
        LocalDateTime createdAt
) {}
