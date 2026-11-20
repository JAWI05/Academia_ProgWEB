package br.com.academiafitness.carga;

import br.com.academiafitness.model.Perfil;
import br.com.academiafitness.model.Usuario;
import br.com.academiafitness.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria os dois usuários de demonstração (um por perfil) quando NÃO existe nenhum usuário.
 * As senhas vêm do application.properties (somente para demonstração) e são gravadas com BCrypt.
 */
@Component
@Order(0)
@ConditionalOnProperty(name = "academia.carga.inicial", havingValue = "true")
public class CargaUsuarios implements ApplicationRunner {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String senhaAdmin;
    private final String senhaOperador;

    public CargaUsuarios(UsuarioRepository usuarios, PasswordEncoder encoder,
                         @Value("${academia.usuarios.admin-senha:admin123}") String senhaAdmin,
                         @Value("${academia.usuarios.operador-senha:operador123}") String senhaOperador) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.senhaAdmin = senhaAdmin;
        this.senhaOperador = senhaOperador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            return;
        }
        usuarios.save(new Usuario("Administrador", "admin", encoder.encode(senhaAdmin), Perfil.ADMIN));
        usuarios.save(new Usuario("Operador (recepção)", "operador", encoder.encode(senhaOperador), Perfil.OPERADOR));
        System.out.println("INSERIDO: usuários de demonstração (admin e operador).");
    }
}
