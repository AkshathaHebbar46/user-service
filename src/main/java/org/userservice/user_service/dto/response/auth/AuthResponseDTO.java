package org.userservice.user_service.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponseDTO", description = "Response payload after successful authentication")
public class AuthResponseDTO {

    @Schema(description = "JWT token for authenticated user", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String token;

    @Schema(description = "Role of the authenticated user", example = "USER", required = true)
    private String role;

    public AuthResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getRole() {
        return role;
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
