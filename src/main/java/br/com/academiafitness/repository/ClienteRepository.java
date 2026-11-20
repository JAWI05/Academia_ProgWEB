package br.com.academiafitness.repository;

import br.com.academiafitness.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    /** Objetivos distintos já cadastrados (para o filtro por objetivo). */
    @Query("select distinct c.objetivo from Cliente c where c.objetivo is not null order by c.objetivo")
    List<String> findObjetivosDistintos();

    Optional<Cliente> findByEmail(String email);
}
