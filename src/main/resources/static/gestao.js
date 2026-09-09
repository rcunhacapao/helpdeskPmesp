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

// Relatar um erro — feedback livre sobre o próprio sistema, em fase de testes.
// Não confundir com Chamado (pedido de suporte de TI do dia a dia).
const ROTULOS_TIPO_ERRO = {
    LAYOUT_QUEBRADO: 'Layout quebrado ou desalinhado',
    BOTAO_OU_LINK_NAO_FUNCIONA: 'Um botão ou link não funciona',
    DADO_NAO_SALVOU_OU_CARREGOU: 'Uma informação não salvou ou não carregou',
    MENSAGEM_DE_ERRO_INESPERADA: 'Apareceu uma mensagem de erro inesperada',
    LENTIDAO: 'O sistema ficou lento',
    OUTRO: 'Outro'
};

const formularioRelatarErro = document.querySelector('#formulario-relatar-erro');
const tipoErroSelect = document.querySelector('#tipo-erro');
const observacaoErroTextarea = document.querySelector('#observacao-erro');
const rotuloObservacaoErro = document.querySelector('#rotulo-observacao-erro');

function atualizarObrigatoriedadeDaObservacaoDeErro() {
    const exigirObservacao = tipoErroSelect?.value === 'OUTRO';
    if (!observacaoErroTextarea || !rotuloObservacaoErro) return;
    observacaoErroTextarea.required = exigirObservacao;
    rotuloObservacaoErro.innerHTML = exigirObservacao
        ? 'Observações <span aria-hidden="true">*</span>'
        : 'Observações (opcional)';
}

tipoErroSelect?.addEventListener('change', atualizarObrigatoriedadeDaObservacaoDeErro);

formularioRelatarErro?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const mensagem = document.querySelector('#mensagem-relatar-erro');

    if (!formularioRelatarErro.checkValidity()) {
        formularioRelatarErro.reportValidity();
        return;
    }

    const corpo = {
        tipoErro: tipoErroSelect.value,
        observacao: observacaoErroTextarea.value.trim() || null
    };

    await executarComEstadoDeEnvio(formularioRelatarErro.querySelector('button[type="submit"]'), 'Enviando...', async () => {
        try {
            await apiFetch('/relatos-erro', { method: 'POST', body: corpo });
            mensagem.classList.remove('is-error');
            mensagem.textContent = '';
            formularioRelatarErro.reset();
            atualizarObrigatoriedadeDaObservacaoDeErro();
            await showRoute('relato-erro-sucesso');
        } catch (erro) {
            mensagem.classList.add('is-error');
            mensagem.textContent = erro.message;
        }
    });
});

function criarLinhaDeRelatoErro(relato) {
    const linha = document.createElement('article');
    linha.className = 'technician-row';

    const info = document.createElement('div');
    const tipo = document.createElement('strong');
    tipo.textContent = ROTULOS_TIPO_ERRO[relato.tipoErro] || relato.tipoErro;
    const detalhes = document.createElement('span');
    const dataFormatada = new Date(relato.dataRelato).toLocaleString('pt-BR');
    detalhes.textContent = `${relato.identificacaoDeQuemRelatou} · ${dataFormatada}`;
    info.append(tipo, detalhes);

    if (relato.observacao) {
        const observacao = document.createElement('p');
        observacao.className = 'meu-chamado-descricao';
        observacao.textContent = relato.observacao;
        info.append(observacao);
    }

    linha.append(info);
    return linha;
}

async function carregarRelatosDeErro() {
    const lista = document.querySelector('#lista-relatos-erro');
    if (!lista) return;
    lista.innerHTML = '';

    try {
        const relatos = await apiFetch('/relatos-erro');
        if (!relatos.length) {
            const vazio = document.createElement('p');
            vazio.className = 'lista-tecnicos-vazia';
            vazio.textContent = 'Nenhum erro relatado até o momento.';
            lista.appendChild(vazio);
            return;
        }
        relatos.forEach((relato) => lista.appendChild(criarLinhaDeRelatoErro(relato)));
    } catch (erro) {
        const mensagem = document.createElement('p');
        mensagem.className = 'lista-tecnicos-vazia';
        mensagem.textContent = erro.message;
        lista.appendChild(mensagem);
    }
}

document.querySelector('#atualizar-relatos-erro')?.addEventListener('click', carregarRelatosDeErro);

restaurarSessaoAoCarregarPagina();
