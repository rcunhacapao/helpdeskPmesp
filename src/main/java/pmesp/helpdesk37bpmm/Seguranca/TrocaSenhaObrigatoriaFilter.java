package pmesp.helpdesk37bpmm.Seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDateTime;

// Reconfere a pendência no banco em cada API protegida. Isso também bloqueia uma
// sessão antiga imediatamente quando o técnico reseta a senha de um usuário logado.
@Component
public class TrocaSenhaObrigatoriaFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao != null && autenticacao.isAuthenticated()
                && !(autenticacao instanceof AnonymousAuthenticationToken)
                && rotaProtegidaDuranteTroca(request.getRequestURI())
                && usuarioRepository.findByRe(autenticacao.getName())
                        .map(usuario -> usuario.isTrocaSenhaObrigatoria())
                        .orElse(false)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"status":403,"codigo":"TROCA_SENHA_OBRIGATORIA",\
                    "mensagem":"Crie uma nova senha antes de acessar o sistema.",\
                    "dataHora":"%s"}
                    """.formatted(LocalDateTime.now()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean rotaProtegidaDuranteTroca(String rota) {
        return rota.startsWith("/usuarios")
                || rota.startsWith("/tecnicos")
                || rota.startsWith("/chamados")
                || rota.startsWith("/mike-ia");
    }
}
