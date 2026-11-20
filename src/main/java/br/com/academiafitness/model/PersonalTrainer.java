package br.com.academiafitness.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PERSONAL_TRAINER",
        indexes = {
            @Index(name = "IDX_PERSONAL_NOME", columnList = "NOME")
        })
public class PersonalTrainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "ESPECIALIDADE", nullable = false, length = 80)
    private String especialidade;

    @Column(name = "ANOS_EXPERIENCIA", nullable = false)
    private int anosExperiencia;

    @Column(name = "VALOR_HORA", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    public PersonalTrainer() {
    }

    public PersonalTrainer(String nome, String especialidade, int anosExperiencia, BigDecimal valorHora) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.anosExperiencia = anosExperiencia;
        this.valorHora = valorHora;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public int getAnosExperiencia() { return anosExperiencia; }
    public void setAnosExperiencia(int anosExperiencia) { this.anosExperiencia = anosExperiencia; }

    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal valorHora) { this.valorHora = valorHora; }
}
