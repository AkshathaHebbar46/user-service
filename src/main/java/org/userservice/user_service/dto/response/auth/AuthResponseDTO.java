package org.userservice.user_service.dto.response.auth;

public class AuthResponseDTO {
    private String token;
    private String role;

    public String getRole() {
        return role;
    }

    public AuthResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}