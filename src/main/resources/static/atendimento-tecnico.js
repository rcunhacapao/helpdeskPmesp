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

// Rastreia qual das duas seções (descrição simples ou contexto do Mike) está ativa no
// momento, para o botão "Ocultar descrição" saber o que mostrar/ocultar.
let elementoDescricaoTecnicaAtivo = null;

function ativarDescricaoTecnica(tipo) {
    const secaoDescricao = document.querySelector('#secao-descricao-tecnica');
    const secaoHistorico = document.querySelector('#historico-mike-tecnico');
    const botaoAlternar = document.querySelector('#alternar-descricao-tecnica');

    secaoDescricao.hidden = tipo !== 'descricao';
    secaoHistorico.hidden = tipo !== 'mike';
    elementoDescricaoTecnicaAtivo = tipo === 'mike' ? secaoHistorico : secaoDescricao;
    if (botaoAlternar) botaoAlternar.textContent = 'Ocultar descrição';
}

document.querySelector('#alternar-descricao-tecnica')?.addEventListener('click', () => {
    if (!elementoDescricaoTecnicaAtivo) return;
    elementoDescricaoTecnicaAtivo.hidden = !elementoDescricaoTecnicaAtivo.hidden;
    document.querySelector('#alternar-descricao-tecnica').textContent =
        elementoDescricaoTecnicaAtivo.hidden ? 'Mostrar descrição' : 'Ocultar descrição';
});

function renderizarHistoricoMikeNoDetalhe(chamadoId) {
    const historico = historicosMikePorChamadoId.get(chamadoId);

    if (historico === undefined) {
        void carregarHistoricoMikeDoChamado(chamadoId);
        ativarDescricaoTecnica('descricao');
        return;
    }

    if (!historico) {
        ativarDescricaoTecnica('descricao');
        return;
    }

    document.querySelector('#detalhe-mike-status').textContent = nomeDoResultadoMike(historico.resultado);
    document.querySelector('#detalhe-mike-relato').textContent = historico.descricaoProblema;
    document.querySelector('#detalhe-mike-sugestoes').textContent = historico.sugestoes;
    // A descrição simples do chamado repete o mesmo conteúdo do contexto do Mike abaixo —
    // mostrar as duas seria redundante, então o contexto substitui a descrição quando
    // existe (o técnico ainda pode reexibi-la pelo botão "Mostrar descrição").
    ativarDescricaoTecnica('mike');
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
            chamadoSelecionadoId = null;
            document.querySelector('#titulo-atendimento-finalizado').textContent = 'Atendimento finalizado com sucesso!';
            document.querySelector('#mensagem-atendimento-finalizado').textContent =
                `Chamado #${chamado.id} concluído. O registro permanece no histórico.`;
            document.querySelector('#modal-atendimento-finalizado').showModal();
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

document.querySelector('#fechar-atendimento-finalizado')?.addEventListener('click', () => {
    const modal = document.querySelector('#modal-atendimento-finalizado');
    if (modal?.open) modal.close();
});
