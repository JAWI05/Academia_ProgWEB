package br.com.academiafitness.service;

import br.com.academiafitness.exception.RecursoNaoEncontradoException;
import br.com.academiafitness.exception.RegraNegocioException;
import br.com.academiafitness.filtro.ClienteFiltro;
import br.com.academiafitness.form.ClienteForm;
import br.com.academiafitness.repository.ClienteSpecs;
import br.com.academiafitness.model.Cliente;
import br.com.academiafitness.repository.AulaRepository;
import br.com.academiafitness.repository.ClienteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;
    private final AulaRepository aulas;

    public ClienteService(ClienteRepository clientes, AulaRepository aulas) {
        this.clientes = clientes;
        this.aulas = aulas;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.findAll(Sort.by("nome"));
    }

    /** Pesquisa com filtros combinados (E), ordenada por nome. */
    @Transactional(readOnly = true)
    public List<Cliente> pesquisar(ClienteFiltro filtro) {
        return clientes.findAll(ClienteSpecs.filtrar(filtro), Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public List<String> objetivos() {
        return clientes.findObjetivosDistintos();
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public Cliente criar(ClienteForm form) {
        String email = normalizarEmail(form.getEmail());
        validarEmailLivre(email, null);
        return clientes.save(new Cliente(form.getNome().trim(), email, objetivoOuNulo(form)));
    }

    public Cliente atualizar(Long id, ClienteForm form) {
        Cliente cliente = buscar(id);
        String email = normalizarEmail(form.getEmail());
        validarEmailLivre(email, id);
        cliente.setNome(form.getNome().trim());
        cliente.setEmail(email);
        cliente.setObjetivo(objetivoOuNulo(form));
        return cliente;
    }

    /** Cliente inscrito em alguma aula não pode ser excluído (preserva o histórico). */
    public void excluir(Long id) {
        Cliente cliente = buscar(id);
        if (aulas.existsByInscritosId(id)) {
            throw new RegraNegocioException("O cliente possui inscrições em aulas e não pode ser excluído.");
        }
        clientes.delete(cliente);
    }

    private void validarEmailLivre(String email, Long idAtual) {
        boolean emUso = clientes.findByEmail(email)
                .filter(c -> idAtual == null || !c.getId().equals(idAtual))
                .isPresent();
        if (emUso) {
            throw new RegraNegocioException("Já existe um cliente com este e-mail.");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String objetivoOuNulo(ClienteForm form) {
        String objetivo = form.getObjetivo();
        return (objetivo == null || objetivo.isBlank()) ? null : objetivo.trim();
    }
}
