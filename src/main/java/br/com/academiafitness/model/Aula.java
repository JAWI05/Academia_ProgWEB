package br.com.academiafitness.model;

import br.com.academiafitness.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade central. As regras de estado (transições, lotação, inscrição duplicada)
 * vivem aqui para que nenhum caminho de código consiga deixar a aula inconsistente.
 */
@Entity
@Table(name = "AULA",
        indexes = {
            @Index(name = "IDX_AULA_PERSONAL_INICIO", columnList = "PERSONAL_ID, INICIO"),
            @Index(name = "IDX_AULA_STATUS_INICIO", columnList = "STATUS, INICIO"),
            @Index(name = "IDX_AULA_ESPORTE", columnList = "ESPORTE_ID"),
            @Index(name = "IDX_AULA_UNIDADE", columnList = "UNIDADE_ID")
        })
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ESPORTE_ID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_AULA_ESPORTE"))
    private Esporte esporte;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONAL_ID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_AULA_PERSONAL"))
    private PersonalTrainer personal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIDADE_ID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_AULA_UNIDADE"))
    private Unidade unidade;

    @Column(name = "INICIO", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "DURACAO_MIN", nullable = false)
    private int duracaoMin;

    @Column(name = "CAPACIDADE", nullable = false)
    private int capacidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private StatusAula status = StatusAula.AGENDADA;

    /** Lado proprietário da associação N:N (tabela AULA_INSCRICAO). */
    @ManyToMany
    @JoinTable(name = "AULA_INSCRICAO",
            joinColumns = @JoinColumn(name = "AULA_ID",
                    foreignKey = @ForeignKey(name = "FK_INSCRICAO_AULA")),
            inverseJoinColumns = @JoinColumn(name = "CLIENTE_ID",
                    foreignKey = @ForeignKey(name = "FK_INSCRICAO_CLIENTE")))
    private Set<Cliente> inscritos = new HashSet<>();

    /** Controle de concorrência otimista: evita estourar a lotação com inscrições simultâneas. */
    @Version
    @Column(name = "VERSAO")
    private Long versao;

    protected Aula() {
    }

    public Aula(Esporte esporte, PersonalTrainer personal, Unidade unidade,
                LocalDateTime inicio, int duracaoMin, int capacidade) {
        this.esporte = esporte;
        this.personal = personal;
        this.unidade = unidade;
        this.inicio = inicio;
        this.duracaoMin = duracaoMin;
        this.capacidade = capacidade;
    }

    public void atualizarDados(Esporte esporte, PersonalTrainer personal, Unidade unidade,
                               LocalDateTime inicio, int duracaoMin, int capacidade) {
        if (status != StatusAula.AGENDADA) {
            throw new RegraNegocioException("Só é possível editar aulas com status AGENDADA.");
        }
        if (capacidade < inscritos.size()) {
            throw new RegraNegocioException("A capacidade (" + capacidade
                    + ") não pode ser menor que o número de inscritos (" + inscritos.size() + ").");
        }
        this.esporte = esporte;
        this.personal = personal;
        this.unidade = unidade;
        this.inicio = inicio;
        this.duracaoMin = duracaoMin;
        this.capacidade = capacidade;
    }

    public void transitarPara(StatusAula novo) {
        if (!status.podeIrPara(novo)) {
            throw new RegraNegocioException("Transição de status inválida: " + status + " -> " + novo
                    + ". Permitidas a partir de " + status + ": " + status.proximos() + ".");
        }
        this.status = novo;
    }

    public void inscrever(Cliente cliente) {
        if (status != StatusAula.AGENDADA) {
            throw new RegraNegocioException("Só é possível se inscrever em aulas AGENDADAS.");
        }
        if (estaInscrito(cliente)) {
            throw new RegraNegocioException("O cliente já está inscrito nesta aula.");
        }
        if (estaLotada()) {
            throw new RegraNegocioException("Aula lotada: capacidade de " + capacidade + " vagas atingida.");
        }
        inscritos.add(cliente);
    }

    public void removerInscricao(Cliente cliente) {
        if (status != StatusAula.AGENDADA) {
            throw new RegraNegocioException("Só é possível cancelar inscrição em aulas AGENDADAS.");
        }
        boolean removido = inscritos.removeIf(c -> c.getId().equals(cliente.getId()));
        if (!removido) {
            throw new RegraNegocioException("O cliente não está inscrito nesta aula.");
        }
    }

    public boolean estaInscrito(Cliente cliente) {
        return inscritos.stream().anyMatch(c -> c.getId().equals(cliente.getId()));
    }

    public boolean estaLotada() {
        return inscritos.size() >= capacidade;
    }

    public int vagasDisponiveis() {
        return Math.max(0, capacidade - inscritos.size());
    }

    public LocalDateTime getFim() {
        return inicio.plusMinutes(duracaoMin);
    }

    public Long getId() { return id; }
    public Esporte getEsporte() { return esporte; }
    public PersonalTrainer getPersonal() { return personal; }
    public Unidade getUnidade() { return unidade; }
    public LocalDateTime getInicio() { return inicio; }
    public int getDuracaoMin() { return duracaoMin; }
    public int getCapacidade() { return capacidade; }
    public StatusAula getStatus() { return status; }
    public Set<Cliente> getInscritos() { return inscritos; }
}
