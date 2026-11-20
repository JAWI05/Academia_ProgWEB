package br.com.academiafitness.form;

import br.com.academiafitness.dto.AulaRequest;
import br.com.academiafitness.dto.AulaResponse;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Objeto do formulário HTML (JavaBean, para o th:field do Thymeleaf). */
public class AulaForm {

    @NotNull(message = "Selecione o esporte")
    private Long esporteId;

    @NotNull(message = "Selecione o personal trainer")
    private Long personalId;

    @NotNull(message = "Selecione a unidade")
    private Long unidadeId;

    @NotNull(message = "Informe a data e a hora de início")
    @Future(message = "O início deve estar no futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime inicio;

    @NotNull(message = "Informe a duração")
    @Min(value = 15, message = "Duração mínima: 15 minutos")
    @Max(value = 240, message = "Duração máxima: 240 minutos")
    private Integer duracaoMin;

    @NotNull(message = "Informe a capacidade")
    @Min(value = 1, message = "Capacidade mínima: 1")
    @Max(value = 100, message = "Capacidade máxima: 100")
    private Integer capacidade;

    public AulaRequest toRequest() {
        return new AulaRequest(esporteId, personalId, unidadeId, inicio, duracaoMin, capacidade);
    }

    public static AulaForm from(AulaResponse a) {
        AulaForm f = new AulaForm();
        f.esporteId = a.esporteId();
        f.personalId = a.personalId();
        f.unidadeId = a.unidadeId();
        f.inicio = a.inicio();
        f.duracaoMin = a.duracaoMin();
        f.capacidade = a.capacidade();
        return f;
    }

    public Long getEsporteId() { return esporteId; }
    public void setEsporteId(Long esporteId) { this.esporteId = esporteId; }

    public Long getPersonalId() { return personalId; }
    public void setPersonalId(Long personalId) { this.personalId = personalId; }

    public Long getUnidadeId() { return unidadeId; }
    public void setUnidadeId(Long unidadeId) { this.unidadeId = unidadeId; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public Integer getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(Integer duracaoMin) { this.duracaoMin = duracaoMin; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
}
