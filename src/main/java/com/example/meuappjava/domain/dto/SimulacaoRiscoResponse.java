package com.example.meuappjava.domain.dto;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.util.List;

public record SimulacaoRiscoResponse(
        int score,
        NivelRisco nivelRisco,
        boolean exigeRevisao,
        String fornecedor,
        int duracaoMeses,
        List<String> sinais,
        List<String> limitacoes) {

    public SimulacaoRiscoResponse {
        sinais = List.copyOf(sinais);
        limitacoes = List.copyOf(limitacoes);
    }
}
