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

// ============================================================
// Comunicação com a API — um único lugar para montar a requisição,
// enviar o cookie de sessão e transformar erros do backend em algo
// fácil de mostrar na tela.
// ============================================================
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

// Desabilita o botão e mostra um texto de carregamento enquanto a ação assíncrona
// (uma chamada à API) não termina, reabilitando o botão ao final, com sucesso ou erro.
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

// ============================================================
// Sessão autenticada
// ============================================================
let sessaoAtual = null; // { identificacaoCompleta, re, tecnico, trocaSenhaObrigatoria }
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

// ============================================================
// Navegação entre telas — ao trocar de rota, também carrega os
// dados reais daquela tela.
// ============================================================
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
    return Promise.resolve();
}

routes.forEach((route) => {
    route.addEventListener('click', () => showRoute(route.dataset.route));
});

// ============================================================
// Login, troca obrigatória, orientação de senha esquecida e logout
// ============================================================
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

// ============================================================
// Vocabulário para exibir os enums do backend em português
// ============================================================
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

// ============================================================
// Visão geral — acompanhamento do chamado em aberto do usuário
// ============================================================
async function carregarVisaoGeral() {
    if (!document.querySelector('#overview-ticket-card')) return;
    try {
        const chamados = await apiFetch('/chamados/meus');
        const ativos = chamados.filter((chamado) => chamado.status === 'ABERTO' || chamado.status === 'EM_ATENDIMENTO');
        // Quando há mais de um chamado ativo, prioriza mostrar o que já está em
        // atendimento (informação mais relevante do que um chamado ainda na fila) —
        // a lista vem do backend do mais recente para o mais antigo, então o find()
        // já pega o mais recente dentro de cada status.
        const destaque = ativos.find((chamado) => chamado.status === 'EM_ATENDIMENTO') || ativos[0];
        exibirResumoDoChamado(destaque, ativos.length);
    } catch (erro) {
        exibirResumoDoChamado(null, 0);
    }
}

function atualizarProgresso(card, status) {
    const etapaPorStatus = { ABERTO: 1, EM_ATENDIMENTO: 2, FECHADO: 3, CANCELADO: 0 };
    const etapaAtual = etapaPorStatus[status] ?? 0;
    card.querySelectorAll('.progress-step').forEach((passo, indice) => {
        passo.classList.toggle('is-complete', indice < etapaAtual);
        passo.classList.toggle('is-current', indice === etapaAtual);
    });
}

function exibirResumoDoChamado(chamado, totalAtivos = 0) {
    const card = document.querySelector('#overview-ticket-card');
    const vazio = document.querySelector('#overview-sem-chamado');

    if (!chamado) {
        card.hidden = true;
        vazio.hidden = false;
        return;
    }

    // Quando há mais de um chamado ativo, só um aparece em destaque aqui — avisa que
    // existem outros, para o usuário não pensar que só tem um chamado aberto.
    const avisoOutros = document.querySelector('#overview-outros-chamados');
    const outrosAtivos = totalAtivos - 1;
    if (outrosAtivos > 0) {
        document.querySelector('#overview-outros-chamados-texto').textContent = outrosAtivos === 1
            ? 'Você tem mais 1 chamado em aberto.'
            : `Você tem mais ${outrosAtivos} chamados em aberto.`;
        avisoOutros.hidden = false;
    } else {
        avisoOutros.hidden = true;
    }

    vazio.hidden = true;
    card.hidden = false;
    document.querySelector('#overview-ticket-titulo').textContent = tituloDoChamado(chamado);
    document.querySelector('#overview-ticket-id').textContent = `#${chamado.id}`;

    const emFila = chamado.status === 'ABERTO';
    const emAtendimento = chamado.status === 'EM_ATENDIMENTO';
    const posicaoAtual = document.querySelector('#overview-queue-position');
    posicaoAtual.textContent = emAtendimento ? 'Em atendimento' : (emFila ? 'Na fila' : '—');
    posicaoAtual.classList.toggle('is-in-service', emAtendimento);
    document.querySelector('#overview-queue-ahead').textContent = emFila ? 'Aguardando um técnico' : 'Atendimento em andamento';
    document.querySelector('#overview-tech-assignment').textContent = chamado.tecnicoResponsavel
        ? `Técnico responsável: ${chamado.tecnicoResponsavel}.`
        : 'Técnico responsável: ainda não atribuído.';
    document.querySelector('#overview-queue-message').textContent = emFila
        ? 'Seu chamado foi recebido e aguarda a definição de um técnico responsável.'
        : `Seu atendimento já foi iniciado${chamado.tecnicoResponsavel ? ` por ${chamado.tecnicoResponsavel}` : ''}.`;

    atualizarProgresso(card, chamado.status);
}

// ============================================================
// Meus chamados — lista por situação + cancelamento contextual
// ============================================================
let chamadoParaCancelarId = null;
let botaoQueAbriuOCancelamento = null;
let chamadosDoUsuario = [];
let assinaturaDosChamadosDoUsuario = '';
let meusChamadosForamCarregados = false;
let carregandoMeusChamados = false;
let filtroAtualDeMeusChamados = 'em-andamento';
let quantidadeVisivelDeMeusChamados = 10;
const QUANTIDADE_INICIAL_DE_CHAMADOS = 10;
const detalhesAbertosDeMeusChamados = new Set();

function chamadosDoFiltroAtual() {
    const statusPorFiltro = {
        'em-andamento': ['ABERTO', 'EM_ATENDIMENTO'],
        finalizados: ['FECHADO'],
        cancelados: ['CANCELADO'],
        todos: ['ABERTO', 'EM_ATENDIMENTO', 'FECHADO', 'CANCELADO']
    };
    return chamadosDoUsuario.filter((chamado) => statusPorFiltro[filtroAtualDeMeusChamados].includes(chamado.status));
}

function criarEstadoDeCarregamentoDosChamados() {
    const carregamento = document.createElement('div');
    carregamento.className = 'skeleton-meus-chamados';
    carregamento.setAttribute('aria-label', 'Carregando chamados');
    carregamento.setAttribute('aria-busy', 'true');
    for (let indice = 0; indice < 3; indice += 1) {
        const linha = document.createElement('span');
        carregamento.appendChild(linha);
    }
    return carregamento;
}

function exibirIndicadorDeSincronizacao(estaSincronizando) {
    if (!indicadorSincronizacaoChamados) return;
    indicadorSincronizacaoChamados.hidden = !estaSincronizando;
}

function exibirFeedbackDeMeusChamados(mensagem, eErro = false) {
    if (!feedbackMeusChamados) return;
    feedbackMeusChamados.classList.toggle('is-error', eErro);
    feedbackMeusChamados.textContent = mensagem;
}

function descricaoDetalhadaDoChamado(chamado) {
    return chamado.descricao.split('\n').slice(1).join('\n').trim() || 'Nenhuma descrição complementar foi informada.';
}

// A assinatura permite atualizar somente a linha que realmente recebeu mudança do
// backend. Assim, um refresh em segundo plano não desmonta os demais chamados.
function assinaturaDoChamado(chamado) {
    return JSON.stringify(chamado);
}

function criarFatoDoChamado(rotulo, valor) {
    const fato = document.createElement('div');
    const titulo = document.createElement('dt');
    const conteudo = document.createElement('dd');
    titulo.textContent = rotulo;
    conteudo.textContent = valor || 'Não informado';
    fato.append(titulo, conteudo);
    return fato;
}

function criarDetalhesDoMeuChamado(chamado) {
    const detalhes = document.createElement('div');
    detalhes.className = 'meu-chamado-detalhes';
    detalhes.hidden = !detalhesAbertosDeMeusChamados.has(chamado.id);

    const descricao = document.createElement('p');
    descricao.className = 'meu-chamado-descricao';
    descricao.textContent = descricaoDetalhadaDoChamado(chamado);

    const fatos = document.createElement('dl');
    fatos.className = 'meu-chamado-fatos';
    fatos.append(
        criarFatoDoChamado('Categoria', nomeDaCategoria[chamado.categoria] || chamado.categoria),
        criarFatoDoChamado('Local / setor', chamado.localAtendimento),
        criarFatoDoChamado('Prioridade', chamado.prioridade ? nomeDaPrioridade[chamado.prioridade] : 'Sem prioridade'),
        criarFatoDoChamado('Técnico responsável', chamado.tecnicoResponsavel),
        criarFatoDoChamado('Atualizado em', chamado.dataUltimaInteracao),
        criarFatoDoChamado('Finalizado em', chamado.dataFinalizacao)
    );
    detalhes.append(descricao, fatos);

    if (chamado.solucao) {
        const solucao = document.createElement('p');
        solucao.className = 'meu-chamado-solucao';
        solucao.textContent = `Solução: ${chamado.solucao}`;
        detalhes.appendChild(solucao);
    }

    if (chamado.motivoCancelamento) {
        const motivo = document.createElement('p');
        motivo.className = 'meu-chamado-motivo-cancelamento';
        motivo.textContent = `Motivo do cancelamento: ${chamado.motivoCancelamento.replaceAll('_', ' ').toLowerCase()}.`;
        detalhes.appendChild(motivo);
    }
    return detalhes;
}

