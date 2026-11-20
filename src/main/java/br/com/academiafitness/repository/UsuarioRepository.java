package br.com.academiafitness.repository;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    long countByPerfilAndAtivoTrue(Perfil perfil);
}
