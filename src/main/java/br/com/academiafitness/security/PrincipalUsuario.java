package br.com.academiafitness.security;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuário autenticado (guardado na sessão): só os dados necessários, sem a entidade JPA. */
public class PrincipalUsuario implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String login;
    private final String senhaHash;
    private final Perfil perfil;
    private final boolean ativo;

    public PrincipalUsuario(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.senhaHash = usuario.getSenha();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() { return senhaHash; }

    @Override
    public String getUsername() { return login; }

    @Override
    public boolean isEnabled() { return ativo; }
}
