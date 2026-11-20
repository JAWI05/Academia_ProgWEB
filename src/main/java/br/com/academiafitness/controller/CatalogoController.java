package br.com.academiafitness.controller;

import br.com.academiafitness.model.*;
import br.com.academiafitness.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Consultas de apoio (somente leitura) para montar requisições de aula. */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    public record UnidadeDto(Long id, String nome, String cidade, String estado) {
    }

    public record EsporteDto(Long id, String nome, StatusEsporte status) {
    }

    public record PersonalDto(Long id, String nome, String especialidade, int anosExperiencia, BigDecimal valorHora) {
    }

    public record ClienteDto(Long id, String nome, String email, String objetivo) {
    }

    private final UnidadeRepository unidades;
    private final EsporteRepository esportes;
    private final PersonalTrainerRepository personais;
    private final ClienteRepository clientes;

    public CatalogoController(UnidadeRepository unidades, EsporteRepository esportes,
                              PersonalTrainerRepository personais, ClienteRepository clientes) {
        this.unidades = unidades;
        this.esportes = esportes;
        this.personais = personais;
        this.clientes = clientes;
    }

    @GetMapping("/unidades")
    public List<UnidadeDto> unidades() {
        return unidades.findAll().stream()
                .map(u -> new UnidadeDto(u.getId(), u.getNome(), u.getCidade(), u.getEstado())).toList();
    }

    @GetMapping("/esportes")
    public List<EsporteDto> esportes() {
        return esportes.findAll().stream()
                .map(e -> new EsporteDto(e.getId(), e.getNome(), e.getStatus())).toList();
    }

    @GetMapping("/personais")
    public List<PersonalDto> personais() {
        return personais.findAll().stream()
                .map(p -> new PersonalDto(p.getId(), p.getNome(), p.getEspecialidade(),
                        p.getAnosExperiencia(), p.getValorHora())).toList();
    }

    @GetMapping("/clientes")
    public List<ClienteDto> clientes() {
        return clientes.findAll().stream()
                .map(c -> new ClienteDto(c.getId(), c.getNome(), c.getEmail(), c.getObjetivo())).toList();
    }
}
