package org.userservice.user_service.dto.request.user;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public record UserPatchRequestDTO(
        @Nullable
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Nullable
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        String password,

        @Nullable
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 100, message = "Age cannot exceed 100")
        Integer age
) {}