function abrirModalDeCancelamento(chamado, botaoDeOrigem) {
    chamadoParaCancelarId = chamado.id;
    botaoQueAbriuOCancelamento = botaoDeOrigem;
    document.querySelector('#titulo-modal-cancelamento').textContent = `Cancelar chamado #${chamado.id}`;
    document.querySelector('#cancelamento-chamado-info').textContent = tituloDoChamado(chamado);
    cancelTicketMessage.textContent = '';
    cancelTicketMessage.classList.remove('is-error');
    modalCancelamento.showModal();
    document.querySelector('#motivo-cancelamento').focus({ preventScroll: true });
}

function fecharModalDeCancelamento() {
    if (modalCancelamento?.open) modalCancelamento.close();
}

function criarItemDeMeuChamado(chamado) {
    const item = document.createElement('article');
    item.className = 'meu-chamado-item';
    item.dataset.chamadoId = chamado.id;
    item.dataset.assinatura = assinaturaDoChamado(chamado);

    const linha = document.createElement('div');
    linha.className = 'meu-chamado-linha';

    const protocolo = document.createElement('span');
    protocolo.className = 'meu-chamado-protocolo';
    protocolo.dataset.label = 'Protocolo';
    protocolo.textContent = `#${chamado.id}`;

    const assunto = document.createElement('div');
    assunto.className = 'meu-chamado-assunto';
    assunto.dataset.label = 'Assunto';
    const titulo = document.createElement('h3');
    titulo.textContent = tituloDoChamado(chamado);
    assunto.appendChild(titulo);

    const dataAbertura = document.createElement('span');
    dataAbertura.className = 'meu-chamado-data';
    dataAbertura.dataset.label = 'Aberto em';
    dataAbertura.textContent = chamado.dataAbertura;

    const status = document.createElement('span');
    status.className = `status-label status-${chamado.status.toLowerCase()}`;
    status.dataset.label = 'Status';
    status.textContent = nomeDoStatus[chamado.status];

    const acoes = document.createElement('div');
    acoes.className = 'meu-chamado-acoes';
    acoes.dataset.label = 'Ações';
    const botaoDetalhes = document.createElement('button');
    botaoDetalhes.type = 'button';
    botaoDetalhes.className = 'text-button';
    botaoDetalhes.textContent = detalhesAbertosDeMeusChamados.has(chamado.id) ? 'Ocultar detalhes' : 'Detalhes';
    botaoDetalhes.setAttribute('aria-expanded', String(detalhesAbertosDeMeusChamados.has(chamado.id)));

    const detalhes = criarDetalhesDoMeuChamado(chamado);
    botaoDetalhes.addEventListener('click', () => {
        const detalhesEstaoAbertos = detalhes.hidden;
        detalhes.hidden = !detalhesEstaoAbertos;
        botaoDetalhes.textContent = detalhesEstaoAbertos ? 'Ocultar detalhes' : 'Detalhes';
        botaoDetalhes.setAttribute('aria-expanded', String(detalhesEstaoAbertos));
        if (detalhesEstaoAbertos) detalhesAbertosDeMeusChamados.add(chamado.id);
        else detalhesAbertosDeMeusChamados.delete(chamado.id);
    });
    acoes.appendChild(botaoDetalhes);

    if (chamado.status === 'ABERTO') {
        const botaoCancelar = document.createElement('button');
        botaoCancelar.type = 'button';
        botaoCancelar.className = 'text-button text-button-danger';
        botaoCancelar.textContent = 'Cancelar';
        botaoCancelar.addEventListener('click', () => abrirModalDeCancelamento(chamado, botaoCancelar));
        acoes.appendChild(botaoCancelar);
    }

    linha.append(protocolo, assunto, dataAbertura, status, acoes);
    item.append(linha, detalhes);
    return item;
}

function renderizarMeusChamados() {
    if (!listaMeusChamados) return;
    const chamadosFiltrados = chamadosDoFiltroAtual();
    const chamadosVisiveis = chamadosFiltrados.slice(0, quantidadeVisivelDeMeusChamados);
    const itensExistentes = new Map([...listaMeusChamados.querySelectorAll('[data-chamado-id]')]
        .map((item) => [item.dataset.chamadoId, item]));
    const idsVisiveis = new Set(chamadosVisiveis.map((chamado) => String(chamado.id)));

    listaMeusChamados.querySelectorAll('.lista-chamados-vazia, .skeleton-meus-chamados').forEach((elemento) => elemento.remove());
    itensExistentes.forEach((item, id) => {
        if (!idsVisiveis.has(id)) item.remove();
    });

    chamadosVisiveis.forEach((chamado, indice) => {
        const itemAtual = itensExistentes.get(String(chamado.id));
        const precisaAtualizarLinha = !itemAtual || itemAtual.dataset.assinatura !== assinaturaDoChamado(chamado);
        const novoItem = precisaAtualizarLinha ? criarItemDeMeuChamado(chamado) : itemAtual;
        if (itemAtual && precisaAtualizarLinha) itemAtual.replaceWith(novoItem);
        const referencia = listaMeusChamados.children[indice];
        if (referencia !== novoItem) listaMeusChamados.insertBefore(novoItem, referencia || null);
    });

    if (!chamadosFiltrados.length) {
        const vazio = document.createElement('p');
        vazio.className = 'lista-chamados-vazia';
        vazio.textContent = chamadosDoUsuario.length
            ? 'Nenhum chamado corresponde a este filtro.'
            : 'Você ainda não abriu nenhum chamado.';
        listaMeusChamados.appendChild(vazio);
    }

    contadorMeusChamados.textContent = chamadosFiltrados.length === 1
        ? '1 chamado encontrado'
        : `${chamadosFiltrados.length} chamados encontrados`;
    botaoCarregarMaisChamados.hidden = chamadosVisiveis.length >= chamadosFiltrados.length;
}

async function carregarMeusChamados() {
    if (!listaMeusChamados || carregandoMeusChamados) return;
    const primeiroCarregamento = !meusChamadosForamCarregados;
    carregandoMeusChamados = true;
    if (primeiroCarregamento) {
        listaMeusChamados.replaceChildren(criarEstadoDeCarregamentoDosChamados());
    } else {
        exibirIndicadorDeSincronizacao(true);
    }

    try {
        const chamados = await apiFetch('/chamados/meus');
        const novaAssinatura = JSON.stringify(chamados);
        const houveAlteracao = novaAssinatura !== assinaturaDosChamadosDoUsuario;
        chamadosDoUsuario = chamados;
        assinaturaDosChamadosDoUsuario = novaAssinatura;
        meusChamadosForamCarregados = true;
        if (primeiroCarregamento || houveAlteracao) renderizarMeusChamados();
    } catch (erro) {
        if (primeiroCarregamento) {
            listaMeusChamados.replaceChildren();
            const mensagem = document.createElement('p');
            mensagem.className = 'lista-chamados-vazia';
            mensagem.textContent = erro.message;
            listaMeusChamados.appendChild(mensagem);
        } else {
            exibirFeedbackDeMeusChamados('Não foi possível atualizar os chamados agora. Os dados exibidos foram mantidos.', true);
        }
    } finally {
        carregandoMeusChamados = false;
        exibirIndicadorDeSincronizacao(false);
    }
}

document.querySelectorAll('[data-ticket-filter]').forEach((botao) => {
    botao.addEventListener('click', () => {
        filtroAtualDeMeusChamados = botao.dataset.ticketFilter;
        quantidadeVisivelDeMeusChamados = QUANTIDADE_INICIAL_DE_CHAMADOS;
        document.querySelectorAll('[data-ticket-filter]').forEach((filtro) => {
            const estaAtivo = filtro === botao;
            filtro.classList.toggle('is-active', estaAtivo);
            filtro.setAttribute('aria-selected', String(estaAtivo));
        });
        renderizarMeusChamados();
    });
});

botaoCarregarMaisChamados?.addEventListener('click', () => {
    quantidadeVisivelDeMeusChamados += QUANTIDADE_INICIAL_DE_CHAMADOS;
    renderizarMeusChamados();
});

document.querySelectorAll('[data-close-cancel]').forEach((botao) => {
    botao.addEventListener('click', fecharModalDeCancelamento);
});

modalCancelamento?.addEventListener('close', () => {
    cancelTicketForm.reset();
    cancelTicketMessage.textContent = '';
    cancelTicketMessage.classList.remove('is-error');
    chamadoParaCancelarId = null;
    botaoQueAbriuOCancelamento?.focus({ preventScroll: true });
    botaoQueAbriuOCancelamento = null;
});

