package br.com.academiafitness.model;

import jakarta.persistence.*;

@Entity
@Table(name = "USUARIO",
        uniqueConstraints = {
            @UniqueConstraint(name = "UQ_USUARIO_LOGIN", columnNames = {"LOGIN"})
        },
        indexes = {
            @Index(name = "IDX_USUARIO_PERFIL", columnList = "PERFIL")
        })
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "LOGIN", nullable = false, length = 50)
    private String login;

    /** Hash BCrypt (nunca a senha em texto). */
    @Column(name = "SENHA", nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "PERFIL", nullable = false, length = 15)
    private Perfil perfil;

    @Column(name = "ATIVO", nullable = false)
    private boolean ativo = true;

    public Usuario() {
    }

    public Usuario(String nome, String login, String senhaHash, Perfil perfil) {
        this.nome = nome;
        this.login = login;
        this.senha = senhaHash;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
