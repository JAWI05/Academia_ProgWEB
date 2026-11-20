package br.com.academiafitness.filtro;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Critérios opcionais de pesquisa de clientes. Campo nulo = critério ignorado; critérios se combinam com E. */
public class ClienteFiltro {

    /** Texto livre: procura no nome, no e-mail e no objetivo. */
    private String q;
    private String objetivo;
    private SituacaoCliente situacao = SituacaoCliente.TODOS;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroDe;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroAte;

    public boolean temTexto() {
        return q != null && !q.isBlank();
    }

    public boolean temObjetivo() {
        return objetivo != null && !objetivo.isBlank();
    }

    public boolean periodoInvertido() {
        return cadastroDe != null && cadastroAte != null && cadastroDe.isAfter(cadastroAte);
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public SituacaoCliente getSituacao() { return situacao; }
    public void setSituacao(SituacaoCliente situacao) {
        this.situacao = (situacao == null) ? SituacaoCliente.TODOS : situacao;
    }

    public LocalDate getCadastroDe() { return cadastroDe; }
    public void setCadastroDe(LocalDate cadastroDe) { this.cadastroDe = cadastroDe; }

    public LocalDate getCadastroAte() { return cadastroAte; }
    public void setCadastroAte(LocalDate cadastroAte) { this.cadastroAte = cadastroAte; }
}
