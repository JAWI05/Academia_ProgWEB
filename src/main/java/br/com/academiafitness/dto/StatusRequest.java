package br.com.academiafitness.dto;

import br.com.academiafitness.model.StatusAula;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "status é obrigatório") StatusAula status) {
}
