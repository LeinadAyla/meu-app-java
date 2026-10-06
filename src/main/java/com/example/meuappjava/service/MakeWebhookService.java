package com.example.meuappjava.service;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MakeWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MakeWebhookService.class);

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public MakeWebhookService(
            RestTemplate makeWebhookRestTemplate,
            @Value("${app.webhook.make-url}") String webhookUrl) {
        this.restTemplate = makeWebhookRestTemplate;
        this.webhookUrl = webhookUrl;
    }

    public void enviarAlerta(
            String id,
            String orgao,
            String fornecedor,
            BigDecimal valor,
            NivelRisco nivelRisco,
            String anomalia) {
        if (nivelRisco != NivelRisco.ALTO && nivelRisco != NivelRisco.CRITICO) {
            return;
        }

        MakeWebhookPayload payload = new MakeWebhookPayload(
                id,
                orgao,
                fornecedor,
                valor,
                nivelRisco.name(),
                anomalia,
                criarMensagemSocial(orgao, valor, nivelRisco));
        try {
            restTemplate.postForEntity(webhookUrl, payload, Void.class);
        } catch (RestClientException exception) {
            String detalhe = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage().replace(webhookUrl, "[URL REDACTED]");
            LOGGER.error("Falha ao enviar alerta ao webhook do Make para {}: {}", id, detalhe);
        }
    }

    private String criarMensagemSocial(String orgao, BigDecimal valor, NivelRisco nivelRisco) {
        NumberFormat formatador = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        formatador.setMinimumFractionDigits(2);
        formatador.setMaximumFractionDigits(2);
        return "🚨 ALERTA AUDITAGOV: Contrato de R$ " + formatador.format(valor)
                + " identificado com risco " + nivelRisco.name() + " na " + orgao + ".";
    }

    private record MakeWebhookPayload(
            String id,
            String orgao,
            String fornecedor,
            BigDecimal valor,
            String nivelRisco,
            String anomalia,
            String mensagemSocial) {
    }
}
