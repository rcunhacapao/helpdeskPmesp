const loginForm = document.querySelector('#formulario-login');
const loginMessage = document.querySelector('#mensagem-login');
const loginPage = document.querySelector('#pagina-login');
const appPage = document.querySelector('#pagina-app');
const routes = document.querySelectorAll('[data-route]');
const views = document.querySelectorAll('[data-view]');
const navigationLinks = document.querySelectorAll('.nav-link, .mobile-nav-link');
const ticketForm = document.querySelector('#formulario-chamado');
const ticketMessage = document.querySelector('#mensagem-chamado');
const mikeForm = document.querySelector('#formulario-mike');
const mikeQuestion = document.querySelector('#pergunta-mike');
const mikeAnswer = document.querySelector('#resposta-mike');
const mikeReply = document.querySelector('#retorno-mike');
const cancelTicketForm = document.querySelector('#formulario-cancelamento');
const cancelTicketMessage = document.querySelector('#mensagem-cancelamento');
const queueMessage = document.querySelector('#mensagem-fila');
const queueList = document.querySelector('#lista-fila');
const queueSearch = document.querySelector('#busca-chamado');
const queueDetailContent = document.querySelector('#conteudo-detalhe-chamado');
const queueDetailEmpty = document.querySelector('#detalhe-chamado-vazio');

const perfilDemonstracao = new URLSearchParams(window.location.search).get('perfil') === 'tecnico'
    ? 'tecnico'
    : 'usuario';

const sessaoDemonstracao = perfilDemonstracao === 'tecnico'
    ? {
        identificacao: '3º SGT PM Técnico de Telemática',
        perfil: 'Técnico',
        rotaInicial: 'central-tecnica'
    }
    : {
        identificacao: '3º SGT PM Usuário',
        perfil: 'Usuário',
        rotaInicial: 'visao-geral'
    };

function pertenceAoPerfil(elemento) {
    const perfilPermitido = elemento.dataset.profile || 'todos';
    return perfilPermitido === 'todos' || perfilPermitido === perfilDemonstracao;
}

function aplicarPerfilDemonstracao() {
    document.querySelectorAll('.nav-link[data-profile], .mobile-nav-link[data-profile]').forEach((item) => {
        item.hidden = !pertenceAoPerfil(item);
    });

    document.querySelector('#identificacao-sidebar').textContent = sessaoDemonstracao.identificacao;
    document.querySelector('#perfil-sessao').textContent = `Perfil: ${sessaoDemonstracao.perfil}`;

    const saudacaoUsuario = document.querySelector('#saudacao-usuario');
    const saudacaoTecnico = document.querySelector('#saudacao-tecnico');

    if (saudacaoUsuario) saudacaoUsuario.textContent = sessaoDemonstracao.identificacao;
    if (saudacaoTecnico) saudacaoTecnico.textContent = sessaoDemonstracao.identificacao;
}

function showRoute(route) {
    const destino = document.querySelector(`#${route}`);
    const rotaValida = destino && pertenceAoPerfil(destino) ? route : sessaoDemonstracao.rotaInicial;

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
}

function mostrarMensagemDeDemonstracao(mensagem, destinoId) {
    const destino = destinoId ? document.querySelector(`#${destinoId}`) : null;
    if (!destino) return;

    destino.classList.remove('is-error');
    destino.textContent = `${mensagem}. A integração com a API será realizada na etapa de lógica.`;
}

