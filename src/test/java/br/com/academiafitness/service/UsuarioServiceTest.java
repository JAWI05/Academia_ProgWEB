package br.com.academiafitness.service;

import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.form.UsuarioForm;
import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import br.com.academiafitness.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;

    UsuarioService service;

    @BeforeEach
    void preparar() {
        service = new UsuarioService(usuarios, encoder);
    }

    private UsuarioForm form(String nome, String login, String senha, Perfil perfil) {
        UsuarioForm f = new UsuarioForm();
        f.setNome(nome);
        f.setLogin(login);
        f.setSenha(senha);
        f.setPerfil(perfil);
        return f;
    }

    private Usuario usuario(Long id, String login, Perfil perfil, boolean ativo) {
        Usuario u = new Usuario("Fulano", login, "HASH", perfil);
        u.setAtivo(ativo);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    void criaUsuarioComSenhaCriptografadaELoginNormalizado() {
        when(usuarios.existsByLogin("maria")).thenReturn(false);
        when(encoder.encode("segredo1")).thenReturn("HASH-BCRYPT");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario criado = service.criar(form(" Maria ", " Maria ", "segredo1", Perfil.OPERADOR));

        assertEquals("maria", criado.getLogin());
        assertEquals("HASH-BCRYPT", criado.getSenha());
        assertTrue(criado.isAtivo());
    }

    @Test
    void rejeitaLoginDuplicado() {
        when(usuarios.existsByLogin("maria")).thenReturn(true);

        assertThrows(RegraNegocioException.class,
                () -> service.criar(form("Maria", "maria", "segredo1", Perfil.OPERADOR)));
        verify(usuarios, never()).save(any());
    }

    @Test
    void exigeSenhaNaCriacao() {
        when(usuarios.existsByLogin("maria")).thenReturn(false);

        assertThrows(RegraNegocioException.class,
                () -> service.criar(form("Maria", "maria", null, Perfil.OPERADOR)));
    }

    @Test
    void atualizarSemSenhaMantemOHash() {
        Usuario existente = usuario(1L, "maria", Perfil.OPERADOR, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(existente));

        service.atualizar(1L, form("Maria Souza", "maria", null, Perfil.OPERADOR));

        assertEquals("Maria Souza", existente.getNome());
        assertEquals("HASH", existente.getSenha());
        verify(encoder, never()).encode(any());
    }

    @Test
    void atualizarComSenhaGravaNovoHash() {
        Usuario existente = usuario(1L, "maria", Perfil.OPERADOR, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(existente));
        when(encoder.encode("novasenha")).thenReturn("NOVO-HASH");

        service.atualizar(1L, form("Maria", "maria", "novasenha", Perfil.OPERADOR));

        assertEquals("NOVO-HASH", existente.getSenha());
    }

    @Test
    void naoRebaixaOUltimoAdminAtivo() {
        Usuario admin = usuario(1L, "admin", Perfil.ADMIN, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(1L);

        assertThrows(RegraNegocioException.class,
                () -> service.atualizar(1L, form("Admin", "admin", null, Perfil.OPERADOR)));
        assertEquals(Perfil.ADMIN, admin.getPerfil());
    }

    @Test
    void naoDesativaAPropriaConta() {
        Usuario admin = usuario(1L, "admin", Perfil.ADMIN, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L, "admin"));
        assertTrue(admin.isAtivo());
    }

    @Test
    void naoDesativaOUltimoAdmin() {
        Usuario admin = usuario(1L, "admin", Perfil.ADMIN, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(1L);

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L, "outro"));
        assertTrue(admin.isAtivo());
    }

    @Test
    void desativaAdminQuandoHaOutroAtivo() {
        Usuario admin = usuario(1L, "admin2", Perfil.ADMIN, true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(2L);

        service.alternarAtivo(1L, "admin");

        assertFalse(admin.isAtivo());
    }

    @Test
    void reativaUsuarioInativo() {
        Usuario operador = usuario(2L, "operador", Perfil.OPERADOR, false);
        when(usuarios.findById(2L)).thenReturn(Optional.of(operador));

        service.alternarAtivo(2L, "admin");

        assertTrue(operador.isAtivo());
    }
}
