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

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private static final String[] ARQUIVOS_PUBLICOS_DO_FRONTEND = {
            "/", "/index.html", "/favicon.png", "/apple-touch-icon.png",
            "/*.css", "/*.js", "/*.png", "/*.jpeg"
    };

    private static final String[] ROTAS_DE_DOCUMENTACAO_DA_API = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml"
    };

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

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository, TrocaSenhaObrigatoriaFilter trocaSenhaObrigatoriaFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityContext(security -> security.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/sessao", "/auth/trocar-senha", "/logout").authenticated()
                        .requestMatchers(ARQUIVOS_PUBLICOS_DO_FRONTEND).permitAll()
                        .requestMatchers(ROTAS_DE_DOCUMENTACAO_DA_API).permitAll()
                        .requestMatchers(HttpMethod.GET, "/relatos-erro").hasRole("TECNICO")
                        .requestMatchers(ROTAS_EXCLUSIVAS_DE_TECNICO).hasRole("TECNICO")
                        // A sessão limitada não possui ROLE_USUARIO.
                        .anyRequest().hasRole("USUARIO")
                )
                .addFilterBefore(trocaSenhaObrigatoriaFilter, AuthorizationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                );

        return http.build();
    }
}
