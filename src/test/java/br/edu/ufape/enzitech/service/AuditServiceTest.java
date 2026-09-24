package br.edu.ufape.enzitech.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.edu.ufape.enzitech.model.AuditLog;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.repository.AuditLogRepository;
import br.edu.ufape.enzitech.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/.env");
        request.setRemoteAddr("34.187.61.132");
        request.addHeader("User-Agent", "bot/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void recordsUnauthenticatedAccessWithRequestData() {
        auditService.record(AuditAction.NAO_AUTORIZADO, 401, "InsufficientAuthenticationException");

        AuditLog saved = captureSaved();
        assertThat(saved.getAction()).isEqualTo("AUTH.NAO.AUTORIZADO");
        assertThat(saved.getResult()).isEqualTo("NEGADO");
        assertThat(saved.getStatusCode()).isEqualTo(401);
        assertThat(saved.getHttpMethod()).isEqualTo("GET");
        assertThat(saved.getEndpoint()).isEqualTo("/api/.env");
        assertThat(saved.getIpAddress()).isEqualTo("34.187.61.132");
        assertThat(saved.getUserId()).isNull();
        assertThat(saved.getUserEmail()).isNull();
        assertThat(saved.getDetails())
                .contains("\"motivo\":\"InsufficientAuthenticationException\"")
                .contains("\"userAgent\":\"bot/1.0\"");
    }

    @Test
    void recordsAuthenticatedUserFromSecurityContext() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("pesquisador@enzitech.com");
        CustomUserDetails principal = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        auditService.record(AuditAction.ACESSO_NEGADO, 403, "AccessDeniedException");

        AuditLog saved = captureSaved();
        assertThat(saved.getAction()).isEqualTo("AUTH.ACESSO.NEGADO");
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getUserEmail()).isEqualTo("pesquisador@enzitech.com");
    }

    @Test
    void recordsAttemptedEmailOnLoginFailure() {
        auditService.record(AuditAction.LOGIN_FALHOU, 400, "admin@admin.com", "BadCredentialsException");

        AuditLog saved = captureSaved();
        assertThat(saved.getAction()).isEqualTo("AUTH.LOGIN.FALHOU");
        assertThat(saved.getUserEmail()).isEqualTo("admin@admin.com");
        assertThat(saved.getUserId()).isNull();
    }

    @Test
    void recordsWithoutRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        auditService.record(AuditAction.ERRO, 500, "NullPointerException: x");

        AuditLog saved = captureSaved();
        assertThat(saved.getResult()).isEqualTo("ERRO");
        assertThat(saved.getEndpoint()).isNull();
        assertThat(saved.getIpAddress()).isNull();
    }

    @Test
    void truncatesLongValuesToColumnSize() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + "a".repeat(600));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        auditService.record(AuditAction.NAO_AUTORIZADO, 401, "x");

        assertThat(captureSaved().getEndpoint()).hasSize(500);
    }

    @Test
    void neverPropagatesRepositoryFailures() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("db down"));

        assertThatCode(() -> auditService.record(AuditAction.ERRO, 500, "x")).doesNotThrowAnyException();
    }

    private AuditLog captureSaved() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        return captor.getValue();
    }
}
