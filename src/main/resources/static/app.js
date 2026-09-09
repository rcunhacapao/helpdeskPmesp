const loginForm = document.querySelector('#formulario-login');
const loginMessage = document.querySelector('#mensagem-login');
const loginPage = document.querySelector('#pagina-login');
const appPage = document.querySelector('#pagina-app');
const routes = document.querySelectorAll('[data-route]');
const views = document.querySelectorAll('[data-view]');
const navigationLinks = document.querySelectorAll('.nav-link');
const ticketForm = document.querySelector('#formulario-chamado');
const ticketMessage = document.querySelector('#mensagem-chamado');
const chatMike = document.querySelector('#mike-chat');
const mensagensChatMike = document.querySelector('#mike-chat-mensagens');
const acoesChatMike = document.querySelector('#mike-chat-acoes');
const instrucaoChatMike = document.querySelector('#mike-chat-instrucao');
const opcoesChatMike = document.querySelector('#mike-chat-opcoes');
const mensagemChatMike = document.querySelector('#mensagem-mike-chat');
const anexoAberturaUsuario = document.querySelector('#abertura-anexo');
const statusAnexoAberturaUsuario = document.querySelector('#abertura-anexo-status');
const formularioEncaminhamentoMike = document.querySelector('#formulario-encaminhamento-mike');
const cancelTicketForm = document.querySelector('#formulario-cancelamento');
const cancelTicketMessage = document.querySelector('#mensagem-cancelamento');
const modalCancelamento = document.querySelector('#modal-cancelamento');
const listaMeusChamados = document.querySelector('#lista-meus-chamados');
const contadorMeusChamados = document.querySelector('#contador-meus-chamados');
const indicadorSincronizacaoChamados = document.querySelector('#indicador-sincronizacao-chamados');
const feedbackMeusChamados = document.querySelector('#mensagem-meus-chamados');
const botaoCarregarMaisChamados = document.querySelector('#carregar-mais-chamados');
const queueMessage = document.querySelector('#mensagem-fila');
const queueList = document.querySelector('#lista-fila');
const queueSearch = document.querySelector('#busca-chamado');
const queueDetailContent = document.querySelector('#conteudo-detalhe-chamado');
const queueDetailEmpty = document.querySelector('#detalhe-chamado-vazio');
const forgotPasswordPanel = document.querySelector('#painel-esqueci-senha');
const passwordChangeForm = document.querySelector('#formulario-troca-senha');
const resetPasswordModal = document.querySelector('#modal-reset-senha');

// Elementos consultados repetidamente pelas funções de renderização da fila;
// cacheados uma única vez em vez de buscados no DOM a cada nova renderização.
const resumoFilaQuantidade = document.querySelector('#resumo-fila-quantidade');
const tituloDetalheChamado = document.querySelector('#titulo-detalhe-chamado');
const resumoFilaDescricao = document.querySelector('#resumo-fila-descricao');
const detalheCodigo = document.querySelector('#detalhe-codigo');
const detalheAssunto = document.querySelector('#detalhe-assunto');
const detalheAbertura = document.querySelector('#detalhe-abertura');
const detalheStatus = document.querySelector('#detalhe-status');
const detalheSolicitante = document.querySelector('#detalhe-solicitante');
const detalheLocal = document.querySelector('#detalhe-local');
const detalheCategoria = document.querySelector('#detalhe-categoria');
const detalheResponsavel = document.querySelector('#detalhe-responsavel');
const detalheDescricao = document.querySelector('#detalhe-descricao');
const detalhePrioridadeElemento = document.querySelector('#detalhe-prioridade');
const acoesChamadoAberto = document.querySelector('#acoes-chamado-aberto');
const acoesTransferencia = document.querySelector('#acoes-transferencia');
const acoesChamadoAtendimento = document.querySelector('#acoes-chamado-atendimento');
const contadorFilaAberta = document.querySelector('#contador-fila-aberta');
const contadorEmAtendimento = document.querySelector('#contador-em-atendimento');
const contadorChamadosAtivos = document.querySelector('#contador-chamados-ativos');
const filtrosDaFila = document.querySelectorAll('[data-queue-filter]');

