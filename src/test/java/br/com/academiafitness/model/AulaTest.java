package br.com.academiafitness.model;

import br.com.academiafitness.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de negócio da entidade Aula (sem Spring e sem banco): válidos, inválidos e casos-limite. */
class AulaTest {

    private final LocalDateTime inicio = LocalDateTime.of(2030, 5, 10, 18, 0);

    private Aula aula(int capacidade) {
        return new Aula(new Esporte("Spinning", StatusEsporte.ATIVO),
                new PersonalTrainer("Carla", "Hipertrofia", 8, new BigDecimal("120.00")),
                new Unidade("Centro", "São Paulo", "SP"),
                inicio, 60, capacidade);
    }

    private Cliente cliente(long id) {
        Cliente c = new Cliente("Cliente " + id, "c" + id + "@example.com", null);
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private void levarPara(Aula aula, StatusAula destino) {
        switch (destino) {
            case EM_ANDAMENTO -> aula.transitarPara(StatusAula.EM_ANDAMENTO);
            case CONCLUIDA -> {
                aula.transitarPara(StatusAula.EM_ANDAMENTO);
                aula.transitarPara(StatusAula.CONCLUIDA);
            }
            case CANCELADA -> aula.transitarPara(StatusAula.CANCELADA);
            default -> { }
        }
    }

    // ---------- estado inicial

    @Test
    void novaAulaNasceAgendadaSemInscritosECalculaOFim() {
        Aula a = aula(10);

        assertEquals(StatusAula.AGENDADA, a.getStatus());
        assertTrue(a.getInscritos().isEmpty());
        assertEquals(10, a.vagasDisponiveis());
        assertFalse(a.estaLotada());
        assertEquals(inicio.plusMinutes(60), a.getFim());
    }

    // ---------- inscrição

    @Test
    void inscreveAteALotacaoEBloqueiaOExcedente() {
        Aula a = aula(2);
        a.inscrever(cliente(1));
        a.inscrever(cliente(2));

        assertTrue(a.estaLotada());
        assertEquals(0, a.vagasDisponiveis());
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> a.inscrever(cliente(3)));
        assertTrue(erro.getMessage().contains("lotada"));
        assertEquals(2, a.getInscritos().size());
    }

    @Test
    void capacidadeUmEhCasoLimiteValido() {
        Aula a = aula(1);
        a.inscrever(cliente(1));

        assertTrue(a.estaLotada());
        assertEquals(0, a.vagasDisponiveis());
    }

    @Test
    void naoPermiteInscricaoDuplicada() {
        Aula a = aula(5);
        Cliente ana = cliente(1);
        a.inscrever(ana);

        assertThrows(RegraNegocioException.class, () -> a.inscrever(ana));
        assertEquals(1, a.getInscritos().size());
    }

    @ParameterizedTest
    @EnumSource(value = StatusAula.class, names = {"EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"})
    void soAceitaInscricaoEmAulaAgendada(StatusAula status) {
        Aula a = aula(5);
        levarPara(a, status);

        assertThrows(RegraNegocioException.class, () -> a.inscrever(cliente(1)));
        assertTrue(a.getInscritos().isEmpty());
    }

    // ---------- cancelamento de inscrição

    @Test
    void removeInscricaoLiberandoVaga() {
        Aula a = aula(1);
        Cliente ana = cliente(1);
        a.inscrever(ana);

        a.removerInscricao(ana);

        assertFalse(a.estaLotada());
        assertEquals(1, a.vagasDisponiveis());
    }

    @Test
    void naoRemoveInscricaoDeQuemNaoEstaInscrito() {
        Aula a = aula(5);

        assertThrows(RegraNegocioException.class, () -> a.removerInscricao(cliente(9)));
    }

    @Test
    void naoRemoveInscricaoDeAulaJaIniciada() {
        Aula a = aula(5);
        Cliente ana = cliente(1);
        a.inscrever(ana);
        a.transitarPara(StatusAula.EM_ANDAMENTO);

        assertThrows(RegraNegocioException.class, () -> a.removerInscricao(ana));
    }

    // ---------- status

    @Test
    void percorreOCicloDeVidaCompleto() {
        Aula a = aula(5);

        a.transitarPara(StatusAula.EM_ANDAMENTO);
        a.transitarPara(StatusAula.CONCLUIDA);

        assertEquals(StatusAula.CONCLUIDA, a.getStatus());
    }

    @Test
    void transicaoInvalidaInformaOrigemEDestino() {
        Aula a = aula(5);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> a.transitarPara(StatusAula.CONCLUIDA));

        assertTrue(erro.getMessage().contains("AGENDADA"));
        assertTrue(erro.getMessage().contains("CONCLUIDA"));
        assertEquals(StatusAula.AGENDADA, a.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = StatusAula.class, names = {"CONCLUIDA", "CANCELADA"})
    void estadosFinaisNaoSaemDoLugar(StatusAula final_) {
        Aula a = aula(5);
        levarPara(a, final_);

        for (StatusAula destino : StatusAula.values()) {
            assertThrows(RegraNegocioException.class, () -> a.transitarPara(destino));
        }
        assertEquals(final_, a.getStatus());
    }

    // ---------- edição

    @Test
    void capacidadeIgualAosInscritosEhPermitida() {
        Aula a = aula(5);
        a.inscrever(cliente(1));
        a.inscrever(cliente(2));

        assertDoesNotThrow(() -> a.atualizarDados(a.getEsporte(), a.getPersonal(), a.getUnidade(), inicio, 45, 2));
        assertEquals(2, a.getCapacidade());
        assertEquals(45, a.getDuracaoMin());
    }

    @Test
    void capacidadeMenorQueOsInscritosEhRejeitada() {
        Aula a = aula(5);
        a.inscrever(cliente(1));
        a.inscrever(cliente(2));

        assertThrows(RegraNegocioException.class,
                () -> a.atualizarDados(a.getEsporte(), a.getPersonal(), a.getUnidade(), inicio, 60, 1));
        assertEquals(5, a.getCapacidade());
    }

    @Test
    void soEditaAulaAgendada() {
        Aula a = aula(5);
        a.transitarPara(StatusAula.CANCELADA);

        assertThrows(RegraNegocioException.class,
                () -> a.atualizarDados(a.getEsporte(), a.getPersonal(), a.getUnidade(), inicio, 60, 5));
    }
}
