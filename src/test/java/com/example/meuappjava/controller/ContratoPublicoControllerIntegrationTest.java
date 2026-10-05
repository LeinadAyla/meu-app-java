package com.example.meuappjava.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Testcontainers(disabledWithoutDocker = true)
class ContratoPublicoControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("audita.demo.jwt-secret", () -> "integration-test-signing-key-32-bytes-minimum");
        registry.add("audita.demo.passwords.ingestor", () -> "integration-ingestor-password");
        registry.add("audita.demo.passwords.auditor", () -> "integration-auditor-password");
        registry.add("audita.demo.passwords.admin", () -> "integration-admin-password");
    }

    @Test
    @WithMockUser(username = "auditor-teste", roles = {"INGESTOR", "AUDITOR"})
    void criaContratoCalculaRiscoERegistraAuditoria() throws Exception {
        String resposta = mockMvc.perform(post("/api/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroProcesso": "PROC-2025-1",
                                  "numeroContrato": "CONT-2025-1",
                                  "objeto": "Serviço de tecnologia",
                                  "orgaoContratante": "Órgão de teste",
                                  "cnpjContratado": "00000000000000",
                                  "nomeContratado": "Fornecedor de teste",
                                  "categoria": "TI",
                                  "valorOrcamento": 100000,
                                  "valorContratado": 120000,
                                  "tipoContratacao": "DISPENSA",
                                  "dataAssinatura": "2025-01-01",
                                  "inicioVigencia": "2025-01-01",
                                  "fimVigencia": "2025-12-31"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelRisco").value("CRITICO"))
                .andExpect(jsonPath("$.detalhesAnomalia", containsString("Contratação direta")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.id");
        mockMvc.perform(get("/api/contratos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/actuator/metrics/auditagov.contratos.suspeitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(1.0));

        org.junit.jupiter.api.Assertions.assertEquals(
                2L, jdbcTemplate.queryForObject("select count(*) from audit_logs", Long.class));
    }

    @Test
    void autenticaAuditorEDaAcessoComJwtAssinado() throws Exception {
        String token = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"auditor","password":"integration-auditor-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = com.jayway.jsonpath.JsonPath.read(token, "$.accessToken");
        mockMvc.perform(get("/api/contratos")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        String ingestorToken = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ingestor","password":"integration-ingestor-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/contratos")
                        .header("Authorization", "Bearer "
                                + com.jayway.jsonpath.JsonPath.read(ingestorToken, "$.accessToken")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"senha-incorreta"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
