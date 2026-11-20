package br.com.academiafitness.form;

import br.com.academiafitness.dto.AulaRequest;
import br.com.academiafitness.model.Perfil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Bean Validation dos formulários e do DTO da API: valores válidos, inválidos e os limites exatos. */
class ValidacaoFormulariosTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void criarValidador() {
        fabrica = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        fabrica.close();
    }

    private Set<String> camposInvalidos(Object objeto) {
        return validador.validate(objeto).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString)
                .collect(Collectors.toSet());
    }

    // ---------- AulaForm

    private AulaForm aulaValida() {
        AulaForm f = new AulaForm();
        f.setEsporteId(1L);
        f.setPersonalId(1L);
        f.setUnidadeId(1L);
        f.setInicio(LocalDateTime.now().plusDays(2));
        f.setDuracaoMin(60);
        f.setCapacidade(10);
        return f;
    }

    @Test
    void aulaValidaNaoTemViolacoes() {
        assertTrue(camposInvalidos(aulaValida()).isEmpty());
    }

    @Test
    void aulaVaziaViolaTodosOsCamposObrigatorios() {
        assertEquals(Set.of("esporteId", "personalId", "unidadeId", "inicio", "duracaoMin", "capacidade"),
                camposInvalidos(new AulaForm()));
    }

    @Test
    void duracaoRespeitaOsLimitesExatos() {
        AulaForm f = aulaValida();
        for (int valida : new int[]{15, 16, 239, 240}) {
            f.setDuracaoMin(valida);
            assertFalse(camposInvalidos(f).contains("duracaoMin"), "duração " + valida + " deveria ser válida");
        }
        for (int invalida : new int[]{-1, 0, 14, 241}) {
            f.setDuracaoMin(invalida);
            assertTrue(camposInvalidos(f).contains("duracaoMin"), "duração " + invalida + " deveria ser inválida");
        }
    }

    @Test
    void capacidadeRespeitaOsLimitesExatos() {
        AulaForm f = aulaValida();
        for (int valida : new int[]{1, 2, 99, 100}) {
            f.setCapacidade(valida);
            assertFalse(camposInvalidos(f).contains("capacidade"), "capacidade " + valida + " deveria ser válida");
        }
        for (int invalida : new int[]{-5, 0, 101}) {
            f.setCapacidade(invalida);
            assertTrue(camposInvalidos(f).contains("capacidade"), "capacidade " + invalida + " deveria ser inválida");
        }
    }

    @Test
    void inicioNoPassadoEhRejeitado() {
        AulaForm f = aulaValida();
        f.setInicio(LocalDateTime.now().minusMinutes(1));

        assertEquals(Set.of("inicio"), camposInvalidos(f));
    }

    @Test
    void dtoDaApiTemAsMesmasRegrasDoFormulario() {
        AulaRequest ok = new AulaRequest(1L, 1L, 1L, LocalDateTime.now().plusDays(1), 15, 1);
        AulaRequest ruim = new AulaRequest(null, 1L, 1L, LocalDateTime.now().minusDays(1), 14, 101);

        assertTrue(camposInvalidos(ok).isEmpty());
        assertEquals(Set.of("esporteId", "inicio", "duracaoMin", "capacidade"), camposInvalidos(ruim));
    }

    // ---------- ClienteForm

    private ClienteForm clienteValido() {
        ClienteForm f = new ClienteForm();
        f.setNome("Ana Souza");
        f.setEmail("ana@example.com");
        f.setObjetivo("Saúde");
        return f;
    }

    @Test
    void clienteValidoEObjetivoOpcional() {
        ClienteForm f = clienteValido();
        assertTrue(camposInvalidos(f).isEmpty());
        f.setObjetivo(null);
        assertTrue(camposInvalidos(f).isEmpty());
    }

    @Test
    void clienteExigeNomeEEmailValido() {
        ClienteForm f = clienteValido();
        f.setNome("   ");
        f.setEmail("isto-nao-e-email");

        assertEquals(Set.of("nome", "email"), camposInvalidos(f));
    }

    @Test
    void clienteRespeitaTamanhosMaximos() {
        ClienteForm f = clienteValido();
        f.setNome("a".repeat(100));
        f.setObjetivo("b".repeat(120));
        assertTrue(camposInvalidos(f).isEmpty(), "100 e 120 caracteres são o limite permitido");

        f.setNome("a".repeat(101));
        f.setObjetivo("b".repeat(121));
        assertEquals(Set.of("nome", "objetivo"), camposInvalidos(f));
    }

    // ---------- UsuarioForm

    private UsuarioForm usuarioValido() {
        UsuarioForm f = new UsuarioForm();
        f.setNome("Maria Souza");
        f.setLogin("maria.souza");
        f.setSenha("segredo1");
        f.setPerfil(Perfil.OPERADOR);
        return f;
    }

    @Test
    void usuarioValidoESenhaOpcionalNaEdicao() {
        UsuarioForm f = usuarioValido();
        assertTrue(camposInvalidos(f).isEmpty());
        f.setSenha(null);
        assertTrue(camposInvalidos(f).isEmpty(), "senha ausente = manter a atual (a obrigatoriedade na criação é do serviço)");
    }

    @Test
    void loginTemTamanhoMinimoECaracteresPermitidos() {
        UsuarioForm f = usuarioValido();

        f.setLogin("ab");
        assertEquals(Set.of("login"), camposInvalidos(f));

        f.setLogin("abc");
        assertTrue(camposInvalidos(f).isEmpty());

        f.setLogin("com espaco");
        assertEquals(Set.of("login"), camposInvalidos(f));

        f.setLogin("tem@arroba");
        assertEquals(Set.of("login"), camposInvalidos(f));
    }

    @Test
    void senhaTemLimitesDeTamanho() {
        UsuarioForm f = usuarioValido();

        f.setSenha("12345");
        assertEquals(Set.of("senha"), camposInvalidos(f));

        f.setSenha("123456");
        assertTrue(camposInvalidos(f).isEmpty());

        f.setSenha("x".repeat(60));
        assertTrue(camposInvalidos(f).isEmpty());

        f.setSenha("x".repeat(61));
        assertEquals(Set.of("senha"), camposInvalidos(f));
    }

    @Test
    void usuarioExigePerfil() {
        UsuarioForm f = usuarioValido();
        f.setPerfil(null);

        assertEquals(Set.of("perfil"), camposInvalidos(f));
    }
}
