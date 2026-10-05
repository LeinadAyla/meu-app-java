package com.example.meuappjava.domain.event;

import com.example.meuappjava.domain.enums.NivelRisco;
import java.util.UUID;

public record ContratoSuspeitoRegistrado(UUID contratoId, NivelRisco nivelRisco) {
}
