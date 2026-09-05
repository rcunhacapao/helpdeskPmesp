const loginForm = document.querySelector('#formulario-login');
const loginMessage = document.querySelector('#mensagem-login');
const loginPage = document.querySelector('#pagina-login');
const appPage = document.querySelector('#pagina-app');
const routes = document.querySelectorAll('[data-route]');
const views = document.querySelectorAll('[data-view]');
const navigationLinks = document.querySelectorAll('.nav-link');
const ticketForm = document.querySelector('#formulario-chamado');
const ticketMessage = document.querySelector('#mensagem-chamado');
const formularioAberturaUsuario = document.querySelector('#formulario-abertura-usuario');
const mensagemAberturaUsuario = document.querySelector('#mensagem-abertura-usuario');
const anexoAberturaUsuario = document.querySelector('#abertura-anexo');
const statusAnexoAberturaUsuario = document.querySelector('#abertura-anexo-status');
const diagnosticoMike = document.querySelector('#diagnostico-mike');
const formularioEncaminhamentoMike = document.querySelector('#formulario-encaminhamento-mike');
const cancelTicketForm = document.querySelector('#formulario-cancelamento');
const cancelTicketMessage = document.querySelector('#mensagem-cancelamento');
const queueMessage = document.querySelector('#mensagem-fila');
const queueList = document.querySelector('#lista-fila');
const queueSearch = document.querySelector('#busca-chamado');
const queueDetailContent = document.querySelector('#conteudo-detalhe-chamado');
const queueDetailEmpty = document.querySelector('#detalhe-chamado-vazio');

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
let sessaoAtual = null; // { identificacaoCompleta, re, tecnico } — preenchido após /auth/login
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
            : 'Descreva o problema para iniciarmos seu atendimento.';
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
        loginPage.hidden = true;
        appPage.hidden = false;
        aplicarSessaoNaInterface();
        await showRoute(sessaoAtual.tecnico ? 'central-tecnica' : 'visao-geral');
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
// Login, primeiro acesso e logout
// ============================================================
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
            loginPage.hidden = true;
            appPage.hidden = false;
            aplicarSessaoNaInterface();
            await showRoute(sessaoAtual.tecnico ? 'central-tecnica' : 'visao-geral');
        } catch (erro) {
            loginMessage.classList.add('is-error');
            loginMessage.textContent = 'RE ou senha inválidos.';
        }
    });
});

document.querySelector('#mostrar-primeiro-acesso')?.addEventListener('click', () => {
    loginForm.hidden = true;
    document.querySelector('#formulario-primeiro-acesso').hidden = false;
    document.querySelector('#mostrar-primeiro-acesso').hidden = true;
});

document.querySelector('#ocultar-primeiro-acesso')?.addEventListener('click', () => {
    document.querySelector('#formulario-primeiro-acesso').hidden = true;
    loginForm.hidden = false;
    document.querySelector('#mostrar-primeiro-acesso').hidden = false;
});