const chamadosDemonstracao = [
    {
        id: 41,
        codigo: '2026-0041',
        assunto: 'Acesso ao sistema administrativo indisponível',
        descricao: 'Não é possível acessar o sistema administrativo desde o início do expediente. A tela informa que a conexão foi recusada.',
        solicitante: 'CB PM Rafael Moreira',
        local: 'Seção administrativa',
        categoria: 'Software',
        prioridade: 'URGENTE',
        status: 'ABERTO',
        dataAbertura: 'Hoje às 08:50',
        responsavel: null
    },
    {
        id: 42,
        codigo: '2026-0042',
        assunto: 'Impressora do setor administrativo não imprime',
        descricao: 'A impressora está ligada, mas os documentos permanecem na fila de impressão. O problema começou nesta manhã.',
        solicitante: '3º SGT PM Usuário',
        local: 'Administração',
        categoria: 'Impressora',
        prioridade: 'MEDIA',
        status: 'ABERTO',
        dataAbertura: 'Hoje às 09:35',
        responsavel: null
    },
    {
        id: 39,
        codigo: '2026-0039',
        assunto: 'Estação de trabalho com falha de conexão',
        descricao: 'A estação de trabalho perdeu o acesso à rede interna. O cabo foi conferido, mas a conexão não retornou.',
        solicitante: 'SD PM Marina Costa',
        local: 'P/1',
        categoria: 'Rede e internet',
        prioridade: 'ALTA',
        status: 'EM_ATENDIMENTO',
        dataAbertura: 'Hoje às 08:20',
        responsavel: 'CB PM Técnico de apoio'
    }
];

let filtroFilaAtual = 'ABERTO';
let termoBuscaFila = '';
let chamadoSelecionadoId = null;

const ordemDePrioridade = { URGENTE: 0, ALTA: 1, MEDIA: 2, BAIXA: 3 };
const nomeDaPrioridade = { URGENTE: 'Urgente', ALTA: 'Alta', MEDIA: 'Média', BAIXA: 'Baixa' };
const nomeDoStatus = { ABERTO: 'Aguardando atendimento', EM_ATENDIMENTO: 'Em atendimento' };

function atualizarTextosDoSeletor(seletor, texto) {
    document.querySelectorAll(seletor).forEach((elemento) => {
        elemento.textContent = texto;
    });
}

function atualizarAcompanhamentoDoUsuario() {
    const chamadoDoUsuario = chamadosDemonstracao.find((chamado) => chamado.solicitante === '3º SGT PM Usuário');
    if (!chamadoDoUsuario) return;

    const aguardando = chamadosDemonstracao
        .filter((chamado) => chamado.status === 'ABERTO')
        .sort((primeiro, segundo) => ordemDePrioridade[primeiro.prioridade] - ordemDePrioridade[segundo.prioridade]);
    const posicao = aguardando.findIndex((chamado) => chamado.id === chamadoDoUsuario.id) + 1;
    const estaNaFila = chamadoDoUsuario.status === 'ABERTO';
    const posicaoExibida = estaNaFila ? `${posicao}º` : '—';
    const chamadosAFrente = Math.max(posicao - 1, 0);
    const resumoDaFila = chamadosAFrente === 0
        ? 'Você é o próximo da fila'
        : `${chamadosAFrente} ${chamadosAFrente === 1 ? 'chamado à frente' : 'chamados à frente'}`;
    const tecnicoResponsavel = chamadoDoUsuario.responsavel
        ? `Técnico responsável: ${chamadoDoUsuario.responsavel}.`
        : 'Técnico responsável: ainda não atribuído.';
    const status = nomeDoStatus[chamadoDoUsuario.status] || 'Atendimento concluído';
    const mensagem = estaNaFila
        ? `Seu chamado está na ${posicao}ª posição da fila e aguarda a definição de um técnico responsável.`
        : chamadoDoUsuario.status === 'EM_ATENDIMENTO'
            ? `Seu atendimento já foi iniciado por ${chamadoDoUsuario.responsavel || 'um técnico responsável'}.`
            : 'Seu atendimento foi concluído. O registro permanece disponível no histórico.';

    atualizarTextosDoSeletor('#overview-ticket-status, #meus-chamados-ticket-status', status);
    atualizarTextosDoSeletor('#overview-queue-position, #meus-chamados-queue-position', posicaoExibida);
    atualizarTextosDoSeletor('#overview-queue-ahead, #meus-chamados-queue-ahead', estaNaFila ? resumoDaFila : 'Atendimento em andamento');
    atualizarTextosDoSeletor('#overview-tech-assignment, #meus-chamados-tech-assignment', tecnicoResponsavel);
    atualizarTextosDoSeletor('#overview-queue-message', mensagem);
    atualizarTextosDoSeletor('#meus-chamados-queue-message', mensagem);
}

