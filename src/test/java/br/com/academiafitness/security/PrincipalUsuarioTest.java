package br.com.academiafitness.security;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class PrincipalUsuarioTest {

    private Usuario usuario(Perfil perfil, boolean ativo) {
        Usuario u = new Usuario("Fulano", "fulano", "$2a$10$hash", perfil);
        u.setAtivo(ativo);
        ReflectionTestUtils.setField(u, "id", 7L);
        return u;
    }

    @Test
    void autoridadeTemOPrefixoROLEEOPerfil() {
        assertEquals("ROLE_ADMIN",
                new PrincipalUsuario(usuario(Perfil.ADMIN, true)).getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_OPERADOR",
                new PrincipalUsuario(usuario(Perfil.OPERADOR, true)).getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void expoeLoginHashEIdentificador() {
        PrincipalUsuario p = new PrincipalUsuario(usuario(Perfil.OPERADOR, true));

        assertEquals("fulano", p.getUsername());
        assertEquals("$2a$10$hash", p.getPassword());
        assertEquals(7L, p.getId());
        assertEquals(Perfil.OPERADOR, p.getPerfil());
    }

    @Test
    void contaDesativadaNaoEstaHabilitada() {
        assertTrue(new PrincipalUsuario(usuario(Perfil.OPERADOR, true)).isEnabled());
        assertFalse(new PrincipalUsuario(usuario(Perfil.OPERADOR, false)).isEnabled());
    }
}
