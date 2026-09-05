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
        String fluxoMike = Files.readString(DIRETORIO_ESTATICO.resolve("mike-triagem.js")).replace("\r\n", "\n");

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
                () -> assertTrue(pagina.contains("styles.css?v=0.0.1-beta.55")),
                () -> assertTrue(pagina.contains("mike-triagem.js?v=0.0.1-beta.38")),
                () -> assertTrue(pagina.contains("app.js?v=0.0.1-beta.42")),
                () -> assertTrue(pagina.contains("id=\"visao-geral\"")),
                () -> assertTrue(pagina.contains("id=\"meus-chamados\"")),
                () -> assertTrue(pagina.contains("id=\"abrir-chamado\"")),
                () -> assertTrue(pagina.contains("id=\"central-tecnica\"")),
                () -> assertTrue(pagina.contains("id=\"fila-atendimento\"")),
                () -> assertTrue(pagina.contains("id=\"gestao-usuarios\"")),
                () -> assertTrue(pagina.contains("id=\"mike-ia\"")),
                () -> assertTrue(pagina.contains("data-profile=\"usuario\"")),
                () -> assertTrue(pagina.contains("data-profile=\"tecnico\"")),

                // Login mostra apenas a ajuda de senha esquecida; a criação de senha aparece
                // depois que o backend autentica uma conta com troca obrigatória pendente.
                () -> assertTrue(pagina.contains("id=\"mostrar-esqueci-senha\"")),
                () -> assertTrue(pagina.contains("id=\"painel-esqueci-senha\"")),
                () -> assertTrue(pagina.contains("id=\"formulario-troca-senha\"")),
                () -> assertTrue(pagina.contains("id=\"confirmacao-nova-senha\"")),
                () -> assertFalse(pagina.contains("formulario-primeiro-acesso")),
                () -> assertFalse(pagina.contains("primeiro-acesso-email")),

                // O e-mail continua no cadastro, mas passou a ser opcional.
                () -> assertTrue(pagina.contains("id=\"cadastro-email\"")),
                () -> assertTrue(pagina.contains("E-mail funcional <span class=\"optional-label\">(opcional)</span>")),
                () -> assertTrue(pagina.contains("value=\"SGT_3\"")),

                // Motivo de cancelamento é um select com os códigos aceitos pelo backend, não mais texto livre
                () -> assertTrue(pagina.contains("id=\"motivo-cancelamento\"")),
                () -> assertTrue(pagina.contains("value=\"RESOLVIDO_NO_LOCAL\"")),
                () -> assertTrue(pagina.contains("value=\"CHAMADO_DUPLICADO\"")),
                () -> assertFalse(pagina.contains("<textarea id=\"motivo-cancelamento\"")),

                // Listas que antes eram estáticas agora são preenchidas via JavaScript
                () -> assertTrue(pagina.contains("id=\"lista-meus-chamados\"")),
                () -> assertTrue(pagina.contains("data-ticket-filter=\"em-andamento\"")),
                () -> assertTrue(pagina.contains("id=\"modal-cancelamento\"")),
                () -> assertTrue(pagina.contains("id=\"modal-reset-senha\"")),
                () -> assertTrue(pagina.contains("id=\"carregar-mais-chamados\"")),
                () -> assertTrue(pagina.contains("id=\"lista-tecnicos\"")),
                () -> assertTrue(pagina.contains("id=\"transferir-responsavel\"")),
                () -> assertFalse(pagina.contains("Histórico de chamados")),
                () -> assertFalse(pagina.contains("history-card")),

                () -> assertTrue(pagina.contains("id=\"overview-ticket-card\"")),
                () -> assertTrue(pagina.contains("id=\"overview-sem-chamado\"")),
                () -> assertTrue(pagina.contains("id=\"overview-ticket-id\"")),
                () -> assertFalse(pagina.contains("id=\"overview-ticket-info\"")),
                () -> assertFalse(pagina.contains("id=\"overview-ticket-status\"")),
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
                () -> assertTrue(comportamento.contains("apiFetch('/auth/trocar-senha'")),
                () -> assertFalse(comportamento.contains("apiFetch('/auth/primeiro-acesso'")),
                () -> assertTrue(comportamento.contains("apiFetch('/logout'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/cadastrar'")),
                () -> assertTrue(comportamento.contains("apiFetch('/chamados/meus'")),
                () -> assertTrue(comportamento.contains("meusChamadosForamCarregados")),
                () -> assertTrue(comportamento.contains("indicador-sincronizacao-chamados")),
                () -> assertTrue(comportamento.contains("modalCancelamento.showModal()")),
                () -> assertFalse(comportamento.contains("cancelTicketForm.scrollIntoView")),
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
                () -> assertTrue(comportamento.contains("/usuarios/resetar-senha/")),
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

                // O usuário recebe a triagem guiada. O técnico continua no formulário
                // completo, inclusive quando registra para outro RE, sem passar pelo chat.
                () -> assertTrue(pagina.contains("id=\"abertura-usuario\"")),
                () -> assertTrue(pagina.contains("id=\"abertura-tecnico\"")),
                () -> assertTrue(pagina.contains("id=\"mike-chat\"")),
                () -> assertTrue(pagina.contains("id=\"mike-chat-mensagens\"")),
                () -> assertTrue(pagina.contains("id=\"mike-chat-opcoes\"")),
                () -> assertTrue(pagina.contains("id=\"mike-chat-instrucao\"")),
                () -> assertTrue(pagina.contains("id=\"mike-chat-cancelar\"")),
                () -> assertTrue(pagina.contains("id=\"formulario-encaminhamento-mike\"")),
                () -> assertTrue(pagina.contains("id=\"abertura-anexo\"")),
                () -> assertTrue(pagina.contains("accept=\"image/png,image/jpeg,image/webp\"")),
                () -> assertTrue(pagina.contains("O envio será conectado ao backend posteriormente.")),
                () -> assertTrue(pagina.contains("id=\"historico-mike-tecnico\"")),
                () -> assertTrue(pagina.contains("class=\"ticket-layout-com-mike\" id=\"abertura-tecnico\"")),
                () -> assertTrue(pagina.contains("class=\"abertura-usuario-layout\"")),
                () -> assertTrue(pagina.contains("src=\"mike-ia-abertura.png\"")),
                () -> assertTrue(estilos.contains(".ticket-layout-com-mike,")),
                () -> assertTrue(estilos.contains(".mike-abertura-avatar")),
                () -> assertTrue(estilos.contains(".mike-chat-card")),
                () -> assertTrue(estilos.contains("flex-direction: column")),
                () -> assertTrue(estilos.contains(".mike-chat-formulario")),
                () -> assertTrue(estilos.contains(".mike-chat-bolha-usuario")),
                () -> assertTrue(estilos.contains(".mike-chat-digitando")),
                () -> assertTrue(estilos.contains(".mike-chat-instrucao")),
                () -> assertTrue(estilos.contains("@keyframes mikeDigitando")),
                () -> assertTrue(estilos.contains("prefers-reduced-motion: reduce")),
                () -> assertTrue(estilos.contains(".complemento-encaminhamento > .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains(".current-ticket-card .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains("#visao-geral .page-heading .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains("#meus-chamados .page-heading .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains(".ticket-list-card .eyebrow { color: var(--primary); }")),
                () -> assertTrue(estilos.contains("--colunas-meus-chamados: 110px minmax(300px, 1fr) 190px 155px 88px;")),
                () -> assertTrue(estilos.contains(".meus-chamados-colunas, .meu-chamado-linha { align-items: center; column-gap: 16px; display: grid; grid-template-columns: var(--colunas-meus-chamados); }")),
                () -> assertTrue(estilos.contains(".queue-position-value > strong { color: var(--danger);")),
                () -> assertTrue(estilos.contains(".queue-position-value > strong.is-in-service { color: var(--success); font-size:")),
                () -> assertTrue(estilos.contains(".mike-chat-formulario-cpf { flex-basis: 520px; }")),
                () -> assertTrue(estilos.contains(".progress-step.is-current span { background: var(--primary); border-color: var(--primary);")),
                () -> assertTrue(estilos.contains("#abrir-chamado { overflow: hidden; }")),
                () -> assertTrue(estilos.contains("@media (max-height: 920px)")),
                () -> assertTrue(estilos.contains("@media (max-height: 620px)")),
                () -> assertTrue(estilos.contains("@media (max-width: 1100px)")),
                () -> assertTrue(estilos.contains("grid-template-columns: minmax(220px, .4fr) minmax(0, 1fr);")),
                () -> assertTrue(pagina.contains("Em criação")),
                () -> assertFalse(pagina.contains("Antes de abrir seu chamado")),
                () -> assertFalse(pagina.contains("id=\"formulario-mike\"")),
                () -> assertTrue(comportamento.contains("apiFetch('/mike-ia/iniciar'")),
                () -> assertTrue(comportamento.contains("FluxoTriagemMike.avancarComOpcao")),
                () -> assertTrue(comportamento.contains("FluxoTriagemMike.montarResumo")),
                () -> assertTrue(comportamento.contains("processandoAcaoMike")),
                () -> assertTrue(comportamento.contains("TEMPO_PADRAO_DE_DIGITACAO_MIKE_EM_MS = 900")),
                () -> assertTrue(comportamento.contains("VERSAO_DA_TRIAGEM_MIKE = 2")),
                () -> assertTrue(comportamento.contains("aria-label', 'Mike está digitando'")),
                () -> assertTrue(comportamento.contains("cancelarEsperaDaRespostaMike")),
                () -> assertTrue(comportamento.contains("iniciarNovaConversaMike();")),
                // A saudação chega em duas mensagens separadas, com o mesmo intervalo de
                // "digitando" do resto da conversa entre a primeira e a segunda.
                () -> assertTrue(comportamento.contains("revelarSaudacaoInicialMike")),
                () -> assertFalse(comportamento.contains("await showRoute('visao-geral');")),
                () -> assertTrue(comportamento.contains("usuarioEstaPertoDoFimDaConversaMike")),
                () -> assertTrue(comportamento.contains("Não foi possível continuar agora. Tente novamente.")),
                () -> assertTrue(comportamento.contains("window.sessionStorage")),
                () -> assertTrue(comportamento.contains("anexoAberturaUsuario?.addEventListener('change'")),
                () -> assertTrue(comportamento.contains("Envio ao chamado ainda não disponível.")),
                () -> assertTrue(comportamento.contains("emAtendimento ? 'Em atendimento' : (emFila ? 'Na fila' : '—')")),
                () -> assertTrue(comportamento.contains("classList.toggle('is-in-service', emAtendimento)")),
                () -> assertTrue(comportamento.contains("#overview-ticket-id').textContent = `#${chamado.id}`")),
                () -> assertFalse(comportamento.contains("#overview-ticket-status")),
                () -> assertTrue(comportamento.contains("apiFetch('/mike-ia/em-diagnostico'")),
                () -> assertTrue(comportamento.contains("/mike-ia/concluir/")),
                () -> assertTrue(comportamento.contains("/mike-ia/encaminhar/")),
                () -> assertTrue(comportamento.contains("/mike-ia/abandonar/")),
                () -> assertTrue(comportamento.contains("/mike-ia/chamado/")),
                () -> assertTrue(comportamento.contains("sessaoAtual.tecnico")),
                () -> assertFalse(comportamento.contains("pularMikeIA")),
                () -> assertFalse(pagina.contains("id=\"diagnostico-mike\"")),
                () -> assertFalse(pagina.contains("id=\"mike-resolvido\"")),
                () -> assertTrue(fluxoMike.contains("Não sei")),
                () -> assertTrue(fluxoMike.contains("Olá! Sou o Mike IA. Irei te ajudar a abrir o chamado.")),
                () -> assertTrue(fluxoMike.contains("Para iniciar, escolha o equipamento ou serviço que você precisa de suporte.")),
                () -> assertTrue(fluxoMike.contains("TRIAGEM MIKE IA")),
                () -> assertTrue(fluxoMike.contains("Atendimento encerrado sem chamado.")),
                () -> assertTrue(fluxoMike.contains("Equipe técnica local.")),

                // Métricas do Mike IA na Central Técnica: numeros acumulados, separados
                // visualmente dos cartões "chamados hoje/semana/mês" (que são por período)
                () -> assertTrue(pagina.contains("id=\"mike-metrica-iniciados\"")),
                () -> assertTrue(pagina.contains("id=\"mike-metrica-resolvidos\"")),
                () -> assertTrue(pagina.contains("id=\"mike-metrica-encaminhados\"")),
                () -> assertTrue(pagina.contains("id=\"mike-metrica-taxa\"")),
                () -> assertTrue(comportamento.contains("apiFetch('/mike-ia/metricas'")),
                () -> assertTrue(comportamento.contains("Ainda não há atendimentos registrados pelo Mike IA."))
        );
    }
}