cancelTicketForm?.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!cancelTicketForm.checkValidity()) {
        cancelTicketMessage.classList.add('is-error');
        cancelTicketMessage.textContent = 'Selecione o motivo do cancelamento.';
        cancelTicketForm.reportValidity();
        return;
    }

    const motivoCancelamento = document.querySelector('#motivo-cancelamento').value;

    await executarComEstadoDeEnvio(cancelTicketForm.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            const chamadoCancelado = await apiFetch(`/chamados/cancelar/${chamadoParaCancelarId}?motivoCancelamento=${motivoCancelamento}`, { method: 'PATCH' });
            chamadosDoUsuario = chamadosDoUsuario.map((chamado) => chamado.id === chamadoCancelado.id ? chamadoCancelado : chamado);
            assinaturaDosChamadosDoUsuario = JSON.stringify(chamadosDoUsuario);
            renderizarMeusChamados();
            fecharModalDeCancelamento();
            exibirFeedbackDeMeusChamados(`Chamado #${chamadoCancelado.id} cancelado com sucesso.`);
        } catch (erro) {
            cancelTicketMessage.classList.add('is-error');
            cancelTicketMessage.textContent = erro.message;
        }
    });
});

// ============================================================
// Abertura integrada com o Mike IA
// ============================================================
// Só existe para o usuário comum. O técnico usa o formulário completo e a decisão é
// feita pelo perfil autenticado, nunca pelo RE que pode ser informado para o solicitante.
let atendimentoMikeAtual = null;
let estadoTriagemMike = null;
let historicoDeEstadosMike = [];
let processandoAcaoMike = false;
const TEMPO_PADRAO_DE_DIGITACAO_MIKE_EM_MS = 900;
const VERSAO_DA_TRIAGEM_MIKE = 2;
let conversaVisivelDuranteRespostaMike = null;
let mikeEstaDigitando = false;
let timerRespostaMike = null;
let resolverEsperaRespostaMike = null;
let versaoDaInteracaoMike = 0;
let operacaoBackendMike = null;
let cancelandoTriagemMike = false;

function chaveDaTriagemMike() {
    return `helpdesk.mike.triagem.${sessaoAtual?.re || 'usuario'}`;
}

function copiarEstadoMike(estado) {
    return JSON.parse(JSON.stringify(estado));
}

function limparTriagemMikeArmazenada() {
    window.sessionStorage.removeItem(chaveDaTriagemMike());
}

function salvarTriagemMike() {
    // O CPF nunca é salvo no navegador. Se a página for recarregada, o fluxo volta
    // ao pedido do CPF e o usuário o informa novamente.
    if (!estadoTriagemMike || estadoTriagemMike.dados?.cpf) return;

    window.sessionStorage.setItem(chaveDaTriagemMike(), JSON.stringify({
        versao: VERSAO_DA_TRIAGEM_MIKE,
        atendimentoId: atendimentoMikeAtual?.atendimentoId || null,
        estado: estadoTriagemMike,
        historico: historicoDeEstadosMike
    }));
}

function carregarTriagemMikeArmazenada() {
    try {
        const dados = JSON.parse(window.sessionStorage.getItem(chaveDaTriagemMike()));
        if (dados?.versao !== VERSAO_DA_TRIAGEM_MIKE) {
            limparTriagemMikeArmazenada();
            return null;
        }
        if (!dados.estado?.etapaAtual || !Array.isArray(dados.estado.conversa)) return null;
        return dados;
    } catch (erro) {
        limparTriagemMikeArmazenada();
        return null;
    }
}

function mostrarMensagemChatMike(texto = '', erro = false) {
    mensagemChatMike.textContent = texto;
    mensagemChatMike.classList.toggle('is-error', erro);
}

function definirControlesDoMikeComoDesabilitados(desabilitados) {
    opcoesChatMike.querySelectorAll('button, input').forEach((controle) => {
        controle.disabled = desabilitados;
    });
}

function criarBotaoMike(texto, estilo, acao) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = `button ${estilo} mike-chat-opcao`;
    botao.textContent = texto;
    botao.disabled = processandoAcaoMike;
    botao.addEventListener('click', acao);
    return botao;
}

function adicionarBotaoVoltarMike() {
    if (historicoDeEstadosMike.length === 0) return;
    opcoesChatMike.appendChild(criarBotaoMike('Voltar', 'button-secondary', voltarTriagemMike));
}

function renderizarPerguntaMike(etapa) {
    etapa.opcoes.forEach((opcao) => {
        const estilo = opcao.id === 'resolvido' ? 'button-primary' : 'button-secondary';
        opcoesChatMike.appendChild(criarBotaoMike(opcao.texto, estilo, () => processarOpcaoMike(opcao.id)));
    });
    adicionarBotaoVoltarMike();
}

function renderizarEntradaMike(etapa) {
    const formulario = document.createElement('form');
    formulario.className = 'mike-chat-formulario';
    const solicitaCpf = etapa.campos.some((campo) => campo.formato === 'cpf');
    if (solicitaCpf) formulario.classList.add('mike-chat-formulario-cpf');

    etapa.campos.forEach((campo) => {
        const grupo = document.createElement('label');
        grupo.className = 'mike-chat-grupo-campo';
        grupo.textContent = campo.rotulo;

        const entrada = document.createElement('input');
        entrada.className = 'mike-chat-campo-livre';
        entrada.name = campo.id;
        entrada.required = campo.obrigatorio === true;
        entrada.autocomplete = 'off';
        if (campo.formato === 'cpf') {
            entrada.inputMode = 'numeric';
            entrada.maxLength = 11;
            // Sem "pattern": um formato inválido deve cair no catch de processarDadosMike,
            // que mostra "Informe um CPF com 11 números." em vez do aviso nativo do navegador.
        }

        grupo.appendChild(entrada);
        formulario.appendChild(grupo);
    });

    const botaoContinuar = criarBotaoMike('Continuar', 'button-primary', () => formulario.requestSubmit());
    formulario.appendChild(botaoContinuar);
    formulario.addEventListener('submit', processarDadosMike);
    if (solicitaCpf) adicionarBotaoVoltarMike();
    opcoesChatMike.appendChild(formulario);
    if (!solicitaCpf) adicionarBotaoVoltarMike();
    formulario.querySelector('input')?.focus();
}

function abrirFormularioEncaminhamentoMike() {
    if (!atendimentoMikeAtual || !estadoTriagemMike) {
        mostrarMensagemChatMike('Não foi possível recuperar este atendimento. Inicie uma nova conversa.', true);
        return;
    }

    document.querySelector('#encaminhamento-categoria').value = estadoTriagemMike.categoria || '';
    chatMike.hidden = true;
    formularioEncaminhamentoMike.hidden = false;
    const mensagem = document.querySelector('#mensagem-encaminhamento-mike');
    mensagem.textContent = '';
    mensagem.classList.remove('is-error');
    document.querySelector('#encaminhamento-local').focus();
}

function renderizarEncaminhamentoMike(etapa) {
    opcoesChatMike.appendChild(criarBotaoMike(etapa.botao, 'button-primary', abrirFormularioEncaminhamentoMike));
    adicionarBotaoVoltarMike();
    if (etapa.cancelar) {
        opcoesChatMike.appendChild(criarBotaoMike('Cancelar atendimento', 'button-secondary', cancelarTriagemMike));
    }
}

function iniciarNovaConversaMike() {
    versaoDaInteracaoMike += 1;
    cancelarEsperaDaRespostaMike();
    processandoAcaoMike = false;
    atendimentoMikeAtual = null;
    estadoTriagemMike = FluxoTriagemMike.criarEstadoInicial();
    historicoDeEstadosMike = [];
    limparTriagemMikeArmazenada();
    salvarTriagemMike();
    revelarSaudacaoInicialMike();
}

// A saudação chega em duas mensagens, como uma conversa real: a primeira aparece na
// hora e a segunda (com as opções de categoria) só depois do mesmo intervalo de
// "digitando" usado no restante da conversa.
function revelarSaudacaoInicialMike() {
    const versaoDestaInteracao = versaoDaInteracaoMike;
    conversaVisivelDuranteRespostaMike = estadoTriagemMike.conversa.slice(0, 1);
    mikeEstaDigitando = true;
    renderizarChatMike({ forcarRolagem: true });

    aguardarRespostaVisualMike().then(() => {
        if (versaoDestaInteracao !== versaoDaInteracaoMike) return;
        conversaVisivelDuranteRespostaMike = null;
        mikeEstaDigitando = false;
        renderizarChatMike({ forcarRolagem: true, focarPrimeiroControle: true });
    });
}

function renderizarConclusaoMike() {
    opcoesChatMike.appendChild(criarBotaoMike('Voltar ao início', 'button-primary', () => showRoute('visao-geral')));
}

function usuarioEstaPertoDoFimDaConversaMike() {
    const distanciaDoFim = mensagensChatMike.scrollHeight
        - mensagensChatMike.scrollTop
        - mensagensChatMike.clientHeight;
    return distanciaDoFim < 80;
}

function criarBolhaDaConversaMike(mensagem) {
    const bolha = document.createElement('div');
    bolha.className = `mike-chat-bolha mike-chat-bolha-${mensagem.autor}`;
    bolha.textContent = mensagem.texto;
    return bolha;
}

