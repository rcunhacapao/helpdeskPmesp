package pmesp.helpdesk37bpmm.Seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// Confirma que a documentação continua disponível apenas quando o perfil dev é ativado.
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_springdoc_dev_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD="
})
class SpringdocDevProfileIntegrationTest {

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
    void deveDisponibilizarOpenApiSomenteNoPerfilDeDesenvolvimento() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isNotEmpty());
    }
}
