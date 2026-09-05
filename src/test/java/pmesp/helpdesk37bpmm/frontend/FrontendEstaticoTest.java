package pmesp.helpdesk37bpmm.frontend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendEstaticoTest {

    private static final Path DIRETORIO_ESTATICO = Path.of("src", "main", "resources", "static");

    @Test
    void deveManterAsTelasPrincipaisEASIdentidadeVisualDoProtótipo() throws IOException {
        String pagina = Files.readString(DIRETORIO_ESTATICO.resolve("index.html"));
        String estilos = Files.readString(DIRETORIO_ESTATICO.resolve("styles.css"));
        String comportamento = Files.readString(DIRETORIO_ESTATICO.resolve("app.js"));

        assertAll(
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("logo-pmesp.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-avatar.jpeg"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-visao-geral.png"))),
                () -> assertTrue(pagina.contains("id=\"pagina-login\"")),
                () -> assertTrue(pagina.contains("id=\"visao-geral\"")),
                () -> assertTrue(pagina.contains("id=\"abrir-chamado\"")),
                () -> assertTrue(pagina.contains("id=\"mike-ia\"")),
                () -> assertTrue(pagina.contains("class=\"mobile-navigation\"")),
                () -> assertTrue(pagina.contains("class=\"mike-overview-avatar\"")),
                () -> assertTrue(pagina.contains("class=\"mike-message-avatar\"")),
                () -> assertTrue(pagina.contains("Suporte inteligente para orientar seu atendimento.")),
                () -> assertTrue(pagina.contains("Versão 0.0.1 (beta)")),
                () -> assertFalse(pagina.contains("Equipamentos")),
                () -> assertFalse(pagina.contains("Base de conhecimento")),
                () -> assertTrue(estilos.contains("--nav: #1A1D20;")),
                () -> assertTrue(estilos.contains("--background: #F8F9FA;")),
                () -> assertTrue(estilos.contains("--surface: #FFFFFF;")),
                () -> assertTrue(estilos.contains("--primary: #8B0000;")),
                () -> assertTrue(estilos.contains("--danger: #DC3545;")),
                () -> assertTrue(estilos.contains("@media (prefers-reduced-motion: reduce)")),
                () -> assertTrue(estilos.contains(".ticket-form input,")),
                () -> assertTrue(estilos.contains(".ticket-form textarea")),
                () -> assertTrue(estilos.contains(".ticket-form {\n    display: flex;\n    flex-direction: column;\n    height: auto;\n    overflow: visible;")),
                () -> assertTrue(estilos.contains(".ticket-form .field-group:focus-within")),
                () -> assertTrue(estilos.contains(".ticket-form select {")),
                () -> assertTrue(estilos.contains(".mobile-navigation")),
                () -> assertTrue(estilos.contains(".mike-overview-avatar")),
                () -> assertTrue(estilos.contains("height: clamp(184px, 28vh, 236px);")),
                () -> assertTrue(estilos.contains(".mike-message-avatar")),
                () -> assertTrue(comportamento.contains("showRoute('visao-geral')")),
                () -> assertTrue(comportamento.contains(".mobile-nav-link")),
                () -> assertTrue(comportamento.contains("Chamado simulado com sucesso"))
        );
    }
}