function criarIndicadorDeDigitacaoMike() {
    const bolha = document.createElement('div');
    bolha.className = 'mike-chat-bolha mike-chat-bolha-mike mike-chat-digitando';
    bolha.setAttribute('aria-label', 'Mike está digitando');
    for (let indice = 0; indice < 3; indice += 1) {
        const ponto = document.createElement('span');
        ponto.setAttribute('aria-hidden', 'true');
        bolha.appendChild(ponto);
    }
    return bolha;
}

function atualizarInstrucaoDasOpcoesMike(etapa) {
    if (mikeEstaDigitando) {
        acoesChatMike.classList.add('is-waiting');
        return;
    }

    acoesChatMike.hidden = false;
    acoesChatMike.classList.remove('is-waiting');
    opcoesChatMike.setAttribute('aria-labelledby', 'mike-chat-instrucao');
    opcoesChatMike.removeAttribute('aria-label');
    if (etapa.tipo === 'pergunta') instrucaoChatMike.textContent = 'Escolha uma opção:';
    if (etapa.tipo === 'entrada') instrucaoChatMike.textContent = 'Preencha as informações:';
    if (etapa.tipo === 'encaminhamento') instrucaoChatMike.textContent = 'Próximo passo:';
    if (etapa.tipo === 'resolvido') {
        instrucaoChatMike.hidden = true;
        opcoesChatMike.removeAttribute('aria-labelledby');
        opcoesChatMike.setAttribute('aria-label', 'Atendimento concluído');
    } else {
        instrucaoChatMike.hidden = false;
    }
}

function renderizarChatMike({ forcarRolagem = false, focarPrimeiroControle = false } = {}) {
    if (!estadoTriagemMike || !mensagensChatMike || !opcoesChatMike) return;

    const acompanharConversa = forcarRolagem || usuarioEstaPertoDoFimDaConversaMike();
    const posicaoAnterior = mensagensChatMike.scrollTop;
    const conversaVisivel = conversaVisivelDuranteRespostaMike || estadoTriagemMike.conversa;
    // Mantém o histórico no DOM para não repetir animações nem perder a âncora do scroll.
    mensagensChatMike.querySelector('.mike-chat-digitando')?.remove();
    let mensagensMantidas = 0;
    while (mensagensMantidas < conversaVisivel.length) {
        const bolha = mensagensChatMike.children[mensagensMantidas];
        const mensagem = conversaVisivel[mensagensMantidas];
        if (!bolha || bolha.textContent !== mensagem.texto
            || !bolha.classList.contains(`mike-chat-bolha-${mensagem.autor}`)) break;
        mensagensMantidas += 1;
    }
    while (mensagensChatMike.children.length > mensagensMantidas) mensagensChatMike.lastElementChild.remove();
    conversaVisivel.slice(mensagensMantidas).forEach((mensagem) => mensagensChatMike.appendChild(criarBolhaDaConversaMike(mensagem)));
    if (mikeEstaDigitando) mensagensChatMike.appendChild(criarIndicadorDeDigitacaoMike());
    mensagensChatMike.setAttribute('aria-busy', String(mikeEstaDigitando));

    const etapa = FluxoTriagemMike.obterEtapa(estadoTriagemMike);
    atualizarInstrucaoDasOpcoesMike(etapa);
    if (!mikeEstaDigitando) {
        opcoesChatMike.innerHTML = '';
        if (etapa.tipo === 'pergunta') renderizarPerguntaMike(etapa);
        if (etapa.tipo === 'entrada') renderizarEntradaMike(etapa);
        if (etapa.tipo === 'encaminhamento') renderizarEncaminhamentoMike(etapa);
        if (etapa.tipo === 'resolvido') renderizarConclusaoMike();
    }

    window.requestAnimationFrame(() => {
        if (acompanharConversa) {
            mensagensChatMike.scrollTo({
                top: mensagensChatMike.scrollHeight,
                behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
            });
        } else {
            mensagensChatMike.scrollTop = posicaoAnterior;
        }
        if (focarPrimeiroControle) opcoesChatMike.querySelector('button, input')?.focus({ preventScroll: true });
    });
}

function cancelarEsperaDaRespostaMike() {
    if (timerRespostaMike !== null) window.clearTimeout(timerRespostaMike);
    timerRespostaMike = null;
    if (resolverEsperaRespostaMike) resolverEsperaRespostaMike();
    resolverEsperaRespostaMike = null;
}

function aguardarRespostaVisualMike() {
    cancelarEsperaDaRespostaMike();
    const reduzirMovimento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const tempoDeEspera = reduzirMovimento ? 0 : TEMPO_PADRAO_DE_DIGITACAO_MIKE_EM_MS;
    return new Promise((resolver) => {
        resolverEsperaRespostaMike = resolver;
        timerRespostaMike = window.setTimeout(() => {
            timerRespostaMike = null;
            resolverEsperaRespostaMike = null;
            resolver();
        }, tempoDeEspera);
    });
}

function mostrarRespostaDoUsuarioEDigitacaoMike(proximoEstado) {
    definirControlesDoMikeComoDesabilitados(true);
    conversaVisivelDuranteRespostaMike = proximoEstado.conversa.slice(0, -1);
    mikeEstaDigitando = true;
    renderizarChatMike({ forcarRolagem: true });
}

async function iniciarAtendimentoMikeSeNecessario(estadoAnterior, proximoEstado) {
    if (atendimentoMikeAtual || estadoAnterior.problema || !proximoEstado.problema) return;

    atendimentoMikeAtual = await apiFetch('/mike-ia/iniciar', {
        method: 'POST',
        body: {
            descricaoProblema: proximoEstado.problema,
            categoria: proximoEstado.categoria
        }
    });
}

async function atualizarAtendimentoMikeNoBackend(estadoAnterior, proximoEstado) {
    await iniciarAtendimentoMikeSeNecessario(estadoAnterior, proximoEstado);
    const proximaEtapa = FluxoTriagemMike.obterEtapa(proximoEstado);
    if (proximaEtapa.tipo !== 'resolvido') return;

    await apiFetch(`/mike-ia/concluir/${atendimentoMikeAtual.atendimentoId}`, {
        method: 'PATCH',
        body: { resumoAtendimento: FluxoTriagemMike.montarResumo(proximoEstado, true) }
    });
    atendimentoMikeAtual = null;
    limparTriagemMikeArmazenada();
}

async function concluirTransicaoDoChatMike(estadoAnterior, proximoEstado) {
    const versaoDestaInteracao = ++versaoDaInteracaoMike;
    processandoAcaoMike = true;
    mostrarRespostaDoUsuarioEDigitacaoMike(proximoEstado);

    const esperaVisual = aguardarRespostaVisualMike();
    operacaoBackendMike = atualizarAtendimentoMikeNoBackend(estadoAnterior, proximoEstado);

    try {
        await Promise.all([operacaoBackendMike, esperaVisual]);
        if (versaoDestaInteracao !== versaoDaInteracaoMike) return;

        historicoDeEstadosMike.push(estadoAnterior);
        estadoTriagemMike = proximoEstado;
        if (FluxoTriagemMike.obterEtapa(proximoEstado).tipo !== 'resolvido') salvarTriagemMike();
        mostrarMensagemChatMike();
    } catch (erro) {
        if (versaoDestaInteracao === versaoDaInteracaoMike) {
            mostrarMensagemChatMike('Não foi possível continuar agora. Tente novamente.', true);
        }
    } finally {
        if (versaoDestaInteracao === versaoDaInteracaoMike) {
            cancelarEsperaDaRespostaMike();
            conversaVisivelDuranteRespostaMike = null;
            mikeEstaDigitando = false;
            processandoAcaoMike = false;
            operacaoBackendMike = null;
            renderizarChatMike({ forcarRolagem: true, focarPrimeiroControle: true });
        }
    }
}

async function processarOpcaoMike(opcaoId) {
    if (processandoAcaoMike || !estadoTriagemMike) return;

    mostrarMensagemChatMike();
    const estadoAnterior = copiarEstadoMike(estadoTriagemMike);
    let proximoEstado;
    try {
        proximoEstado = FluxoTriagemMike.avancarComOpcao(estadoAnterior, opcaoId);
    } catch (erro) {
        mostrarMensagemChatMike(erro.message, true);
        return;
    }

    await concluirTransicaoDoChatMike(estadoAnterior, proximoEstado);
}

async function processarDadosMike(event) {
    event.preventDefault();
    if (processandoAcaoMike || !estadoTriagemMike) return;

    if (!event.currentTarget.checkValidity()) {
        event.currentTarget.reportValidity();
        return;
    }

    const estadoAnterior = copiarEstadoMike(estadoTriagemMike);
    let proximoEstado;
    try {
        proximoEstado = FluxoTriagemMike.avancarComDados(
            estadoAnterior,
            Object.fromEntries(new FormData(event.currentTarget).entries())
        );
    } catch (erro) {
        mostrarMensagemChatMike(erro.message, true);
        return;
    }

    await concluirTransicaoDoChatMike(estadoAnterior, proximoEstado);
}

