package br.com.academiafitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * PROVISÓRIO (2ª entrega): libera tudo para demonstrar backend e persistência.
 * Na 3ª entrega (fases 9-10) será trocado por login/logout, dois perfis e proteção no servidor.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // console H2 usa frames
        return http.build();
    }
}