function obterChamadoSelecionado() {
    return chamadosDemonstracao.find((chamado) => chamado.id === chamadoSelecionadoId);
}

function mostrarMensagemDaFila(mensagem, isError = false) {
    if (!queueMessage) return;
    queueMessage.classList.toggle('is-error', isError);
    queueMessage.textContent = mensagem;
}

function chamadosDaFilaAtual() {
    const termoNormalizado = termoBuscaFila.trim().toLocaleLowerCase('pt-BR');

    return chamadosDemonstracao
        .filter((chamado) => chamado.status === filtroFilaAtual)
        .filter((chamado) => !termoNormalizado
            || chamado.codigo.includes(termoNormalizado)
            || chamado.assunto.toLocaleLowerCase('pt-BR').includes(termoNormalizado))
        .sort((primeiro, segundo) => ordemDePrioridade[primeiro.prioridade] - ordemDePrioridade[segundo.prioridade]);
}

function renderizarDetalheDoChamado() {
    const chamado = obterChamadoSelecionado();
    const podeSerConduzido = chamado && ['ABERTO', 'EM_ATENDIMENTO'].includes(chamado.status);

    if (!chamado || !podeSerConduzido) {
        queueDetailContent.hidden = true;
        queueDetailEmpty.hidden = false;
        const pendentes = chamadosDemonstracao.filter((item) => item.status === 'ABERTO').length;
        const emAtendimento = chamadosDemonstracao.filter((item) => item.status === 'EM_ATENDIMENTO').length;
        const exibindoPendentes = filtroFilaAtual === 'ABERTO';
        const quantidade = exibindoPendentes ? pendentes : emAtendimento;

        document.querySelector('#resumo-fila-quantidade').textContent = quantidade;
        document.querySelector('#titulo-detalhe-chamado').textContent = exibindoPendentes
            ? 'Chamados pendentes'
            : 'Chamados em atendimento';
        document.querySelector('#resumo-fila-descricao').textContent = exibindoPendentes
            ? quantidade === 0
                ? 'Não há chamados pendentes no momento.'
                : `${quantidade} ${quantidade === 1 ? 'chamado aguarda' : 'chamados aguardam'} atendimento. Selecione um item na lista para continuar.`
            : quantidade === 0
                ? 'Não há chamados em atendimento no momento.'
                : `${quantidade} ${quantidade === 1 ? 'atendimento está em andamento' : 'atendimentos estão em andamento'}. Selecione um item para acompanhar.`;
        return;
    }

    queueDetailContent.hidden = false;
    queueDetailEmpty.hidden = true;
    document.querySelector('#detalhe-codigo').textContent = `Chamado #${chamado.codigo}`;
    document.querySelector('#detalhe-assunto').textContent = chamado.assunto;
    document.querySelector('#detalhe-abertura').textContent = `Aberto ${chamado.dataAbertura.toLocaleLowerCase('pt-BR')}`;
    document.querySelector('#detalhe-status').textContent = nomeDoStatus[chamado.status];
    document.querySelector('#detalhe-solicitante').textContent = chamado.solicitante;
    document.querySelector('#detalhe-local').textContent = chamado.local;
    document.querySelector('#detalhe-categoria').textContent = chamado.categoria;
    document.querySelector('#detalhe-responsavel').textContent = chamado.responsavel || 'Aguardando assunção';
    document.querySelector('#detalhe-descricao').textContent = chamado.descricao;
    const detalhePrioridade = document.querySelector('#detalhe-prioridade');
    detalhePrioridade.textContent = nomeDaPrioridade[chamado.prioridade];
    detalhePrioridade.className = `priority-label priority-${chamado.prioridade.toLocaleLowerCase('pt-BR')}`;
    document.querySelector('#acoes-chamado-aberto').hidden = chamado.status !== 'ABERTO';
    document.querySelector('#acoes-transferencia').hidden = !podeSerConduzido;
    document.querySelector('#acoes-chamado-atendimento').hidden = chamado.status !== 'EM_ATENDIMENTO';
}

