package com.tokio.financialtransfer.config;

import com.tokio.financialtransfer.dto.ApiResponse;
import com.tokio.financialtransfer.util.LoggingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${server.error.include-stacktrace:never}")
    private String includeStacktrace;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        LoggingContext.set("validationError", "true");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String mainMessage = "Dados inválidos fornecidos";
        if (errors.size() == 1) {
            mainMessage = errors.values().iterator().next();
        }

        LoggingContext.set("validationErrors", errors.toString());
        log.warn("Validation failed: {}", errors);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(mainMessage, errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        LoggingContext.set("businessRuleError", "true");
        LoggingContext.set("error", ex.getMessage());
        
        log.warn("Business rule validation failed: {}", ex.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        LoggingContext.set("unexpectedError", "true");
        LoggingContext.set("error", ex.getMessage());
        
        log.error("Unexpected error occurred", ex);
        
        String message = "prod".equals(activeProfile) 
            ? "Erro interno do servidor" 
            : "Erro interno: " + ex.getMessage();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }
}
