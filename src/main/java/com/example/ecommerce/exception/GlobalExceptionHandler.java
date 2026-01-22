package com.example.ecommerce.exception;

import com.mongodb.MongoCommandException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return buildValidationResponse(ex);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionException(TransactionSystemException ex) {
        if (ex.getRootCause() instanceof ConstraintViolationException constraintEx) {
            return buildValidationResponse(constraintEx);
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction failed internally");
    }

    @ExceptionHandler(MongoCommandException.class)
    public ResponseEntity<Map<String, Object>> handleMongoCommandException(MongoCommandException ex) {
        if (ex.getCode() == 13){
            return buildErrorResponse(HttpStatus.FORBIDDEN,
                    "Database error: User lacks 'insert' permissions for the specified collection.");
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database command failed: " + ex.getErrorMessage());
    }


    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        return new ResponseEntity<>(body, status);
    }

    private ResponseEntity<Map<String, Object>> buildValidationResponse(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String shortFieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            fieldErrors.put(shortFieldName, violation.getMessage());
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("validationErrors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(io.jsonwebtoken.security.SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(SignatureException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Your session has expired or the security key has changed. Please log in again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
        }

        return buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException ex) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Your session has expired. Please log in again."
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
