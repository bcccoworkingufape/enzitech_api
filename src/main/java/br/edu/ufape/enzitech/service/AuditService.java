package br.edu.ufape.enzitech.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.edu.ufape.enzitech.model.AuditLog;
import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.repository.AuditLogRepository;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final AuditLogRepository auditLogRepository;

    public void record(AuditAction action, int statusCode, String reason) {
        UUID userId = null;
        String userEmail = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details) {
            userId = details.getUser().getId();
            userEmail = details.getUser().getEmail();
        }

        save(action, statusCode, userId, userEmail, reason);
    }

    public void record(AuditAction action, int statusCode, String userEmail, String reason) {
        save(action, statusCode, null, userEmail, reason);
    }

    // A auditoria nunca pode derrubar a requisição que está sendo auditada.
    private void save(AuditAction action, int statusCode, UUID userId, String userEmail, String reason) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action.getCode());
            entry.setResult(action.getResult());
            entry.setStatusCode(statusCode);
            entry.setUserId(userId);
            entry.setUserEmail(truncate(userEmail, 255));

            Map<String, String> details = new LinkedHashMap<>();
            details.put("motivo", truncate(reason, 500));

            HttpServletRequest request = currentRequest();
            if (request != null) {
                entry.setIpAddress(truncate(request.getRemoteAddr(), 45));
                entry.setHttpMethod(truncate(request.getMethod(), 10));
                entry.setEndpoint(truncate(request.getRequestURI(), 500));
                details.put("userAgent", truncate(request.getHeader("User-Agent"), 255));
            }

            entry.setDetails(JSON.writeValueAsString(details));
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Falha ao registrar auditoria: action={}", action.getCode(), e);
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
