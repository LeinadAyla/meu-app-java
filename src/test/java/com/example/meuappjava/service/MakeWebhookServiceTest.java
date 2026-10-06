package com.example.meuappjava.service;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

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
}
