package org.userservice.user_service.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserUpdateRequestDTO", description = "Request payload to update an existing user")
public class UserUpdateRequestDTO {

    @Schema(description = "Updated username of the user", example = "JaneDoe", required = false)
    private String name;

    @Schema(description = "Updated email address of the user", example = "janedoe@example.com", required = false)
    private String email;

    @Schema(description = "Updated age of the user", example = "30", required = false)
    private Integer age;

    public UserUpdateRequestDTO() {}

    public UserUpdateRequestDTO(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
