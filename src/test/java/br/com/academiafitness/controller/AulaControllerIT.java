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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** API REST: HTTP -> segurança -> serviço -> JPA -> banco H2 (com a carga real). Testes sem efeito colateral. */
@SpringBootTest
class AulaControllerIT {

    private static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void listaAulasDoBanco() throws Exception {
        mvc.perform(get("/api/aulas").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void aulaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/aulas/99999").with(ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void corpoInvalidoRetorna400ComErrosPorCampo() throws Exception {
        String corpo = "{\"esporteId\":1,\"personalId\":1,\"unidadeId\":1,"
                + "\"inicio\":\"2020-01-01T10:00:00\",\"duracaoMin\":5,\"capacidade\":0}";
        mvc.perform(post("/api/aulas").with(ADMIN).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.inicio").exists())
                .andExpect(jsonPath("$.erros.duracaoMin").exists())
                .andExpect(jsonPath("$.erros.capacidade").exists());
    }

    @Test
    void transicaoInvalidaRetorna409() throws Exception {
        mvc.perform(patch("/api/aulas/1/status").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isConflict());
    }
}
