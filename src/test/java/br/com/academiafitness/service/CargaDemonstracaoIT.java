package br.com.academiafitness.service;

import br.com.academiafitness.model.Aula;
import br.com.academiafitness.model.StatusAula;
import br.com.academiafitness.repository.AulaRepository;
import br.com.academiafitness.repository.ClienteRepository;
import br.com.academiafitness.repository.PersonalTrainerRepository;
import br.com.academiafitness.repository.UnidadeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Roda a carga de demonstração em um banco em memória exclusivo deste teste. */
@SpringBootTest(properties = {
        "academia.carga.demo=true",
        "spring.datasource.url=jdbc:h2:mem:demotest;DB_CLOSE_DELAY=-1"
})
class CargaDemonstracaoIT {

    @Autowired AulaRepository aulas;
    @Autowired ClienteRepository clientes;
    @Autowired PersonalTrainerRepository personais;
    @Autowired UnidadeRepository unidades;

    @Test
    void populaUmaBaseVolumosa() {
        assertTrue(clientes.count() >= 60);
        assertTrue(personais.count() >= 12);
        assertTrue(unidades.count() >= 6);
        assertTrue(aulas.count() >= 30);
    }

    @Test
    void geraAulasEmVariosStatus() {
        for (StatusAula status : List.of(StatusAula.AGENDADA, StatusAula.CONCLUIDA, StatusAula.CANCELADA)) {
            assertFalse(aulas.findByStatusOrderByInicioAsc(status).isEmpty(), "sem aulas " + status);
        }
    }

    @Test
    void respeitaLotacaoESemConflitoDeHorarioDoPersonal() {
        List<Aula> todas = aulas.findAllByOrderByInicioAsc();
        for (Aula a : todas) {
            assertTrue(a.getInscritos().size() <= a.getCapacidade(), "lotação excedida na aula " + a.getId());
        }
        Map<Long, List<Aula>> porPersonal = todas.stream()
                .filter(a -> a.getStatus() != StatusAula.CANCELADA)
                .collect(Collectors.groupingBy(a -> a.getPersonal().getId()));
        porPersonal.values().forEach(lista -> {
            lista.sort(Comparator.comparing(Aula::getInicio));
            for (int i = 1; i < lista.size(); i++) {
                assertFalse(lista.get(i).getInicio().isBefore(lista.get(i - 1).getFim()),
                        "conflito de horário do personal " + lista.get(i).getPersonal().getNome());
            }
        });
    }
}
