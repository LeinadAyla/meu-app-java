package com.example.meuappjava.domain.dto;

import com.example.meuappjava.domain.enums.TipoContratacao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SimulacaoRiscoRequest(
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal valor,
        @NotBlank @Size(max = 120) String categoria,
        @NotNull @Min(1) @Max(1200) Integer duracaoMeses,
        @NotBlank @Size(max = 200) String fornecedor,
        @NotNull TipoContratacao tipoContratacao) {
}
