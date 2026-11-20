package br.com.academiafitness.service;

import br.com.academiafitness.model.*;
import br.com.academiafitness.dto.AulaRequest;
import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.exception.RecursoNaoEncontradoException;
import br.com.academiafitness.filtro.AulaFiltro;
import br.com.academiafitness.repository.AulaSpecs;
import org.springframework.data.domain.Sort;
import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AulaService {

    private final AulaRepository aulas;
    private final EsporteRepository esportes;
    private final PersonalTrainerRepository personais;
    private final UnidadeRepository unidades;
    private final ClienteRepository clientes;

    public AulaService(AulaRepository aulas, EsporteRepository esportes, PersonalTrainerRepository personais,
                       UnidadeRepository unidades, ClienteRepository clientes) {
        this.aulas = aulas;
        this.esportes = esportes;
        this.personais = personais;
        this.unidades = unidades;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listar(StatusAula status) {
        List<Aula> resultado = (status == null)
                ? aulas.findAllByOrderByInicioAsc()
                : aulas.findByStatusOrderByInicioAsc(status);
        return resultado.stream().map(AulaResponse::from).toList();
    }

    /** Pesquisa com filtros combinados (E), ordenada pelo início da aula. */
    @Transactional(readOnly = true)
    public List<AulaResponse> pesquisar(AulaFiltro filtro) {
        return aulas.findAll(AulaSpecs.filtrar(filtro), Sort.by("inicio")).stream()
                .map(AulaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AulaResponse buscar(Long id) {
        return AulaResponse.from(carregar(id));
    }

    public AulaResponse criar(AulaRequest req) {
        Esporte esporte = buscarEsporteAtivo(req.esporteId());
        PersonalTrainer personal = buscarPersonal(req.personalId());
        Unidade unidade = buscarUnidade(req.unidadeId());
        validarConflito(personal, req.inicio(), req.duracaoMin(), null);

        Aula aula = new Aula(esporte, personal, unidade, req.inicio(), req.duracaoMin(), req.capacidade());
        return AulaResponse.from(aulas.save(aula));
    }

    public AulaResponse atualizar(Long id, AulaRequest req) {
        Aula aula = carregar(id);
        Esporte esporte = buscarEsporteAtivo(req.esporteId());
        PersonalTrainer personal = buscarPersonal(req.personalId());
        Unidade unidade = buscarUnidade(req.unidadeId());
        validarConflito(personal, req.inicio(), req.duracaoMin(), aula.getId());

        aula.atualizarDados(esporte, personal, unidade, req.inicio(), req.duracaoMin(), req.capacidade());
        return AulaResponse.from(aula);
    }

    public AulaResponse alterarStatus(Long id, StatusAula novoStatus) {
        Aula aula = carregar(id);
        aula.transitarPara(novoStatus);
        return AulaResponse.from(aula);
    }

    /** Só aulas AGENDADAS e sem inscritos podem ser excluídas; as demais devem ser canceladas. */
    public void excluir(Long id) {
        Aula aula = carregar(id);
        if (aula.getStatus() != StatusAula.AGENDADA || !aula.getInscritos().isEmpty()) {
            throw new RegraNegocioException(
                    "Só é possível excluir aulas AGENDADAS sem inscritos. Use a transição para CANCELADA.");
        }
        aulas.delete(aula);
    }

    public AulaResponse inscrever(Long aulaId, Long clienteId) {
        Aula aula = carregar(aulaId);
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
        aula.inscrever(cliente);
        return AulaResponse.from(aula);
    }

    public AulaResponse cancelarInscricao(Long aulaId, Long clienteId) {
        Aula aula = carregar(aulaId);
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", clienteId));
        aula.removerInscricao(cliente);
        return AulaResponse.from(aula);
    }

    // ---------- auxiliares

    private Aula carregar(Long id) {
        return aulas.findWithDetalhesById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula", id));
    }

    private Esporte buscarEsporteAtivo(Long id) {
        Esporte esporte = esportes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Esporte", id));
        if (esporte.getStatus() != StatusEsporte.ATIVO) {
            throw new RegraNegocioException("O esporte '" + esporte.getNome() + "' está INATIVO.");
        }
        return esporte;
    }

    private PersonalTrainer buscarPersonal(Long id) {
        return personais.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Personal trainer", id));
    }

    private Unidade buscarUnidade(Long id) {
        return unidades.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", id));
    }

    /** Um personal não pode ter duas aulas (não canceladas) sobrepostas no tempo. */
    private void validarConflito(PersonalTrainer personal, LocalDateTime inicio, int duracaoMin, Long ignorarAulaId) {
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);
        boolean conflito = aulas.findAtivasDoPersonal(personal.getId()).stream()
                .filter(a -> ignorarAulaId == null || !Objects.equals(a.getId(), ignorarAulaId))
                .anyMatch(a -> inicio.isBefore(a.getFim()) && a.getInicio().isBefore(fim));
        if (conflito) {
            throw new RegraNegocioException("O personal " + personal.getNome()
                    + " já possui uma aula que se sobrepõe a esse horário.");
        }
    }
}
