package com.example.meuappjava.service;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

class MakeWebhookServiceTest {

    private static final String WEBHOOK_URL = "https://hook.example.test/make";

    @Test
    void enviaJsonDeAlertaComMensagemFormatadaEmPortugues() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "id": "contrato-1",
                          "orgao": "Prefeitura de Exemplo",
                          "fornecedor": "Fornecedor Suspeito LTDA",
                          "valor": 72000.00,
                          "nivelRisco": "ALTO",
                          "anomalia": "Valor 40% acima da media da categoria",
                          "mensagemSocial": "🚨 ALERTA AUDITAGOV: Contrato de R$ 72.000,00 identificado com risco ALTO na Prefeitura de Exemplo."
                        }
                        """))
                .andRespond(withSuccess());

        MakeWebhookService service = new MakeWebhookService(restTemplate, WEBHOOK_URL);
        service.enviarAlerta(
                "contrato-1",
                "Prefeitura de Exemplo",
                "Fornecedor Suspeito LTDA",
                new BigDecimal("72000.00"),
                NivelRisco.ALTO,
                "Valor 40% acima da media da categoria");

        server.verify();
    }

    @Test
    void naoEnviaAlertaParaRiscoMedio() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        MakeWebhookService service = new MakeWebhookService(restTemplate, WEBHOOK_URL);
        service.enviarAlerta(
                "contrato-2",
                "Prefeitura de Exemplo",
                "Fornecedor",
                new BigDecimal("1000.00"),
                NivelRisco.MEDIO,
                "Sem alerta crítico");

        server.verify();
    }

    @Test
    void publicaContratoSobDemandaMesmoQuandoRiscoNaoEAlto() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "id": "contrato-3",
                          "orgao": "Prefeitura de Teste",
                          "fornecedor": "Empresa QA LTDA",
                          "valor": 150000.00,
                          "nivelRisco": "MEDIO",
                          "anomalia": "Análise preventiva",
                          "mensagemSocial": "Publicação AuditaGov: contrato PROC-3 de R$ 150.000,00 em Prefeitura de Teste, classificado com risco MEDIO."
                        }
                        """))
                .andRespond(withSuccess());

        MakeWebhookService service = new MakeWebhookService(restTemplate, WEBHOOK_URL);
        service.publicarContrato(
                "contrato-3",
                "PROC-3",
                "Prefeitura de Teste",
                "Empresa QA LTDA",
                new BigDecimal("150000.00"),
                NivelRisco.MEDIO,
                "Análise preventiva");

        server.verify();
    }

    @Test
    void sinalizaFalhaQuandoWebhookRecusaPublicacao() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(WEBHOOK_URL))
                .andRespond(withStatus(HttpStatus.GONE));

        MakeWebhookService service = new MakeWebhookService(restTemplate, WEBHOOK_URL);
        org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> service.publicarContrato(
                        "contrato-4",
                        "PROC-4",
                        "Prefeitura de Teste",
                        "Empresa QA LTDA",
                        new BigDecimal("150000.00"),
                        NivelRisco.CRITICO,
                        "Análise preventiva"));

        server.verify();
    }
}