async function voltarTriagemMike() {
    if (processandoAcaoMike || historicoDeEstadosMike.length === 0) return;

    const estadoAnterior = historicoDeEstadosMike[historicoDeEstadosMike.length - 1];
    processandoAcaoMike = true;
    renderizarChatMike();
    try {
        if (atendimentoMikeAtual && !estadoAnterior.problema) {
            await apiFetch(`/mike-ia/abandonar/${atendimentoMikeAtual.atendimentoId}`, { method: 'PATCH' });
            atendimentoMikeAtual = null;
        }
        historicoDeEstadosMike.pop();
        estadoTriagemMike = estadoAnterior;
        salvarTriagemMike();
        mostrarMensagemChatMike();
    } catch (erro) {
        mostrarMensagemChatMike(erro.message, true);
    } finally {
        processandoAcaoMike = false;
        renderizarChatMike();
    }
}

async function cancelarTriagemMike() {
    if (cancelandoTriagemMike) return;

    cancelandoTriagemMike = true;
    versaoDaInteracaoMike += 1;
    cancelarEsperaDaRespostaMike();
    conversaVisivelDuranteRespostaMike = null;
    mikeEstaDigitando = false;
    processandoAcaoMike = true;
    document.querySelector('#mike-chat-cancelar').disabled = true;
    try {
        if (operacaoBackendMike) {
            try {
                await operacaoBackendMike;
            } catch (erro) {
                // O cancelamento continua mesmo se a operação anterior tiver falhado.
            }
        }
        if (atendimentoMikeAtual) {
            await apiFetch(`/mike-ia/abandonar/${atendimentoMikeAtual.atendimentoId}`, { method: 'PATCH' });
        }
        iniciarNovaConversaMike();
    } catch (erro) {
        mostrarMensagemChatMike('Não foi possível cancelar agora. Tente novamente.', true);
    } finally {
        operacaoBackendMike = null;
        processandoAcaoMike = false;
        cancelandoTriagemMike = false;
        document.querySelector('#mike-chat-cancelar').disabled = false;
        renderizarChatMike();
    }
}

anexoAberturaUsuario?.addEventListener('change', () => {
    const imagemSelecionada = anexoAberturaUsuario.files[0];
    statusAnexoAberturaUsuario.textContent = imagemSelecionada
        ? `Selecionada: ${imagemSelecionada.name}. Envio ao chamado ainda não disponível.`
        : 'O envio será conectado ao backend posteriormente.';
});

async function carregarAberturaDeChamado() {
    if (!sessaoAtual || sessaoAtual.tecnico) return;

    chatMike.hidden = false;
    formularioEncaminhamentoMike.hidden = true;
    mostrarMensagemChatMike();

    try {
        const diagnosticoEmAndamento = await apiFetch('/mike-ia/em-diagnostico');
        const dadosArmazenados = carregarTriagemMikeArmazenada();
        if (diagnosticoEmAndamento) {
            atendimentoMikeAtual = diagnosticoEmAndamento;
            if (dadosArmazenados?.atendimentoId === diagnosticoEmAndamento.atendimentoId) {
                estadoTriagemMike = dadosArmazenados.estado;
                historicoDeEstadosMike = dadosArmazenados.historico || [];
            } else {
                estadoTriagemMike = FluxoTriagemMike.restaurarPeloAtendimento(diagnosticoEmAndamento);
                historicoDeEstadosMike = [];
            }
        } else if (dadosArmazenados && !dadosArmazenados.atendimentoId && !dadosArmazenados.estado.problema) {
            atendimentoMikeAtual = null;
            estadoTriagemMike = dadosArmazenados.estado;
            historicoDeEstadosMike = dadosArmazenados.historico || [];
        } else {
            iniciarNovaConversaMike();
        }
        salvarTriagemMike();
        renderizarChatMike();
    } catch (erro) {
        if (!estadoTriagemMike) estadoTriagemMike = FluxoTriagemMike.criarEstadoInicial();
        renderizarChatMike();
        mostrarMensagemChatMike('Não foi possível recuperar uma conversa anterior. Você pode iniciar novamente.', true);
    }
}

document.querySelector('#mike-chat-cancelar')?.addEventListener('click', cancelarTriagemMike);

document.querySelector('#mike-voltar-ao-diagnostico')?.addEventListener('click', () => {
    if (processandoAcaoMike) return;
    formularioEncaminhamentoMike.hidden = true;
    chatMike.hidden = false;
    renderizarChatMike();
});

formularioEncaminhamentoMike?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const mensagem = document.querySelector('#mensagem-encaminhamento-mike');
    if (processandoAcaoMike) return;

    if (!formularioEncaminhamentoMike.checkValidity()) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Preencha categoria, prioridade e local para encaminhar o atendimento.';
        formularioEncaminhamentoMike.reportValidity();
        return;
    }

    const corpo = {
        categoria: document.querySelector('#encaminhamento-categoria').value,
        prioridade: document.querySelector('#encaminhamento-prioridade').value,
        localAtendimento: document.querySelector('#encaminhamento-local').value.trim(),
        resumoAtendimento: FluxoTriagemMike.montarResumo(estadoTriagemMike, false),
        cpf: estadoTriagemMike.dados?.cpf || ''
    };

    processandoAcaoMike = true;
    try {
        await executarComEstadoDeEnvio(formularioEncaminhamentoMike.querySelector('button[type="submit"]'),
            'Encaminhando...', async () => {
            try {
                await apiFetch(`/mike-ia/encaminhar/${atendimentoMikeAtual.atendimentoId}`, {
                    method: 'PATCH', body: corpo
                });
                formularioEncaminhamentoMike.reset();
                atendimentoMikeAtual = null;
                estadoTriagemMike = null;
                historicoDeEstadosMike = [];
                limparTriagemMikeArmazenada();
                const botaoAcompanhar = document.querySelector('#botao-sucesso-acompanhar');
                document.querySelector('#titulo-chamado-sucesso').textContent = 'Chamado aberto com sucesso!';
                document.querySelector('#mensagem-chamado-sucesso').textContent = 'A equipe técnica recebeu as informações da triagem.';
                botaoAcompanhar.textContent = 'Ver meus chamados';
                botaoAcompanhar.dataset.route = 'meus-chamados';
                await showRoute('chamado-sucesso');
            } catch (erro) {
                mensagem.classList.add('is-error');
                mensagem.textContent = 'Não foi possível abrir o chamado agora. Tente novamente.';
            }
            });
    } finally {
        processandoAcaoMike = false;
    }
});

// ============================================================
// Abrir chamado
// ============================================================
ticketForm?.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!ticketForm.checkValidity()) {
        ticketMessage.classList.add('is-error');
        ticketMessage.textContent = 'Preencha os campos obrigatórios para continuar.';
        ticketForm.reportValidity();
        return;
    }

    const assunto = document.querySelector('#assunto').value.trim();
    const descricaoDetalhada = document.querySelector('#descricao').value.trim();
    const reSolicitante = document.querySelector('#re-solicitante')?.value.trim();
    const corpo = {
        re: (sessaoAtual.tecnico && reSolicitante) ? reSolicitante : sessaoAtual.re,
        descricao: `${assunto}\n\n${descricaoDetalhada}`,
        categoria: document.querySelector('#categoria').value,
        localAtendimento: document.querySelector('#local').value.trim(),
        prioridade: document.querySelector('#prioridade').value
    };

    await executarComEstadoDeEnvio(ticketForm.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            await apiFetch('/chamados/cadastrar', { method: 'POST', body: corpo });
            ticketMessage.classList.remove('is-error');
            ticketMessage.textContent = '';
            ticketForm.reset();
            document.querySelector('#prioridade').value = 'MEDIA';

            const botaoAcompanhar = document.querySelector('#botao-sucesso-acompanhar');
            document.querySelector('#titulo-chamado-sucesso').textContent = 'Chamado registrado com sucesso!';
            document.querySelector('#mensagem-chamado-sucesso').textContent = 'A equipe técnica foi notificada. Você pode acompanhar o andamento a qualquer momento.';
            if (sessaoAtual.tecnico) {
                botaoAcompanhar.textContent = 'Ver central técnica';
                botaoAcompanhar.dataset.route = 'central-tecnica';
            } else {
                botaoAcompanhar.textContent = 'Ver meus chamados';
                botaoAcompanhar.dataset.route = 'meus-chamados';
            }
            await showRoute('chamado-sucesso');
        } catch (erro) {
            ticketMessage.classList.add('is-error');
            ticketMessage.textContent = erro.message;
        }
    });
});

// ============================================================
// Central técnica — disponibilidade do técnico logado
// ============================================================
function atualizarToggleDisponibilidade(botao, disponivel) {
    if (!botao) return;
    botao.setAttribute('aria-pressed', String(disponivel));
    botao.classList.toggle('is-available', disponivel);
    botao.querySelector('strong').textContent = disponivel ? 'Disponível para atendimento' : 'Indisponível para atendimento';
}

