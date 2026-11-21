package br.com.academiafitness.service;

import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.form.ClienteForm;
import br.com.academiafitness.model.Cliente;
import br.com.academiafitness.repository.AulaRepository;
import br.com.academiafitness.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository clientes;
    @Mock AulaRepository aulas;

    private ClienteForm form(String nome, String email, String objetivo) {
        ClienteForm f = new ClienteForm();
        f.setNome(nome);
        f.setEmail(email);
        f.setObjetivo(objetivo);
        return f;
    }

    private Cliente cliente(Long id, String email) {
        Cliente c = new Cliente("Ana", email, null);
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    @Test
    void criaClienteNormalizandoEmail() {
        ClienteService service = new ClienteService(clientes, aulas);
        when(clientes.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(clientes.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente criado = service.criar(form(" Ana ", " ANA@Example.com ", "  "));

        assertEquals("Ana", criado.getNome());
        assertEquals("ana@example.com", criado.getEmail());
        assertNull(criado.getObjetivo());
    }

    @Test
    void rejeitaEmailDuplicado() {
        ClienteService service = new ClienteService(clientes, aulas);
        when(clientes.findByEmail("ana@example.com")).thenReturn(Optional.of(cliente(1L, "ana@example.com")));

        assertThrows(RegraNegocioException.class,
                () -> service.criar(form("Outra Ana", "ana@example.com", null)));
        verify(clientes, never()).save(any());
    }

    @Test
    void permiteManterOProprioEmailAoAtualizar() {
        ClienteService service = new ClienteService(clientes, aulas);
        Cliente existente = cliente(1L, "ana@example.com");
        when(clientes.findById(1L)).thenReturn(Optional.of(existente));
        when(clientes.findByEmail("ana@example.com")).thenReturn(Optional.of(existente));

        assertDoesNotThrow(() -> service.atualizar(1L, form("Ana Maria", "ana@example.com", "Saúde")));
        assertEquals("Ana Maria", existente.getNome());
    }

    @Test
    void naoExcluiClienteInscritoEmAula() {
        ClienteService service = new ClienteService(clientes, aulas);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente(1L, "ana@example.com")));
        when(aulas.existsByInscritosId(1L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
        verify(clientes, never()).delete(any(Cliente.class));
    }

    @Test
    void excluiClienteSemInscricoes() {
        ClienteService service = new ClienteService(clientes, aulas);
        Cliente c = cliente(1L, "ana@example.com");
        when(clientes.findById(1L)).thenReturn(Optional.of(c));
        when(aulas.existsByInscritosId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(clientes).delete(c);
    }
}
