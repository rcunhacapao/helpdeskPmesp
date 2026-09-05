# Helpdesk Telemática - PMESP

> Sistema em desenvolvimento para organizar os chamados de suporte de Telemática, acompanhar os atendimentos e gerar dados úteis para o setor.

## Sobre o projeto

Este projeto foi pensado para centralizar pedidos de suporte que normalmente chegam por telefone, mensagem ou conversa pessoal. A ideia é dar visibilidade ao que está pendente, definir o que deve ser atendido primeiro e manter um histórico confiável dos atendimentos.

O objetivo é criar uma solução simples para quem solicita ajuda e organizada para quem atende. Cada unidade poderá manter a sua própria instalação, com sua base de usuários e seus dados. O repositório servirá como uma base que outros batalhões poderão adaptar à própria realidade.

## O que o sistema busca resolver

| Situação | Como o Helpdesk ajuda |
| --- | --- |
| Pedidos em vários canais | Cada solicitação fica registrada em um único lugar. |
| Dificuldade para definir o próximo atendimento | A fila respeita a prioridade de cada chamado. |
| Histórico perdido | Chamados fechados e cancelados continuam disponíveis para consultas e relatórios. |
| Falta de dados para planejamento | Os registros poderão mostrar volume, tempo de atendimento e problemas recorrentes. |

## Estado atual

O backend e o frontend já estão integrados de verdade: a interface web consome a API real
(login, chamados, usuários e técnicos), sem dados simulados. As funcionalidades abaixo foram
validadas por testes automatizados e revisão de código.

### Funcionalidades disponíveis

