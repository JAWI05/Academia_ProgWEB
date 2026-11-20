package br.com.academiafitness.service;

import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.filtro.AulaFiltro;
import br.com.academiafitness.filtro.ClienteFiltro;
import br.com.academiafitness.filtro.SituacaoCliente;
import br.com.academiafitness.model.Cliente;
import br.com.academiafitness.model.StatusAula;
import br.com.academiafitness.repository.AulaRepository;
import br.com.academiafitness.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Filtros de aulas e clientes contra o banco real (H2 em memória) com a base de demonstração. */
@SpringBootTest(properties = {
        "academia.carga.demo=true",
        "spring.datasource.url=jdbc:h2:mem:filtros;DB_CLOSE_DELAY=-1"
})
class FiltrosIT {

    @Autowired AulaService aulaService;
    @Autowired ClienteService clienteService;
    @Autowired AulaRepository aulas;
    @Autowired ClienteRepository clientes;

    // ---------- aulas

    @Test
    void semFiltroRetornaTodasOrdenadasPorInicio() {
        List<AulaResponse> todas = aulaService.pesquisar(new AulaFiltro());

        assertEquals(aulas.count(), todas.size());
        for (int i = 1; i < todas.size(); i++) {
            assertFalse(todas.get(i).inicio().isBefore(todas.get(i - 1).inicio()));
        }
    }

    @Test
    void filtraPorStatus() {
        AulaFiltro f = new AulaFiltro();
        f.setStatus(StatusAula.CONCLUIDA);

        List<AulaResponse> resultado = aulaService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(a -> a.status() == StatusAula.CONCLUIDA));
        assertEquals(aulas.findByStatusOrderByInicioAsc(StatusAula.CONCLUIDA).size(), resultado.size());
    }

    @Test
    void filtraPorEsportePersonalEUnidade() {
        AulaResponse base = aulaService.pesquisar(new AulaFiltro()).get(0);

        AulaFiltro porEsporte = new AulaFiltro();
        porEsporte.setEsporteId(base.esporteId());
        assertTrue(aulaService.pesquisar(porEsporte).stream().allMatch(a -> a.esporteId().equals(base.esporteId())));

        AulaFiltro porPersonal = new AulaFiltro();
        porPersonal.setPersonalId(base.personalId());
        assertTrue(aulaService.pesquisar(porPersonal).stream().allMatch(a -> a.personalId().equals(base.personalId())));

        AulaFiltro porUnidade = new AulaFiltro();
        porUnidade.setUnidadeId(base.unidadeId());
        assertTrue(aulaService.pesquisar(porUnidade).stream().allMatch(a -> a.unidadeId().equals(base.unidadeId())));

        AulaFiltro combinado = new AulaFiltro();
        combinado.setEsporteId(base.esporteId());
        combinado.setPersonalId(base.personalId());
        combinado.setUnidadeId(base.unidadeId());
        assertTrue(aulaService.pesquisar(combinado).stream().anyMatch(a -> a.id().equals(base.id())));
    }

    @Test
    void apenasComVagasExcluiAulasLotadas() {
        List<AulaResponse> todas = aulaService.pesquisar(new AulaFiltro());
        AulaFiltro f = new AulaFiltro();
        f.setApenasComVagas(true);

        List<AulaResponse> resultado = aulaService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(a -> a.vagasDisponiveis() > 0));
        assertEquals(todas.stream().filter(a -> a.vagasDisponiveis() > 0).count(), resultado.size());
    }

    @Test
    void filtraPorPeriodoIncluindoOsDoisExtremos() {
        LocalDate dia = aulaService.pesquisar(new AulaFiltro()).get(0).inicio().toLocalDate();
        AulaFiltro f = new AulaFiltro();
        f.setDe(dia);
        f.setAte(dia);

        List<AulaResponse> resultado = aulaService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(a -> a.inicio().toLocalDate().equals(dia)));
    }

    @Test
    void buscaPorTextoIgnoraMaiusculasEProcuraEmEsportePersonalEUnidade() {
        AulaFiltro f = new AulaFiltro();
        f.setQ("MENDES");

        List<AulaResponse> resultado = aulaService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(a ->
                (a.esporte() + " " + a.personal() + " " + a.unidade()).toLowerCase().contains("mendes")));
    }

    @Test
    void curingasDoUsuarioSaoTratadosComoTexto() {
        for (String curinga : List.of("%", "_")) {
            AulaFiltro f = new AulaFiltro();
            f.setQ(curinga);
            assertTrue(aulaService.pesquisar(f).isEmpty(), "o texto '" + curinga + "' não deveria casar com tudo");
        }
    }

    @Test
    void combinaFiltrosComE() {
        AulaFiltro f = new AulaFiltro();
        f.setStatus(StatusAula.AGENDADA);
        f.setApenasComVagas(true);

        List<AulaResponse> resultado = aulaService.pesquisar(f);

        assertTrue(resultado.stream().allMatch(a -> a.status() == StatusAula.AGENDADA && a.vagasDisponiveis() > 0));
    }

    // ---------- clientes

    @Test
    void clientesPorTexto() {
        ClienteFiltro f = new ClienteFiltro();
        f.setQ("MIRIAM");

        List<Cliente> resultado = clienteService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(c ->
                (c.getNome() + " " + c.getEmail() + " " + c.getObjetivo()).toLowerCase().contains("miriam")));
    }

    @Test
    void clientesPorObjetivo() {
        List<String> objetivos = clienteService.objetivos();
        assertFalse(objetivos.isEmpty());
        assertEquals(objetivos.size(), objetivos.stream().distinct().count());

        ClienteFiltro f = new ClienteFiltro();
        f.setObjetivo(objetivos.get(0));

        List<Cliente> resultado = clienteService.pesquisar(f);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(c -> objetivos.get(0).equals(c.getObjetivo())));
    }

    @Test
    void clientesComESemInscricaoSeComplementam() {
        ClienteFiltro com = new ClienteFiltro();
        com.setSituacao(SituacaoCliente.COM_INSCRICAO);
        ClienteFiltro sem = new ClienteFiltro();
        sem.setSituacao(SituacaoCliente.SEM_INSCRICAO);

        List<Cliente> comInscricao = clienteService.pesquisar(com);
        List<Cliente> semInscricao = clienteService.pesquisar(sem);

        assertTrue(comInscricao.stream().allMatch(c -> aulas.existsByInscritosId(c.getId())));
        assertTrue(semInscricao.stream().noneMatch(c -> aulas.existsByInscritosId(c.getId())));
        assertEquals(clientes.count(), comInscricao.size() + semInscricao.size());
    }

    @Test
    void clientesPorPeriodoDeCadastro() {
        LocalDate corte = LocalDate.now().minusDays(90);

        ClienteFiltro depois = new ClienteFiltro();
        depois.setCadastroDe(corte);
        ClienteFiltro antes = new ClienteFiltro();
        antes.setCadastroAte(corte.minusDays(1));

        List<Cliente> recentes = clienteService.pesquisar(depois);
        List<Cliente> antigos = clienteService.pesquisar(antes);

        assertTrue(recentes.stream().allMatch(c -> !c.getDataCadastro().isBefore(corte)));
        assertTrue(antigos.stream().allMatch(c -> c.getDataCadastro().isBefore(corte)));
        assertEquals(clientes.count(), recentes.size() + antigos.size());
    }
}
