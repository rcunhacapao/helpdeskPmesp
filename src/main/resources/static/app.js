const loginForm = document.querySelector('#formulario-login');
const loginMessage = document.querySelector('#mensagem-login');
const loginPage = document.querySelector('#pagina-login');
const appPage = document.querySelector('#pagina-app');
const routes = document.querySelectorAll('[data-route]');
const views = document.querySelectorAll('[data-view]');
const navigationLinks = document.querySelectorAll('.nav-link');
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
    document.querySelectorAll('.nav-link[data-profile]').forEach((item) => {
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

        resumoFilaQuantidade.textContent = quantidade;
        tituloDetalheChamado.textContent = exibindoPendentes
            ? 'Chamados pendentes'
            : 'Chamados em atendimento';
        resumoFilaDescricao.textContent = exibindoPendentes
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
    detalheCodigo.textContent = `Chamado #${chamado.codigo}`;
    detalheAssunto.textContent = chamado.assunto;
    detalheAbertura.textContent = `Aberto ${chamado.dataAbertura.toLocaleLowerCase('pt-BR')}`;
    detalheStatus.textContent = nomeDoStatus[chamado.status];
    detalheSolicitante.textContent = chamado.solicitante;
    detalheLocal.textContent = chamado.local;
    detalheCategoria.textContent = chamado.categoria;
    detalheResponsavel.textContent = chamado.responsavel || 'Aguardando assunção';
    detalheDescricao.textContent = chamado.descricao;
    detalhePrioridadeElemento.textContent = nomeDaPrioridade[chamado.prioridade];
    detalhePrioridadeElemento.className = `priority-label priority-${chamado.prioridade.toLocaleLowerCase('pt-BR')}`;
    acoesChamadoAberto.hidden = chamado.status !== 'ABERTO';
    acoesTransferencia.hidden = !podeSerConduzido;
    acoesChamadoAtendimento.hidden = chamado.status !== 'EM_ATENDIMENTO';
}

// Monta o card de um chamado da fila usando textContent (nunca innerHTML) para que
// campos digitados por qualquer usuário (assunto, categoria, local) nunca sejam
// interpretados como HTML.
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
    codigo.textContent = `#${chamado.codigo}`;
    const prioridade = document.createElement('span');
    prioridade.className = `priority-label priority-${chamado.prioridade.toLocaleLowerCase('pt-BR')}`;
    prioridade.textContent = nomeDaPrioridade[chamado.prioridade];
    topo.append(codigo, prioridade);

    const titulo = document.createElement('strong');
    titulo.className = 'queue-ticket-title';
    titulo.textContent = chamado.assunto;

    const meta = document.createElement('span');
    meta.className = 'queue-ticket-meta';
    meta.textContent = `${chamado.categoria} · ${chamado.local} · ${chamado.dataAbertura.toLocaleLowerCase('pt-BR')}`;

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
    const aguardando = chamadosDemonstracao.filter((chamado) => chamado.status === 'ABERTO').length;
    const emAtendimento = chamadosDemonstracao.filter((chamado) => chamado.status === 'EM_ATENDIMENTO').length;

    contadorFilaAberta.textContent = aguardando;
    contadorEmAtendimento.textContent = emAtendimento;
    contadorChamadosAtivos.textContent = `${aguardando + emAtendimento} ativos`;

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
        listaVazia.textContent = filtroFilaAtual === 'ABERTO'
            ? 'Não há chamados pendentes no momento.'
            : 'Não há chamados em atendimento no momento.';
        queueList.appendChild(listaVazia);
    }

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

// Desabilita o botão e mostra um texto de carregamento por um instante, no mesmo padrão
// que será reaproveitado quando os formulários chamarem a API real (fetch) na próxima etapa.
function executarComEstadoDeEnvio(botao, textoEnviando, aoConcluir) {
    if (!botao) { aoConcluir(); return; }
    const textoOriginal = botao.textContent;
    botao.disabled = true;
    botao.textContent = textoEnviando;
    window.setTimeout(() => {
        botao.disabled = false;
        botao.textContent = textoOriginal;
        aoConcluir();
    }, 500);
}

ticketForm?.addEventListener('submit', (event) => {
    event.preventDefault();

    if (!ticketForm.checkValidity()) {
        ticketMessage.classList.add('is-error');
        ticketMessage.textContent = 'Preencha os campos obrigatórios para continuar na demonstração.';
        ticketForm.reportValidity();
        return;
    }

    executarComEstadoDeEnvio(ticketForm.querySelector('button[type="submit"]'), 'Enviando...', () => {
        ticketMessage.classList.remove('is-error');
        ticketMessage.textContent = 'Chamado simulado com sucesso. O envio real será conectado em uma próxima etapa.';
        ticketForm.reset();
    });
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

    executarComEstadoDeEnvio(cancelTicketForm.querySelector('button[type="submit"]'), 'Enviando...', () => {
        cancelTicketMessage.classList.remove('is-error');
        cancelTicketMessage.textContent = 'Cancelamento simulado. A confirmação real será conectada à API de chamados.';
        cancelTicketForm.reset();
    });
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

    executarComEstadoDeEnvio(form.querySelector('button[type="submit"]'), 'Enviando...', () => {
        message.classList.remove('is-error');
        message.textContent = 'Cadastro visual validado. O envio ao serviço de usuários será ligado na etapa de lógica.';
        form.reset();
    });
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
