package br.com.academiafitness.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prova no servidor que os dois perfis têm permissões distintas (não basta esconder botões):
 * autenticação (BCrypt + banco), bloqueio de rotas, página de acesso negado e CSRF.
 * Os testes não alteram dados.
 */
@SpringBootTest
class SegurancaIT {

    private static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");
    private static final RequestPostProcessor OPERADOR = user("operador").roles("OPERADOR");

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---------- login / logout

    @Test
    void anonimoEhRedirecionadoParaLogin() throws Exception {
        mvc.perform(get("/aulas").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void paginaDeLoginEhPublica() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void loginComUsuarioDaCargaAutentica() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin123"))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/home"));
        mvc.perform(formLogin("/login").user("operador").password("operador123"))
                .andExpect(authenticated().withUsername("operador"));
    }

    @Test
    void loginComSenhaErradaFalha() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logoutExigeCsrfEEncerraASessao() throws Exception {
        mvc.perform(post("/logout").with(ADMIN)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(ADMIN).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ---------- perfil OPERADOR

    @Test
    void operadorConsultaAulasEClientes() throws Exception {
        mvc.perform(get("/aulas").with(OPERADOR)).andExpect(status().isOk());
        mvc.perform(get("/aulas/1").with(OPERADOR)).andExpect(status().isOk());
        mvc.perform(get("/clientes").with(OPERADOR)).andExpect(status().isOk());
        mvc.perform(get("/clientes/novo").with(OPERADOR)).andExpect(status().isOk());
    }

    @Test
    void operadorNaoCriaNemEditaAula() throws Exception {
        mvc.perform(get("/aulas/nova").with(OPERADOR))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
        mvc.perform(get("/aulas/1/editar").with(OPERADOR)).andExpect(status().isForbidden());
        mvc.perform(post("/aulas").with(OPERADOR).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/aulas/1").with(OPERADOR).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void operadorNaoAlteraStatusNemExcluiAulaOuCliente() throws Exception {
        mvc.perform(post("/aulas/1/status").param("status", "EM_ANDAMENTO").with(OPERADOR).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/aulas/1/excluir").with(OPERADOR).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/clientes/1/excluir").with(OPERADOR).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void operadorNaoAcessaUsuarios() throws Exception {
        mvc.perform(get("/usuarios").with(OPERADOR)).andExpect(status().isForbidden());
        mvc.perform(post("/usuarios").with(OPERADOR).with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    void operadorPodeInscreverClientes() throws Exception {
        // cliente inexistente: a rota é permitida (3xx com mensagem de erro), sem alterar dados
        mvc.perform(post("/aulas/1/inscricoes").param("clienteId", "99999").with(OPERADOR).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void apiOperadorLeMasNaoExclui() throws Exception {
        mvc.perform(get("/api/aulas").with(OPERADOR)).andExpect(status().isOk());
        mvc.perform(delete("/api/aulas/1").with(OPERADOR)).andExpect(status().isForbidden());
    }

    // ---------- perfil ADMIN e CSRF

    @Test
    void adminAcessaAreasRestritas() throws Exception {
        mvc.perform(get("/usuarios").with(ADMIN)).andExpect(status().isOk());
        mvc.perform(get("/aulas/nova").with(ADMIN)).andExpect(status().isOk());
    }

    @Test
    void postNoSiteSemCsrfEhBloqueado() throws Exception {
        mvc.perform(post("/aulas/1/inscricoes").param("clienteId", "99999").with(ADMIN))
                .andExpect(status().isForbidden());
    }
}
