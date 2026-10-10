package br.edu.ufape.enzitech.exception;

import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void preAuthorizeDenialBecomes403() {
        ResponseEntity<Map<String, String>> response = handler.handleAccessDenied(new AuthorizationDeniedException("Access Denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("error", "Acesso negado.");
        verify(auditService).record(eq(AuditAction.ACESSO_NEGADO), eq(403), anyString());
    }

    @Test
    void unexpectedErrorsDoNotLeakInternalMessages() {
        ResponseEntity<Map<String, String>> response =
                handler.handleRuntimeExceptions(new RuntimeException("could not execute statement; SQL [select * from users]"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", GlobalExceptionHandler.INTERNAL_ERROR_MESSAGE);
    }

    @Test
    void rateLimitIsAudited() {
        ResponseEntity<Map<String, String>> response =
                handler.handleResponseStatusExceptions(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(auditService).record(eq(AuditAction.LIMITE_EXCEDIDO), anyInt(), anyString());
    }
}
