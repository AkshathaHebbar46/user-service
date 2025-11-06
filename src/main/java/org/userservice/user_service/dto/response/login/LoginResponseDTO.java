package org.userservice.user_service.dto.response.login;

public record LoginResponseDTO(String token, Long id, String username, String email){}