function renderizarFilaAtendimento() {
    if (!queueList) return;

    const chamadosVisiveis = chamadosDaFilaAtual();
    const aguardando = chamadosDemonstracao.filter((chamado) => chamado.status === 'ABERTO').length;
    const emAtendimento = chamadosDemonstracao.filter((chamado) => chamado.status === 'EM_ATENDIMENTO').length;

    document.querySelector('#contador-fila-aberta').textContent = aguardando;
    document.querySelector('#contador-em-atendimento').textContent = emAtendimento;
    document.querySelector('#contador-chamados-ativos').textContent = `${aguardando + emAtendimento} ativos`;

    document.querySelectorAll('[data-queue-filter]').forEach((button) => {
        const isActive = button.dataset.queueFilter === filtroFilaAtual;
        button.classList.toggle('is-active', isActive);
        button.setAttribute('aria-selected', String(isActive));
    });

    if (chamadoSelecionadoId && !chamadosVisiveis.some((chamado) => chamado.id === chamadoSelecionadoId)) {
        chamadoSelecionadoId = null;
    }

    queueList.innerHTML = chamadosVisiveis.length
        ? chamadosVisiveis.map((chamado) => `
            <button class="queue-ticket${chamado.id === chamadoSelecionadoId ? ' is-selected' : ''}" type="button" role="listitem" data-select-ticket="${chamado.id}" aria-pressed="${chamado.id === chamadoSelecionadoId}">
                <span class="queue-ticket-top"><span class="queue-ticket-code">#${chamado.codigo}</span><span class="priority-label priority-${chamado.prioridade.toLocaleLowerCase('pt-BR')}">${nomeDaPrioridade[chamado.prioridade]}</span></span>
                <strong class="queue-ticket-title">${chamado.assunto}</strong>
                <span class="queue-ticket-meta">${chamado.categoria} · ${chamado.local} · ${chamado.dataAbertura.toLocaleLowerCase('pt-BR')}</span>
                <span class="queue-ticket-footer"><span class="queue-ticket-status">${nomeDoStatus[chamado.status]}</span><span aria-hidden="true">›</span></span>
            </button>`).join('')
        : `<p class="queue-empty-list">${filtroFilaAtual === 'ABERTO'
            ? 'Não há chamados pendentes no momento.'
            : 'Não há chamados em atendimento no momento.'}</p>`;

    renderizarDetalheDoChamado();
    atualizarAcompanhamentoDoUsuario();
}

function executarAcaoDaFila(acao) {
    const chamado = obterChamadoSelecionado();

    if (!chamado) {
        mostrarMensagemDaFila('Selecione um chamado antes de escolher uma ação.', true);
        return;
    }

    if (acao === 'iniciar') {
        if (chamado.status !== 'ABERTO') return;
        chamado.status = 'EM_ATENDIMENTO';
        chamado.responsavel = sessaoDemonstracao.identificacao;
        filtroFilaAtual = 'EM_ATENDIMENTO';
        mostrarMensagemDaFila(`Você assumiu o chamado #${chamado.codigo}. O atendimento foi iniciado.`);
    }

    if (acao === 'transferir') {
        if (!['ABERTO', 'EM_ATENDIMENTO'].includes(chamado.status)) return;
        chamado.responsavel = document.querySelector('#transferir-responsavel').value;
        mostrarMensagemDaFila(`Responsável do chamado #${chamado.codigo} atualizado com sucesso.`);
    }

    if (acao === 'finalizar') {
        if (chamado.status !== 'EM_ATENDIMENTO') return;
        chamado.status = 'FECHADO';
        chamadoSelecionadoId = null;
        mostrarMensagemDaFila(`Atendimento do chamado #${chamado.codigo} finalizado. O registro permanece no histórico.`);
    }

    renderizarFilaAtendimento();
}