async function carregarCentralTecnica() {
    const botao = document.querySelector('#alternar-disponibilidade');
    if (botao && sessaoAtual) {
        try {
            const tecnico = await apiFetch(`/tecnicos/buscar/${sessaoAtual.re}`);
            atualizarToggleDisponibilidade(botao, tecnico.disponivel);
        } catch (erro) {
            // Sem dado real disponível agora; mantém o último estado visual conhecido.
        }
    }

    try {
        const resumo = await apiFetch('/chamados/resumo');
        document.querySelector('#resumo-chamados-hoje').textContent = resumo.chamadosHoje;
        document.querySelector('#resumo-chamados-hoje-legenda').textContent = 'Registrados desde o início do dia';
        document.querySelector('#resumo-chamados-semana').textContent = resumo.chamadosSemana;
        document.querySelector('#resumo-chamados-semana-legenda').textContent = 'Chamados registrados nos últimos dias';
        document.querySelector('#resumo-chamados-mes').textContent = resumo.chamadosMes;
        document.querySelector('#resumo-chamados-mes-legenda').textContent = 'Total de chamados registrados no mês';
    } catch (erro) {
        ['hoje', 'semana', 'mes'].forEach((periodo) => {
            document.querySelector(`#resumo-chamados-${periodo}`).textContent = '—';
            document.querySelector(`#resumo-chamados-${periodo}-legenda`).textContent = 'Não foi possível carregar.';
        });
    }

    try {
        const metricas = await apiFetch('/mike-ia/metricas');
        document.querySelector('#mike-metrica-iniciados').textContent = metricas.totalAtendimentosIniciados;
        document.querySelector('#mike-metrica-resolvidos').textContent = metricas.totalResolvidosPeloMike;
        document.querySelector('#mike-metrica-encaminhados').textContent = metricas.totalEncaminhadosParaTecnico;
        document.querySelector('#mike-metrica-taxa').textContent = `${Math.round(metricas.taxaResolucaoAutomatica)}%`;

        const mensagem = document.querySelector('#mensagem-metricas-mike');
        mensagem.classList.remove('is-error');
        // Com zero atendimentos, "0%" pareceria que o Mike não está funcionando, quando
        // na verdade ninguém usou ainda — mensagem evita essa leitura errada.
        mensagem.textContent = metricas.totalAtendimentosIniciados === 0
            ? 'Ainda não há atendimentos registrados pelo Mike IA.'
            : '';

        // Abandonado fica à parte dos indicadores principais porque não representa
        // resolução nem encaminhamento e só precisa aparecer quando realmente existir.
        const notaAbandonados = document.querySelector('#mike-metrica-abandonados');
        if (metricas.totalAbandonados > 0) {
            notaAbandonados.hidden = false;
            notaAbandonados.textContent = metricas.totalAbandonados === 1
                ? '1 atendimento foi abandonado pelo usuário antes de uma resposta.'
                : `${metricas.totalAbandonados} atendimentos foram abandonados pelos usuários antes de uma resposta.`;
        } else {
            notaAbandonados.hidden = true;
        }
    } catch (erro) {
        ['iniciados', 'resolvidos', 'encaminhados', 'taxa'].forEach((campo) => {
            document.querySelector(`#mike-metrica-${campo}`).textContent = '—';
        });
        document.querySelector('#mike-metrica-abandonados').hidden = true;
        const mensagemDeErro = document.querySelector('#mensagem-metricas-mike');
        mensagemDeErro.classList.add('is-error');
        mensagemDeErro.textContent = 'Não foi possível carregar as métricas do Mike IA agora.';
    }
}

document.querySelector('#alternar-disponibilidade')?.addEventListener('click', async (event) => {
    const botao = event.currentTarget;
    const mensagem = document.querySelector('#mensagem-disponibilidade');
    const estaDisponivel = botao.getAttribute('aria-pressed') === 'true';
    const acao = estaDisponivel ? 'ficar-indisponivel' : 'ficar-disponivel';

    try {
        const tecnico = await apiFetch(`/tecnicos/${acao}/${sessaoAtual.re}`, { method: 'PATCH' });
        atualizarToggleDisponibilidade(botao, tecnico.disponivel);
        mensagem.classList.remove('is-error');
        mensagem.textContent = tecnico.disponivel
            ? 'Você está disponível para novos atendimentos.'
            : 'Você ficou indisponível para novos atendimentos.';
    } catch (erro) {
        mensagem.classList.add('is-error');
        mensagem.textContent = erro.message;
    }
});

// ============================================================
// Fila de atendimento (técnico)
// ============================================================
let filtroFilaAtual = 'ABERTO';
let termoBuscaFila = '';
let chamadoSelecionadoId = null;
let filaAbertos = [];
let filaEmAtendimento = [];
const historicosMikePorChamadoId = new Map();
// Distingue "a fila está vazia mesmo" de "não conseguimos carregar a fila" — sem isso,
// as duas situações mostravam o mesmo texto de lista vazia, escondendo o erro real.
let houveErroAoCarregarFila = false;

async function carregarFilaAtendimento() {
    try {
        [filaAbertos, filaEmAtendimento] = await Promise.all([
            apiFetch('/chamados/fila'),
            apiFetch('/chamados/em-atendimento')
        ]);
        houveErroAoCarregarFila = false;
        queueMessage.classList.remove('is-error');
        queueMessage.textContent = '';
    } catch (erro) {
        filaAbertos = [];
        filaEmAtendimento = [];
        houveErroAoCarregarFila = true;
        mostrarMensagemDaFila(erro.message, true);
    }
    renderizarFilaAtendimento();
}

function obterChamadoSelecionado() {
    return filaAbertos.find((chamado) => chamado.id === chamadoSelecionadoId)
        || filaEmAtendimento.find((chamado) => chamado.id === chamadoSelecionadoId);
}

function mostrarMensagemDaFila(mensagem, isError = false) {
    if (!queueMessage) return;
    queueMessage.classList.toggle('is-error', isError);
    queueMessage.textContent = mensagem;
}

function chamadosDaFilaAtual() {
    const base = filtroFilaAtual === 'ABERTO' ? filaAbertos : filaEmAtendimento;
    const termoNormalizado = termoBuscaFila.trim().toLocaleLowerCase('pt-BR');

    return base.filter((chamado) => !termoNormalizado
        || String(chamado.id).includes(termoNormalizado)
        || chamado.descricao.toLocaleLowerCase('pt-BR').includes(termoNormalizado));
}

// Preenche o seletor de transferência com os técnicos realmente disponíveis agora.
async function popularSelectDeTecnicos() {
    const select = document.querySelector('#transferir-responsavel');
    if (!select) return;
    try {
        const tecnicos = await apiFetch('/tecnicos/disponiveis');
        select.innerHTML = '';
        tecnicos.forEach((tecnico) => {
            const opcao = document.createElement('option');
            opcao.value = tecnico.re;
            opcao.textContent = tecnico.identificacao;
            select.appendChild(opcao);
        });
    } catch (erro) {
        select.innerHTML = '';
    }
}

function renderizarDetalheDoChamado() {
    const chamado = obterChamadoSelecionado();
    const podeSerConduzido = chamado && ['ABERTO', 'EM_ATENDIMENTO'].includes(chamado.status);

    if (!chamado || !podeSerConduzido) {
        queueDetailContent.hidden = true;
        queueDetailEmpty.hidden = false;
        const exibindoPendentes = filtroFilaAtual === 'ABERTO';
        const quantidade = exibindoPendentes ? filaAbertos.length : filaEmAtendimento.length;

        resumoFilaQuantidade.textContent = houveErroAoCarregarFila ? '-' : quantidade;
        tituloDetalheChamado.textContent = exibindoPendentes ? 'Chamados pendentes' : 'Chamados em atendimento';
        if (houveErroAoCarregarFila) {
            resumoFilaDescricao.textContent = 'Não foi possível carregar a fila. ' + queueMessage.textContent;
        } else {
            resumoFilaDescricao.textContent = exibindoPendentes
                ? quantidade === 0
                    ? 'Não há chamados pendentes no momento.'
                    : `${quantidade} ${quantidade === 1 ? 'chamado aguarda' : 'chamados aguardam'} atendimento. Selecione um item na lista para continuar.`
                : quantidade === 0
                    ? 'Não há chamados em atendimento no momento.'
                    : `${quantidade} ${quantidade === 1 ? 'atendimento está em andamento' : 'atendimentos estão em andamento'}. Selecione um item para acompanhar.`;
        }
        return;
    }

    queueDetailContent.hidden = false;
    queueDetailEmpty.hidden = true;
    detalheCodigo.textContent = `Chamado #${chamado.id}`;
    detalheAssunto.textContent = tituloDoChamado(chamado);
    detalheAbertura.textContent = `Aberto em ${chamado.dataAbertura}`;
    detalheStatus.textContent = nomeDoStatus[chamado.status];
    detalheSolicitante.textContent = chamado.solicitante;
    detalheLocal.textContent = chamado.localAtendimento;
    detalheCategoria.textContent = nomeDaCategoria[chamado.categoria];
    detalheResponsavel.textContent = chamado.tecnicoResponsavel || 'Aguardando assunção';
    detalheDescricao.textContent = chamado.descricao;
    detalhePrioridadeElemento.textContent = chamado.prioridade ? nomeDaPrioridade[chamado.prioridade] : 'Sem prioridade';
    detalhePrioridadeElemento.className = chamado.prioridade
        ? `priority-label priority-${chamado.prioridade.toLowerCase()}`
        : 'priority-label priority-sem-prioridade';
    acoesChamadoAberto.hidden = chamado.status !== 'ABERTO';
    acoesTransferencia.hidden = !podeSerConduzido;
    acoesChamadoAtendimento.hidden = chamado.status !== 'EM_ATENDIMENTO';

    popularSelectDeTecnicos();
    renderizarHistoricoMikeNoDetalhe(chamado.id);
}

