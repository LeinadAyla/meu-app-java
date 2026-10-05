package com.example.meuappjava.metrics;

import com.example.meuappjava.domain.event.ContratoSuspeitoRegistrado;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MetricasAuditaGov {

    private final MeterRegistry meterRegistry;

    public MetricasAuditaGov(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @TransactionalEventListener
    public void registrarContratoSuspeito(ContratoSuspeitoRegistrado evento) {
        meterRegistry.counter(
                        "auditagov.contratos.suspeitos",
                        "nivel_risco",
                        evento.nivelRisco().name())
                .increment();
    }
}