- Cadastro de usuários com posto/graduação, nome e RE.
- Validação de RE: somente números, de 1 a 6 dígitos.
- Inativação de usuários sem apagar seu histórico.
- Cancelamento automático de chamados abertos quando o usuário é inativado.
- Abertura integrada de chamado: o primeiro relato já é registrado e recebe diagnóstico inicial.
- Categorias: computador, monitor, impressora, rede/internet, e-mail e outro.
- Prioridades: baixa, média, alta e urgente.
- Fluxo de status: em diagnóstico, aberto, em atendimento, fechado, cancelado e abandonado.
- Resolução registrada separadamente: pelo Mike IA ou por técnico.
- Histórico das orientações do Mike disponível no detalhe técnico do chamado encaminhado.
- Cancelamento de chamado aberto com motivo registrado.
- Fila de atendimento: urgentes primeiro, depois alta, média e baixa prioridade. Dentro da mesma prioridade, o chamado mais antigo vem antes.
- Consulta de chamados em atendimento, separada da fila de chamados abertos.
- Login com RE e senha, primeiro acesso com confirmação de e-mail funcional, e perfis de
  usuário comum/técnico (veja [Autenticação](#autenticação)).
- Banco H2 local para desenvolvimento e testes.

### Fluxo do chamado

```text
Usuário ativo -> relata o problema -> EM_DIAGNOSTICO
                                      |
                                      +-> FECHADO + resolvidoPor=MIKE_IA
                                      |
                                      `-> ABERTO -> EM_ATENDIMENTO -> FECHADO + resolvidoPor=TECNICO
                                            |
                                            `-> CANCELADO, quando necessário

EM_DIAGNOSTICO -> ABANDONADO, quando não houver interação por 24 horas
```

Regras importantes:

- Para usuário comum, um chamado novo começa como `EM_DIAGNOSTICO` depois do primeiro relato.
- Somente o encaminhamento não resolvido muda esse mesmo chamado para `ABERTO`.
- Técnico abre diretamente como `ABERTO`, inclusive quando registra em nome de outro RE.
- Somente chamados `ABERTOS` aparecem na fila.
- O técnico inicia o atendimento e muda o status para `EM_ATENDIMENTO`.
- Somente chamados em atendimento podem ser finalizados.
- Chamados não são apagados pela aplicação: o histórico será importante para os relatórios futuros.
- A posição numérica na fila para o solicitante ainda será implementada.

## Rotas disponíveis

Rotas marcadas como **pública** não exigem login. Todas as outras exigem uma sessão autenticada
(veja [Autenticação](#autenticação)); as marcadas como **técnico** só funcionam para quem tem
esse perfil.

| Método | Rota | Finalidade | Acesso |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | Autentica com RE e senha. | pública |
| `POST` | `/auth/primeiro-acesso` | Confirma RE + e-mail funcional e define a senha. | pública |
| `POST` | `/logout` | Encerra a sessão. | logado |
| `POST` | `/usuarios/cadastrar` | Cadastra um usuário. | técnico |
| `GET` | `/usuarios/buscar/{re}` | Busca usuário pelo RE. | técnico |
| `PUT` | `/usuarios/atualizar-dados/{re}` | Atualiza nome e posto/graduação. | técnico |
| `PATCH` | `/usuarios/inativar/{re}` | Inativa usuário e cancela seus chamados abertos. | técnico |
| `POST` | `/tecnicos/cadastrar` | Torna um usuário existente um técnico. | técnico |
| `GET` | `/tecnicos` | Lista todos os técnicos, disponíveis ou não. | técnico |
| `GET` | `/tecnicos/buscar/{re}` | Busca técnico pelo RE do usuário. | técnico |
| `GET` | `/tecnicos/disponiveis` | Lista técnicos disponíveis no momento. | técnico |
| `PATCH` | `/tecnicos/ficar-disponivel/{re}` | Marca o técnico como disponível. | técnico |
| `PATCH` | `/tecnicos/ficar-indisponivel/{re}` | Marca o técnico como indisponível. | técnico |
| `POST` | `/chamados/cadastrar` | Abre chamado completo, inclusive para outro RE. | técnico |
| `GET` | `/chamados/buscar/{id}` | Busca chamado pelo identificador (usuário comum só vê os próprios). | logado |
| `GET` | `/chamados/meus` | Lista os chamados do usuário autenticado. | logado |
| `GET` | `/chamados/fila` | Mostra a fila de chamados abertos. | técnico |
| `GET` | `/chamados/em-atendimento` | Mostra os chamados que já estão sendo atendidos. | técnico |
| `PUT` | `/chamados/atualizar-dados/{id}?prioridade=ALTA` | Altera a prioridade de um chamado aberto. | técnico |
| `PATCH` | `/chamados/iniciar-atendimento/{id}?reTecnico={re}` | Técnico assume e inicia o atendimento. | técnico |
| `PATCH` | `/chamados/transferir-responsavel/{id}?reTecnico={re}` | Transfere o chamado para outro técnico. | técnico |
| `PATCH` | `/chamados/finalizar/{id}` | Finaliza um chamado em atendimento. | técnico |
| `PATCH` | `/chamados/cancelar/{id}?motivoCancelamento=OUTRO` | Cancela um chamado aberto (usuário comum só o próprio). | logado |
| `POST` | `/mike-ia/iniciar` | Cria o chamado em diagnóstico e devolve as orientações iniciais. | usuário comum |
| `GET` | `/mike-ia/em-diagnostico` | Recupera o diagnóstico em andamento após recarregar a página. | usuário comum |
| `PATCH` | `/mike-ia/concluir/{chamadoId}` | Fecha o mesmo chamado como resolvido pelo Mike IA. | usuário comum |
| `PATCH` | `/mike-ia/encaminhar/{chamadoId}` | Completa e encaminha o mesmo chamado para a fila técnica. | usuário comum |
| `GET` | `/mike-ia/chamado/{chamadoId}` | Consulta o histórico do Mike no detalhe técnico do chamado. | técnico |
| `GET` | `/mike-ia/metricas` | Consulta totais e taxa inicial de resolução automática. | técnico |

## Autenticação

O técnico cadastra o usuário previamente (RE, nome, posto/graduação e e-mail funcional
`@policiamilitar.sp.gov.br`). Depois disso:

1. O usuário chama `POST /auth/primeiro-acesso` com RE, e-mail funcional e a senha desejada.
2. A partir daí, ele loga com `POST /auth/login` (RE + senha).
3. O login fica valendo por sessão (cookie), por até 2 horas.

Existem dois perfis: **usuário comum** (só acessa os próprios chamados) e **técnico** (acessa
tudo). Um usuário vira técnico quando alguém já técnico chama `POST /tecnicos/cadastrar` para
o RE dele. Para o primeiro técnico do sistema (antes de existir qualquer técnico), configure as
variáveis de ambiente `BOOTSTRAP_TECNICO_RE`, `BOOTSTRAP_TECNICO_NOME`, `BOOTSTRAP_TECNICO_EMAIL`
e `BOOTSTRAP_TECNICO_SENHA` — a aplicação cria esse técnico automaticamente na primeira vez que
subir.

### Exemplos para teste

Cadastro de usuário:

```json
{
  "postoGraduacao": "SD PM",
  "nome": "Nome de teste",
  "re": "123456",
  "email": "nome.teste@policiamilitar.sp.gov.br"
}
```

Cadastro de chamado:

```json
{
  "re": "123456",
  "descricao": "Monitor não apresenta imagem.",
  "categoria": "MONITOR",
  "localAtendimento": "Sala do P1",
  "prioridade": "MEDIA"
}
```

## Tecnologias utilizadas

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- H2 Database
- Lombok
- Maven
- Postman para testes manuais
- Git e GitHub

## Como executar localmente

1. Configure os dados locais do banco no arquivo `.env`.
2. Abra o projeto em uma IDE Java, como o IntelliJ IDEA.
3. Execute a classe principal da aplicação.
4. Use o Postman para testar as rotas em `http://localhost:8080`.

Exemplo de estrutura do `.env`:

```env
DATABASE_URL=jdbc:h2:./Data/helpdesk
DATABASE_USERNAME=seu_usuario_local
DATABASE_PASSWORD=sua_senha_local
```

> O arquivo `.env`, dados reais e o arquivo do banco não devem ser enviados ao GitHub.

Para acessar o console do H2 (`/h2-console`) em desenvolvimento, ative o profile `dev` ao rodar a aplicação (ex.: `--spring.profiles.active=dev` ou a variável de ambiente `SPRING_PROFILES_ACTIVE=dev`). Por padrão o console fica desligado.

### Primeiro técnico (bootstrap)

Como só um técnico pode cadastrar usuários e técnicos, a aplicação cria automaticamente o
primeiro técnico ao subir, **somente se ainda não existir nenhum** e estas variáveis de
ambiente estiverem definidas:

```env
BOOTSTRAP_TECNICO_RE=100001
BOOTSTRAP_TECNICO_NOME=Fulano de Tal
BOOTSTRAP_TECNICO_EMAIL=fulano.detal@policiamilitar.sp.gov.br
BOOTSTRAP_TECNICO_SENHA=uma-senha-forte
BOOTSTRAP_TECNICO_POSTO=SGT_3
```

Com isso, já é possível logar em `POST /auth/login` com esse RE e senha e usar a tela de
Gestão de usuários para cadastrar o restante da equipe. Usuários cadastrados por um técnico
ainda precisam completar o primeiro acesso (`POST /auth/primeiro-acesso`) para poderem logar.

### Frontend

O frontend (`src/main/resources/static`) é servido pela própria aplicação Spring, na mesma
origem — acesse `http://localhost:8080/` no navegador. Ele já está conectado à API real
(login, chamados, usuários e técnicos); não usa mais dados simulados.

## Próximas etapas

1. ~~Melhorar as mensagens de erro da API.~~ ✅ concluído.
2. ~~Criar testes automatizados para as regras principais.~~ ✅ concluído.
3. Criar filtros de chamados por status, prioridade, categoria e período.
4. Implementar a posição do solicitante na fila.
5. ~~Registrar solução ou observação ao finalizar um chamado.~~ ✅ concluído.
6. ~~Conectar o frontend (hoje um protótipo visual) à API real.~~ ✅ concluído.
7. ~~Criar login com primeiro acesso por RE e senha própria do Helpdesk.~~ ✅ concluído.
8. Migrar o banco de H2 para PostgreSQL e preparar a aplicação para Docker.
9. Criar relatórios de volume, tempo médio de atendimento, categorias mais frequentes e chamados por usuário/local. Os dados de diagnóstico, resolução pelo Mike e abandono já ficam registrados para essa etapa.
10. Permitir alterar a prioridade de um chamado diretamente na tela de fila do técnico (o endpoint já existe: `PUT /chamados/atualizar-dados/{id}`).
11. Registrar código de confirmação por e-mail no primeiro acesso e recuperação de senha (a estrutura já foi pensada para isso, veja [Autenticação](#autenticação)).

## Visão futura

O planejamento pode evoluir conforme os testes e as necessidades do setor, mas a direção atual do projeto inclui:

- Tela para abertura e acompanhamento de chamados.
- Painel do técnico com fila, chamados em atendimento, finalizados e filtros.
- Indicadores para ajudar o setor a entender volume de trabalho, tempo médio e problemas mais frequentes.
- Instalação independente para outras unidades.
- **Mike IA** integrado à abertura do chamado, com motor inicial de regras e estrutura preparada para evoluir para base aprovada, RAG ou LLM.

## Identidade visual planejada

| Elemento | Cor |
| --- | --- |
| Menu lateral e navegação | `#1A1D20` |
| Fundo principal | `#F8F9FA` |
| Cards | `#FFFFFF` |
| Ações principais | `#8B0000` |
| Alertas e urgências | `#DC3545` |

---

Projeto pessoal de estudo e portfólio, desenvolvido de forma gradual para representar uma solução real para a rotina de Telemática.
