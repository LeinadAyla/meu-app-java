package com.example.meuappjava.service;

import com.example.meuappjava.config.AnomaliaProperties;
import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.ResultadoAnomalia;
import com.example.meuappjava.domain.dto.SimulacaoRiscoRequest;
import com.example.meuappjava.domain.dto.SimulacaoRiscoResponse;
import com.example.meuappjava.domain.enums.NivelRisco;
import com.example.meuappjava.domain.enums.TipoContratacao;
import com.example.meuappjava.repository.ContratoPublicoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnomaliaService {

    private final ContratoPublicoRepository repository;
    private final AnomaliaProperties properties;

    public AnomaliaService(ContratoPublicoRepository repository, AnomaliaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public ResultadoAnomalia analisar(ContratoPublico contrato) {
        return analisar(
                contrato.getValorContratado(),
                contrato.getCategoria(),
                contrato.getTipoContratacao(),
                contrato.getValorOrcamento(),
                0);
    }

    public SimulacaoRiscoResponse simular(SimulacaoRiscoRequest request) {
        ResultadoAnomalia resultado = analisar(
                request.valor(),
                request.categoria(),
                request.tipoContratacao(),
                null,
                request.duracaoMeses());
        int score = switch (resultado.nivelRisco()) {
            case BAIXO -> 10;
            case MEDIO -> 35;
            case ALTO -> 70;
            case CRITICO -> 90;
        };
        return new SimulacaoRiscoResponse(
                score,
                resultado.nivelRisco(),
                resultado.nivelRisco() != NivelRisco.BAIXO,
                request.fornecedor(),
                request.duracaoMeses(),
                resultado.alertas(),
                List.of(
                        "Estimativa informativa; a classificação oficial ocorre na ingestão.",
                        "A identidade e o histórico cadastral do fornecedor não são verificados nesta simulação.",
                        "Durações superiores a 60 meses geram sinal de revisão preventiva."));
    }

    private ResultadoAnomalia analisar(
            BigDecimal valorContratado,
            String categoria,
            TipoContratacao tipoContratacao,
            BigDecimal valorOrcamento,
            int duracaoMeses) {
        List<String> alertas = new ArrayList<>();
        NivelRisco nivel = NivelRisco.BAIXO;

        BigDecimal mediaCategoria = repository.mediaValorPorCategoria(categoria)
                .map(media -> BigDecimal.valueOf(media))
                .orElse(null);
        if (mediaCategoria != null && mediaCategoria.signum() > 0) {
            BigDecimal limiteCritico = mediaCategoria.multiply(properties.multiplicadorMediaCritico());
            BigDecimal limiteAlto = mediaCategoria.multiply(properties.multiplicadorMediaAlto());
            if (valorContratado.compareTo(limiteCritico) >= 0) {
                alertas.add("Valor contratado igual ou superior ao limite crítico da média da categoria.");
                nivel = NivelRisco.CRITICO;
            } else if (valorContratado.compareTo(limiteAlto) > 0) {
                alertas.add("Valor contratado acima do limite de alerta da média da categoria.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            }
        }

        BigDecimal orcamento = valorOrcamento;
        if (orcamento != null && orcamento.signum() > 0) {
            if (valorContratado.compareTo(orcamento.multiply(properties.fatorOrcamentoAlto())) > 0) {
                alertas.add("Valor contratado excede significativamente o orçamento informado.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            } else if (valorContratado.compareTo(orcamento.multiply(properties.fatorOrcamentoMedio())) > 0) {
                alertas.add("Valor contratado excede o orçamento informado.");
                nivel = elevar(nivel, NivelRisco.MEDIO);
            }
        }

        if (contratacaoDireta(tipoContratacao)) {
            BigDecimal limiteAlto = properties.limiteContratacaoDiretaAlto();
            if (valorContratado.compareTo(
                    limiteAlto.multiply(properties.multiplicadorContratacaoDiretaCritico())) >= 0) {
                alertas.add("Contratação direta acima do limite crítico configurado.");
                nivel = elevar(nivel, NivelRisco.CRITICO);
            } else if (valorContratado.compareTo(limiteAlto) >= 0) {
                alertas.add("Contratação direta acima do limite de alerta configurado.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            }
        }

        if (duracaoMeses > 60) {
            alertas.add("Duração superior a 60 meses; recomenda-se revisar a justificativa e a vigência.");
            nivel = elevar(nivel, NivelRisco.MEDIO);
        }

        return new ResultadoAnomalia(nivel, alertas);
    }

    private boolean contratacaoDireta(TipoContratacao tipo) {
        return tipo == TipoContratacao.DISPENSA
                || tipo == TipoContratacao.INEXIGIBILIDADE
                || tipo == TipoContratacao.CONTRATACAO_DIRETA;
    }

    private NivelRisco elevar(NivelRisco atual, NivelRisco candidato) {
        return candidato.ordinal() > atual.ordinal() ? candidato : atual;
    }
}
