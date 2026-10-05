package com.example.meuappjava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.meuappjava.config.AnomaliaProperties;
import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.ResultadoAnomalia;
import com.example.meuappjava.domain.dto.SimulacaoRiscoRequest;
import com.example.meuappjava.domain.dto.SimulacaoRiscoResponse;
import com.example.meuappjava.domain.enums.NivelRisco;
import com.example.meuappjava.domain.enums.TipoContratacao;
import com.example.meuappjava.repository.ContratoPublicoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnomaliaServiceTest {

    @Mock
    private ContratoPublicoRepository repository;

    private AnomaliaService service;

    @BeforeEach
    void configurar() {
        service = new AnomaliaService(
                repository,
                new AnomaliaProperties(
                        new BigDecimal("1.5"),
                        new BigDecimal("2.0"),
                        new BigDecimal("1.1"),
                        new BigDecimal("1.2"),
                        new BigDecimal("50000"),
                        new BigDecimal("2.0")));
    }

    @Test
    void classificaDiscrepanciaCriticaEmRelacaoAMediaDaCategoria() {
        when(repository.mediaValorPorCategoria("TI")).thenReturn(Optional.of(100.0));

        ResultadoAnomalia resultado = service.analisar(contrato(
                "200", null, TipoContratacao.LICITACAO));

        assertEquals(NivelRisco.CRITICO, resultado.nivelRisco());
        assertEquals(1, resultado.alertas().size());
    }

    @Test
    void sinalizaValorAcimaDoOrcamento() {
        when(repository.mediaValorPorCategoria("TI")).thenReturn(Optional.empty());

        ResultadoAnomalia resultado = service.analisar(contrato(
                "115", "100", TipoContratacao.LICITACAO));

        assertEquals(NivelRisco.MEDIO, resultado.nivelRisco());
        assertEquals(1, resultado.alertas().size());
    }

    @Test
    void classificaDispensaElevadaComoAltoRisco() {
        when(repository.mediaValorPorCategoria("TI")).thenReturn(Optional.empty());

        ResultadoAnomalia resultado = service.analisar(contrato(
                "50000", null, TipoContratacao.DISPENSA));

        assertEquals(NivelRisco.ALTO, resultado.nivelRisco());
        assertEquals(1, resultado.alertas().size());
    }

    @Test
    void permaneceBaixoQuandoNaoHaRegraDisparada() {
        when(repository.mediaValorPorCategoria("TI")).thenReturn(Optional.of(100.0));

        ResultadoAnomalia resultado = service.analisar(contrato(
                "100", "100", TipoContratacao.LICITACAO));

        assertEquals(NivelRisco.BAIXO, resultado.nivelRisco());
        assertEquals(0, resultado.alertas().size());
    }

    @Test
    void simulaRiscoESinalizaVigenciaLongaSemPersistirContrato() {
        when(repository.mediaValorPorCategoria("Nova categoria")).thenReturn(Optional.empty());

        SimulacaoRiscoResponse resposta = service.simular(new SimulacaoRiscoRequest(
                new BigDecimal("1000"),
                "Nova categoria",
                72,
                "Fornecedor de teste",
                TipoContratacao.LICITACAO));

        assertEquals(NivelRisco.MEDIO, resposta.nivelRisco());
        assertEquals(35, resposta.score());
        assertEquals("Fornecedor de teste", resposta.fornecedor());
        assertTrue(resposta.exigeRevisao());
        assertEquals(1, resposta.sinais().size());
    }

    private ContratoPublico contrato(String valor, String orcamento, TipoContratacao tipo) {
        return new ContratoPublico(
                "PROC-1",
                "CONT-1",
                "Objeto de teste",
                "Órgão teste",
                "00000000000000",
                "Fornecedor teste",
                "TI",
                orcamento == null ? null : new BigDecimal(orcamento),
                new BigDecimal(valor),
                tipo,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));
    }
}