async function apiFetch(caminho, opcoes = {}) {
    const configuracao = {
        method: opcoes.method || 'GET',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', ...(opcoes.headers || {}) }
    };
    if (opcoes.body !== undefined) {
        configuracao.body = JSON.stringify(opcoes.body);
    }

    let resposta;
    try {
        resposta = await fetch(caminho, configuracao);
    } catch (erroDeRede) {
        // fetch() rejeita (em vez de responder com um status HTTP) quando a rede cai ou o
        // servidor está inacessível; sem isso, o usuário veria "Failed to fetch" em inglês.
        throw new Error('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.');
    }

    if (resposta.status === 401) {
        sessaoAtual = null;
        if (appPage && !appPage.hidden) {
            appPage.hidden = true;
            loginPage.hidden = false;
        }
    }

    if (resposta.status === 204) {
        return null;
    }

    const texto = await resposta.text();
    const dados = texto ? JSON.parse(texto) : null;

    if (!resposta.ok) {
        const mensagem = dados?.mensagem || 'Ocorreu um erro inesperado. Tente novamente.';
        const erro = new Error(mensagem);
        erro.codigo = dados?.codigo;
        erro.status = resposta.status;
        throw erro;
    }

    return dados;
}

async function executarComEstadoDeEnvio(botao, textoEnviando, acaoAssincrona) {
    if (!botao) { await acaoAssincrona(); return; }
    const textoOriginal = botao.textContent;
    botao.disabled = true;
    botao.textContent = textoEnviando;
    try {
        await acaoAssincrona();
    } finally {
        botao.disabled = false;
        botao.textContent = textoOriginal;
    }
}

let sessaoAtual = null;
let intervaloDeAtualizacaoDoUsuario = null;
const INTERVALO_DE_ATUALIZACAO_DO_USUARIO_EM_MS = 5000;

function pertenceAoPerfil(elemento) {
    const perfilPermitido = elemento.dataset.profile || 'todos';
    const perfilAtual = sessaoAtual?.tecnico ? 'tecnico' : 'usuario';
    return perfilPermitido === 'todos' || perfilPermitido === perfilAtual;
}

function aplicarSessaoNaInterface() {
    document.querySelectorAll('.nav-link[data-profile]').forEach((item) => {
        item.hidden = !pertenceAoPerfil(item);
    });

    // Só o técnico pode abrir um chamado em nome de outra pessoa
    document.querySelector('#grupo-re-solicitante').hidden = !sessaoAtual.tecnico;
    document.querySelector('#abertura-tecnico').hidden = !sessaoAtual.tecnico;
    document.querySelector('#abertura-usuario').hidden = sessaoAtual.tecnico;

    const descricaoAbertura = document.querySelector('#descricao-abrir-chamado');
    if (descricaoAbertura) {
        descricaoAbertura.textContent = sessaoAtual.tecnico
            ? 'Preencha as informações para registrar o atendimento, inclusive em nome de outro RE quando necessário.'
            : 'Converse com o Mike para receber orientações rápidas ou abrir um chamado.';
    }

    document.querySelector('#identificacao-sidebar').textContent = sessaoAtual.identificacaoCompleta;
    document.querySelector('#perfil-sessao').textContent = `Perfil: ${sessaoAtual.tecnico ? 'Técnico' : 'Usuário'}`;

    const saudacaoUsuario = document.querySelector('#saudacao-usuario');
    const saudacaoTecnico = document.querySelector('#saudacao-tecnico');
    if (saudacaoUsuario) saudacaoUsuario.textContent = sessaoAtual.identificacaoCompleta;
    if (saudacaoTecnico) saudacaoTecnico.textContent = sessaoAtual.identificacaoCompleta;
}

async function restaurarSessaoAoCarregarPagina() {
    try {
        sessaoAtual = await apiFetch('/auth/sessao');
        if (sessaoAtual.trocaSenhaObrigatoria) {
            mostrarTrocaObrigatoriaDeSenha();
            return;
        }
        await abrirAplicacaoComSessaoAtual();
    } catch (erro) {
        // Ausência de sessão é o estado normal de quem ainda precisa entrar.
        sessaoAtual = null;
        appPage.hidden = true;
        loginPage.hidden = false;
    }
}

