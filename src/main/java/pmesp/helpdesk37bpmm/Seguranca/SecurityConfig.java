package pmesp.helpdesk37bpmm.Seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;

// Configuração central de segurança: quem precisa estar logado, quem precisa ser técnico,
// e como a senha é conferida. Este é o primeiro lugar a olhar para entender autenticação
// e autorização no projeto.
@Configuration
public class SecurityConfig {

    // Rotas de arquivos estáticos do frontend (precisam continuar públicas, senão
    // ninguém conseguiria nem carregar a tela de login).
    private static final String[] ARQUIVOS_PUBLICOS_DO_FRONTEND = {
            "/", "/index.html", "/favicon.png", "/apple-touch-icon.png",
            "/*.css", "/*.js", "/*.png", "/*.jpeg"
    };

    // Rotas exclusivas de técnico: cadastro/gestão de usuários, gestão de técnicos
    // e as ações que conduzem a fila de atendimento.
    private static final String[] ROTAS_EXCLUSIVAS_DE_TECNICO = {
            "/usuarios/**", "/tecnicos/**",
            "/chamados/fila", "/chamados/em-atendimento", "/chamados/resumo",
            "/chamados/atualizar-dados/**", "/chamados/iniciar-atendimento/**",
            "/chamados/transferir-responsavel/**", "/chamados/finalizar/**",
            "/mike-ia/chamado/**", "/mike-ia/metricas"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Guarda o login do usuário na sessão HTTP entre uma requisição e outra
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // O Spring Security já monta a autenticação sozinho a partir do UsuarioDetailsService
    // (é um UserDetailsService) e do PasswordEncoder acima; só precisamos expor o
    // AuthenticationManager para o AutenticacaoController poder usá-lo no /auth/login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                // API própria (sem formulário HTML tradicional) autenticada por sessão/cookie;
                // desligamos o CSRF nesta primeira versão para não travar as chamadas do
                // frontend antes de ele enviar o token — reavaliar quando o login estiver
                // integrado de ponta a ponta.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityContext(security -> security.securityContextRepository(securityContextRepository))
                // O console do H2 usa <iframe> e só existe quando o profile "dev" está ativo
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/primeiro-acesso").permitAll()
                        .requestMatchers(ARQUIVOS_PUBLICOS_DO_FRONTEND).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(ROTAS_EXCLUSIVAS_DE_TECNICO).hasRole("TECNICO")
                        .anyRequest().authenticated()
                )
                // Invalida a sessão e limpa o cookie; devolve 204 em vez do redirecionamento
                // padrão, já que quem chama aqui é o frontend via fetch(), não um formulário.
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                );

        return http.build();
    }
}
