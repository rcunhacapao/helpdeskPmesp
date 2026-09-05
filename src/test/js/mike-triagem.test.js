const test = require('node:test');
const assert = require('node:assert/strict');
const fluxo = require('../../main/resources/static/mike-triagem.js');

function escolher(estado, opcao) {
    return fluxo.avancarComOpcao(estado, opcao);
}

function informar(estado, dados) {
    return fluxo.avancarComDados(estado, dados);
}

function iniciarCategoria(categoria, problema) {
    let estado = fluxo.criarEstadoInicial();
    estado = escolher(estado, categoria);
    return escolher(estado, problema);
}

function confirmarEtapa(estado, tipoEsperado) {
    assert.equal(fluxo.obterEtapa(estado).tipo, tipoEsperado);
}

test('computador lento orienta a DTIC e encerra sem encaminhamento', () => {
    let estado = iniciarCategoria('computador', 'lento');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
    assert.match(fluxo.montarResumo(estado, true), /0800 738-0190/);
});

test('mouse que continua com problema oferece chamado', () => {
    let estado = iniciarCategoria('computador', 'mouse-teclado');
    estado = escolher(estado, 'mouse');
    estado = escolher(estado, 'nao');
    confirmarEtapa(estado, 'encaminhamento');
});

test('problema de programa coleta nome e erro antes do chamado', () => {
    let estado = iniciarCategoria('computador', 'programa');
    estado = escolher(estado, 'sim');
    estado = informar(estado, { programa: 'SIGA', erroPrograma: 'Acesso negado' });
    confirmarEtapa(estado, 'encaminhamento');
    assert.match(fluxo.montarResumo(estado, false), /Programa informado: SIGA/);
});

test('monitor com cabo solto pode ser resolvido', () => {
    let estado = iniciarCategoria('monitor', 'nao-funciona');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'solto');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('monitor continua sem imagem depois das verificações e oferece chamado', () => {
    let estado = iniciarCategoria('monitor', 'nao-funciona');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'nao');
    confirmarEtapa(estado, 'encaminhamento');
});

test('disponibilidade de monitor encaminha diretamente', () => {
    const estado = iniciarCategoria('monitor', 'disponibilidade');
    confirmarEtapa(estado, 'encaminhamento');
});

