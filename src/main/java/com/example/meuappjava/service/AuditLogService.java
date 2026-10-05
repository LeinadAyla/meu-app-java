package com.example.meuappjava.service;

import com.example.meuappjava.domain.AuditLog;
import com.example.meuappjava.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String usuario, String acao, String ip, String detalhes) {
        repository.save(new AuditLog(usuario, acao, ip, detalhes));
    }
}
