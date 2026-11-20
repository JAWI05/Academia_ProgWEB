package br.com.academiafitness.controller;

import br.com.academiafitness.filtro.AulaFiltro;
import br.com.academiafitness.dto.AulaRequest;
import br.com.academiafitness.dto.AulaResponse;
import br.com.academiafitness.dto.StatusRequest;
import br.com.academiafitness.service.AulaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/aulas")
public class AulaController {

    private final AulaService service;

    public AulaController(AulaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AulaResponse> listar(AulaFiltro filtro) {
        return service.pesquisar(filtro);
    }

    @GetMapping("/{id}")
    public AulaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AulaResponse> criar(@Valid @RequestBody AulaRequest req) {
        AulaResponse criada = service.criar(req);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PutMapping("/{id}")
    public AulaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AulaRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public AulaResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(id, req.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/inscricoes/{clienteId}")
    public AulaResponse inscrever(@PathVariable Long id, @PathVariable Long clienteId) {
        return service.inscrever(id, clienteId);
    }

    @DeleteMapping("/{id}/inscricoes/{clienteId}")
    public AulaResponse cancelarInscricao(@PathVariable Long id, @PathVariable Long clienteId) {
        return service.cancelarInscricao(id, clienteId);
    }
}
