package com.example.meuappjava.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(nullable = false, updatable = false, length = 150)
    private String usuario;

    @Column(nullable = false, updatable = false, length = 45)
    private String ip;

    @Column(nullable = false, updatable = false, length = 100)
    private String acao;

    @Column(nullable = false, updatable = false, length = 2000)
    private String detalhes;

    protected AuditLog() {
    }

    public AuditLog(String usuario, String acao, String ip, String detalhes) {
        this.usuario = usuario;
        this.acao = acao;
        this.ip = ip;
        this.detalhes = detalhes;
    }

    @PrePersist
    private void prepararPersistencia() {
        id = UUID.randomUUID();
        timestamp = Instant.now();
    }

    @PreUpdate
    private void impedirAtualizacao() {
        throw new UnsupportedOperationException("Registros de auditoria são imutáveis.");
    }

    @PreRemove
    private void impedirRemocao() {
        throw new UnsupportedOperationException("Registros de auditoria são imutáveis.");
    }

    public UUID getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAcao() {
        return acao;
    }

    public String getIp() {
        return ip;
    }

    public String getDetalhes() {
        return detalhes;
    }
}
