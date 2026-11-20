package br.com.academiafitness.dto;

import br.com.academiafitness.model.Aula;
import br.com.academiafitness.model.StatusAula;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record AulaResponse(
        Long id,
        String esporte,
        String personal,
        String unidade,
        LocalDateTime inicio,
        LocalDateTime fim,
        int duracaoMin,
        int capacidade,
        int vagasDisponiveis,
        StatusAula status,
        List<ClienteResumo> inscritos
) {
    public record ClienteResumo(Long id, String nome) {
    }

    public static AulaResponse from(Aula a) {
        List<ClienteResumo> inscritos = a.getInscritos().stream()
                .map(c -> new ClienteResumo(c.getId(), c.getNome()))
                .sorted(Comparator.comparing(ClienteResumo::nome))
                .toList();
        return new AulaResponse(a.getId(), a.getEsporte().getNome(), a.getPersonal().getNome(),
                a.getUnidade().getNome(), a.getInicio(), a.getFim(), a.getDuracaoMin(),
                a.getCapacidade(), a.vagasDisponiveis(), a.getStatus(), inscritos);
    }
}
