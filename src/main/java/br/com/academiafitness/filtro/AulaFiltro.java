package br.com.academiafitness.filtro;

import br.com.academiafitness.model.StatusAula;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Critérios opcionais de pesquisa de aulas (formulário do site e query string da API).
 * Campo nulo = critério ignorado; vários critérios se combinam com E.
 */
public class AulaFiltro {

    /** Texto livre: procura no nome do esporte, do personal e da unidade. */
    private String q;
    private Long esporteId;
    private Long personalId;
    private Long unidadeId;
    private StatusAula status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate de;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ate;

    private boolean apenasComVagas;

    public boolean temTexto() {
        return q != null && !q.isBlank();
    }

    public boolean periodoInvertido() {
        return de != null && ate != null && de.isAfter(ate);
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public Long getEsporteId() { return esporteId; }
    public void setEsporteId(Long esporteId) { this.esporteId = esporteId; }

    public Long getPersonalId() { return personalId; }
    public void setPersonalId(Long personalId) { this.personalId = personalId; }

    public Long getUnidadeId() { return unidadeId; }
    public void setUnidadeId(Long unidadeId) { this.unidadeId = unidadeId; }

    public StatusAula getStatus() { return status; }
    public void setStatus(StatusAula status) { this.status = status; }

    public LocalDate getDe() { return de; }
    public void setDe(LocalDate de) { this.de = de; }

    public LocalDate getAte() { return ate; }
    public void setAte(LocalDate ate) { this.ate = ate; }

    public boolean isApenasComVagas() { return apenasComVagas; }
    public void setApenasComVagas(boolean apenasComVagas) { this.apenasComVagas = apenasComVagas; }
}
