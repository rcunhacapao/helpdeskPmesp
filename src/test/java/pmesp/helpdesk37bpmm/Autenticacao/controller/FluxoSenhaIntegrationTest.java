package pmesp.helpdesk37bpmm.Autenticacao.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_fluxo_senha_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD=",
        "bootstrap.tecnico.re=100001",
        "bootstrap.tecnico.nome=Tecnico Bootstrap",
        "bootstrap.tecnico.email=tecnico.bootstrap@policiamilitar.sp.gov.br",
        "bootstrap.tecnico.senha=senha123",
        "bootstrap.tecnico.posto=SD"
})
class FluxoSenhaIntegrationTest {

    @Autowired
    private WebApplicationContext contextoDaAplicacao;
    @Autowired
    private FilterChainProxy filtroDeSeguranca;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararClienteHttpDeTeste() {
        mockMvc = webAppContextSetup(contextoDaAplicacao)
                .addFilters(filtroDeSeguranca)
                .build();
    }

    @Test
    void deveExecutarCadastroPrimeiroLoginTrocasLoginNormalEReset() throws Exception {
        MockHttpSession sessaoDoTecnico = autenticar("100001", "senha123");

        mockMvc.perform(post("/usuarios/cadastrar")
                        .session(sessaoDoTecnico)
                        .contentType("application/json")
                        .content("""
                                {"re":"250861","postoGraduacao":"SD","nome":"Ruan","email":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(true));

        UsuarioModel usuarioNovo = usuarioRepository.findByRe("250861").orElseThrow();
        assertNull(usuarioNovo.getEmail());
        assertTrue(passwordEncoder.matches("250861", usuarioNovo.getSenhaHash()));

        MvcResult primeiroLogin = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"250861\",\"senha\":\"250861\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(true))
                .andReturn();
        MockHttpSession sessaoDoUsuario = (MockHttpSession) primeiroLogin.getRequest().getSession(false);

        mockMvc.perform(get("/chamados/meus").session(sessaoDoUsuario))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/auth/trocar-senha")
                        .session(sessaoDoUsuario)
                        .contentType("application/json")
                        .content("""
                                {"novaSenha":"250861","confirmacaoNovaSenha":"250861"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SENHA_IGUAL_AO_RE"));

        mockMvc.perform(post("/auth/trocar-senha")
                        .session(sessaoDoUsuario)
                        .contentType("application/json")
                        .content("""
                                {"novaSenha":"senhaNova1","confirmacaoNovaSenha":"senhaDiferente"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SENHAS_NAO_CONFEREM"));

        trocarSenha(sessaoDoUsuario, "senhaNova1");
        mockMvc.perform(get("/chamados/meus").session(sessaoDoUsuario))
                .andExpect(status().isOk());

        recusarLogin("250861", "250861");
        autenticar("250861", "senhaNova1");

        mockMvc.perform(patch("/usuarios/resetar-senha/250861").session(sessaoDoTecnico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(true));

        // A sessão aberta antes do reset também é bloqueada imediatamente.
        mockMvc.perform(get("/chamados/meus").session(sessaoDoUsuario))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TROCA_SENHA_OBRIGATORIA"));

        MvcResult loginDepoisDoReset = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"250861\",\"senha\":\"250861\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(true))
                .andReturn();
        MockHttpSession sessaoDepoisDoReset = (MockHttpSession) loginDepoisDoReset.getRequest().getSession(false);

        trocarSenha(sessaoDepoisDoReset, "senhaNova2");
        autenticar("250861", "senhaNova2");
        recusarLogin("250861", "250861");

        UsuarioModel usuarioAtualizado = usuarioRepository.findByRe("250861").orElseThrow();
        assertFalse(usuarioAtualizado.isTrocaSenhaObrigatoria());
    }

    private MockHttpSession autenticar(String re, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"" + re + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private void recusarLogin(String re, String senha) throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"re\":\"" + re + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void trocarSenha(MockHttpSession sessao, String novaSenha) throws Exception {
        mockMvc.perform(post("/auth/trocar-senha")
                        .session(sessao)
                        .contentType("application/json")
                        .content("{\"novaSenha\":\"" + novaSenha
                                + "\",\"confirmacaoNovaSenha\":\"" + novaSenha + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocaSenhaObrigatoria").value(false));
    }
}
