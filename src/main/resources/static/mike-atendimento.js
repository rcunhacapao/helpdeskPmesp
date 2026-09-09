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
