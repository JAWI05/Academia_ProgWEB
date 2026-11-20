package br.com.academiafitness.controller;

import br.com.academiafitness.model.Aula;
import br.com.academiafitness.model.StatusAula;
import br.com.academiafitness.repository.AulaRepository;
import br.com.academiafitness.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxos completos de ponta a ponta (fase 11): navegador simulado -> segurança -> controller -> serviço -> JPA -> banco.
 * Usam um banco em memória exclusivo, com a carga inicial (3 clientes, 2 personais, 1 aula).
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:fluxo;DB_CLOSE_DELAY=-1")
class FluxoCompletoIT {

    private static final RequestPostProcessor ADMIN = user("admin").roles("ADMIN");
    private static final RequestPostProcessor OPERADOR = user("operador").roles("OPERADOR");
    private static final DateTimeFormatter CAMPO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired WebApplicationContext context;
    @Autowired AulaRepository aulas;
    @Autowired UsuarioRepository usuarios;

    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private MvcResult criarAula(int diasAFrente, int hora, int capacidade) throws Exception {
        String inicio = LocalDateTime.now().plusDays(diasAFrente).withHour(hora).withMinute(0).format(CAMPO_DATA);
        return mvc.perform(post("/aulas").with(ADMIN).with(csrf())
                        .param("esporteId", "1").param("personalId", "2").param("unidadeId", "1")
                        .param("inicio", inicio).param("duracaoMin", "45").param("capacidade", String.valueOf(capacidade)))
                .andReturn();
    }

    private String flash(MvcResult resultado, String chave) {
        Object valor = resultado.getFlashMap().get(chave);
        return valor == null ? "" : valor.toString();
    }

    @Test
    void cicloCompletoDeUmaAulaDaCriacaoAConclusao() throws Exception {
        // 1. o administrador cria a aula
        MvcResult criada = criarAula(3, 10, 1);
        assertEquals(302, criada.getResponse().getStatus());
        assertEquals("Aula criada com sucesso.", flash(criada, "sucesso"));
        String destino = criada.getResponse().getRedirectedUrl();
        long id = Long.parseLong(destino.substring(destino.lastIndexOf('/') + 1));

        // 2. o operador consulta a aula
        mvc.perform(get("/aulas/" + id).with(OPERADOR))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rafael Lima")));

        // 3. o operador inscreve um cliente (única vaga)
        MvcResult inscrito = mvc.perform(post("/aulas/" + id + "/inscricoes").param("clienteId", "1")
                .with(OPERADOR).with(csrf())).andReturn();
        assertEquals("Cliente inscrito.", flash(inscrito, "sucesso"));

        // 4. a aula está lotada: a segunda inscrição é recusada
        MvcResult lotada = mvc.perform(post("/aulas/" + id + "/inscricoes").param("clienteId", "2")
                .with(OPERADOR).with(csrf())).andReturn();
        assertTrue(flash(lotada, "erro").contains("lotada"), "esperava aviso de aula lotada");

        // 5. o operador não altera status; o administrador não pode pular etapas
        mvc.perform(post("/aulas/" + id + "/status").param("status", "EM_ANDAMENTO").with(OPERADOR).with(csrf()))
                .andExpect(status().isForbidden());
        MvcResult pulo = mvc.perform(post("/aulas/" + id + "/status").param("status", "CONCLUIDA")
                .with(ADMIN).with(csrf())).andReturn();
        assertTrue(flash(pulo, "erro").contains("Transição"), "esperava recusa da transição AGENDADA -> CONCLUIDA");

        // 6. a aula começa: novas inscrições deixam de ser aceitas
        MvcResult iniciada = mvc.perform(post("/aulas/" + id + "/status").param("status", "EM_ANDAMENTO")
                .with(ADMIN).with(csrf())).andReturn();
        assertTrue(flash(iniciada, "sucesso").contains("EM_ANDAMENTO"));
        MvcResult tarde = mvc.perform(post("/aulas/" + id + "/inscricoes").param("clienteId", "3")
                .with(OPERADOR).with(csrf())).andReturn();
        assertTrue(flash(tarde, "erro").contains("AGENDADAS"));

        // 7. a aula termina e não pode mais ser excluída
        MvcResult concluida = mvc.perform(post("/aulas/" + id + "/status").param("status", "CONCLUIDA")
                .with(ADMIN).with(csrf())).andReturn();
        assertTrue(flash(concluida, "sucesso").contains("CONCLUIDA"));
        MvcResult exclusao = mvc.perform(post("/aulas/" + id + "/excluir").with(ADMIN).with(csrf())).andReturn();
        assertFalse(flash(exclusao, "erro").isBlank(), "esperava recusa da exclusão");

        // 8. persistência real: o banco guarda o resultado final
        Aula salva = aulas.findWithDetalhesById(id).orElseThrow();
        assertEquals(StatusAula.CONCLUIDA, salva.getStatus());
        assertEquals(1, salva.getInscritos().size());
    }

