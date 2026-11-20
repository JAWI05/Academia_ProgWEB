package br.com.academiafitness.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ESPORTE",
        uniqueConstraints = {
            @UniqueConstraint(name = "UQ_ESPORTE_NOME", columnNames = {"NOME"})
        },
        indexes = {
            @Index(name = "IDX_ESPORTE_STATUS", columnList = "STATUS")
        })
public class Esporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 60)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private StatusEsporte status = StatusEsporte.ATIVO;

    public Esporte() {
    }

    public Esporte(String nome, StatusEsporte status) {
        this.nome = nome;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public StatusEsporte getStatus() { return status; }
    public void setStatus(StatusEsporte status) { this.status = status; }
}