document.querySelector('#formulario-primeiro-acesso')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const mensagem = document.querySelector('#mensagem-primeiro-acesso');

    if (!form.checkValidity()) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Preencha RE, e-mail funcional e a nova senha.';
        form.reportValidity();
        return;
    }

    const corpo = {
        re: document.querySelector('#primeiro-acesso-re').value.trim(),
        email: document.querySelector('#primeiro-acesso-email').value.trim(),
        novaSenha: document.querySelector('#primeiro-acesso-senha').value
    };

    await executarComEstadoDeEnvio(form.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            await apiFetch('/auth/primeiro-acesso', { method: 'POST', body: corpo });
            mensagem.classList.remove('is-error');
            mensagem.textContent = 'Senha criada com sucesso. Faça login para continuar.';
            form.reset();
            window.setTimeout(() => document.querySelector('#ocultar-primeiro-acesso').click(), 1500);
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

document.querySelector('#sair')?.addEventListener('click', async () => {
    try {
        await apiFetch('/logout', { method: 'POST' });
    } catch (erro) {
        // Mesmo se a chamada falhar, a sessão local é encerrada abaixo.
    }
    pararAtualizacaoAutomaticaDoUsuario();
    sessaoAtual = null;
    appPage.hidden = true;
    loginPage.hidden = false;
    loginForm.reset();
    loginMessage.textContent = '';
});

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
    document.querySelector('#overview-ticket-info').textContent = `Chamado #${chamado.id} · aberto em ${chamado.dataAbertura}`;
    document.querySelector('#overview-ticket-status').textContent = nomeDoStatus[chamado.status];

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
// Meus chamados — lista completa + cancelamento
// ============================================================
let chamadoParaCancelarId = null;

async function carregarMeusChamados() {
    const lista = document.querySelector('#lista-meus-chamados');
    if (!lista) return;
    lista.innerHTML = '';

    try {
        const chamados = await apiFetch('/chamados/meus');
        if (!chamados.length) {
            const vazio = document.createElement('p');
            vazio.className = 'lista-chamados-vazia';
            vazio.textContent = 'Você ainda não abriu nenhum chamado.';
            lista.appendChild(vazio);
            return;
        }
        chamados.forEach((chamado) => lista.appendChild(criarItemDeMeuChamado(chamado)));
    } catch (erro) {
        const mensagem = document.createElement('p');
        mensagem.className = 'lista-chamados-vazia';
        mensagem.textContent = erro.message;
        lista.appendChild(mensagem);
    }
}

function criarItemDeMeuChamado(chamado) {
    const item = document.createElement('article');
    item.className = 'meu-chamado-item';

    const cabecalho = document.createElement('div');
    cabecalho.className = 'card-heading';

    const titulo = document.createElement('div');
    const h3 = document.createElement('h3');
    h3.textContent = `#${chamado.id} · ${tituloDoChamado(chamado)}`;
    const dataAbertura = document.createElement('p');
    dataAbertura.textContent = `Aberto em ${chamado.dataAbertura}`;
    titulo.append(h3, dataAbertura);

    const status = document.createElement('span');
    status.className = `status-label status-${chamado.status.toLowerCase()}`;
    status.textContent = nomeDoStatus[chamado.status];

    cabecalho.append(titulo, status);
    item.appendChild(cabecalho);

    if (chamado.status === 'FECHADO' && chamado.solucao) {
        const solucao = document.createElement('p');
        solucao.className = 'meu-chamado-solucao';
        solucao.textContent = `Solução: ${chamado.solucao}`;
        item.appendChild(solucao);
    }

    if (chamado.status === 'ABERTO') {
        const acoes = document.createElement('div');
        acoes.className = 'card-actions';
        const botaoCancelar = document.createElement('button');
        botaoCancelar.type = 'button';
        botaoCancelar.className = 'button button-secondary';
        botaoCancelar.textContent = 'Cancelar chamado';
        botaoCancelar.addEventListener('click', () => {
            chamadoParaCancelarId = chamado.id;
            document.querySelector('#cancelamento-chamado-info').textContent =
                `Você está cancelando o chamado #${chamado.id} · ${tituloDoChamado(chamado)}`;
            cancelTicketForm.hidden = false;
            cancelTicketForm.scrollIntoView({ behavior: 'smooth', block: 'center' });
            document.querySelector('#motivo-cancelamento').focus();
        });
        acoes.appendChild(botaoCancelar);
        item.appendChild(acoes);
    }

    return item;
}

document.querySelector('[data-close-cancel]')?.addEventListener('click', () => {
    cancelTicketForm.hidden = true;
    cancelTicketForm.reset();
    cancelTicketMessage.textContent = '';
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
            await apiFetch(`/chamados/cancelar/${chamadoParaCancelarId}?motivoCancelamento=${motivoCancelamento}`, { method: 'PATCH' });
            cancelTicketMessage.classList.remove('is-error');
            cancelTicketMessage.textContent = 'Chamado cancelado com sucesso.';
            cancelTicketForm.reset();
            cancelTicketForm.hidden = true;
            await carregarMeusChamados();
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

anexoAberturaUsuario?.addEventListener('change', () => {
    const imagemSelecionada = anexoAberturaUsuario.files[0];
    statusAnexoAberturaUsuario.textContent = imagemSelecionada
        ? `Selecionada: ${imagemSelecionada.name}. Envio ao chamado ainda não disponível.`
        : 'O envio será conectado ao backend posteriormente.';
});

async function carregarAberturaDeChamado() {
    if (!sessaoAtual || sessaoAtual.tecnico) {
        return;
    }

    formularioAberturaUsuario.hidden = false;
    diagnosticoMike.hidden = true;
    formularioEncaminhamentoMike.hidden = true;
    mensagemAberturaUsuario.textContent = '';
    mensagemAberturaUsuario.classList.remove('is-error');

    try {
        const diagnosticoEmAndamento = await apiFetch('/mike-ia/em-diagnostico');
        if (diagnosticoEmAndamento) {
            exibirDiagnosticoMike(diagnosticoEmAndamento);
        }
    } catch (erro) {
        // O formulário continua disponível: a mensagem só é necessária quando o usuário
        // enviar os dados, evitando um bloqueio visual se a recuperação falhar.
    }
}

function exibirDiagnosticoMike(atendimento) {
    atendimentoMikeAtual = atendimento;
    formularioAberturaUsuario.hidden = true;
    formularioEncaminhamentoMike.hidden = true;

    const listaDePassos = document.querySelector('#mike-diagnostico-passos');
    listaDePassos.innerHTML = '';
    atendimento.sugestoes.split('\n').filter(Boolean).forEach((passo) => {
        const item = document.createElement('li');
        item.textContent = passo;
        listaDePassos.appendChild(item);
    });

    const possuiOrientacaoTestavel = atendimento.possuiOrientacaoTestavel === true;
    document.querySelector('#mike-pergunta-resolvido').hidden = !possuiOrientacaoTestavel;
    document.querySelector('#mike-resolveu').hidden = !possuiOrientacaoTestavel;

    diagnosticoMike.hidden = false;
    diagnosticoMike.focus();
}

formularioAberturaUsuario?.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!formularioAberturaUsuario.checkValidity()) {
        mensagemAberturaUsuario.classList.add('is-error');
        mensagemAberturaUsuario.textContent = 'Descreva o problema para continuar o atendimento.';
        formularioAberturaUsuario.reportValidity();
        return;
    }

    const corpo = {
        descricaoProblema: document.querySelector('#abertura-descricao').value.trim(),
        categoria: document.querySelector('#abertura-categoria').value || null
    };

    await executarComEstadoDeEnvio(formularioAberturaUsuario.querySelector('button[type="submit"]'),
        'Iniciando...', async () => {
            try {
                const atendimento = await apiFetch('/mike-ia/iniciar', { method: 'POST', body: corpo });
                exibirDiagnosticoMike(atendimento);
            } catch (erro) {
                mensagemAberturaUsuario.classList.add('is-error');
                mensagemAberturaUsuario.textContent = erro.message;
            }
        });
});

