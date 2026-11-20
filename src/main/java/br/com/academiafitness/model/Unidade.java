package br.com.academiafitness.model;

import jakarta.persistence.*;

@Entity
@Table(name = "UNIDADE",
        uniqueConstraints = {
            @UniqueConstraint(name = "UQ_UNIDADE_NOME", columnNames = {"NOME"})
        },
        indexes = {
            @Index(name = "IDX_UNIDADE_CIDADE", columnList = "CIDADE")
        })
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "CIDADE", nullable = false, length = 80)
    private String cidade;

    @Column(name = "ESTADO", nullable = false, length = 2)
    private String estado;

    public Unidade() {
    }

    public Unidade(String nome, String cidade, String estado) {
        this.nome = nome;
        this.cidade = cidade;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
