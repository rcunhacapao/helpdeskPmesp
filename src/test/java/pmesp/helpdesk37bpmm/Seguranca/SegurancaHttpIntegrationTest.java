package pmesp.helpdesk37bpmm.Seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// Exercita as proteções HTTP que ficam antes dos controllers e não aparecem nos testes de serviço.
@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_seguranca_http_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD=",
        "bootstrap.tecnico.re=100001",
        "bootstrap.tecnico.nome=Tecnico Bootstrap",
        "bootstrap.tecnico.email=tecnico.bootstrap@policiamilitar.sp.gov.br",
        "bootstrap.tecnico.senha=senha123",
        "bootstrap.tecnico.posto=SD"
})
class SegurancaHttpIntegrationTest {

    @Autowired
    private WebApplicationContext contextoDaAplicacao;
    @Autowired
    private FilterChainProxy filtroDeSeguranca;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararClienteHttpDeTeste() {
        mockMvc = webAppContextSetup(contextoDaAplicacao)
                .addFilters(filtroDeSeguranca)
                .build();
    }

    @Test
    void deveRecusarOperacaoSemTokenCsrf() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"100001\",\"senha\":\"senha123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void deveEntregarTokenCsrfParaOFrontend() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void deveTrocarIdentificadorDaSessaoAoAutenticar() throws Exception {
        MockHttpSession sessaoAntesDoLogin = new MockHttpSession();
        String identificadorAnterior = sessaoAntesDoLogin.getId();

        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .session(sessaoAntesDoLogin)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"re\":\"100001\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        assertNotEquals(identificadorAnterior, resultado.getRequest().getSession(false).getId());
    }

    @Test
    void deveRetornarErroJsonQuandoNaoHaAutenticacao() throws Exception {
        mockMvc.perform(get("/chamados/meus"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACAO_NECESSARIA"));
    }

    @Test
    void deveBloquearRotaTecnicaParaUsuarioComum() throws Exception {
        mockMvc.perform(get("/usuarios").with(user("200001").roles("USUARIO")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void deveEnviarHeadersDeSeguranca() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=()"));
    }

    @Test
    void deveManterSwaggerDesabilitadoSemPerfilDev() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRecusarParametroTecnicoForaDoFormatoAntesDoServico() throws Exception {
        mockMvc.perform(patch("/chamados/iniciar-atendimento/1")
                        .with(user("100001").roles("USUARIO", "TECNICO"))
                        .with(csrf())
                        .param("reTecnico", "100001<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
    }

    @Test
    void deveLimitarTentativasRepetidasDeAutenticacao() throws Exception {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.77");
                                return request;
                            })
                            .contentType("application/json")
                            .content("{\"re\":\"987654\",\"senha\":\"incorreta\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.77");
                            return request;
                        })
                        .contentType("application/json")
                        .content("{\"re\":\"987654\",\"senha\":\"incorreta\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.codigo").value("MUITAS_TENTATIVAS"));
    }
}
