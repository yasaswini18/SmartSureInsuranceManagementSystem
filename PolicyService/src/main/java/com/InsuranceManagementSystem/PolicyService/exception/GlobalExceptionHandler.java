package com.InsuranceManagementSystem.PolicyService.exception;

import feign.FeignException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(403)
                        .message("Access denied: you don't have permission for this action")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Token has expired. Please login again")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSignature(
            SignatureException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Invalid token signature")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwt(
            MalformedJwtException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Malformed token")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex
    ) {
        if (ex.status() == 401) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(401)
                            .message("Authentication failed. Please login again")
                            .timestamp(LocalDateTime.now())
                            .build());
        }

        if (ex.status() == 403) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(403)
                            .message("You do not have permission for this action")
                            .timestamp(LocalDateTime.now())
                            .build());
        }

        if (ex.status() == -1 || ex.status() == 503) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ErrorResponse.builder()
                            .status(503)
                            .message("Authentication service is temporarily unavailable. Please try again later")
                            .timestamp(LocalDateTime.now())
                            .build());
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(500)
                        .message("Error communicating with authentication service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        String message = "Data integrity violation";
        if (ex.getMessage() != null &&
            ex.getMessage().contains("Duplicate entry")) {
            message = "A record with this information already exists";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(409)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.builder()
                        .status(405)
                        .message("HTTP method '" + ex.getMethod() +
                                "' is not supported for this endpoint")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ?
                        ex.getRequiredType().getSimpleName() : "unknown"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(400)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        int statusCode = 400;

        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                statusCode = 404;
            } else if (message.contains("permission") ||
                       message.contains("Unauthorized")) {
                status = HttpStatus.UNAUTHORIZED;
                statusCode = 401;
            } else if (message.contains("unavailable") ||
                       message.contains("try again later")) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                statusCode = 503;
            }else if (message.contains("already active")) {
                status = HttpStatus.CONFLICT;
                statusCode = 409;
            }
        }

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .status(statusCode)
                        .message(message != null ? message : "An error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex); 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .status(500)
                .message("An unexpected error occurred. Please try again later")
                .timestamp(LocalDateTime.now())
                .build());
    
    }
}