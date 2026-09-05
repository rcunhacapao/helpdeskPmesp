(function (escopoGlobal) {
    'use strict';

    const etapas = {
        inicio: {
            tipo: 'pergunta',
            mensagensIniciais: [
                'Olá! Sou o Mike IA. Irei te ajudar a abrir o chamado.',
                'Para iniciar, escolha o equipamento ou serviço que você precisa de suporte.'
            ],
            opcoes: [
                { id: 'computador', texto: 'Computador', categoria: 'COMPUTADOR', categoriaNome: 'Computador', proxima: 'computador-problema' },
                { id: 'monitor', texto: 'Monitor', categoria: 'MONITOR', categoriaNome: 'Monitor', proxima: 'monitor-problema' },
                { id: 'impressora', texto: 'Impressora', categoria: 'IMPRESSORA', categoriaNome: 'Impressora', proxima: 'impressora-problema' },
                { id: 'rede', texto: 'Rede / Internet', categoria: 'REDE_INTERNET', categoriaNome: 'Rede / Internet', proxima: 'rede-problema' },
                { id: 'email', texto: 'E-mail', categoria: 'E_MAIL', categoriaNome: 'E-mail', proxima: 'email-problema' },
                { id: 'outro', texto: 'Outro', categoria: 'OUTRO', categoriaNome: 'Outro', proxima: 'outro-descricao' }
            ]
        },

        'computador-problema': {
            tipo: 'pergunta', mensagem: 'O que está acontecendo com o computador?',
            opcoes: [
                { id: 'lento', texto: 'Computador está lento', problema: 'Computador está lento', proxima: 'computador-lento-confirmar' },
                { id: 'mouse-teclado', texto: 'Mouse ou teclado com problema', problema: 'Mouse ou teclado com problema', proxima: 'computador-periferico' },
                { id: 'programa', texto: 'Não consigo acessar um programa', problema: 'Não consigo acessar um programa', proxima: 'computador-programa-confirmar' }
            ]
        },
        'computador-lento-confirmar': {
            tipo: 'pergunta', mensagem: 'Seu computador está muito lento para abrir programas, arquivos ou realizar tarefas?',
            registrarResposta: 'Computador apresenta lentidão',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'computador-lento-orientacao' },
                { id: 'nao', texto: 'Não', proxima: 'computador-lento-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'computador-lento-encaminhar' }
            ]
        },
        'computador-lento-orientacao': {
            tipo: 'pergunta',
            mensagem: 'Computadores antigos podem ficar lentos pelo tempo de uso. Nesses casos, uma formatação pode ser necessária.\n\nPara solicitar:\n1. Ligue para a DTIC: 0800 738-0190.\n2. Diga que precisa formatar o computador da unidade.\n\nO atendimento inicial costuma levar de 1 a 2 minutos. A DTIC normalmente indica um técnico no mesmo dia ou no próximo dia útil.\n\nConseguiu entender como solicitar a formatação?',
            procedimentos: ['Orientação para solicitar a formatação diretamente à DTIC pelo telefone 0800 738-0190'],
            registrarResposta: 'Entendeu como solicitar a formatação',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'ajuda', texto: 'Ainda preciso de ajuda', proxima: 'computador-lento-encaminhar' }
            ]
        },
        'computador-lento-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica local poderá verificar o computador com você.', botao: 'Abrir chamado' },
        'computador-periferico': {
            tipo: 'pergunta', mensagem: 'Qual equipamento está apresentando problema?', registrarResposta: 'Equipamento com problema',
            opcoes: [
                { id: 'mouse', texto: 'Mouse', proxima: 'computador-periferico-orientacao' },
                { id: 'teclado', texto: 'Teclado', proxima: 'computador-periferico-orientacao' },
                { id: 'ambos', texto: 'Os dois', proxima: 'computador-periferico-orientacao' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'computador-periferico-orientacao' }
            ]
        },
        'computador-periferico-orientacao': {
            tipo: 'pergunta',
            mensagem: 'Verifique se o equipamento está conectado corretamente. Se ele for sem fio, confirme também se está ligado. Depois, teste novamente.\n\nVoltou a funcionar?',
            procedimentos: ['Verificação da conexão do mouse ou teclado', 'Verificação do botão de ligar em equipamento sem fio'],
            registrarResposta: 'Equipamento voltou a funcionar',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'computador-periferico-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'computador-periferico-encaminhar' }
            ]
        },
        'computador-periferico-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica deverá verificar o equipamento e poderá fazer a troca se houver outro disponível.', botao: 'Abrir chamado' },
        'computador-programa-confirmar': {
            tipo: 'pergunta', mensagem: 'O problema acontece apenas em um programa específico?', registrarResposta: 'Problema acontece em um programa específico',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'computador-programa-dados' },
                { id: 'nao', texto: 'Não', proxima: 'computador-programa-dados' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'computador-programa-dados' }
            ]
        },
        'computador-programa-dados': {
            tipo: 'entrada', mensagem: 'A equipe técnica local precisa verificar esse caso. Informe o nome do programa e, se quiser, uma descrição curta do erro.',
            campos: [
                { id: 'programa', rotulo: 'Qual programa você está tentando acessar?', obrigatorio: true },
                { id: 'erroPrograma', rotulo: 'O que aparece na tela? (opcional)', obrigatorio: false }
            ],
            proxima: 'computador-programa-encaminhar'
        },
        'computador-programa-encaminhar': { tipo: 'encaminhamento', mensagem: 'As informações estão prontas para a equipe técnica.', botao: 'Abrir chamado' },

        'monitor-problema': {
            tipo: 'pergunta', mensagem: 'O que está acontecendo com o monitor?',
            opcoes: [
                { id: 'nao-funciona', texto: 'Monitor não está funcionando', problema: 'Monitor não está funcionando', proxima: 'monitor-luz' },
                { id: 'disponibilidade', texto: 'Solicitar outro monitor', problema: 'Verificar disponibilidade de outro monitor', proxima: 'monitor-disponibilidade' },
                { id: 'outro', texto: 'Outro problema com o monitor', problema: 'Outro problema com o monitor', proxima: 'monitor-outro-descricao' }
            ]
        },
        'monitor-luz': {
            tipo: 'pergunta', mensagem: 'O monitor possui alguma luz acesa?', registrarResposta: 'Monitor possui alguma luz acesa',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'monitor-cabo-video' },
                { id: 'nao', texto: 'Não', proxima: 'monitor-energia' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'monitor-energia' }
            ]
        },
        'monitor-energia': {
            tipo: 'pergunta',
            mensagem: 'Verifique se o cabo de energia está bem conectado ao monitor e à tomada. Depois, pressione o botão de ligar.\n\nFuncionou?',
            procedimentos: ['Verificação da alimentação e do botão de ligar do monitor'], registrarResposta: 'Monitor ligou após verificar a energia',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'monitor-cabo-video' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'monitor-cabo-video' }
            ]
        },
        'monitor-cabo-video': {
            tipo: 'pergunta',
            mensagem: 'Agora verifique o cabo que liga o monitor ao computador. Pode ser aquele conector azul, usado em monitores mais antigos, ou um cabo HDMI. Veja se está bem conectado nas duas pontas.\n\nO cabo parece estar bem conectado?',
            procedimentos: ['Verificação do cabo que liga o monitor ao computador'], registrarResposta: 'Cabo do monitor está bem conectado',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'monitor-mau-contato' },
                { id: 'solto', texto: 'Não, estava solto', proxima: 'monitor-reconectar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'monitor-mau-contato' }
            ]
        },
        'monitor-reconectar': {
            tipo: 'pergunta', mensagem: 'Conecte o cabo novamente, sem forçar. A imagem voltou?',
            procedimentos: ['Reconexão cuidadosa do cabo do monitor'], registrarResposta: 'Imagem voltou após reconectar o cabo',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'monitor-mau-contato' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'monitor-mau-contato' }
            ]
        },
        'monitor-mau-contato': {
            tipo: 'pergunta',
            mensagem: 'Sem forçar, mexa levemente no cabo perto do conector para verificar se há mau contato. A imagem voltou?',
            procedimentos: ['Verificação simples de mau contato, sem forçar o cabo'], registrarResposta: 'Imagem voltou após verificar mau contato',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'monitor-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'monitor-encaminhar' }
            ]
        },
        'monitor-encaminhar': { tipo: 'encaminhamento', mensagem: 'O monitor precisa ser verificado pela equipe técnica local.', botao: 'Abrir chamado' },
        'monitor-disponibilidade': { tipo: 'encaminhamento', mensagem: 'A equipe técnica precisa verificar se existe outro monitor disponível para troca.', botao: 'Abrir chamado' },
        'monitor-outro-descricao': {
            tipo: 'entrada', mensagem: 'Descreva brevemente o que está acontecendo com o monitor.',
            campos: [{ id: 'descricaoAdicional', rotulo: 'Descrição do problema', obrigatorio: true }], proxima: 'monitor-outro-encaminhar'
        },
        'monitor-outro-encaminhar': { tipo: 'encaminhamento', mensagem: 'A descrição está pronta para a equipe técnica.', botao: 'Abrir chamado' },

        'impressora-problema': {
            tipo: 'pergunta', mensagem: 'O que está acontecendo com a impressora?',
            opcoes: [
                { id: 'nao-imprime', texto: 'Impressora não imprime', problema: 'Impressora não imprime', proxima: 'impressora-ligada' },
                { id: 'papel-atolado', texto: 'Papel atolado', problema: 'Papel atolado', proxima: 'impressora-papel-visivel' },
                { id: 'impressao-torta', texto: 'Impressão torta ou mal formatada', problema: 'Impressão torta ou mal formatada', proxima: 'impressora-torta-confirmar' },
                { id: 'nao-aparece', texto: 'Impressora não aparece na lista', problema: 'Impressora não aparece para imprimir', proxima: 'impressora-lista-confirmar' }
            ]
        },
        'impressora-ligada': {
            tipo: 'pergunta', mensagem: 'A impressora está ligada?', registrarResposta: 'Impressora está ligada',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'impressora-mensagem-erro' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-ligar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-mensagem-erro' }
            ]
        },
        'impressora-ligar': {
            tipo: 'pergunta', mensagem: 'Ligue a impressora e tente imprimir novamente. Funcionou?',
            procedimentos: ['A impressora foi ligada e testada novamente'], registrarResposta: 'Impressora funcionou após ser ligada',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-mensagem-erro' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-mensagem-erro' }
            ]
        },
        'impressora-mensagem-erro': {
            tipo: 'pergunta', mensagem: 'Existe alguma mensagem de erro visível na impressora ou no computador?', registrarResposta: 'Existe mensagem de erro visível',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'impressora-reiniciar' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-reiniciar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-reiniciar' }
            ]
        },
        'impressora-reiniciar': {
            tipo: 'pergunta', mensagem: 'Desligue a impressora, aguarde alguns segundos e ligue novamente. Depois, faça um novo teste. A impressão voltou a funcionar?',
            procedimentos: ['Reinicialização simples da impressora'], registrarResposta: 'Resultado após reiniciar a impressora',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-encaminhar' }
            ]
        },
        'impressora-encaminhar': { tipo: 'encaminhamento', mensagem: 'A impressora precisa ser verificada pela equipe técnica.', botao: 'Abrir chamado' },
        'impressora-papel-visivel': {
            tipo: 'pergunta', mensagem: 'Existe algum papel visivelmente preso na entrada ou saída da impressora?', registrarResposta: 'Papel está visível e acessível',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'impressora-retirar-papel' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-papel-interno' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-papel-interno' }
            ]
        },
        'impressora-retirar-papel': {
            tipo: 'pergunta', mensagem: 'Se o papel estiver fácil de alcançar, retire devagar e sem forçar. Não desmonte a impressora. O problema foi resolvido?',
            procedimentos: ['Retirada cuidadosa do papel acessível, sem desmontar a impressora'], registrarResposta: 'Papel foi retirado e o problema foi resolvido',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-papel-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-papel-encaminhar' }
            ]
        },
        'impressora-papel-interno': { tipo: 'encaminhamento', mensagem: 'Não tente desmontar nem forçar a impressora. A equipe técnica fará a verificação com segurança.', botao: 'Abrir chamado' },
        'impressora-papel-encaminhar': { tipo: 'encaminhamento', mensagem: 'Como o papel continua preso, a equipe técnica precisa verificar a impressora.', botao: 'Abrir chamado' },
        'impressora-torta-confirmar': {
            tipo: 'pergunta', mensagem: 'O documento aparece correto na tela, mas sai errado quando é impresso?', registrarResposta: 'Documento aparece correto na tela',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'impressora-torta-encaminhar' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-torta-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-torta-encaminhar' }
            ]
        },
        'impressora-torta-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica poderá verificar a configuração e o resultado da impressão.', botao: 'Abrir chamado' },
        'impressora-lista-confirmar': {
            tipo: 'pergunta', mensagem: 'A impressora que você costuma usar aparece na lista?', registrarResposta: 'Impressora aparece na lista',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'impressora-lista-encaminhar' },
                { id: 'nao', texto: 'Não', proxima: 'impressora-lista-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'impressora-lista-encaminhar' }
            ]
        },
        'impressora-lista-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica deverá verificar a configuração da impressora.', botao: 'Abrir chamado' },

        'rede-problema': {
            tipo: 'pergunta', mensagem: 'O que está acontecendo com a rede ou internet?',
            opcoes: [
                { id: 'sem-internet', texto: 'Estou sem internet', problema: 'Sem acesso à internet', proxima: 'rede-tipo-conexao' },
                { id: 'sem-intranet', texto: 'Estou sem acesso à Intranet', problema: 'Sem acesso à Intranet', proxima: 'rede-intranet-confirmar' },
                { id: 'pasta-secao', texto: 'Acessar pasta da minha seção', problema: 'Acesso à pasta da seção', proxima: 'rede-pasta-orientacao' }
            ]
        },
        'rede-tipo-conexao': {
            tipo: 'pergunta', mensagem: 'Você usa a internet nesse computador por cabo ou Wi-Fi?', registrarResposta: 'Tipo de conexão usada',
            opcoes: [
                { id: 'cabo', texto: 'Cabo', proxima: 'rede-cabo' },
                { id: 'wifi', texto: 'Wi-Fi', proxima: 'rede-wifi' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'rede-encaminhar' }
            ]
        },
        'rede-cabo': {
            tipo: 'pergunta', mensagem: 'Verifique se o cabo de rede está bem conectado ao computador. Você pode retirar e conectar novamente. A internet voltou?',
            procedimentos: ['Verificação e reconexão do cabo de rede'], registrarResposta: 'Internet voltou após reconectar o cabo',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'rede-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'rede-encaminhar' }
            ]
        },
        'rede-wifi': {
            tipo: 'pergunta', mensagem: 'Verifique se o computador está conectado à rede Wi-Fi correta da unidade. Está conectado à rede correta?',
            procedimentos: ['Verificação da rede Wi-Fi selecionada'], registrarResposta: 'Computador está na rede Wi-Fi correta',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'rede-encaminhar' },
                { id: 'nao', texto: 'Não', proxima: 'rede-wifi-testar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'rede-encaminhar' }
            ]
        },
        'rede-wifi-testar': {
            tipo: 'pergunta', mensagem: 'Conecte à rede correta da unidade e teste novamente. A internet voltou?',
            procedimentos: ['Conexão à rede Wi-Fi correta da unidade'], registrarResposta: 'Internet voltou na rede correta',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não', proxima: 'rede-encaminhar' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'rede-encaminhar' }
            ]
        },
        'rede-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica precisa verificar a conexão do computador.', botao: 'Abrir chamado' },
        'rede-intranet-confirmar': {
            tipo: 'pergunta', mensagem: 'Os outros sites abrem normalmente?', registrarResposta: 'Outros sites abrem normalmente',
            opcoes: [
                { id: 'sim', texto: 'Sim', proxima: 'rede-intranet-encaminhar' },
                { id: 'nao', texto: 'Não', proxima: 'rede-tipo-conexao' },
                { id: 'nao-sei', texto: 'Não sei', proxima: 'rede-tipo-conexao' }
            ]
        },
        'rede-intranet-encaminhar': { tipo: 'encaminhamento', mensagem: 'Provavelmente será necessário configurar ou verificar o acesso à Intranet.', botao: 'Abrir chamado' },
        'rede-pasta-orientacao': {
            tipo: 'entrada',
            mensagem: 'Esse acesso precisa ser configurado pela equipe técnica da unidade. Isso normalmente acontece quando alguém chega à unidade ou muda de setor. Informe o CPF da pessoa que precisa do acesso.',
            campos: [{ id: 'cpf', rotulo: 'CPF (somente números)', obrigatorio: true, formato: 'cpf' }], proxima: 'rede-pasta-encaminhar'
        },
        'rede-pasta-encaminhar': { tipo: 'encaminhamento', mensagem: 'O CPF será enviado de forma segura junto às informações do chamado.', botao: 'Abrir chamado', cancelar: true },

        'email-problema': {
            tipo: 'pergunta', mensagem: 'O que está acontecendo com o e-mail?',
            opcoes: [
                { id: 'resetar-senha', texto: 'Trocar ou redefinir senha', problema: 'Trocar ou resetar a senha do e-mail', proxima: 'email-senha-orientacao' },
                { id: 'email-secao', texto: 'Acessar e-mail da seção', problema: 'Acesso ao e-mail da seção', proxima: 'email-secao-orientacao' }
            ]
        },
        'email-senha-orientacao': {
            tipo: 'pergunta',
            mensagem: '1. Acesse a Intranet da Corporação.\n2. Abra o ChatBotMike disponível nela.\n3. Informe seu CPF.\n4. Escolha “Resetar senha”.\n5. Siga as orientações.\n6. Você receberá um código para redefinir a senha.\n7. Informe o código e escolha a nova senha.\n\nConseguiu redefinir sua senha?',
            procedimentos: ['Orientação para redefinição de senha pelo ChatBotMike da Intranet'], registrarResposta: 'Conseguiu redefinir a senha',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não consegui', proxima: 'email-senha-encaminhar' }
            ]
        },
        'email-senha-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica local poderá ajudar com a redefinição da senha.', botao: 'Abrir chamado' },
        'email-secao-orientacao': {
            tipo: 'pergunta',
            mensagem: '1. Acesse a Intranet da Corporação.\n2. Abra o ChatBotMike.\n3. Informe seu CPF.\n4. Selecione “iNotes”.\n5. Selecione “Acesso ao e-mail de seção”.\n6. Siga o procedimento solicitado.\n\nConseguiu realizar a solicitação?',
            procedimentos: ['Orientação para solicitar acesso ao e-mail da seção pelo ChatBotMike da Intranet'], registrarResposta: 'Conseguiu solicitar acesso ao e-mail da seção',
            opcoes: [
                { id: 'resolvido', texto: 'Sim, resolvido', proxima: 'resolvido' },
                { id: 'nao', texto: 'Não consegui', proxima: 'email-secao-encaminhar' }
            ]
        },
        'email-secao-encaminhar': { tipo: 'encaminhamento', mensagem: 'A equipe técnica local poderá ajudar com o acesso ao e-mail da seção.', botao: 'Abrir chamado' },

        'outro-descricao': {
            tipo: 'entrada', mensagem: 'Descreva brevemente o problema para que a equipe técnica receba as informações corretas.',
            campos: [{ id: 'descricaoAdicional', rotulo: 'O que está acontecendo?', obrigatorio: true }],
            problema: 'Outro problema', proxima: 'outro-encaminhar'
        },
        'outro-encaminhar': { tipo: 'encaminhamento', mensagem: 'A descrição está pronta para a equipe técnica.', botao: 'Abrir chamado' },

        resolvido: {
            tipo: 'resolvido',
            mensagem: 'Problema resolvido. Se precisar de ajuda com outra situação, posso iniciar um novo atendimento.'
        }
    };

    function criarEstadoInicial() {
        return {
            etapaAtual: 'inicio', categoria: null, categoriaNome: null, problema: null,
            respostas: [], procedimentos: [], dados: {},
            conversa: etapas.inicio.mensagensIniciais.map((texto) => ({ autor: 'mike', texto }))
        };
    }

    function copiarEstado(estado) {
        return JSON.parse(JSON.stringify(estado));
    }

    function obterEtapa(estado) {
        return etapas[estado.etapaAtual];
    }

    function listarEtapas() {
        return copiarEstado(etapas);
    }

    function obterOpcao(estado, opcaoId) {
        const etapa = obterEtapa(estado);
        return etapa?.opcoes?.find((opcao) => opcao.id === opcaoId) || null;
    }

    function adicionarProcedimentos(estado, procedimentos) {
        (procedimentos || []).forEach((procedimento) => {
            if (!estado.procedimentos.includes(procedimento)) estado.procedimentos.push(procedimento);
        });
    }

    function avancarComOpcao(estadoAtual, opcaoId) {
        const etapa = obterEtapa(estadoAtual);
        const opcao = obterOpcao(estadoAtual, opcaoId);
        if (!etapa || !opcao) throw new Error('Opção de triagem inválida.');

        const estado = copiarEstado(estadoAtual);
        if (opcao.categoria) {
            estado.categoria = opcao.categoria;
            estado.categoriaNome = opcao.categoriaNome;
        }
        if (opcao.problema) estado.problema = opcao.problema;
        if (etapa.registrarResposta) {
            estado.respostas.push({ pergunta: etapa.registrarResposta, resposta: opcao.texto });
        }
        adicionarProcedimentos(estado, etapa.procedimentos);
        estado.conversa.push({ autor: 'usuario', texto: opcao.texto });
        estado.etapaAtual = opcao.proxima;
        estado.conversa.push({ autor: 'mike', texto: etapas[opcao.proxima].mensagem });
        return estado;
    }

    function avancarComDados(estadoAtual, valores) {
        const etapa = obterEtapa(estadoAtual);
        if (!etapa || etapa.tipo !== 'entrada') throw new Error('Esta etapa não aceita informações digitadas.');

        const estado = copiarEstado(estadoAtual);
        etapa.campos.forEach((campo) => {
            const valor = String(valores[campo.id] || '').trim();
            if (campo.obrigatorio && !valor) throw new Error(`Preencha: ${campo.rotulo}`);
            if (campo.formato === 'cpf' && !/^\d{11}$/.test(valor)) throw new Error('Informe um CPF com 11 números.');
            if (valor) estado.dados[campo.id] = valor;
        });
        if (etapa.problema) estado.problema = etapa.problema;
        estado.conversa.push({ autor: 'usuario', texto: etapa.campos.some((campo) => campo.formato === 'cpf')
            ? 'CPF informado.' : 'Informações preenchidas.' });
        estado.etapaAtual = etapa.proxima;
        estado.conversa.push({ autor: 'mike', texto: etapas[etapa.proxima].mensagem });
        return estado;
    }

    function montarResumo(estado, resolvido) {
        const respostas = estado.respostas.length
            ? estado.respostas.map((item) => `- ${item.pergunta}: ${item.resposta}`).join('\n')
            : '- Nenhuma pergunta adicional foi necessária.';
        const procedimentos = estado.procedimentos.length
            ? estado.procedimentos.map((item) => `- ${item}`).join('\n')
            : '- Nenhum procedimento foi orientado antes do encaminhamento.';
        const dadosAdicionais = [];
        if (estado.dados.programa) dadosAdicionais.push(`Programa informado: ${estado.dados.programa}`);
        if (estado.dados.erroPrograma) dadosAdicionais.push(`Descrição do erro: ${estado.dados.erroPrograma}`);
        if (estado.dados.descricaoAdicional) dadosAdicionais.push(`Descrição adicional: ${estado.dados.descricaoAdicional}`);
        return [
            'TRIAGEM MIKE IA', '', `Categoria: ${estado.categoriaNome}`, `Problema: ${estado.problema}`,
            '', 'Respostas:', respostas, '', 'Procedimentos orientados:', procedimentos,
            ...(dadosAdicionais.length ? ['', 'Informações adicionais:', ...dadosAdicionais] : []),
            '', `Resultado: ${resolvido ? 'Problema resolvido.' : 'Problema não resolvido.'}`,
            `Encaminhamento: ${resolvido ? 'Atendimento encerrado sem chamado.' : 'Equipe técnica local.'}`
        ].join('\n');
    }

    function restaurarPeloAtendimento(atendimento) {
        let estado = criarEstadoInicial();
        const opcaoCategoria = etapas.inicio.opcoes.find((opcao) => opcao.categoria === atendimento.categoria);
        if (!opcaoCategoria) return estado;
        estado = avancarComOpcao(estado, opcaoCategoria.id);
        const etapaProblema = obterEtapa(estado);
        const opcaoProblema = etapaProblema.opcoes?.find((opcao) => opcao.problema === atendimento.descricaoProblema);
        return opcaoProblema ? avancarComOpcao(estado, opcaoProblema.id) : estado;
    }

    const api = { criarEstadoInicial, obterEtapa, listarEtapas, obterOpcao, avancarComOpcao, avancarComDados, montarResumo, restaurarPeloAtendimento };
    escopoGlobal.FluxoTriagemMike = api;
    if (typeof module !== 'undefined' && module.exports) module.exports = api;
}(typeof window !== 'undefined' ? window : globalThis));
