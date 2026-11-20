package br.com.academiafitness.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static br.com.academiafitness.model.StatusAula.*;
import static org.junit.jupiter.api.Assertions.*;

class StatusAulaTest {

    @ParameterizedTest
    @CsvSource({
            "AGENDADA, EM_ANDAMENTO",
            "AGENDADA, CANCELADA",
            "EM_ANDAMENTO, CONCLUIDA"
    })
    void transicoesValidas(StatusAula origem, StatusAula destino) {
        assertTrue(origem.podeIrPara(destino));
    }

    @ParameterizedTest
    @CsvSource({
            "AGENDADA, CONCLUIDA",
            "AGENDADA, AGENDADA",
            "EM_ANDAMENTO, AGENDADA",
            "EM_ANDAMENTO, CANCELADA",
            "CONCLUIDA, AGENDADA",
            "CONCLUIDA, CANCELADA",
            "CANCELADA, AGENDADA",
            "CANCELADA, EM_ANDAMENTO"
    })
    void transicoesInvalidas(StatusAula origem, StatusAula destino) {
        assertFalse(origem.podeIrPara(destino));
    }

    @Test
    void estadosFinaisNaoTemProximos() {
        assertTrue(CONCLUIDA.proximos().isEmpty());
        assertTrue(CANCELADA.proximos().isEmpty());
    }
}
