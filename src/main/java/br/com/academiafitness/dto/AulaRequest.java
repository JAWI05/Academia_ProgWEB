package br.com.academiafitness.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Entrada da API: só os campos que o cliente pode definir (sem id, status ou versão). */
public record AulaRequest(
        @NotNull(message = "esporteId é obrigatório") Long esporteId,
        @NotNull(message = "personalId é obrigatório") Long personalId,
        @NotNull(message = "unidadeId é obrigatório") Long unidadeId,
        @NotNull(message = "inicio é obrigatório") @Future(message = "inicio deve estar no futuro") LocalDateTime inicio,
        @NotNull(message = "duracaoMin é obrigatório")
        @Min(value = 15, message = "duracaoMin mínima é 15") @Max(value = 240, message = "duracaoMin máxima é 240") Integer duracaoMin,
        @NotNull(message = "capacidade é obrigatória")
        @Min(value = 1, message = "capacidade mínima é 1") @Max(value = 100, message = "capacidade máxima é 100") Integer capacidade
) {
}
