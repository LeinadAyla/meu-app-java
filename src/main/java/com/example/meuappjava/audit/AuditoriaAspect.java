package com.example.meuappjava.audit;

import com.example.meuappjava.domain.dto.ContratoPublicoResponse;
import com.example.meuappjava.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditoriaAspect {

    private final AuditLogService auditLogService;

    public AuditoriaAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @AfterReturning(
            pointcut = "@annotation(com.example.meuappjava.audit.Auditado)",
            returning = "resultado")
    public void registrar(JoinPoint joinPoint, Object resultado) {
        Auditado auditado = ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(Auditado.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuario = authentication == null ? "desconhecido" : authentication.getName();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        auditLogService.registrar(
                usuario,
                auditado.value(),
                request.getRemoteAddr(),
                detalhes(joinPoint, resultado));
    }

    private String detalhes(JoinPoint joinPoint, Object resultado) {
        if (resultado instanceof org.springframework.http.ResponseEntity<?> response
                && response.getBody() instanceof ContratoPublicoResponse contrato) {
            return "contratoId=" + contrato.id();
        }
        String recurso = Arrays.stream(joinPoint.getArgs())
                .filter(UUID.class::isInstance)
                .map(UUID.class::cast)
                .map(UUID::toString)
                .findFirst()
                .orElse("consulta");
        return "recurso=" + recurso;
    }
}