aplicarPerfilDemonstracao();

loginForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    loginMessage.classList.remove('is-error');
    loginMessage.textContent = '';
    loginPage.hidden = true;
    appPage.hidden = false;
    showRoute(sessaoDemonstracao.rotaInicial);
});

routes.forEach((route) => {
    route.addEventListener('click', () => showRoute(route.dataset.route));
});

ticketForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    if (!ticketForm.checkValidity()) {
        ticketMessage.classList.add('is-error');
        ticketMessage.textContent = 'Preencha os campos obrigatórios para continuar na demonstração.';
        ticketForm.reportValidity();
        return;
    }

    ticketMessage.classList.remove('is-error');
    ticketMessage.textContent = 'Chamado simulado com sucesso. O envio real será conectado em uma próxima etapa.';
    ticketForm.reset();
});

mikeForm?.addEventListener('submit', (event) => {
    event.preventDefault();
    const message = mikeQuestion.value.trim();

    if (!message) return;

    mikeAnswer.querySelector('p').textContent = message;
    mikeAnswer.hidden = false;
    mikeReply.hidden = false;
    mikeQuestion.value = '';
});

document.querySelector('#sair')?.addEventListener('click', () => {
    appPage.hidden = true;
    loginPage.hidden = false;
    loginForm.reset();
    loginMessage.textContent = '';
});

document.querySelectorAll('[data-focus-cancel], [data-cancel-ticket]').forEach((button) => {
    button.addEventListener('click', () => {
        showRoute('meus-chamados');
        cancelTicketForm.hidden = false;
        document.querySelector('#motivo-cancelamento').focus();
    });
});

document.querySelector('[data-close-cancel]')?.addEventListener('click', () => {
    cancelTicketForm.hidden = true;
    cancelTicketForm.reset();
    cancelTicketMessage.textContent = '';
});

document.querySelectorAll('[data-queue-filter]').forEach((button) => {
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
    button.addEventListener('click', () => executarAcaoDaFila(button.dataset.queueAction));
});

cancelTicketForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    if (!cancelTicketForm.checkValidity()) {
        cancelTicketMessage.classList.add('is-error');
        cancelTicketMessage.textContent = 'Informe o motivo para cancelar o chamado.';
        cancelTicketForm.reportValidity();
        return;
    }

    cancelTicketMessage.classList.remove('is-error');
    cancelTicketMessage.textContent = 'Cancelamento simulado. A confirmação real será conectada à API de chamados.';
    cancelTicketForm.reset();
});

document.querySelectorAll('[data-demo-action]').forEach((button) => {
    button.addEventListener('click', () => mostrarMensagemDeDemonstracao(button.dataset.demoAction, button.dataset.demoMessage));
});

document.querySelector('#formulario-cadastro-usuario')?.addEventListener('submit', (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const message = form.querySelector('.form-message');

    if (!form.checkValidity()) {
        message.classList.add('is-error');
        message.textContent = 'Preencha RE, posto ou graduação e nome para continuar.';
        form.reportValidity();
        return;
    }

    message.classList.remove('is-error');
    message.textContent = 'Cadastro visual validado. O envio ao serviço de usuários será ligado na etapa de lógica.';
    form.reset();
});

document.querySelector('#alternar-disponibilidade')?.addEventListener('click', (buttonEvent) => {
    const button = buttonEvent.currentTarget;
    const isAvailable = button.getAttribute('aria-pressed') === 'true';
    const nextState = !isAvailable;

    button.setAttribute('aria-pressed', String(nextState));
    button.classList.toggle('is-available', nextState);
    button.querySelector('strong').textContent = nextState
        ? 'Disponível para atendimento'
        : 'Indisponível para atendimento';
    document.querySelector('#mensagem-disponibilidade').textContent = nextState
        ? 'Disponibilidade visualmente atualizada. A API fará a persistência na próxima etapa.'
        : 'Indisponibilidade visualmente atualizada. A API fará a persistência na próxima etapa.';
});

renderizarFilaAtendimento();