function renderizarHistoricoMikeNoDetalhe(chamadoId) {
    const secao = document.querySelector('#historico-mike-tecnico');
    const historico = historicosMikePorChamadoId.get(chamadoId);

    if (historico === undefined) {
        void carregarHistoricoMikeDoChamado(chamadoId);
        secao.hidden = true;
        return;
    }

    if (!historico) {
        secao.hidden = true;
        return;
    }

    document.querySelector('#detalhe-mike-status').textContent = nomeDoResultadoMike(historico.resultado);
    document.querySelector('#detalhe-mike-relato').textContent = historico.descricaoProblema;
    document.querySelector('#detalhe-mike-sugestoes').textContent = historico.sugestoes;
    secao.hidden = false;
}

async function carregarHistoricoMikeDoChamado(chamadoId) {
    try {
        const historico = await apiFetch(`/mike-ia/chamado/${chamadoId}`);
        historicosMikePorChamadoId.set(chamadoId, historico);
    } catch (erro) {
        // A ausência de histórico do Mike não impede o técnico de atender o chamado.
        historicosMikePorChamadoId.set(chamadoId, null);
    }

    if (chamadoSelecionadoId === chamadoId) {
        renderizarHistoricoMikeNoDetalhe(chamadoId);
    }
}

function nomeDoResultadoMike(resultado) {
    const nomePorResultado = {
        RESOLVIDO: 'Resolvido com orientações do Mike IA.',
        ENCAMINHADO_PARA_CHAMADO: 'Encaminhado à equipe técnica após as orientações.',
        ABANDONADO: 'Diagnóstico abandonado pelo usuário.'
    };
    return nomePorResultado[resultado] || 'Diagnóstico em andamento.';
}

// Monta o card de um chamado da fila usando textContent (nunca innerHTML) para que
// campos digitados por qualquer usuário (descrição, local) nunca sejam interpretados
// como HTML.
function criarElementoDoChamado(chamado) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = `queue-ticket${chamado.id === chamadoSelecionadoId ? ' is-selected' : ''}`;
    botao.setAttribute('role', 'listitem');
    botao.dataset.selectTicket = chamado.id;
    botao.setAttribute('aria-pressed', String(chamado.id === chamadoSelecionadoId));

    const topo = document.createElement('span');
    topo.className = 'queue-ticket-top';
    const codigo = document.createElement('span');
    codigo.className = 'queue-ticket-code';
    codigo.textContent = `#${chamado.id}`;
    const prioridade = document.createElement('span');
    prioridade.className = chamado.prioridade
        ? `priority-label priority-${chamado.prioridade.toLowerCase()}`
        : 'priority-label priority-sem-prioridade';
    prioridade.textContent = chamado.prioridade ? nomeDaPrioridade[chamado.prioridade] : 'Sem prioridade';
    topo.append(codigo, prioridade);

    const titulo = document.createElement('strong');
    titulo.className = 'queue-ticket-title';
    titulo.textContent = tituloDoChamado(chamado);

    const meta = document.createElement('span');
    meta.className = 'queue-ticket-meta';
    meta.textContent = `${nomeDaCategoria[chamado.categoria]} · ${chamado.localAtendimento} · ${chamado.dataAbertura}`;

    const rodape = document.createElement('span');
    rodape.className = 'queue-ticket-footer';
    const status = document.createElement('span');
    status.className = 'queue-ticket-status';
    status.textContent = nomeDoStatus[chamado.status];
    const seta = document.createElement('span');
    seta.setAttribute('aria-hidden', 'true');
    seta.textContent = '›';
    rodape.append(status, seta);

    botao.append(topo, titulo, meta, rodape);
    return botao;
}

function renderizarFilaAtendimento() {
    if (!queueList) return;

    const chamadosVisiveis = chamadosDaFilaAtual();

    contadorFilaAberta.textContent = filaAbertos.length;
    contadorEmAtendimento.textContent = filaEmAtendimento.length;
    contadorChamadosAtivos.textContent = `${filaAbertos.length + filaEmAtendimento.length} ativos`;

    filtrosDaFila.forEach((button) => {
        const isActive = button.dataset.queueFilter === filtroFilaAtual;
        button.classList.toggle('is-active', isActive);
        button.setAttribute('aria-selected', String(isActive));
    });

    if (chamadoSelecionadoId && !chamadosVisiveis.some((chamado) => chamado.id === chamadoSelecionadoId)) {
        chamadoSelecionadoId = null;
    }

    queueList.innerHTML = '';
    if (chamadosVisiveis.length) {
        chamadosVisiveis.forEach((chamado) => queueList.appendChild(criarElementoDoChamado(chamado)));
    } else {
        const listaVazia = document.createElement('p');
        listaVazia.className = 'queue-empty-list';
        if (houveErroAoCarregarFila) {
            listaVazia.classList.add('is-error');
            listaVazia.textContent = 'Não foi possível carregar a fila. ' + queueMessage.textContent;
        } else {
            listaVazia.textContent = filtroFilaAtual === 'ABERTO'
                ? 'Não há chamados pendentes no momento.'
                : 'Não há chamados em atendimento no momento.';
        }
        queueList.appendChild(listaVazia);
    }

    renderizarDetalheDoChamado();
}

async function executarAcaoDaFila(acao) {
    const chamado = obterChamadoSelecionado();

    if (!chamado) {
        mostrarMensagemDaFila('Selecione um chamado antes de escolher uma ação.', true);
        return;
    }

    try {
        if (acao === 'iniciar') {
            await apiFetch(`/chamados/iniciar-atendimento/${chamado.id}?reTecnico=${sessaoAtual.re}`, { method: 'PATCH' });
            mostrarMensagemDaFila(`Você assumiu o chamado #${chamado.id}. O atendimento foi iniciado.`);
            filtroFilaAtual = 'EM_ATENDIMENTO';
        }

        if (acao === 'transferir') {
            const reTecnico = document.querySelector('#transferir-responsavel').value;
            if (!reTecnico) {
                mostrarMensagemDaFila('Selecione um técnico para transferir.', true);
                return;
            }
            await apiFetch(`/chamados/transferir-responsavel/${chamado.id}?reTecnico=${reTecnico}`, { method: 'PATCH' });
            mostrarMensagemDaFila(`Responsável do chamado #${chamado.id} atualizado com sucesso.`);
        }

        if (acao === 'finalizar') {
            const solucao = document.querySelector('#solucao-atendimento').value.trim();
            const caminho = `/chamados/finalizar/${chamado.id}` + (solucao ? `?solucao=${encodeURIComponent(solucao)}` : '');
            await apiFetch(caminho, { method: 'PATCH' });
            mostrarMensagemDaFila(`Atendimento do chamado #${chamado.id} finalizado. O registro permanece no histórico.`);
            chamadoSelecionadoId = null;
        }

        await carregarFilaAtendimento();
    } catch (erro) {
        mostrarMensagemDaFila(erro.message, true);
    }
}

filtrosDaFila.forEach((button) => {
    button.addEventListener('click', () => {
        filtroFilaAtual = button.dataset.queueFilter;
        termoBuscaFila = '';
        if (queueSearch) queueSearch.value = '';
        renderizarFilaAtendimento();
    });
});

queueList?.addEventListener('click', (event) => {
    const ticket = event.target.closest('[data-select-ticket]');
    if (!ticket) return;

    chamadoSelecionadoId = Number(ticket.dataset.selectTicket);
    renderizarFilaAtendimento();
});

function aplicarBuscaNaFila() {
    termoBuscaFila = queueSearch?.value || '';
    renderizarFilaAtendimento();
}

document.querySelector('#buscar-chamado')?.addEventListener('click', aplicarBuscaNaFila);
queueSearch?.addEventListener('input', aplicarBuscaNaFila);

document.querySelectorAll('[data-queue-action]').forEach((button) => {
    button.addEventListener('click', () => executarComEstadoDeEnvio(button, 'Aguarde...', () => executarAcaoDaFila(button.dataset.queueAction)));
});

