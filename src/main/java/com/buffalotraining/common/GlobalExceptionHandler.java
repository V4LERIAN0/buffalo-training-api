package com.buffalotraining.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = determineStatus(ex.getMessage());

        Map<String, Object> body = buildBaseBody(status, ex.getMessage());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = buildBaseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("messages", validationErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter: " + ex.getName();

        Map<String, Object> body = buildBaseBody(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "This operation violates a database rule. The record may already exist or may be linked to other records.";

        Map<String, Object> body = buildBaseBody(HttpStatus.BAD_REQUEST, message);
        body.put("error", "Database Constraint Error");

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception ex) {
        Map<String, Object> body = buildBaseBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again or contact support."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> buildBaseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return body;
    }

    private HttpStatus determineStatus(String message) {
        if (message == null) {
            return HttpStatus.BAD_REQUEST;
        }

        if (message.equals("Invalid email or password") || message.equals("User account is not active")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (message.toLowerCase().contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        return HttpStatus.BAD_REQUEST;
    }
}