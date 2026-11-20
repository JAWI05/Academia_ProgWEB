package br.com.academiafitness.service;

import br.com.academiafitness.model.*;
import br.com.academiafitness.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consultas de apoio para formulários e para o painel inicial. */
@Service
@Transactional(readOnly = true)
public class CadastroService {

    private final EsporteRepository esportes;
    private final PersonalTrainerRepository personais;
    private final UnidadeRepository unidades;
    private final ClienteRepository clientes;

    public CadastroService(EsporteRepository esportes, PersonalTrainerRepository personais,
                           UnidadeRepository unidades, ClienteRepository clientes) {
        this.esportes = esportes;
        this.personais = personais;
        this.unidades = unidades;
        this.clientes = clientes;
    }

    public List<Esporte> esportesAtivos() {
        return esportes.findAll(Sort.by("nome")).stream()
                .filter(e -> e.getStatus() == StatusEsporte.ATIVO).toList();
    }

    public List<PersonalTrainer> personais() {
        return personais.findAll(Sort.by("nome"));
    }

    public List<Unidade> unidades() {
        return unidades.findAll(Sort.by("nome"));
    }

    public List<Cliente> clientes() {
        return clientes.findAll(Sort.by("nome"));
    }

    public long totalClientes() { return clientes.count(); }
    public long totalPersonais() { return personais.count(); }
    public long totalEsportesAtivos() { return esportesAtivos().size(); }
}
