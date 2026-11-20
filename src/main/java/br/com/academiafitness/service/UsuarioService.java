package br.com.academiafitness.service;

import br.com.academiafitness.exception.RecursoNaoEncontradoException;
import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.form.UsuarioForm;
import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import br.com.academiafitness.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAll(Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    public Usuario criar(UsuarioForm form) {
        String login = form.getLogin().trim().toLowerCase();
        if (usuarios.existsByLogin(login)) {
            throw new RegraNegocioException("Já existe um usuário com este login.");
        }
        if (form.getSenha() == null || form.getSenha().isBlank()) {
            throw new RegraNegocioException("Informe a senha do novo usuário.");
        }
        Usuario novo = new Usuario(form.getNome().trim(), login, encoder.encode(form.getSenha()), form.getPerfil());
        return usuarios.save(novo);
    }

    /** O login não muda. Senha em branco mantém a atual. */
    public Usuario atualizar(Long id, UsuarioForm form) {
        Usuario usuario = buscar(id);
        if (usuario.getPerfil() == Perfil.ADMIN && form.getPerfil() != Perfil.ADMIN) {
            garantirQueNaoEOUltimoAdmin(usuario);
        }
        usuario.setNome(form.getNome().trim());
        usuario.setPerfil(form.getPerfil());
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            usuario.setSenha(encoder.encode(form.getSenha()));
        }
        return usuario;
    }

    public Usuario alternarAtivo(Long id, String loginLogado) {
        Usuario usuario = buscar(id);
        if (usuario.getLogin().equals(loginLogado)) {
            throw new RegraNegocioException("Você não pode desativar a própria conta.");
        }
        if (usuario.isAtivo()) {
            if (usuario.getPerfil() == Perfil.ADMIN) {
                garantirQueNaoEOUltimoAdmin(usuario);
            }
            usuario.setAtivo(false);
        } else {
            usuario.setAtivo(true);
        }
        return usuario;
    }

    private void garantirQueNaoEOUltimoAdmin(Usuario usuario) {
        if (usuario.isAtivo() && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new RegraNegocioException("Deve existir pelo menos um administrador ativo.");
        }
    }
}
