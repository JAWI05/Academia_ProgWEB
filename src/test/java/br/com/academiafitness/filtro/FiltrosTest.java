package br.com.academiafitness.filtro;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Regras simples dos objetos de filtro (sem Spring). */
class FiltrosTest {

    private final LocalDate hoje = LocalDate.of(2026, 11, 20);

    @Test
    void textoEmBrancoNaoContaComoFiltro() {
        AulaFiltro f = new AulaFiltro();
        assertFalse(f.temTexto());
        f.setQ("   ");
        assertFalse(f.temTexto());
        f.setQ("carla");
        assertTrue(f.temTexto());
    }

    @Test
    void periodoDeAulasInvertidoSoQuandoInicioPosteriorAoFim() {
        AulaFiltro f = new AulaFiltro();
        assertFalse(f.periodoInvertido());

        f.setDe(hoje);
        assertFalse(f.periodoInvertido());

        f.setAte(hoje);
        assertFalse(f.periodoInvertido(), "mesmo dia é um período válido");

        f.setAte(hoje.minusDays(1));
        assertTrue(f.periodoInvertido());
    }

    @Test
    void situacaoNulaDeClienteViraTodos() {
        ClienteFiltro f = new ClienteFiltro();
        assertEquals(SituacaoCliente.TODOS, f.getSituacao());

        f.setSituacao(SituacaoCliente.SEM_INSCRICAO);
        assertEquals(SituacaoCliente.SEM_INSCRICAO, f.getSituacao());

        f.setSituacao(null);
        assertEquals(SituacaoCliente.TODOS, f.getSituacao());
    }

    @Test
    void objetivoEmBrancoNaoContaComoFiltro() {
        ClienteFiltro f = new ClienteFiltro();
        assertFalse(f.temObjetivo());
        f.setObjetivo(" ");
        assertFalse(f.temObjetivo());
        f.setObjetivo("Emagrecimento");
        assertTrue(f.temObjetivo());
    }

    @Test
    void periodoDeCadastroInvertido() {
        ClienteFiltro f = new ClienteFiltro();
        f.setCadastroDe(hoje);
        f.setCadastroAte(hoje.minusDays(5));
        assertTrue(f.periodoInvertido());

        f.setCadastroAte(hoje.plusDays(5));
        assertFalse(f.periodoInvertido());
    }

    @Test
    void cadaSituacaoTemRotuloParaATela() {
        for (SituacaoCliente s : SituacaoCliente.values()) {
            assertNotNull(s.getRotulo());
            assertFalse(s.getRotulo().isBlank());
        }
    }
}