document.querySelector('#mike-resolveu')?.addEventListener('click', async () => {
    if (!atendimentoMikeAtual) return;

    const botao = document.querySelector('#mike-resolveu');
    await executarComEstadoDeEnvio(botao, 'Concluindo...', async () => {
        try {
            await apiFetch(`/mike-ia/concluir/${atendimentoMikeAtual.atendimentoId}`, { method: 'PATCH' });
            await showRoute('mike-resolvido');
        } catch (erro) {
            const mensagem = document.querySelector('#mensagem-diagnostico-mike');
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

document.querySelector('#mike-voltar')?.addEventListener('click', async () => {
    if (!atendimentoMikeAtual) return;

    const botao = document.querySelector('#mike-voltar');
    await executarComEstadoDeEnvio(botao, 'Voltando...', async () => {
        try {
            await apiFetch(`/mike-ia/abandonar/${atendimentoMikeAtual.atendimentoId}`, { method: 'PATCH' });
            atendimentoMikeAtual = null;
            await showRoute('abrir-chamado');
        } catch (erro) {
            const mensagem = document.querySelector('#mensagem-diagnostico-mike');
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

document.querySelector('#mike-nao-resolveu')?.addEventListener('click', () => {
    if (!atendimentoMikeAtual) return;

    document.querySelector('#encaminhamento-categoria').value = atendimentoMikeAtual.categoria || '';
    diagnosticoMike.hidden = true;
    formularioEncaminhamentoMike.hidden = false;
    document.querySelector('#encaminhamento-local').focus();
});

document.querySelector('#mike-voltar-ao-diagnostico')?.addEventListener('click', () => {
    if (!atendimentoMikeAtual) return;
    exibirDiagnosticoMike(atendimentoMikeAtual);
});

formularioEncaminhamentoMike?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const mensagem = document.querySelector('#mensagem-encaminhamento-mike');

    if (!formularioEncaminhamentoMike.checkValidity()) {
        mensagem.classList.add('is-error');
        mensagem.textContent = 'Preencha categoria, prioridade e local para encaminhar o atendimento.';
        formularioEncaminhamentoMike.reportValidity();
        return;
    }

    const corpo = {
        categoria: document.querySelector('#encaminhamento-categoria').value,
        prioridade: document.querySelector('#encaminhamento-prioridade').value,
        localAtendimento: document.querySelector('#encaminhamento-local').value.trim()
    };

    await executarComEstadoDeEnvio(formularioEncaminhamentoMike.querySelector('button[type="submit"]'),
        'Encaminhando...', async () => {
            try {
                await apiFetch(`/mike-ia/encaminhar/${atendimentoMikeAtual.atendimentoId}`, {
                    method: 'PATCH', body: corpo
                });
                formularioEncaminhamentoMike.reset();
                atendimentoMikeAtual = null;
                const botaoAcompanhar = document.querySelector('#botao-sucesso-acompanhar');
                botaoAcompanhar.textContent = 'Ver meus chamados';
                botaoAcompanhar.dataset.route = 'meus-chamados';
                await showRoute('chamado-sucesso');
            } catch (erro) {
                mensagem.classList.add('is-error');
                mensagem.textContent = erro.message;
            }
        });
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
        mensagem.textContent = 'Preencha RE, posto/graduação, nome e e-mail funcional.';
        form.reportValidity();
        return;
    }

    const corpo = {
        re: document.querySelector('#cadastro-re').value.trim(),
        postoGraduacao: document.querySelector('#cadastro-posto').value,
        nome: document.querySelector('#cadastro-nome').value.trim(),
        email: document.querySelector('#cadastro-email').value.trim()
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

function exibirUsuarioConsultado(usuario) {
    usuarioConsultadoRe = usuario.re;
    document.querySelector('#consulta-usuario-nome').textContent = `${usuario.postoGraduacao} ${usuario.nome}`;
    document.querySelector('#consulta-usuario-info').textContent = `RE ${usuario.re} · ${usuario.email}`;

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
