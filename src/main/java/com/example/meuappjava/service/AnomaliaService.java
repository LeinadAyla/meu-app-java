package com.example.meuappjava.service;

import com.example.meuappjava.config.AnomaliaProperties;
import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.ResultadoAnomalia;
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
        List<String> alertas = new ArrayList<>();
        NivelRisco nivel = NivelRisco.BAIXO;

        BigDecimal mediaCategoria = repository.mediaValorPorCategoria(contrato.getCategoria())
                .map(media -> BigDecimal.valueOf(media))
                .orElse(null);
        if (mediaCategoria != null && mediaCategoria.signum() > 0) {
            BigDecimal limiteCritico = mediaCategoria.multiply(properties.multiplicadorMediaCritico());
            BigDecimal limiteAlto = mediaCategoria.multiply(properties.multiplicadorMediaAlto());
            if (contrato.getValorContratado().compareTo(limiteCritico) >= 0) {
                alertas.add("Valor contratado igual ou superior ao limite crítico da média da categoria.");
                nivel = NivelRisco.CRITICO;
            } else if (contrato.getValorContratado().compareTo(limiteAlto) > 0) {
                alertas.add("Valor contratado acima do limite de alerta da média da categoria.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            }
        }

        BigDecimal orcamento = contrato.getValorOrcamento();
        if (orcamento != null && orcamento.signum() > 0) {
            if (contrato.getValorContratado().compareTo(
                    orcamento.multiply(properties.fatorOrcamentoAlto())) > 0) {
                alertas.add("Valor contratado excede significativamente o orçamento informado.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            } else if (contrato.getValorContratado().compareTo(
                    orcamento.multiply(properties.fatorOrcamentoMedio())) > 0) {
                alertas.add("Valor contratado excede o orçamento informado.");
                nivel = elevar(nivel, NivelRisco.MEDIO);
            }
        }

        if (contratacaoDireta(contrato.getTipoContratacao())) {
            BigDecimal limiteAlto = properties.limiteContratacaoDiretaAlto();
            if (contrato.getValorContratado().compareTo(
                    limiteAlto.multiply(properties.multiplicadorContratacaoDiretaCritico())) >= 0) {
                alertas.add("Contratação direta acima do limite crítico configurado.");
                nivel = elevar(nivel, NivelRisco.CRITICO);
            } else if (contrato.getValorContratado().compareTo(limiteAlto) >= 0) {
                alertas.add("Contratação direta acima do limite de alerta configurado.");
                nivel = elevar(nivel, NivelRisco.ALTO);
            }
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
