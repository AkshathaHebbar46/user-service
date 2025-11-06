package org.userservice.user_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.userservice.user_service.dto.response.error.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors (e.g., invalid age, missing fields)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle database constraint violations (e.g., duplicate email)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Duplicate entry or constraint violation";

        if (ex.getMessage() != null && ex.getMessage().contains("users.UK6dotkott2kjsp8vw4d0m25fb7")) {
            message = "A user with this email already exists.";
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Data Integrity Violation",
                message
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Handle Wallet Service REST client failures
    @ExceptionHandler(WalletServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleWalletServiceError(WalletServiceException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Wallet Service Error",
                ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Handle external service unavailability
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceUnavailable(ResourceAccessException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Wallet Service is currently unavailable. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // Fallback for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        ErrorResponseDTO error = ErrorResponseDTO.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
