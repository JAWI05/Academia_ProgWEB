package br.com.academiafitness.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza os templates Thymeleaf de verdade (controller -> serviço -> JPA -> HTML). Sem efeitos colaterais. */
@SpringBootTest
class SiteIT {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void paginaInicialRenderiza() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("stat-card")));
    }

    @Test
    void listaDeAulasMostraACargaInicial() throws Exception {
        mvc.perform(get("/aulas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carla Mendes")));
    }

    @Test
    void detalheMostraInscritos() throws Exception {
        mvc.perform(get("/aulas/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Miriam Ester")));
    }

    @Test
    void formulariosCarregam() throws Exception {
        mvc.perform(get("/aulas/nova")).andExpect(status().isOk());
        mvc.perform(get("/aulas/1/editar")).andExpect(status().isOk());
        mvc.perform(get("/clientes/novo")).andExpect(status().isOk());
    }

    @Test
    void listaDeClientesRenderiza() throws Exception {
        mvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("miriam@example.com")));
    }

    @Test
    void aulaInexistenteMostraPaginaDeErro404() throws Exception {
        mvc.perform(get("/aulas/99999")).andExpect(status().isNotFound());
    }

    @Test
    void formularioVazioVoltaComMensagensDeValidacao() throws Exception {
        mvc.perform(post("/aulas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Selecione o esporte")));
    }
}
