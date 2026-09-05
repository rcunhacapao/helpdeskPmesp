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
        // Normaliza quebras de linha (o arquivo pode estar em CRLF ou LF conforme o sistema) para que
        // as comparações de trechos com múltiplas linhas não dependam do fim de linha do disco.
        String pagina = Files.readString(DIRETORIO_ESTATICO.resolve("index.html")).replace("\r\n", "\n");
        String estilos = Files.readString(DIRETORIO_ESTATICO.resolve("styles.css")).replace("\r\n", "\n");
        String comportamento = Files.readString(DIRETORIO_ESTATICO.resolve("app.js")).replace("\r\n", "\n");

        assertAll(
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("logo-pmesp.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("favicon.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("apple-touch-icon.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-avatar.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-visao-geral.png"))),
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-livre.png"))),

                // Telas principais continuam presentes
                () -> assertTrue(pagina.contains("id=\"pagina-login\"")),
                () -> assertTrue(pagina.contains("styles.css?v=0.0.1-beta.18")),
                () -> assertTrue(pagina.contains("app.js?v=0.0.1-beta.18")),
                () -> assertTrue(pagina.contains("id=\"visao-geral\"")),
                () -> assertTrue(pagina.contains("id=\"meus-chamados\"")),
                () -> assertTrue(pagina.contains("id=\"abrir-chamado\"")),
                () -> assertTrue(pagina.contains("id=\"central-tecnica\"")),
                () -> assertTrue(pagina.contains("id=\"fila-atendimento\"")),
                () -> assertTrue(pagina.contains("id=\"gestao-usuarios\"")),
                () -> assertTrue(pagina.contains("id=\"mike-ia\"")),
                () -> assertTrue(pagina.contains("data-profile=\"usuario\"")),
                () -> assertTrue(pagina.contains("data-profile=\"tecnico\"")),

                // Primeiro acesso: tela nova, precisa existir para o fluxo real de autenticação
                () -> assertTrue(pagina.contains("id=\"formulario-primeiro-acesso\"")),
                () -> assertTrue(pagina.contains("id=\"primeiro-acesso-re\"")),
                () -> assertTrue(pagina.contains("id=\"primeiro-acesso-email\"")),
                () -> assertTrue(pagina.contains("id=\"primeiro-acesso-senha\"")),

                // Cadastro de usuário agora exige e-mail funcional (contrato real do backend)
                () -> assertTrue(pagina.contains("id=\"cadastro-email\"")),
                () -> assertTrue(pagina.contains("value=\"SGT_3\"")),

                // Motivo de cancelamento é um select com os códigos aceitos pelo backend, não mais texto livre
                () -> assertTrue(pagina.contains("id=\"motivo-cancelamento\"")),
                () -> assertTrue(pagina.contains("value=\"RESOLVIDO_NO_LOCAL\"")),
                () -> assertTrue(pagina.contains("value=\"CHAMADO_DUPLICADO\"")),
                () -> assertFalse(pagina.contains("<textarea id=\"motivo-cancelamento\"")),

                // Listas que antes eram estáticas agora são preenchidas via JavaScript
                () -> assertTrue(pagina.contains("id=\"lista-meus-chamados\"")),
                () -> assertTrue(pagina.contains("id=\"lista-tecnicos\"")),
                () -> assertTrue(pagina.contains("id=\"transferir-responsavel\"")),
                () -> assertFalse(pagina.contains("Histórico de chamados")),
                () -> assertFalse(pagina.contains("history-card")),

                () -> assertTrue(pagina.contains("id=\"overview-ticket-card\"")),
                () -> assertTrue(pagina.contains("id=\"overview-sem-chamado\"")),
                () -> assertTrue(pagina.contains("Avisos gerais")),
                () -> assertTrue(pagina.contains("Gestão de usuários")),
                () -> assertTrue(pagina.contains("Controle de acesso")),
                () -> assertTrue(pagina.contains("Equipe técnica")),
                () -> assertTrue(pagina.contains("id=\"resumo-fila-quantidade\"")),
                () -> assertTrue(pagina.contains("id=\"resumo-fila-descricao\"")),
                () -> assertTrue(pagina.contains("data-queue-filter")),
                () -> assertTrue(pagina.contains("id=\"detalhe-chamado-tecnico\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"iniciar\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"transferir\"")),
                () -> assertTrue(pagina.contains("data-queue-action=\"finalizar\"")),
                () -> assertFalse(pagina.contains("data-demo-action")),
                () -> assertTrue(pagina.contains("id=\"saudacao-usuario\"")),
                () -> assertFalse(pagina.contains("mobile-navigation")),
                () -> assertFalse(pagina.contains("mobile-header")),
                () -> assertTrue(pagina.contains("id=\"sair\"")),
                () -> assertTrue(pagina.contains("class=\"mike-overview-avatar\"")),
                () -> assertTrue(pagina.contains("class=\"mike-message-avatar\"")),
                () -> assertTrue(pagina.contains("class=\"mike-fullbody-avatar\"")),
                () -> assertTrue(pagina.contains("src=\"mike-ia-livre.png\"")),
                () -> assertTrue(pagina.contains("Versão 0.0.1 (beta)")),

                // Paleta e identidade visual (auditoria de UI/UX) continuam intactas
                () -> assertTrue(estilos.contains("--nav: #1A1D20;")),
                () -> assertTrue(estilos.contains("--primary: #8B0000;")),
                () -> assertTrue(estilos.contains("--danger: #DC3545;")),
                () -> assertTrue(estilos.contains(".button-primary {\n    background: var(--primary);")),
                () -> assertFalse(estilos.contains(".login-card .button-primary")),
                () -> assertTrue(estilos.contains(".current-ticket-card")),
                () -> assertTrue(estilos.contains(".technical-queue-layout")),
                () -> assertTrue(estilos.contains(".queue-detail-card")),
                () -> assertTrue(estilos.contains(".technician-list")),
                () -> assertTrue(estilos.contains(".management-actions")),
                () -> assertFalse(estilos.contains(".mobile-navigation")),
                () -> assertFalse(estilos.contains("@media (max-width: 768px)")),

                // Comunicação real com a API, não mais dados simulados em memória
                () -> assertTrue(comportamento.contains("async function apiFetch")),
                () -> assertTrue(comportamento.contains("credentials: 'include'")),
                () -> assertTrue(comportamento.contains("apiFetch('/auth/login'")),
                () -> assertTrue(comportamento.contains("apiFetch('/auth/primeiro-acesso'")),
                () -> assertTrue(comportamento.contains("apiFetch('/logout'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/cadastrar'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/meus'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/fila'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/em-atendimento'")),
                () -> assertTrue(comportamento.contains("/chamados/cancelar/")),
                () -> assertTrue(comportamento.contains("/chamados/iniciar-atendimento/")),
                () -> assertTrue(comportamento.contains("/chamados/transferir-responsavel/")),
                () -> assertTrue(comportamento.contains("/chamados/finalizar/")),
                () -> assertTrue(comportamento.contains("apiFetch('/usuarios/cadastrar'")),
                () -> assertTrue(comportamento.contains("/usuarios/buscar/")),
                () -> assertTrue(comportamento.contains("/usuarios/atualizar-dados/")),
                () -> assertTrue(comportamento.contains("/usuarios/inativar/")),
                () -> assertTrue(comportamento.contains("apiFetch('/tecnicos/cadastrar'")),
                () -> assertTrue(comportamento.contains("apiFetch('/tecnicos/disponiveis'")),
                () -> assertTrue(comportamento.contains("apiFetch('/tecnicos')")),

                // Não sobrou nenhum vestígio do modo de demonstração mockado
                () -> assertFalse(comportamento.contains("chamadosDemonstracao")),
                () -> assertFalse(comportamento.contains("sessaoDemonstracao")),
                () -> assertFalse(comportamento.contains("perfilDemonstracao")),
                () -> assertFalse(comportamento.contains("mostrarMensagemDeDemonstracao")),
                () -> assertFalse(comportamento.contains("data-demo-action")),
                () -> assertFalse(comportamento.contains("get('perfil') === 'tecnico'")),
                () -> assertTrue(comportamento.contains("aplicarSessaoNaInterface")),
                () -> assertTrue(comportamento.contains("sessaoAtual?.tecnico")),
                () -> assertTrue(comportamento.contains("renderizarFilaAtendimento")),
                () -> assertTrue(comportamento.contains("executarAcaoDaFila")),
                () -> assertTrue(comportamento.contains("showRoute('chamado-sucesso')")),
                () -> assertTrue(pagina.contains("id=\"chamado-sucesso\"")),
                () -> assertTrue(pagina.contains("class=\"success-check\""))
        );
    }
}