// ============================================================
// Gestão de usuários — cadastro, consulta, atualização, técnico
// ============================================================
document.querySelector('#formulario-cadastro-usuario')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const mensagem = document.querySelector('#mensagem-cadastro-usuario');

    if (!form.checkValidity()) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Preencha RE, posto/graduação e nome.';
        form.reportValidity();
        return;
    }

    const corpo = {
        re: document.querySelector('#cadastro-re').value.trim(),
        postoGraduacao: document.querySelector('#cadastro-posto').value,
        nome: document.querySelector('#cadastro-nome').value.trim(),
        email: document.querySelector('#cadastro-email').value.trim() || null
    };

    await executarComEstadoDeEnvio(form.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            await apiFetch('/usuarios/cadastrar', { method: 'POST', body: corpo });
            mensagem.classList.remove('is-error');
            mensagem.textContent = 'Usuário cadastrado com sucesso.';
            form.reset();
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

let usuarioConsultadoRe = null;
let usuarioConsultado = null;

function exibirUsuarioConsultado(usuario) {
    usuarioConsultado = usuario;
    usuarioConsultadoRe = usuario.re;
    document.querySelector('#consulta-usuario-nome').textContent = `${usuario.postoGraduacao} ${usuario.nome}`;
    const email = usuario.email || 'E-mail não informado';
    const situacaoSenha = usuario.trocaSenhaObrigatoria ? ' · Troca de senha pendente' : '';
    document.querySelector('#consulta-usuario-info').textContent = `RE ${usuario.re} · ${email}${situacaoSenha}`;

    const status = document.querySelector('#consulta-usuario-status');
    status.textContent = usuario.ativo ? 'Ativo' : 'Inativo';
    status.classList.toggle('status-active', usuario.ativo);

    document.querySelector('#registro-usuario-consultado').hidden = false;
    document.querySelector('#acoes-usuario-consultado').hidden = false;
    document.querySelector('#formulario-atualizar-usuario').hidden = true;
}

document.querySelector('#consultar-usuario')?.addEventListener('click', async () => {
    const re = document.querySelector('#consulta-re').value.trim();
    const mensagem = document.querySelector('#mensagem-controle-acesso');

    if (!re) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Informe o RE para consultar.';
        return;
    }

    try {
        const usuario = await apiFetch(`/usuarios/buscar/${re}`);
        exibirUsuarioConsultado(usuario);
        mensagem.classList.remove('is-error');
        mensagem.textContent = '';
    } catch (erro) {
        usuarioConsultado = null;
        usuarioConsultadoRe = null;
        document.querySelector('#registro-usuario-consultado').hidden = true;
        document.querySelector('#acoes-usuario-consultado').hidden = true;
        mensagem.classList.add('is-error');
        mensagem.textContent = erro.message;
    }
});

document.querySelector('#alternar-edicao-usuario')?.addEventListener('click', async () => {
    if (!usuarioConsultadoRe) return;
    const form = document.querySelector('#formulario-atualizar-usuario');

    if (!form.hidden) {
        form.hidden = true;
        return;
    }

    try {
        const usuario = await apiFetch(`/usuarios/buscar/${usuarioConsultadoRe}`);
        document.querySelector('#atualizar-nome').value = usuario.nome;
        document.querySelector('#atualizar-posto').value = chaveDoPostoPorDescricao[usuario.postoGraduacao] || '';
        form.hidden = false;
    } catch (erro) {
        const mensagem = document.querySelector('#mensagem-controle-acesso');
        mensagem.classList.add('is-error');
        mensagem.textContent = erro.message;
    }
});

document.querySelector('#formulario-atualizar-usuario')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const mensagem = document.querySelector('#mensagem-controle-acesso');
    const corpo = {
        nome: document.querySelector('#atualizar-nome').value.trim(),
        postoGraduacao: document.querySelector('#atualizar-posto').value
    };

    await executarComEstadoDeEnvio(form.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            const usuario = await apiFetch(`/usuarios/atualizar-dados/${usuarioConsultadoRe}`, { method: 'PUT', body: corpo });
            exibirUsuarioConsultado(usuario);
            form.hidden = true;
            mensagem.classList.remove('is-error');
            mensagem.textContent = 'Dados atualizados com sucesso.';
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

document.querySelector('#conceder-acesso-tecnico')?.addEventListener('click', async () => {
    if (!usuarioConsultadoRe) return;
    const mensagem = document.querySelector('#mensagem-controle-acesso');

    try {
        await apiFetch('/tecnicos/cadastrar', { method: 'POST', body: { re: usuarioConsultadoRe } });
        mensagem.classList.remove('is-error');
        mensagem.textContent = 'Usuário agora é técnico.';
        await carregarListaTecnicos();
    } catch (erro) {
        mensagem.classList.add('is-error');
        mensagem.textContent = erro.message;
    }
});

function fecharModalDeResetDeSenha() {
    if (resetPasswordModal?.open) resetPasswordModal.close();
}

document.querySelector('#abrir-reset-senha')?.addEventListener('click', (event) => {
    if (!usuarioConsultado) return;
    document.querySelector('#reset-senha-identificacao').textContent =
        `Deseja realmente resetar a senha de ${usuarioConsultado.nome} — RE ${usuarioConsultado.re}?`;
    document.querySelector('#mensagem-reset-senha').textContent = '';
    resetPasswordModal.showModal();
    document.querySelector('#confirmar-reset-senha').focus({ preventScroll: true });
});

document.querySelectorAll('[data-close-reset]').forEach((botao) => {
    botao.addEventListener('click', fecharModalDeResetDeSenha);
});

document.querySelector('#confirmar-reset-senha')?.addEventListener('click', async (event) => {
    if (!usuarioConsultadoRe) return;
    const botao = event.currentTarget;
    const mensagemModal = document.querySelector('#mensagem-reset-senha');
    const mensagemControle = document.querySelector('#mensagem-controle-acesso');

    await executarComEstadoDeEnvio(botao, 'Resetando...', async () => {
        try {
            const usuario = await apiFetch(`/usuarios/resetar-senha/${usuarioConsultadoRe}`, { method: 'PATCH' });
            exibirUsuarioConsultado(usuario);
            fecharModalDeResetDeSenha();
            mensagemControle.classList.remove('is-error');
            mensagemControle.textContent = 'Senha resetada. O usuário deverá entrar com RE/RE e criar uma nova senha.';
        } catch (erro) {
            mensagemModal.classList.add('is-error');
            mensagemModal.textContent = erro.message;
        }
    });
});

resetPasswordModal?.addEventListener('close', () => {
    const mensagem = document.querySelector('#mensagem-reset-senha');
    mensagem.textContent = '';
    mensagem.classList.remove('is-error');
    document.querySelector('#abrir-reset-senha')?.focus({ preventScroll: true });
});

document.querySelector('#inativar-usuario')?.addEventListener('click', async () => {
    if (!usuarioConsultadoRe) return;
    const mensagem = document.querySelector('#mensagem-controle-acesso');

    try {
        await apiFetch(`/usuarios/inativar/${usuarioConsultadoRe}`, { method: 'PATCH' });
        const usuario = await apiFetch(`/usuarios/buscar/${usuarioConsultadoRe}`);
        exibirUsuarioConsultado(usuario);
        mensagem.classList.remove('is-error');
        mensagem.textContent = 'Usuário inativado com sucesso.';
    } catch (erro) {
        mensagem.classList.add('is-error');
        mensagem.textContent = erro.message;
    }
});

function criarLinhaDeTecnico(tecnico) {
    const linha = document.createElement('article');
    linha.className = 'technician-row';

    const info = document.createElement('div');
    const nome = document.createElement('strong');
    nome.textContent = tecnico.identificacao;
    const re = document.createElement('span');
    re.textContent = `RE ${tecnico.re} · Técnico`;
    info.append(nome, re);

    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = `availability-toggle${tecnico.disponivel ? ' is-available' : ''}`;
    const bolinha = document.createElement('span');
    bolinha.setAttribute('aria-hidden', 'true');
    botao.append(bolinha, document.createTextNode(tecnico.disponivel ? 'Disponível' : 'Indisponível'));
    botao.addEventListener('click', async () => {
        const acao = tecnico.disponivel ? 'ficar-indisponivel' : 'ficar-disponivel';
        const mensagem = document.querySelector('#mensagem-disponibilidade-tecnica');
        try {
            await apiFetch(`/tecnicos/${acao}/${tecnico.re}`, { method: 'PATCH' });
            mensagem.classList.remove('is-error');
            mensagem.textContent = `Disponibilidade de ${tecnico.identificacao} atualizada.`;
            await carregarListaTecnicos();
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });

    linha.append(info, botao);
    return linha;
}

async function carregarListaTecnicos() {
    const lista = document.querySelector('#lista-tecnicos');
    if (!lista) return;
    lista.innerHTML = '';

    try {
        const tecnicos = await apiFetch('/tecnicos');
        if (!tecnicos.length) {
            const vazio = document.createElement('p');
            vazio.className = 'lista-tecnicos-vazia';
            vazio.textContent = 'Nenhum técnico cadastrado ainda.';
            lista.appendChild(vazio);
            return;
        }
        tecnicos.forEach((tecnico) => lista.appendChild(criarLinhaDeTecnico(tecnico)));
    } catch (erro) {
        const mensagem = document.createElement('p');
        mensagem.className = 'lista-tecnicos-vazia';
        mensagem.textContent = erro.message;
        lista.appendChild(mensagem);
    }
}

document.querySelector('#atualizar-lista-tecnicos')?.addEventListener('click', carregarListaTecnicos);

restaurarSessaoAoCarregarPagina();
