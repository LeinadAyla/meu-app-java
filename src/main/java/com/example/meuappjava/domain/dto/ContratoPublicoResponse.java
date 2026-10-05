package com.example.meuappjava.domain.dto;

import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.enums.NivelRisco;
import com.example.meuappjava.domain.enums.TipoContratacao;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoPublicoResponse(
        UUID id,
        String numeroProcesso,
        String numeroContrato,
        String objeto,
        String orgaoContratante,
        String cnpjContratado,
        String nomeContratado,
        String categoria,
        BigDecimal valorOrcamento,
        BigDecimal valorContratado,
        TipoContratacao tipoContratacao,
        NivelRisco nivelRisco,
        String detalhesAnomalia,
        LocalDate dataAssinatura,
        LocalDate inicioVigencia,
        LocalDate fimVigencia,
        Instant criadoEm) {

    public static ContratoPublicoResponse from(ContratoPublico contrato) {
        return new ContratoPublicoResponse(
                contrato.getId(),
                contrato.getNumeroProcesso(),
                contrato.getNumeroContrato(),
                contrato.getObjeto(),
                contrato.getOrgaoContratante(),
                contrato.getCnpjContratado(),
                contrato.getNomeContratado(),
                contrato.getCategoria(),
                contrato.getValorOrcamento(),
                contrato.getValorContratado(),
                contrato.getTipoContratacao(),
                contrato.getNivelRisco(),
                contrato.getDetalhesAnomalia(),
                contrato.getDataAssinatura(),
                contrato.getInicioVigencia(),
                contrato.getFimVigencia(),
                contrato.getCriadoEm());
    }
}
