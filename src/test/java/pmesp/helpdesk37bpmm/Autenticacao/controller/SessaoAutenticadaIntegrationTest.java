package pmesp.helpdesk37bpmm.Autenticacao.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// Garante o fluxo que o usuário executa no navegador: entrar, atualizar a página
// e continuar autenticado usando a mesma sessão HTTP.
@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_sessao_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD=",
        "bootstrap.tecnico.re=100001",
        "bootstrap.tecnico.nome=Tecnico Bootstrap",
        "bootstrap.tecnico.email=tecnico.bootstrap@policiamilitar.sp.gov.br",
        "bootstrap.tecnico.senha=senha123",
        "bootstrap.tecnico.posto=SD"
})
class SessaoAutenticadaIntegrationTest {

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
    void deveManterASessaoDepoisDoLogin() throws Exception {
        MvcResult resultadoDoLogin = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"100001\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession sessao = (MockHttpSession) resultadoDoLogin.getRequest().getSession(false);

        mockMvc.perform(get("/auth/sessao").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.re").value("100001"))
                .andExpect(jsonPath("$.tecnico").value(true))
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(false));
    }
}
