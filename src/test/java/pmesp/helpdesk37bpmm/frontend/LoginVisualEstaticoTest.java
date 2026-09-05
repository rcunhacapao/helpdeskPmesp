package pmesp.helpdesk37bpmm.frontend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginVisualEstaticoTest {

    @Test
    void deveAbrirAVisaoGeralSemValidarCredenciaisNaDemonstracao() throws IOException {
        String comportamento = Files.readString(Path.of("src", "main", "resources", "static", "app.js"));
        String estilos = Files.readString(Path.of("src", "main", "resources", "static", "styles.css"));

        assertFalse(comportamento.contains("loginForm.checkValidity()"));
        assertTrue(comportamento.contains("appPage.hidden = false;"));
        assertTrue(comportamento.contains("rotaInicial: 'visao-geral'"));
        assertTrue(comportamento.contains("showRoute(sessaoDemonstracao.rotaInicial)"));
        assertTrue(estilos.contains("[hidden]"));
        assertTrue(estilos.contains("display: none !important;"));
    }
}
