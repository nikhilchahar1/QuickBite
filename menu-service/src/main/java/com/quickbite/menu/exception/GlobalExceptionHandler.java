package com.quickbite.menu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status.value());        // e.g. 404
        error.put("error", status.getReasonPhrase()); // e.g. "Not Found"
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }

    // Triggered when: throw new ResourceNotFoundException("User not found")
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    // ── Handler 2: BadRequestException → 400 ─────────────────────
    // Triggered when: throw new BadRequestException("Email already registered")
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // ── Handler 3: Validation errors → 400 ───────────────────────
    // Triggered when: @Valid fails on a DTO
    // e.g. email is blank, password too short
    // This replaces the default Spring validation error response
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        // Collect all field errors into a map
        // e.g. { "email": "must be a valid email", "password": "size must be >= 6" }
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(),
                    fieldError.getDefaultMessage());
        }

        Map<String, Object> error = buildError(HttpStatus.BAD_REQUEST,
                "Validation failed");
        // Add the field-level errors as a nested object
        error.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ── Handler 4: Any other unexpected exception → 500 ──────────
    // Safety net — catches anything we did not explicitly handle
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong: " + ex.getMessage()));
    }
}