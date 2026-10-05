package com.example.meuappjava.repository;

import com.example.meuappjava.domain.AuditLog;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface AuditLogRepository extends Repository<AuditLog, UUID> {

    <S extends AuditLog> S save(S auditLog);
}
