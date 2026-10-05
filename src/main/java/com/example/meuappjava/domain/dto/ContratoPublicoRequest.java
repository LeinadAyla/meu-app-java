package com.example.meuappjava.domain.dto;

import com.example.meuappjava.domain.enums.TipoContratacao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoPublicoRequest(
        @NotBlank @Size(max = 80) String numeroProcesso,
        @NotBlank @Size(max = 80) String numeroContrato,
        @NotBlank @Size(max = 2000) String objeto,
        @NotBlank @Size(max = 200) String orgaoContratante,
        @NotBlank @Size(max = 18) String cnpjContratado,
        @NotBlank @Size(max = 200) String nomeContratado,
        @NotBlank @Size(max = 120) String categoria,
        @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal valorOrcamento,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal valorContratado,
        @NotNull TipoContratacao tipoContratacao,
        @NotNull @PastOrPresent LocalDate dataAssinatura,
        @NotNull LocalDate inicioVigencia,
        @NotNull LocalDate fimVigencia) {

    @AssertTrue(message = "fimVigencia deve ser igual ou posterior a inicioVigencia")
    public boolean isVigenciaValida() {
        return inicioVigencia == null || fimVigencia == null || !fimVigencia.isBefore(inicioVigencia);
    }
}
