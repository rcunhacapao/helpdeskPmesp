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
                () -> assertTrue(Files.exists(DIRETORIO_ESTATICO.resolve("mike-ia-abertura.png"))),

                // Telas principais continuam presentes
                () -> assertTrue(pagina.contains("id=\"pagina-login\"")),
                () -> assertTrue(pagina.contains("styles.css?v=0.0.1-beta.29")),
                () -> assertTrue(pagina.contains("app.js?v=0.0.1-beta.28")),
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
                () -> assertTrue(comportamento.contains("apiFetch('/auth/sessao'")),
                () -> assertTrue(comportamento.contains("configurarAtualizacaoAutomaticaDoUsuario")),
                () -> assertTrue(comportamento.contains("INTERVALO_DE_ATUALIZACAO_DO_USUARIO_EM_MS = 5000")),
                () -> assertTrue(comportamento.contains("window.setInterval")),
                () -> assertTrue(comportamento.contains("pararAtualizacaoAutomaticaDoUsuario")),
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
                () -> assertTrue(pagina.contains("class=\"success-check\"")),

                // O Mike faz parte do atendimento já iniciado. O técnico continua no
                // formulário completo, inclusive quando registra para outro RE.
                () -> assertTrue(pagina.contains("id=\"abertura-usuario\"")),
                () -> assertTrue(pagina.contains("id=\"abertura-tecnico\"")),
                () -> assertTrue(pagina.contains("id=\"mike-resolvido\"")),
                () -> assertTrue(pagina.contains("id=\"diagnostico-mike\"")),
                () -> assertTrue(pagina.contains("id=\"mike-pergunta-resolvido\"")),
                () -> assertTrue(pagina.contains("id=\"formulario-encaminhamento-mike\"")),
                () -> assertTrue(pagina.contains("id=\"abertura-anexo\"")),
                () -> assertTrue(pagina.contains("accept=\"image/png,image/jpeg,image/webp\"")),
                () -> assertTrue(pagina.contains("O envio será conectado ao backend posteriormente.")),
                () -> assertTrue(pagina.contains("type=\"submit\">Continuar</button>")),
                () -> assertTrue(pagina.contains("id=\"historico-mike-tecnico\"")),
                () -> assertTrue(pagina.contains("class=\"ticket-layout-com-mike\" id=\"abertura-tecnico\"")),
                () -> assertTrue(pagina.contains("class=\"abertura-usuario-layout\"")),
                () -> assertTrue(pagina.contains("src=\"mike-ia-abertura.png\"")),
                () -> assertTrue(estilos.contains(".ticket-layout-com-mike,")),
                () -> assertTrue(estilos.contains(".mike-abertura-avatar")),
                () -> assertTrue(estilos.contains(".abertura-inicial-form { padding: 16px 20px; }")),
                () -> assertTrue(estilos.contains(".abertura-inicial-form textarea { height: clamp(80px, 12vh, 104px); min-height: 80px; }")),
                () -> assertTrue(estilos.contains(".abertura-inicial-form .attachment-area")),
                () -> assertTrue(estilos.contains(".diagnostico-mike-cabecalho .eyebrow { color: var(--primary);")),
                () -> assertTrue(estilos.contains(".complemento-encaminhamento > .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains(".current-ticket-card .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains("#visao-geral .page-heading .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains("#meus-chamados .page-heading .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains(".ticket-list-card .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains(".queue-position-value > strong { color: var(--danger);")),
                () -> assertTrue(estilos.contains(".queue-position-value > strong.is-in-service { color: var(--success-bright); font-size:")),
                () -> assertTrue(estilos.contains(".progress-step.is-current span { background: var(--primary); border-color: var(--primary);")),
                () -> assertTrue(estilos.contains("#abrir-chamado { overflow-x: hidden; overflow-y: auto;")),
                () -> assertTrue(estilos.contains("@media (max-height: 820px)")),
                () -> assertTrue(estilos.contains("@media (max-height: 620px)")),
                () -> assertTrue(pagina.contains("Em criação")),
                () -> assertFalse(pagina.contains("Antes de abrir seu chamado")),
                () -> assertFalse(pagina.contains("id=\"formulario-mike\"")),
                () -> assertTrue(comportamento.contains("apiFetch('/mike-ia/iniciar'")),
                () -> assertTrue(comportamento.contains("atendimento.possuiOrientacaoTestavel === true")),
                () -> assertTrue(comportamento.contains("#mike-pergunta-resolvido")),
                () -> assertTrue(comportamento.contains("anexoAberturaUsuario?.addEventListener('change'")),
                () -> assertTrue(comportamento.contains("Envio ao chamado ainda não disponível.")),
                () -> assertTrue(comportamento.contains("emAtendimento ? 'Em atendimento' : (emFila ? 'Na fila' : '—')")),
                () -> assertTrue(comportamento.contains("classList.toggle('is-in-service', emAtendimento)")),
                () -> assertTrue(comportamento.contains("apiFetch('/mike-ia/em-diagnostico'")),
                () -> assertTrue(comportamento.contains("/mike-ia/concluir/")),
                () -> assertTrue(comportamento.contains("/mike-ia/encaminhar/")),
                () -> assertTrue(comportamento.contains("/mike-ia/abandonar/")),
                () -> assertTrue(comportamento.contains("/mike-ia/chamado/")),
                () -> assertTrue(comportamento.contains("sessaoAtual.tecnico")),
                () -> assertFalse(comportamento.contains("pularMikeIA")),
                () -> assertFalse(comportamento.contains("mike-triagem"))
        );
    }
}