    @Test
    void personalNaoPodeTerDuasAulasNoMesmoHorario() throws Exception {
        MvcResult primeira = criarAula(5, 9, 10);
        assertEquals("Aula criada com sucesso.", flash(primeira, "sucesso"));

        // mesmo personal, mesmo dia e horário: o formulário volta com a mensagem de conflito (HTTP 200)
        String inicio = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).format(CAMPO_DATA);
        mvc.perform(post("/aulas").with(ADMIN).with(csrf())
                        .param("esporteId", "1").param("personalId", "2").param("unidadeId", "1")
                        .param("inicio", inicio).param("duracaoMin", "45").param("capacidade", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("possui uma aula")));
    }

    @Test
    void clienteInscritoNaoPodeSerExcluido() throws Exception {
        // o cliente 1 (Miriam) está inscrito na aula da carga inicial
        MvcResult resultado = mvc.perform(post("/clientes/1/excluir").with(ADMIN).with(csrf())).andReturn();

        assertTrue(flash(resultado, "erro").contains("inscrições"));
    }

    @Test
    void cicloDeVidaDeUmUsuarioComSessaoEncerradaAoDesativar() throws Exception {
        // administrador cria a conta; a senha é gravada em hash
        mvc.perform(post("/usuarios").with(ADMIN).with(csrf())
                        .param("nome", "Maria Souza").param("login", "maria")
                        .param("senha", "segredo1").param("perfil", "OPERADOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));
        var maria = usuarios.findByLogin("maria").orElseThrow();
        assertNotEquals("segredo1", maria.getSenha());
        assertTrue(maria.getSenha().startsWith("$2"), "esperava hash BCrypt");

        // a nova conta entra, consulta aulas e é barrada em /usuarios
        MvcResult login = mvc.perform(formLogin("/login").user("maria").password("segredo1"))
                .andExpect(authenticated().withUsername("maria")).andReturn();
        MockHttpSession sessao = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/aulas").session(sessao)).andExpect(status().isOk());
        mvc.perform(get("/usuarios").session(sessao)).andExpect(status().isForbidden());

        // o administrador desativa a conta: a sessão aberta cai na requisição seguinte
        mvc.perform(post("/usuarios/" + maria.getId() + "/ativo").with(ADMIN).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/aulas").session(sessao))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?desativado"));

        // e um novo login com a conta desativada é recusado
        mvc.perform(formLogin("/login").user("maria").password("segredo1"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));

        // reativada, volta a entrar
        mvc.perform(post("/usuarios/" + maria.getId() + "/ativo").with(ADMIN).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(formLogin("/login").user("maria").password("segredo1"))
                .andExpect(authenticated().withUsername("maria"));
    }

    @Test
    void loginDuplicadoVoltaComMensagemNoFormulario() throws Exception {
        mvc.perform(post("/usuarios").with(ADMIN).with(csrf())
                        .param("nome", "Outro Admin").param("login", "admin")
                        .param("senha", "segredo1").param("perfil", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("existe um usu")));
    }
}
