package org.userservice.user_service.dto.response.error;

import java.time.LocalDateTime;

public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
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
