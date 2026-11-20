package br.com.academiafitness.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CLIENTE",
        uniqueConstraints = {
            @UniqueConstraint(name = "UQ_CLIENTE_EMAIL", columnNames = {"EMAIL"})
        },
        indexes = {
            @Index(name = "IDX_CLIENTE_NOME", columnList = "NOME")
        })
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "EMAIL", nullable = false, length = 120)
    private String email;

    @Column(name = "OBJETIVO", length = 120)
    private String objetivo;

    @Column(name = "DATA_CADASTRO", nullable = false)
    private LocalDate dataCadastro = LocalDate.now();

    public Cliente() {
    }

    public Cliente(String nome, String email, String objetivo) {
        this.nome = nome;
        this.email = email;
        this.objetivo = objetivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
}