test('impressora desligada pode ser resolvida depois de ligar', () => {
    let estado = iniciarCategoria('impressora', 'nao-imprime');
    estado = escolher(estado, 'nao');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('impressora que continua sem imprimir oferece chamado', () => {
    let estado = iniciarCategoria('impressora', 'nao-imprime');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'nao');
    estado = escolher(estado, 'nao');
    confirmarEtapa(estado, 'encaminhamento');
});

test('papel atolado acessível pode ser retirado com segurança', () => {
    let estado = iniciarCategoria('impressora', 'papel-atolado');
    estado = escolher(estado, 'sim');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('papel preso internamente deve ser encaminhado sem desmontar', () => {
    let estado = iniciarCategoria('impressora', 'papel-atolado');
    estado = escolher(estado, 'nao');
    confirmarEtapa(estado, 'encaminhamento');
    assert.match(fluxo.obterEtapa(estado).mensagem, /Não tente desmontar/);
});

test('rede por cabo pode ser resolvida ao reconectar', () => {
    let estado = iniciarCategoria('rede', 'sem-internet');
    estado = escolher(estado, 'cabo');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('falta de internet sem identificação segura oferece chamado', () => {
    let estado = iniciarCategoria('rede', 'sem-internet');
    estado = escolher(estado, 'nao-sei');
    confirmarEtapa(estado, 'encaminhamento');
});

test('somente a Intranet com problema oferece chamado', () => {
    let estado = iniciarCategoria('rede', 'sem-intranet');
    estado = escolher(estado, 'sim');
    confirmarEtapa(estado, 'encaminhamento');
});

test('acesso à pasta exige CPF sem copiar o dado sensível para o resumo do navegador', () => {
    let estado = iniciarCategoria('rede', 'pasta-secao');
    assert.throws(() => informar(estado, { cpf: '123' }), /11 números/);
    estado = informar(estado, { cpf: '12345678901' });
    confirmarEtapa(estado, 'encaminhamento');
    assert.equal(estado.dados.cpf, '12345678901');
    assert.doesNotMatch(fluxo.montarResumo(estado, false), /12345678901/);
});

test('reset de senha de e-mail pode ser resolvido', () => {
    let estado = iniciarCategoria('email', 'resetar-senha');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('reset de senha não resolvido oferece chamado', () => {
    let estado = iniciarCategoria('email', 'resetar-senha');
    estado = escolher(estado, 'nao');
    confirmarEtapa(estado, 'encaminhamento');
});

test('acesso ao e-mail da seção pode ser resolvido', () => {
    let estado = iniciarCategoria('email', 'email-secao');
    estado = escolher(estado, 'resolvido');
    confirmarEtapa(estado, 'resolvido');
});

test('Não sei sempre avança para uma etapa válida', () => {
    let estado = iniciarCategoria('monitor', 'nao-funciona');
    estado = escolher(estado, 'nao-sei');
    assert.ok(fluxo.obterEtapa(estado));
    estado = escolher(estado, 'nao-sei');
    assert.ok(fluxo.obterEtapa(estado));
});

test('um estado anterior pode ser restaurado sem duplicar mensagens', () => {
    const anterior = iniciarCategoria('rede', 'sem-internet');
    const posterior = escolher(anterior, 'cabo');
    assert.equal(anterior.etapaAtual, 'rede-tipo-conexao');
    assert.notEqual(posterior.etapaAtual, anterior.etapaAtual);
    assert.equal(anterior.conversa.length, 6);
});

test('recarregar reconstrói a conversa sem criar outro atendimento', () => {
    const estado = fluxo.restaurarPeloAtendimento({ categoria: 'MONITOR', descricaoProblema: 'Monitor não está funcionando' });
    assert.equal(estado.etapaAtual, 'monitor-luz');
    assert.equal(estado.problema, 'Monitor não está funcionando');
});

test('uma conversa nova não reutiliza respostas da anterior', () => {
    let anterior = iniciarCategoria('impressora', 'nao-imprime');
    anterior = escolher(anterior, 'sim');
    const nova = fluxo.criarEstadoInicial();
    assert.equal(nova.respostas.length, 0);
    assert.equal(nova.problema, null);
    assert.deepEqual(nova.conversa.map((mensagem) => mensagem.texto), [
        'Olá! Sou o Mike IA. Irei te ajudar a abrir o chamado.',
        'Para iniciar, escolha o equipamento ou serviço que você precisa de suporte.'
    ]);
});

test('perguntas revisadas usam linguagem direta, positiva e sem ambiguidade', () => {
    const etapas = fluxo.listarEtapas();

    assert.equal(etapas['computador-problema'].mensagem, 'O que está acontecendo com o computador?');
    assert.equal(etapas['monitor-problema'].mensagem, 'O que está acontecendo com o monitor?');
    assert.equal(etapas['impressora-problema'].mensagem, 'O que está acontecendo com a impressora?');
    assert.equal(etapas['rede-problema'].mensagem, 'O que está acontecendo com a rede ou internet?');
    assert.equal(etapas['email-problema'].mensagem, 'O que está acontecendo com o e-mail?');
    assert.equal(etapas['monitor-luz'].mensagem, 'O monitor possui alguma luz acesa?');
    assert.equal(etapas['impressora-lista-confirmar'].mensagem, 'A impressora que você costuma usar aparece na lista?');
    assert.equal(etapas['rede-intranet-confirmar'].mensagem, 'Os outros sites abrem normalmente?');
});

test('opções longas foram encurtadas sem mudar o caminho da triagem', () => {
    const etapas = fluxo.listarEtapas();

    assert.equal(etapas['monitor-problema'].opcoes[1].texto, 'Solicitar outro monitor');
    assert.equal(etapas['impressora-problema'].opcoes[3].texto, 'Impressora não aparece na lista');
    assert.equal(etapas['email-problema'].opcoes[0].texto, 'Trocar ou redefinir senha');
    assert.equal(etapas['email-problema'].opcoes[1].texto, 'Acessar e-mail da seção');
});

test('reinício da impressora pergunta claramente se o teste funcionou', () => {
    const etapa = fluxo.listarEtapas()['impressora-reiniciar'];

    assert.match(etapa.mensagem, /A impressão voltou a funcionar\?$/);
    assert.deepEqual(etapa.opcoes.map((opcao) => opcao.texto), ['Sim, resolvido', 'Não', 'Não sei']);
});

test('a conclusão usa mensagem curta e orienta o próximo passo', () => {
    const etapa = fluxo.listarEtapas().resolvido;

    assert.equal(etapa.mensagem, 'Problema resolvido. Se precisar de ajuda com outra situação, posso iniciar um novo atendimento.');
});

test('todas as etapas e opções levam para destinos válidos e alcançáveis', () => {
    const etapas = fluxo.listarEtapas();
    const alcancadas = new Set();
    const pendentes = ['inicio'];

    while (pendentes.length > 0) {
        const etapaId = pendentes.shift();
        if (alcancadas.has(etapaId)) continue;
        alcancadas.add(etapaId);

        const etapa = etapas[etapaId];
        assert.ok(etapa, `Etapa inexistente: ${etapaId}`);
        const destinos = etapa.tipo === 'entrada'
            ? [etapa.proxima]
            : (etapa.opcoes || []).map((opcao) => opcao.proxima);

        const idsDasOpcoes = (etapa.opcoes || []).map((opcao) => opcao.id);
        assert.equal(new Set(idsDasOpcoes).size, idsDasOpcoes.length, `Opção duplicada em ${etapaId}`);
        destinos.filter(Boolean).forEach((destino) => {
            assert.ok(etapas[destino], `Destino ${destino} não existe`);
            pendentes.push(destino);
        });
    }

    assert.deepEqual([...alcancadas].sort(), Object.keys(etapas).sort());
});

test('todas as perguntas técnicas apresentam apenas uma pergunta por etapa', () => {
    const etapas = fluxo.listarEtapas();

    Object.entries(etapas)
        .filter(([id, etapa]) => id !== 'inicio' && etapa.tipo === 'pergunta')
        .forEach(([id, etapa]) => {
            const quantidadeDePerguntas = (etapa.mensagem.match(/\?/g) || []).length;
            assert.equal(quantidadeDePerguntas, 1, `A etapa ${id} deve apresentar uma pergunta por vez`);
        });
});

test('nenhuma opção ultrapassa o limite de leitura compacta', () => {
    const etapas = fluxo.listarEtapas();

    Object.entries(etapas).forEach(([id, etapa]) => {
        (etapa.opcoes || []).forEach((opcao) => {
            assert.ok(opcao.texto.length <= 36, `Opção muito longa em ${id}: ${opcao.texto}`);
        });
        if (etapa.botao) {
            assert.ok(etapa.botao.length <= 24, `Ação muito longa em ${id}: ${etapa.botao}`);
        }
    });
});
