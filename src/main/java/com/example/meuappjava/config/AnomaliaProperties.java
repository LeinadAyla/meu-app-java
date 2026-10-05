package com.example.meuappjava.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audita.anomalias")
public record AnomaliaProperties(
        BigDecimal multiplicadorMediaAlto,
        BigDecimal multiplicadorMediaCritico,
        BigDecimal fatorOrcamentoMedio,
        BigDecimal fatorOrcamentoAlto,
        BigDecimal limiteContratacaoDiretaAlto,
        BigDecimal multiplicadorContratacaoDiretaCritico) {
}
