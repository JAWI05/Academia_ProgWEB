package br.com.academiafitness.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Montagem do padrão LIKE: minúsculas, trim e curingas do usuário tratados como texto comum. */
class AulaSpecsTest {

    @Test
    void usaMinusculasETiraEspacosDasPontas() {
        assertEquals("%carla%", AulaSpecs.padraoLike("  CARLA "));
    }

    @Test
    void escapaPorcentagem() {
        assertEquals("%100\\%%", AulaSpecs.padraoLike("100%"));
    }

    @Test
    void escapaSublinhado() {
        assertEquals("%a\\_b%", AulaSpecs.padraoLike("a_b"));
    }

    @Test
    void escapaABarraInvertida() {
        assertEquals("%a\\\\b%", AulaSpecs.padraoLike("a\\b"));
    }
}
