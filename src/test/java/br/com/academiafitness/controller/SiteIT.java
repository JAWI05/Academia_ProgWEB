package br.com.academiafitness.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza os templates Thymeleaf de verdade (segurança -> controller -> serviço -> JPA -> HTML). Sem efeitos colaterais. */
@SpringBootTest
class SiteIT {

    private static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");
    private static final RequestPostProcessor OPERADOR = user("operador").roles("OPERADOR");

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void paginaInicialRenderiza() throws Exception {
        mvc.perform(get("/home").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("stat-card")));
    }

    @Test
    void listaDeAulasMostraACargaInicial() throws Exception {
        mvc.perform(get("/aulas").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carla Mendes")));
    }

    @Test
    void detalheMostraInscritos() throws Exception {
        mvc.perform(get("/aulas/1").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Miriam Ester")));
    }

    @Test
    void formulariosCarregam() throws Exception {
        mvc.perform(get("/aulas/nova").with(ADMIN)).andExpect(status().isOk());
        mvc.perform(get("/aulas/1/editar").with(ADMIN)).andExpect(status().isOk());
        mvc.perform(get("/clientes/novo").with(ADMIN)).andExpect(status().isOk());
        mvc.perform(get("/usuarios/novo").with(ADMIN)).andExpect(status().isOk());
    }

    @Test
    void listaDeClientesRenderiza() throws Exception {
        mvc.perform(get("/clientes").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("miriam@example.com")));
    }

    @Test
    void listaDeUsuariosRenderizaParaAdmin() throws Exception {
        mvc.perform(get("/usuarios").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("operador")));
    }

    @Test
    void aulaInexistenteMostraPaginaDeErro404() throws Exception {
        mvc.perform(get("/aulas/99999").with(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void formularioVazioVoltaComMensagensDeValidacao() throws Exception {
        mvc.perform(post("/aulas").with(ADMIN).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Selecione o esporte")));
    }

    @Test
    void menuMostraUsuariosEBotoesDeAdminSoParaAdmin() throws Exception {
        mvc.perform(get("/aulas").with(ADMIN))
                .andExpect(content().string(containsString("/usuarios")))
                .andExpect(content().string(containsString("Nova aula")));
        mvc.perform(get("/aulas").with(OPERADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/usuarios"))))
                .andExpect(content().string(not(containsString("Nova aula"))));
    }

    // ---------- filtros

    @Test
    void filtroDeAulasSemResultadoMostraMensagem() throws Exception {
        mvc.perform(get("/aulas").param("status", "CANCELADA").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma aula encontrada")));
    }

    @Test
    void filtroDeAulasPorEsporteMantemOResultado() throws Exception {
        mvc.perform(get("/aulas").param("esporteId", "1").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carla Mendes")));
    }

    @Test
    void periodoInvertidoEValorInvalidoGeramAvisoSemQuebrarAPagina() throws Exception {
        mvc.perform(get("/aulas").param("de", "2026-12-31").param("ate", "2026-01-01").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ignorado")));
        mvc.perform(get("/aulas").param("status", "XYZ").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ignorado")));
    }

    @Test
    void filtroDeClientes() throws Exception {
        mvc.perform(get("/clientes").param("q", "miriam").with(OPERADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("miriam@example.com")));
        mvc.perform(get("/clientes").param("q", "zzzz").with(OPERADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum cliente encontrado")));
        mvc.perform(get("/clientes").param("situacao", "SEM_INSCRICAO").with(OPERADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alexandre@example.com")))
                .andExpect(content().string(not(containsString("miriam@example.com"))));
    }
}
