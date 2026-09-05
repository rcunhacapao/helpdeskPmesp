package pmesp.helpdesk37bpmm.frontend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginVisualEstaticoTest {

    @Test
    void deveAutenticarDeVerdadeAntesDeMostrarOAppShell() throws IOException {
        String comportamento = Files.readString(Path.of("src", "main", "resources", "static", "app.js"));
        String estilos = Files.readString(Path.of("src", "main", "resources", "static", "styles.css"));

        // O login não usa mais checkValidity/dados simulados: ele depende do resultado
        // real de POST /auth/login. Só mostra o app se a API confirmar a sessão.
        assertFalse(comportamento.contains("loginForm.checkValidity()"));
        assertTrue(comportamento.contains("apiFetch('/auth/login'"));
        assertTrue(comportamento.contains("apiFetch('/auth/sessao'"));
        assertTrue(comportamento.contains("sessaoAtual.trocaSenhaObrigatoria"));
        assertTrue(comportamento.contains("apiFetch('/auth/trocar-senha'"));
        assertTrue(comportamento.contains("mostrarTrocaObrigatoriaDeSenha"));
        assertFalse(comportamento.contains("/auth/primeiro-acesso"));
        assertTrue(comportamento.contains("sessaoAtual = await apiFetch"));
        assertTrue(comportamento.contains("appPage.hidden = false;"));
        assertTrue(comportamento.contains("loginMessage.textContent = 'RE ou senha inválidos.';"));
        assertTrue(estilos.contains("[hidden]"));
        assertTrue(estilos.contains("display: none !important;"));
    }
}
