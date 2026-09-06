package pmesp.helpdesk37bpmm.Seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

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

    // A documentação só é habilitada pelo perfil de desenvolvimento. Quando estiver
    // desabilitada, estes caminhos não existem; a regra mantém o Swagger utilizável no dev.
    private static final String[] ROTAS_DE_DOCUMENTACAO_DA_API = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml"
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository,
                                                   TrocaSenhaObrigatoriaFilter trocaSenhaObrigatoriaFilter) throws Exception {
        http
                // A proteção CSRF padrão usa um token vinculado à sessão. O frontend busca
                // esse valor em /auth/csrf e o envia em toda operação que altera dados.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityContext(security -> security.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/csrf", "/auth/login").permitAll()
                        .requestMatchers("/auth/sessao", "/auth/trocar-senha", "/logout").authenticated()
                        .requestMatchers(ARQUIVOS_PUBLICOS_DO_FRONTEND).permitAll()
                        .requestMatchers(ROTAS_DE_DOCUMENTACAO_DA_API).permitAll()
                        // Relatar um erro é livre a qualquer pessoa logada; só a consulta (GET) é do técnico.
                        .requestMatchers(HttpMethod.GET, "/relatos-erro").hasRole("TECNICO")
                        .requestMatchers(ROTAS_EXCLUSIVAS_DE_TECNICO).hasRole("TECNICO")
                        // Uma sessão com troca pendente não recebe ROLE_USUARIO e, portanto,
                        // não consegue contornar a tela acessando outra API diretamente.
                        .anyRequest().hasRole("USUARIO")
                )
                .addFilterBefore(trocaSenhaObrigatoriaFilter, AuthorizationFilter.class)
                .headers(headers -> headers
                        .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy",
                                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                                        + "img-src 'self' data:; connect-src 'self'; font-src 'self'; "
                                        + "object-src 'none'; base-uri 'self'; frame-ancestors 'none'; "
                                        + "form-action 'self'"))
                        .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "no-referrer"))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                                "camera=(), microphone=(), geolocation=()")))
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint((request, response, exception) ->
                                escreverErroDeSeguranca(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "AUTENTICACAO_NECESSARIA", "Faça login para acessar este recurso."))
                        .accessDeniedHandler((request, response, exception) ->
                                escreverErroDeSeguranca(response, HttpServletResponse.SC_FORBIDDEN,
                                        "ACESSO_NEGADO", "Você não tem permissão para realizar esta ação.")))
                // Invalida a sessão e limpa o cookie; devolve 204 em vez do redirecionamento
                // padrão, já que quem chama aqui é o frontend via fetch(), não um formulário.
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                );

        return http.build();
    }

    private void escreverErroDeSeguranca(HttpServletResponse response, int status,
                                         String codigo, String mensagem) throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"codigo\":\"" + codigo
                + "\",\"mensagem\":\"" + mensagem + "\"}");
    }
}
