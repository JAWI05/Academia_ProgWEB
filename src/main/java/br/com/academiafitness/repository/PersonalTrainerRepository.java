package br.com.academiafitness.repository;

import br.com.academiafitness.model.PersonalTrainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalTrainerRepository extends JpaRepository<PersonalTrainer, Long> {

    Optional<PersonalTrainer> findByNome(String nome);
}
