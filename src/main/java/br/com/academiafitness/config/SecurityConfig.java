package br.com.academiafitness.config;

import br.com.academiafitness.repository.UsuarioRepository;
import br.com.academiafitness.security.EstadoContaFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

/**
 * Autorização feita NO SERVIDOR (as regras valem mesmo digitando a URL na barra de endereços).
 *
 *  ADMIN    : tudo (aulas, clientes, usuários).
 *  OPERADOR : consulta aulas e clientes, cadastra/edita clientes, inscreve e cancela inscrições.
 *             NÃO cria/edita/exclui aula, não altera status, não exclui cliente, não gerencia usuários.
 *
 * A ordem das regras importa: vale a primeira que combinar.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UsuarioRepository usuarios) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // públicas
                .requestMatchers("/login", "/acesso-negado", "/error", "/css/**", "/js/**", "/images/**",
                        "/design-preview.html").permitAll()

                // gestão de usuários
                .requestMatchers("/usuarios/**").hasRole("ADMIN")

                // API REST: leitura e inscrições para os dois perfis; o resto só ADMIN
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers(HttpMethod.POST, "/api/aulas/*/inscricoes/*").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/aulas/*/inscricoes/*").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers("/api/**").hasRole("ADMIN")

                // site: operações restritas ao ADMIN
                .requestMatchers("/aulas/nova", "/aulas/*/editar", "/aulas/*/excluir", "/aulas/*/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/aulas", "/aulas/*").hasRole("ADMIN")
                .requestMatchers("/clientes/*/excluir").hasRole("ADMIN")

                // site: demais telas para os dois perfis
                .requestMatchers("/", "/home", "/aulas/**", "/clientes/**").hasAnyRole("ADMIN", "OPERADOR")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll())
            .httpBasic(Customizer.withDefaults()) // permite testar a API com curl/Postman
            .logout(logout -> logout
                .logoutUrl("/logout")                    // exige POST + token CSRF
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // CSRF ligado no site; a API autentica a cada requisição
            .addFilterAfter(new EstadoContaFilter(usuarios), SecurityContextHolderFilter.class);
        return http.build();
    }
}
