package br.com.academiafitness.security;

import br.com.academiafitness.model.Usuario;
import br.com.academiafitness.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Se um administrador desativar (ou remover) uma conta, a sessão que ela já tinha aberta
 * é encerrada na próxima requisição, em vez de continuar valendo até expirar.
 * Não é um @Component: é registrado apenas na cadeia do Spring Security (SecurityConfig).
 */
public class EstadoContaFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarios;

    public EstadoContaFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        return caminho.startsWith(request.getContextPath() + "/css/")
                || caminho.startsWith(request.getContextPath() + "/js/")
                || caminho.startsWith(request.getContextPath() + "/images/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PrincipalUsuario principal) {
            boolean ativo = usuarios.findById(principal.getId()).map(Usuario::isAtivo).orElse(false);
            if (!ativo) {
                SecurityContextHolder.clearContext();
                HttpSession sessao = request.getSession(false);
                if (sessao != null) {
                    sessao.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login?desativado");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
