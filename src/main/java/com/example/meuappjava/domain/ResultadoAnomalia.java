package com.example.meuappjava.domain;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.util.List;

public record ResultadoAnomalia(NivelRisco nivelRisco, List<String> alertas) {

    public ResultadoAnomalia {
        alertas = List.copyOf(alertas);
    }

    public boolean suspeito() {
        return nivelRisco == NivelRisco.ALTO || nivelRisco == NivelRisco.CRITICO;
    }

    public String detalhes() {
        return String.join("; ", alertas);
    }
}
