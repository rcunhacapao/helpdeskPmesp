package pmesp.helpdesk37bpmm.frontend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginVisualEstaticoTest {

    private static final Path DIRETORIO_ESTATICO = Path.of("src", "main", "resources", "static");

    @Test
    void deveAutenticarDeVerdadeAntesDeMostrarOAppShell() throws IOException {
        String comportamento = String.join("\n",
                Files.readString(DIRETORIO_ESTATICO.resolve("app.js")),
                Files.readString(DIRETORIO_ESTATICO.resolve("chamados-usuario.js")),
                Files.readString(DIRETORIO_ESTATICO.resolve("mike-atendimento.js")),
                Files.readString(DIRETORIO_ESTATICO.resolve("atendimento-tecnico.js")),
                Files.readString(DIRETORIO_ESTATICO.resolve("gestao.js")));
        String estilos = Files.readString(DIRETORIO_ESTATICO.resolve("styles.css"));

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
