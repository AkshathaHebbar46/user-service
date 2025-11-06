package org.userservice.user_service.dto.response.user;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Integer age,
        LocalDateTime createdAt
) {}
