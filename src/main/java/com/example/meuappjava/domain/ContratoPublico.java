package com.example.meuappjava.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.example.meuappjava.domain.enums.NivelRisco;
import com.example.meuappjava.domain.enums.TipoContratacao;

@Entity
@Table(name = "contratos_publicos")
public class ContratoPublico {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String numeroProcesso;

    @Column(nullable = false, length = 80)
    private String numeroContrato;

    @Column(nullable = false, length = 2000)
    private String objeto;

    @Column(nullable = false, length = 200)
    private String orgaoContratante;

    @Column(nullable = false, length = 18)
    private String cnpjContratado;

    @Column(nullable = false, length = 200)
    private String nomeContratado;

    @Column(nullable = false, length = 120)
    private String categoria;

    @Column(precision = 17, scale = 2)
    private BigDecimal valorOrcamento;

    @Column(nullable = false, precision = 17, scale = 2)
    private BigDecimal valorContratado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoContratacao tipoContratacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelRisco nivelRisco = NivelRisco.BAIXO;

    @Column(length = 2000)
    private String detalhesAnomalia;

    @Column(nullable = false)
    private LocalDate dataAssinatura;

    @Column(nullable = false)
    private LocalDate inicioVigencia;

    @Column(nullable = false)
    private LocalDate fimVigencia;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected ContratoPublico() {
    }

    public ContratoPublico(
            String numeroProcesso,
            String numeroContrato,
            String objeto,
            String orgaoContratante,
            String cnpjContratado,
            String nomeContratado,
            String categoria,
            BigDecimal valorOrcamento,
            BigDecimal valorContratado,
            TipoContratacao tipoContratacao,
            LocalDate dataAssinatura,
            LocalDate inicioVigencia,
            LocalDate fimVigencia) {
        this.numeroProcesso = numeroProcesso;
        this.numeroContrato = numeroContrato;
        this.objeto = objeto;
        this.orgaoContratante = orgaoContratante;
        this.cnpjContratado = cnpjContratado;
        this.nomeContratado = nomeContratado;
        this.categoria = categoria;
        this.valorOrcamento = valorOrcamento;
        this.valorContratado = valorContratado;
        this.tipoContratacao = tipoContratacao;
        this.dataAssinatura = dataAssinatura;
        this.inicioVigencia = inicioVigencia;
        this.fimVigencia = fimVigencia;
    }

    public void registrarRisco(NivelRisco nivelRisco, String detalhesAnomalia) {
        this.nivelRisco = nivelRisco;
        this.detalhesAnomalia = detalhesAnomalia;
    }

    @PrePersist
    void prepararPersistencia() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public String getObjeto() {
        return objeto;
    }

    public String getOrgaoContratante() {
        return orgaoContratante;
    }

    public String getCnpjContratado() {
        return cnpjContratado;
    }

    public String getNomeContratado() {
        return nomeContratado;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getValorOrcamento() {
        return valorOrcamento;
    }

    public BigDecimal getValorContratado() {
        return valorContratado;
    }

    public TipoContratacao getTipoContratacao() {
        return tipoContratacao;
    }

    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }

    public String getDetalhesAnomalia() {
        return detalhesAnomalia;
    }

    public LocalDate getDataAssinatura() {
        return dataAssinatura;
    }

    public LocalDate getInicioVigencia() {
        return inicioVigencia;
    }

    public LocalDate getFimVigencia() {
        return fimVigencia;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
