package br.com.academiafitness.repository;

import br.com.academiafitness.model.Aula;
import br.com.academiafitness.model.StatusAula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long>, JpaSpecificationExecutor<Aula> {

    @EntityGraph(attributePaths = {"esporte", "personal", "unidade", "inscritos"})
    List<Aula> findAllByOrderByInicioAsc();

    @EntityGraph(attributePaths = {"esporte", "personal", "unidade", "inscritos"})
    List<Aula> findByStatusOrderByInicioAsc(StatusAula status);

    @EntityGraph(attributePaths = {"esporte", "personal", "unidade", "inscritos"})
    Optional<Aula> findWithDetalhesById(Long id);

    boolean existsByInscritosId(Long clienteId);

    /** Aulas não canceladas de um personal, usadas na detecção de conflito de horário. */
    @Query("select a from Aula a where a.personal.id = :personalId and a.status <> br.com.academiafitness.model.StatusAula.CANCELADA")
    List<Aula> findAtivasDoPersonal(@Param("personalId") Long personalId);
}
