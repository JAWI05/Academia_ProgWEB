package br.com.academiafitness.security;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import br.com.academiafitness.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Conta desativada (ou removida) tem a sessão encerrada na requisição seguinte. */
@ExtendWith(MockitoExtension.class)
class EstadoContaFilterTest {

    @Mock UsuarioRepository usuarios;

    EstadoContaFilter filtro;
    MockHttpServletRequest requisicao;
    MockHttpServletResponse resposta;
    MockFilterChain cadeia;
    MockHttpSession sessao;

    @BeforeEach
    void preparar() {
        filtro = new EstadoContaFilter(usuarios);
        requisicao = new MockHttpServletRequest("GET", "/aulas");
        sessao = (MockHttpSession) requisicao.getSession(true);
        resposta = new MockHttpServletResponse();
        cadeia = new MockFilterChain();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuario(boolean ativo) {
        Usuario u = new Usuario("Maria", "maria", "hash", Perfil.OPERADOR);
        u.setAtivo(ativo);
        ReflectionTestUtils.setField(u, "id", 5L);
        return u;
    }

    private void autenticarComoMaria() {
        PrincipalUsuario p = new PrincipalUsuario(usuario(true));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    @Test
    void contaAtivaSegueNormalmente() throws Exception {
        autenticarComoMaria();
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(true)));

        filtro.doFilter(requisicao, resposta, cadeia);

        assertNotNull(cadeia.getRequest(), "a requisição deveria continuar pela cadeia");
        assertNull(resposta.getRedirectedUrl());
        assertFalse(sessao.isInvalid());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void contaDesativadaTemASessaoEncerradaERedirecionaParaOLogin() throws Exception {
        autenticarComoMaria();
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario(false)));

        filtro.doFilter(requisicao, resposta, cadeia);

        assertEquals("/login?desativado", resposta.getRedirectedUrl());
        assertTrue(sessao.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(cadeia.getRequest(), "a requisição não deveria chegar ao controller");
    }

    @Test
    void contaRemovidaDoBancoTambemEEncerrada() throws Exception {
        autenticarComoMaria();
        when(usuarios.findById(5L)).thenReturn(Optional.empty());

        filtro.doFilter(requisicao, resposta, cadeia);

        assertEquals("/login?desativado", resposta.getRedirectedUrl());
        assertNull(cadeia.getRequest());
    }

    @Test
    void visitanteSemLoginNaoConsultaOBanco() throws Exception {
        filtro.doFilter(requisicao, resposta, cadeia);

        assertNotNull(cadeia.getRequest());
        verify(usuarios, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void arquivosEstaticosNaoSaoFiltrados() throws Exception {
        autenticarComoMaria();
        MockHttpServletRequest estatico = new MockHttpServletRequest("GET", "/css/custom.css");

        filtro.doFilter(estatico, resposta, cadeia);

        assertNotNull(cadeia.getRequest());
        verify(usuarios, never()).findById(org.mockito.ArgumentMatchers.any());
    }
}
