package br.edu.ufape.enzitech.exception;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    static final String INTERNAL_ERROR_MESSAGE = "Erro interno do servidor.";

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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        auditService.record(AuditAction.ACESSO_NEGADO, HttpStatus.FORBIDDEN.value(), ex.getClass().getSimpleName());
        return error(HttpStatus.FORBIDDEN, "Acesso negado.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Corpo da requisição inválido: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parâmetro inválido: {}", ex.getName());
        return error(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusExceptions(ResponseStatusException ex) {
        log.warn("Erro [{}]: {}", ex.getStatusCode(), ex.getReason());
        audit(ex.getStatusCode().value(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason() != null ? ex.getReason() : ""));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        String message;
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Erro inesperado não tratado", ex);
            message = INTERNAL_ERROR_MESSAGE;
        } else {
            log.warn("Erro [{}]: {}", status, ex.getMessage());
            message = ex.getMessage();
        }

        audit(status.value(), ex);
        return error(status, message);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    private void audit(int status, Throwable ex) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        if (status == HttpStatus.FORBIDDEN.value()) {
            auditService.record(AuditAction.ACESSO_NEGADO, status, reason);
        } else if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            auditService.record(AuditAction.LIMITE_EXCEDIDO, status, reason);
        } else if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            auditService.record(AuditAction.ERRO, status, reason);
        }
    }
}