async function showRoute(route) {
    const destino = document.querySelector(`#${route}`);
    const rotaPadrao = sessaoAtual?.tecnico ? 'central-tecnica' : 'visao-geral';
    const rotaValida = destino && pertenceAoPerfil(destino) ? route : rotaPadrao;

    views.forEach((view) => {
        const isCurrentView = view.id === rotaValida && pertenceAoPerfil(view);
        view.hidden = !isCurrentView;
        view.classList.toggle('is-visible', isCurrentView);
    });

    navigationLinks.forEach((link) => {
        const isActive = link.dataset.route === rotaValida && !link.hidden;
        link.classList.toggle('is-active', isActive);
        link.toggleAttribute('aria-current', isActive);
    });

    await carregarDadosDaRota(rotaValida);
    configurarAtualizacaoAutomaticaDoUsuario(rotaValida);
}

// Enquanto o usuário acompanha um chamado, busca o estado novo periodicamente. Assim,
// quando o técnico iniciar ou finalizar o atendimento, a tela muda sem exigir F5.
function configurarAtualizacaoAutomaticaDoUsuario(rotaAtual) {
    const deveAtualizarAutomaticamente = !sessaoAtual?.tecnico
        && (rotaAtual === 'visao-geral' || rotaAtual === 'meus-chamados');

    if (!deveAtualizarAutomaticamente) {
        pararAtualizacaoAutomaticaDoUsuario();
        return;
    }

    if (intervaloDeAtualizacaoDoUsuario !== null) return;

    intervaloDeAtualizacaoDoUsuario = window.setInterval(async () => {
        if (document.hidden) return;

        const rotaVisivel = document.querySelector('[data-view]:not([hidden])')?.id;
        if (rotaVisivel === 'visao-geral') await carregarVisaoGeral();
        if (rotaVisivel === 'meus-chamados') await carregarMeusChamados();
    }, INTERVALO_DE_ATUALIZACAO_DO_USUARIO_EM_MS);
}

function pararAtualizacaoAutomaticaDoUsuario() {
    if (intervaloDeAtualizacaoDoUsuario === null) return;

    window.clearInterval(intervaloDeAtualizacaoDoUsuario);
    intervaloDeAtualizacaoDoUsuario = null;
}

function carregarDadosDaRota(rota) {
    if (rota === 'visao-geral') return carregarVisaoGeral();
    if (rota === 'meus-chamados') return carregarMeusChamados();
    if (rota === 'abrir-chamado') return carregarAberturaDeChamado();
    if (rota === 'central-tecnica') return carregarCentralTecnica();
    if (rota === 'fila-atendimento') return carregarFilaAtendimento();
    if (rota === 'gestao-usuarios') return carregarListaTecnicos();
    if (rota === 'relatos-erro-tecnico') return carregarRelatosDeErro();
    return Promise.resolve();
}

routes.forEach((route) => {
    route.addEventListener('click', () => showRoute(route.dataset.route));
});

function mostrarFormularioDeLogin() {
    loginPage.hidden = false;
    appPage.hidden = true;
    loginForm.hidden = false;
    forgotPasswordPanel.hidden = true;
    passwordChangeForm.hidden = true;
    document.querySelector('#mostrar-esqueci-senha').hidden = false;
    loginMessage.textContent = '';
}

function mostrarTrocaObrigatoriaDeSenha() {
    loginPage.hidden = false;
    appPage.hidden = true;
    loginForm.hidden = true;
    forgotPasswordPanel.hidden = true;
    passwordChangeForm.hidden = false;
    document.querySelector('#mostrar-esqueci-senha').hidden = true;
    document.querySelector('#nova-senha')?.focus({ preventScroll: true });
}

async function abrirAplicacaoComSessaoAtual() {
    loginPage.hidden = true;
    appPage.hidden = false;
    aplicarSessaoNaInterface();
    await showRoute(sessaoAtual.tecnico ? 'central-tecnica' : 'visao-geral');
}

loginForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const re = document.querySelector('#login').value.trim();
    const senha = document.querySelector('#senha').value;

    await executarComEstadoDeEnvio(loginForm.querySelector('button[type="submit"]'), 'Entrando...', async () => {
        try {
            sessaoAtual = await apiFetch('/auth/login', { method: 'POST', body: { re, senha } });
            loginMessage.classList.remove('is-error');
            loginMessage.textContent = '';
            loginForm.reset();
            if (sessaoAtual.trocaSenhaObrigatoria) {
                mostrarTrocaObrigatoriaDeSenha();
                return;
            }
            await abrirAplicacaoComSessaoAtual();
        } catch (erro) {
            loginMessage.classList.add('is-error');
            loginMessage.textContent = 'RE ou senha inválidos.';
        }
    });
});

document.querySelector('#mostrar-esqueci-senha')?.addEventListener('click', () => {
    loginForm.hidden = true;
    forgotPasswordPanel.hidden = false;
    document.querySelector('#mostrar-esqueci-senha').hidden = true;
});

document.querySelector('#ocultar-esqueci-senha')?.addEventListener('click', mostrarFormularioDeLogin);

passwordChangeForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const mensagem = document.querySelector('#mensagem-troca-senha');
    const novaSenha = document.querySelector('#nova-senha').value;
    const confirmacaoNovaSenha = document.querySelector('#confirmacao-nova-senha').value;

    if (!passwordChangeForm.checkValidity()) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Preencha os dois campos com uma senha de pelo menos 6 caracteres.';
        passwordChangeForm.reportValidity();
        return;
    }

    if (novaSenha !== confirmacaoNovaSenha) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'As senhas informadas não são iguais.';
        return;
    }

    await executarComEstadoDeEnvio(passwordChangeForm.querySelector('button[type="submit"]'), 'Salvando...', async () => {
        try {
            sessaoAtual = await apiFetch('/auth/trocar-senha', {
                method: 'POST', body: { novaSenha, confirmacaoNovaSenha }
            });
            mensagem.classList.remove('is-error');
            mensagem.textContent = '';
            passwordChangeForm.reset();
            await abrirAplicacaoComSessaoAtual();
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

async function encerrarSessao() {
    try {
        await apiFetch('/logout', { method: 'POST' });
    } catch (erro) {
        // Mesmo se a chamada falhar, a sessão local é encerrada abaixo.
    }
    pararAtualizacaoAutomaticaDoUsuario();
    sessaoAtual = null;
    loginForm.reset();
    passwordChangeForm?.reset();
    mostrarFormularioDeLogin();
}

document.querySelector('#sair')?.addEventListener('click', encerrarSessao);
document.querySelector('#sair-da-troca-senha')?.addEventListener('click', encerrarSessao);

const nomeDaCategoria = {
    COMPUTADOR: 'Computador', MONITOR: 'Monitor', IMPRESSORA: 'Impressora',
    REDE_INTERNET: 'Rede / Internet', E_MAIL: 'E-mail', OUTRO: 'Outro'
};
const nomeDaPrioridade = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', URGENTE: 'Urgente' };
const nomeDoStatus = {
    ABERTO: 'Aguardando atendimento',
    EM_ATENDIMENTO: 'Em atendimento', FECHADO: 'Finalizado',
    CANCELADO: 'Cancelado'
};

// O backend só devolve a descrição do posto (ex.: "3° SGT PM"), mas o formulário de
// edição precisa do valor do enum (ex.: SGT_3) para pré-selecionar a opção certa.
const chaveDoPostoPorDescricao = {
    'SD PM': 'SD', 'CB PM': 'CB', '3° SGT PM': 'SGT_3', '2° SGT PM': 'SGT_2',
    '1° SGT PM': 'SGT_1', 'SUBTEN PM': 'SUBTEN', 'ASP OF PM': 'ASP_OF',
    '2° TEN PM': 'TEN_2', '1° TEN PM': 'TEN_1', 'CAP PM': 'CAP',
    'MAJ PM': 'MAJ', 'TEN CEL PM': 'TEN_CEL', 'CEL PM': 'CEL'
};

// O backend guarda um único campo "descricao" (o formulário de abertura pede um
// assunto curto e uma descrição detalhada); a primeira linha funciona como título.
function tituloDoChamado(chamado) {
    return chamado.descricao.split('\n')[0];
}
