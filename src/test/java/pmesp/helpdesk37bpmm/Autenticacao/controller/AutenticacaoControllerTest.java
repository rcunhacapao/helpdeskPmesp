package pmesp.helpdesk37bpmm.Autenticacao.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginRespostaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.service.AutenticacaoService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

    @Mock
    private AutenticacaoService autenticacaoService;

    @Mock
    private Authentication autenticacao;

    @InjectMocks
    private AutenticacaoController controller;

    @BeforeEach
    void autenticarUsuarioDeTeste() {
        when(autenticacao.isAuthenticated()).thenReturn(true);
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRecuperarOsDadosDaSessaoAutenticada() {
        LoginRespostaDTO respostaEsperada = new LoginRespostaDTO("SD PM Teste", "123456", false);
        when(autenticacao.getName()).thenReturn("123456");
        when(autenticacaoService.montarRespostaDeLogin("123456")).thenReturn(respostaEsperada);

        LoginRespostaDTO resposta = controller.consultarSessao();

        assertSame(respostaEsperada, resposta);
        verify(autenticacaoService).montarRespostaDeLogin("123456");
    }
}
