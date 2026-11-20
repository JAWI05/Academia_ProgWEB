package br.com.academiafitness.model;

import java.util.EnumSet;
import java.util.Set;

/** Ciclo de vida de uma aula. CONCLUIDA e CANCELADA são estados finais. */
public enum StatusAula {
    AGENDADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA;

    public Set<StatusAula> proximos() {
        return switch (this) {
            case AGENDADA -> EnumSet.of(EM_ANDAMENTO, CANCELADA);
            case EM_ANDAMENTO -> EnumSet.of(CONCLUIDA);
            case CONCLUIDA, CANCELADA -> EnumSet.noneOf(StatusAula.class);
        };
    }

    public boolean podeIrPara(StatusAula destino) {
        return proximos().contains(destino);
    }
}
