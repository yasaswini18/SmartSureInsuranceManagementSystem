package com.InsuranceManagementSystem.AdminService.exception;

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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(403)
                        .message("Access denied. Admin privileges required")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException ex
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Admin token has expired. Please login again")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSignature(
            SignatureException ex
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Invalid token signature")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwt(
            MalformedJwtException ex
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .message("Malformed token")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex
    ) {
        String failedService = identifyFailedService(ex.getMessage());
        String bodyMessage = extractFeignBody(ex);

        return switch (ex.status()) {
            case 400 -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .status(400)
                            .message(bodyMessage.isEmpty()
                                    ? "Bad request to " + failedService + ". Check request data"
                                    : bodyMessage)
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 401 -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(401)
                            .message("Authentication failed when calling " + failedService + ". Please login again")
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 403 -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .status(403)
                            .message(failedService + " rejected the admin request. Check token validity")
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 404 -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .status(404)
                            .message(bodyMessage.isEmpty()
                                    ? "Resource not found in " + failedService
                                    : bodyMessage)
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 409 -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .status(409)
                            .message(bodyMessage.isEmpty()
                                    ? "Conflict in " + failedService + ": Resource already exists or invalid state"
                                    : bodyMessage)
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 422 -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ErrorResponse.builder()
                            .status(422)
                            .message(bodyMessage.isEmpty()
                                    ? "Business rule violation in " + failedService
                                    : bodyMessage)
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            case 503, -1 -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ErrorResponse.builder()
                            .status(503)
                            .message(failedService + " is temporarily unavailable. Please try again later")
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());

            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status(500)
                            .message("Error communicating with " + failedService + ": HTTP " + ex.status())
                            .failedService(failedService)
                            .timestamp(LocalDateTime.now())
                            .build());
        };
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(409)
                        .message("Data integrity violation: record already exists")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex
    ) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.builder()
                        .status(405)
                        .message("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint")
                        .failedService("Admin Service")
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
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "unknown"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(400)
                        .message(message)
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        int statusCode = 500;
        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
                statusCode = 404;
            } else if (message.contains("Unauthorized") ||
                       message.contains("expired") ||
                       message.contains("Invalid token")) {
                status = HttpStatus.UNAUTHORIZED;
                statusCode = 401;
            } else if (message.contains("Forbidden") ||
                       message.contains("Access denied")) {
                status = HttpStatus.FORBIDDEN;
                statusCode = 403;
            } else if (message.contains("already exists") ||
                       message.contains("already active") ||
                       message.contains("Invalid claim state") ||
                       message.contains("Invalid state") ||
                       message.contains("Current status:") ||
                       message.contains("Only PENDING") ||
                       message.contains("cannot be") ||
                       message.contains("not allowed")) {
                status = HttpStatus.CONFLICT;
                statusCode = 409;
            } else if (message.contains("unavailable") ||
                       message.contains("try again later")) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                statusCode = 503;
            } else if (message.contains("Invalid") ||
                       message.contains("required") ||
                       message.contains("must be")) {
                status = HttpStatus.BAD_REQUEST;
                statusCode = 400;
            }
        }

        String failedService = identifyFailedService(
                message != null ? message : ""
        );

        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(statusCode)
                        .message(message != null ? message : "An unexpected error occurred")
                        .failedService(failedService)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(500)
                        .message("An unexpected error occurred in Admin Service. Please try again later")
                        .failedService("Admin Service")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    private String extractFeignBody(FeignException ex) {
        try {
            if (ex.responseBody().isPresent()) {
                String body = new String(
                        ex.responseBody().get().array(),
                        java.nio.charset.StandardCharsets.UTF_8
                );
                com.fasterxml.jackson.databind.ObjectMapper mapper =
                        new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node =
                        mapper.readTree(body);
                if (node.has("message")) {
                    return node.get("message").asText();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String identifyFailedService(String message) {
        if (message == null) return "Unknown Service";

        if (message.contains("AuthService") ||
            message.contains("auth-service") ||
            message.contains("Auth service") ||
            message.contains("8081")) {
            return "Auth Service";
        }
        if (message.contains("PolicyService") ||
            message.contains("policy-service") ||
            message.contains("Policy service") ||
            message.contains("8082")) {
            return "Policy Service";
        }
        if (message.contains("ClaimsService") ||
            message.contains("claims-service") ||
            message.contains("Claims service") ||
            message.contains("8083")) {
            return "Claims Service";
        }
        return "Admin Service";
    }
}