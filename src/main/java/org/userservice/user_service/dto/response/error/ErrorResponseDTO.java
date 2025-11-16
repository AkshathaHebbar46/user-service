package org.userservice.user_service.dto.response.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "ErrorResponseDTO", description = "Standard error response payload")
public class ErrorResponseDTO {

    @Schema(description = "Timestamp of when the error occurred", example = "2025-11-14T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code of the error", example = "404")
    private int status;

    @Schema(description = "Short description of the error", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "User with ID 123 not found")
    private String message;

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public static ErrorResponseDTO of(int status, String error, String message) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
