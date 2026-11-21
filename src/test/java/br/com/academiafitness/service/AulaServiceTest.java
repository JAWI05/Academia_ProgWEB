package br.com.academiafitness.service;

import br.com.academiafitness.model.*;
import br.com.academiafitness.dto.AulaRequest;
import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.exception.RecursoNaoEncontradoException;
import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock AulaRepository aulas;
    @Mock EsporteRepository esportes;
    @Mock PersonalTrainerRepository personais;
    @Mock UnidadeRepository unidades;
    @Mock ClienteRepository clientes;

    AulaService service;

    Esporte esporte;
    PersonalTrainer personal;
    Unidade unidade;
    LocalDateTime inicio;

    @BeforeEach
    void preparar() {
        service = new AulaService(aulas, esportes, personais, unidades, clientes);
        esporte = new Esporte("Spinning", StatusEsporte.ATIVO);
        personal = new PersonalTrainer("Carla", "Hipertrofia", 8, new BigDecimal("120.00"));
        unidade = new Unidade("Centro", "São Paulo", "SP");
        ReflectionTestUtils.setField(personal, "id", 1L);
        inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    private Aula aula(Long id, LocalDateTime inicio, int duracao, int capacidade) {
        Aula a = new Aula(esporte, personal, unidade, inicio, duracao, capacidade);
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private Cliente cliente(Long id, String nome) {
        Cliente c = new Cliente(nome, nome.toLowerCase() + "@example.com", "Saúde");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private AulaRequest request(LocalDateTime inicio, int duracao, int capacidade) {
        return new AulaRequest(1L, 1L, 1L, inicio, duracao, capacidade);
    }

    // ---------- criação

    @Test
    void criaAulaValidaComStatusAgendada() {
        when(esportes.findById(1L)).thenReturn(Optional.of(esporte));
        when(personais.findById(1L)).thenReturn(Optional.of(personal));
        when(unidades.findById(1L)).thenReturn(Optional.of(unidade));
        when(aulas.findAtivasDoPersonal(1L)).thenReturn(List.of());
        when(aulas.save(any(Aula.class))).thenAnswer(inv -> inv.getArgument(0));

        AulaResponse resposta = service.criar(request(inicio, 60, 10));

        assertEquals(StatusAula.AGENDADA, resposta.status());
        assertEquals(10, resposta.vagasDisponiveis());
    }

    @Test
    void rejeitaEsporteInativo() {
        Esporte inativo = new Esporte("Natação", StatusEsporte.INATIVO);
        when(esportes.findById(1L)).thenReturn(Optional.of(inativo));

        assertThrows(RegraNegocioException.class, () -> service.criar(request(inicio, 60, 10)));
        verify(aulas, never()).save(any());
    }

    @Test
    void rejeitaConflitoDeHorarioDoPersonal() {
        Aula existente = aula(5L, inicio, 60, 10);
        when(esportes.findById(1L)).thenReturn(Optional.of(esporte));
        when(personais.findById(1L)).thenReturn(Optional.of(personal));
        when(unidades.findById(1L)).thenReturn(Optional.of(unidade));
        when(aulas.findAtivasDoPersonal(1L)).thenReturn(List.of(existente));

        // começa 30 min depois do início da existente -> sobreposição
        assertThrows(RegraNegocioException.class,
                () -> service.criar(request(inicio.plusMinutes(30), 60, 10)));
    }

    @Test
    void aceitaAulaLogoAposOFimDaAnterior() {
        Aula existente = aula(5L, inicio, 60, 10);
        when(esportes.findById(1L)).thenReturn(Optional.of(esporte));
        when(personais.findById(1L)).thenReturn(Optional.of(personal));
        when(unidades.findById(1L)).thenReturn(Optional.of(unidade));
        when(aulas.findAtivasDoPersonal(1L)).thenReturn(List.of(existente));
        when(aulas.save(any(Aula.class))).thenAnswer(inv -> inv.getArgument(0));

        // caso-limite: começa exatamente quando a outra termina
        assertDoesNotThrow(() -> service.criar(request(inicio.plusMinutes(60), 60, 10)));
    }

    // ---------- inscrição

    @Test
    void inscreveClienteEReduzVagas() {
        Aula aula = aula(1L, inicio, 60, 2);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(clientes.findById(10L)).thenReturn(Optional.of(cliente(10L, "Ana")));

        AulaResponse resposta = service.inscrever(1L, 10L);

        assertEquals(1, resposta.vagasDisponiveis());
        assertEquals(1, resposta.inscritos().size());
    }

    @Test
    void rejeitaInscricaoDuplicada() {
        Aula aula = aula(1L, inicio, 60, 5);
        Cliente ana = cliente(10L, "Ana");
        aula.inscrever(ana);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(clientes.findById(10L)).thenReturn(Optional.of(ana));

        assertThrows(RegraNegocioException.class, () -> service.inscrever(1L, 10L));
    }

    @Test
    void rejeitaInscricaoEmAulaLotada() {
        Aula aula = aula(1L, inicio, 60, 1);
        aula.inscrever(cliente(10L, "Ana"));
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(clientes.findById(11L)).thenReturn(Optional.of(cliente(11L, "Bia")));

        assertThrows(RegraNegocioException.class, () -> service.inscrever(1L, 11L));
    }

    @Test
    void rejeitaInscricaoEmAulaCancelada() {
        Aula aula = aula(1L, inicio, 60, 5);
        aula.transitarPara(StatusAula.CANCELADA);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(clientes.findById(10L)).thenReturn(Optional.of(cliente(10L, "Ana")));

        assertThrows(RegraNegocioException.class, () -> service.inscrever(1L, 10L));
    }

    @Test
    void clienteInexistenteGera404() {
        Aula aula = aula(1L, inicio, 60, 5);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(clientes.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.inscrever(1L, 99L));
    }

    // ---------- status

    @Test
    void bloqueiaPularDeAgendadaParaConcluida() {
        Aula aula = aula(1L, inicio, 60, 5);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));

        assertThrows(RegraNegocioException.class, () -> service.alterarStatus(1L, StatusAula.CONCLUIDA));
        assertEquals(StatusAula.AGENDADA, aula.getStatus());
    }

    @Test
    void permiteFluxoCompletoDeStatus() {
        Aula aula = aula(1L, inicio, 60, 5);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));

        service.alterarStatus(1L, StatusAula.EM_ANDAMENTO);
        AulaResponse resposta = service.alterarStatus(1L, StatusAula.CONCLUIDA);

        assertEquals(StatusAula.CONCLUIDA, resposta.status());
    }

    @Test
    void estadoFinalNaoPodeMudar() {
        Aula aula = aula(1L, inicio, 60, 5);
        aula.transitarPara(StatusAula.CANCELADA);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));

        assertThrows(RegraNegocioException.class, () -> service.alterarStatus(1L, StatusAula.AGENDADA));
    }

    // ---------- exclusão e atualização

    @Test
    void naoExcluiAulaComInscritos() {
        Aula aula = aula(1L, inicio, 60, 5);
        aula.inscrever(cliente(10L, "Ana"));
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));

        assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
        verify(aulas, never()).delete(any(Aula.class));
    }

    @Test
    void excluiAulaAgendadaSemInscritos() {
        Aula aula = aula(1L, inicio, 60, 5);
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));

        service.excluir(1L);

        verify(aulas).delete(aula);
    }

    @Test
    void naoReduzCapacidadeAbaixoDosInscritos() {
        Aula aula = aula(1L, inicio, 60, 5);
        aula.inscrever(cliente(10L, "Ana"));
        aula.inscrever(cliente(11L, "Bia"));
        when(aulas.findWithDetalhesById(1L)).thenReturn(Optional.of(aula));
        when(esportes.findById(1L)).thenReturn(Optional.of(esporte));
        when(personais.findById(1L)).thenReturn(Optional.of(personal));
        when(unidades.findById(1L)).thenReturn(Optional.of(unidade));
        when(aulas.findAtivasDoPersonal(1L)).thenReturn(List.of(aula));

        assertThrows(RegraNegocioException.class, () -> service.atualizar(1L, request(inicio, 60, 1)));
    }

    @Test
    void aulaInexistenteGera404() {
        when(aulas.findWithDetalhesById(42L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(42L));
    }
}
