package com.example.meuappjava.domain.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalContratos,
        long contratosSuspeitos,
        BigDecimal valorAuditado,
        Map<String, Long> anomaliasPorCategoria) {

    public DashboardSummaryResponse {
        anomaliasPorCategoria = Map.copyOf(anomaliasPorCategoria);
    }
}
