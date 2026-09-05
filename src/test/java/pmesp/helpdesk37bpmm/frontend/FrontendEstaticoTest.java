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
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-livre.png"))),
                () -> assertTrue(pagina.contains("id=\"pagina-login\"")),
                () -> assertTrue(pagina.contains("styles.css?v=0.0.1-beta.10")),
                () -> assertTrue(pagina.contains("app.js?v=0.0.1-beta.10")),
                () -> assertTrue(pagina.contains("id=\"visao-geral\"")),
                () -> assertTrue(pagina.contains("id=\"abrir-chamado\"")),
                () -> assertTrue(pagina.contains("id=\"mike-ia\"")),
                () -> assertTrue(pagina.contains("data-profile=\"usuario\"")),
                () -> assertTrue(pagina.contains("data-profile=\"tecnico\"")),
                () -> assertTrue(pagina.contains("id=\"meus-chamados\"")),
                () -> assertTrue(pagina.contains("Andamento detalhado")),
                () -> assertTrue(pagina.contains("Histórico de chamados")),
                () -> assertTrue(pagina.contains("Acompanhamento do chamado")),
                () -> assertTrue(pagina.contains("id=\"overview-queue-position\"")),
                () -> assertTrue(pagina.contains("id=\"overview-queue-ahead\"")),
                () -> assertTrue(pagina.contains("id=\"overview-tech-assignment\"")),
                () -> assertTrue(pagina.contains("id=\"meus-chamados-queue-position\"")),
                () -> assertTrue(pagina.contains("id=\"meus-chamados-queue-ahead\"")),
                () -> assertTrue(pagina.contains("Avisos gerais")),
                () -> assertTrue(pagina.contains("Gestão de usuários")),
                () -> assertTrue(pagina.contains("Controle de acesso")),
                () -> assertTrue(pagina.contains("Equipe técnica")),
                () -> assertTrue(pagina.contains("Chamados hoje")),
                () -> assertTrue(pagina.contains("Nesta semana")),
                () -> assertTrue(pagina.contains("Neste mês")),
                () -> assertTrue(pagina.contains("id=\"fila-atendimento\"")),
                () -> assertTrue(pagina.contains("id=\"resumo-fila-quantidade\"")),
                () -> assertTrue(pagina.contains("id=\"resumo-fila-descricao\"")),
                () -> assertTrue(pagina.contains("data-queue-filter")),
                () -> assertTrue(pagina.contains("id=\"detalhe-chamado-tecnico\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"iniciar\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"transferir\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"finalizar\"")),
                () -> assertFalse(pagina.contains("data-queue-action=\"prioridade\"")),
                () -> assertFalse(pagina.contains("data-queue-action=\"cancelar\"")),
                () -> assertFalse(pagina.contains("id=\"prioridade-fila\"")),
                () -> assertFalse(pagina.contains("formulario-cancelamento-tecnico")),
                () -> assertFalse(pagina.contains("Coordenador de Telemática")),
                () -> assertTrue(pagina.contains("id=\"saudacao-usuario\"")),
                () -> assertFalse(pagina.contains("<p>Chamado atual</p>")),
                () -> assertFalse(pagina.contains("<p>Último atendimento</p>")),
                () -> assertFalse(pagina.contains("Acompanhar chamado atual")),
                () -> assertFalse(pagina.contains("<dt>Local</dt>")),
                () -> assertTrue(pagina.contains("class=\"mobile-navigation\"")),
                () -> assertTrue(pagina.contains("class=\"mike-overview-avatar\"")),
                () -> assertTrue(pagina.contains("class=\"mike-message-avatar\"")),
                () -> assertTrue(pagina.contains("class=\"mike-fullbody-avatar\"")),
                () -> assertTrue(pagina.contains("src=\"mike-ia-livre.png\"")),
                () -> assertFalse(pagina.contains("mike-persona-card")),
                () -> assertTrue(pagina.contains("Suporte inteligente para orientar seu atendimento.")),
                () -> assertTrue(pagina.contains("Versão 0.0.1 (beta)")),
                () -> assertFalse(pagina.contains("Equipamentos")),
                () -> assertFalse(pagina.contains("Base de conhecimento")),
                () -> assertTrue(estilos.contains("--nav: #1A1D20;")),
                () -> assertTrue(estilos.contains("--background: #F8F9FA;")),
                () -> assertTrue(estilos.contains("--surface: #FFFFFF;")),
                () -> assertTrue(estilos.contains("--primary: #8B0000;")),
                () -> assertTrue(estilos.contains("--danger: #DC3545;")),
                () -> assertTrue(estilos.contains("--success: #2E7D32;")),
                () -> assertTrue(estilos.contains("--success-bright: #78D88A;")),
                () -> assertTrue(estilos.contains("--priority-high: #6A3E8E;")),
                () -> assertTrue(estilos.contains("--info: #2D5F8B;")),
                () -> assertTrue(estilos.contains("--accent: #C79A32;")),
                () -> assertTrue(estilos.contains("--muted-blue: #5F6F7B;")),
                () -> assertTrue(estilos.contains("@media (prefers-reduced-motion: reduce)")),
                () -> assertTrue(estilos.contains(".ticket-form input,")),
                () -> assertTrue(estilos.contains(".ticket-form textarea")),
                () -> assertTrue(estilos.contains(".ticket-form {")),
                () -> assertTrue(estilos.contains(".ticket-form .field-group:focus-within")),
                () -> assertTrue(estilos.contains(".ticket-form select {")),
                () -> assertTrue(estilos.contains(".mobile-navigation")),
                () -> assertTrue(estilos.contains(".mike-overview-avatar")),
                () -> assertTrue(estilos.contains("height: clamp(184px, 28vh, 236px);")),
                () -> assertTrue(estilos.contains(".mike-message-avatar")),
                () -> assertTrue(estilos.contains(".mike-conversation-layout")),
                () -> assertTrue(estilos.contains(".mike-persona {")),
                () -> assertTrue(estilos.contains(".current-ticket-card")),
                () -> assertTrue(estilos.contains(".notice-list")),
                () -> assertTrue(estilos.contains(".technical-queue-layout")),
                () -> assertTrue(estilos.contains(".metric-today strong")),
                () -> assertTrue(estilos.contains(".availability-toggle.is-available")),
                () -> assertTrue(estilos.contains(".button-success")),
                () -> assertTrue(estilos.contains(".button-primary {\n    background: var(--success);")),
                () -> assertTrue(estilos.contains(".priority-label.priority-urgente")),
                () -> assertTrue(estilos.contains(".priority-label.priority-alta")),
                () -> assertTrue(estilos.contains("rgba(46, 125, 50, .24)")),
                () -> assertTrue(estilos.contains(".queue-filter")),
                () -> assertTrue(estilos.contains(".queue-detail-card")),
                () -> assertTrue(estilos.contains(".queue-summary-number")),
                () -> assertTrue(estilos.contains(".technician-list")),
                () -> assertTrue(estilos.contains(".management-actions")),
                () -> assertTrue(estilos.contains(".lookup-row { display: grid; grid-template-columns: minmax(0, 1fr) auto;")),
                () -> assertTrue(comportamento.contains("aplicarPerfilDemonstracao")),
                () -> assertTrue(comportamento.contains("get('perfil') === 'tecnico'")),
                () -> assertTrue(comportamento.contains(".mobile-nav-link")),
                () -> assertTrue(comportamento.contains("Chamado simulado com sucesso"))
                ,() -> assertTrue(comportamento.contains("renderizarFilaAtendimento"))
                ,() -> assertTrue(comportamento.contains("atualizarAcompanhamentoDoUsuario"))
                ,() -> assertTrue(comportamento.contains("chamadoSelecionadoId = null"))
                ,() -> assertTrue(comportamento.contains("Não há chamados pendentes no momento."))
                ,() -> assertTrue(comportamento.contains("executarAcaoDaFila"))
                ,() -> assertTrue(comportamento.contains("button.dataset.demoMessage"))
                ,() -> assertFalse(comportamento.contains("mostrarMensagemDeDemonstracao(button.dataset.demoAction)"))
                ,() -> assertFalse(comportamento.contains("acao === 'prioridade'"))
                ,() -> assertFalse(comportamento.contains("acao === 'cancelar'"))
                ,() -> assertFalse(comportamento.contains("Coordenador técnico"))
        );
    }
}
