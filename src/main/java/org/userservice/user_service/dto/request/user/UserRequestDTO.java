package org.userservice.user_service.dto.request.user;

import jakarta.validation.constraints.*;

public record UserRequestDTO(
        @NotBlank(message = "Name is required") String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        String password,
        @NotNull(message = "Age is required")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 100, message = "Age cannot exceed 100")
        Integer age

) {}
