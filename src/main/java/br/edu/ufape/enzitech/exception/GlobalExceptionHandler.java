package br.edu.ufape.enzitech.exception;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Erro de validação: {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusExceptions(ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getReason());

        log.warn("Erro [{}]: {}", ex.getStatusCode(), ex.getReason());
        audit(ex.getStatusCode().value(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Erro inesperado não tratado", ex);
        } else {
            log.warn("Erro [{}]: {}", status, ex.getMessage());
        }

        audit(status.value(), ex);
        return ResponseEntity.status(status).body(error);
    }

    private void audit(int status, Throwable ex) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        if (status == HttpStatus.FORBIDDEN.value()) {
            auditService.record(AuditAction.ACESSO_NEGADO, status, reason);
        } else if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            auditService.record(AuditAction.ERRO, status, reason);
        }
    }
}