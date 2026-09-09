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

// O usuário só precisa ver o que ele mesmo relatou ao Mike (categoria, problema e
// respostas) — os procedimentos orientados e o resultado/encaminhamento são contexto
// operacional para o técnico, já disponível na tela técnica separadamente.
function descricaoDetalhadaDoChamado(chamado) {
    const detalhes = chamado.descricao.split('\n').slice(1).join('\n').trim();
    if (!detalhes) return 'Nenhuma descrição complementar foi informada.';

    const indiceProcedimentos = detalhes.indexOf('\nProcedimentos orientados:');
    const detalhesParaOUsuario = indiceProcedimentos === -1 ? detalhes : detalhes.slice(0, indiceProcedimentos).trim();
    return detalhesParaOUsuario || 'Nenhuma descrição complementar foi informada.';
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
