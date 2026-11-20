package br.com.academiafitness.repository;

import br.com.academiafitness.model.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {

    Optional<Unidade> findByNome(String nome);
}
