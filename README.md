# Helpdesk Telemática — 37º BPM/M

Sistema em desenvolvimento para organizar os chamados de suporte de Telemática nos batalhões. A proposta é centralizar pedidos que hoje podem chegar por telefone, mensagem ou pessoalmente, deixando claro **quem pediu ajuda, onde está o problema, qual a prioridade e em que etapa está o atendimento**.

Mais do que registrar chamados, a ideia é gerar informação útil para o setor: entender os problemas mais recorrentes, acompanhar a quantidade de atendimentos e ter dados reais para planejar melhorias.

> Projeto pessoal de estudo e portfólio, construído em Java com Spring Boot. O código está sendo desenvolvido de forma gradual, com comentários simples e decisões registradas para facilitar a manutenção e o aprendizado.

## Sumário

- [Por que este sistema está sendo criado?](#por-que-este-sistema-está-sendo-criado)
- [O que o sistema deve ajudar a controlar](#o-que-o-sistema-deve-ajudar-a-controlar)
- [Situação atual do projeto](#situação-atual-do-projeto)
- [Como funciona hoje](#como-funciona-hoje)
- [Regras importantes já definidas](#regras-importantes-já-definidas)
- [Rotas disponíveis na API](#rotas-disponíveis-na-api)
- [Como executar localmente](#como-executar-localmente)
- [Evolução planejada](#evolução-planejada)
- [Indicadores e relatórios planejados](#indicadores-e-relatórios-planejados)
- [Tecnologias utilizadas](#tecnologias-utilizadas)

## Por que este sistema está sendo criado?

Em um setor de Telemática, diversos problemas podem aparecer durante o dia: computador sem acesso à rede, monitor sem imagem, dificuldade para entrar em algum sistema, falha de impressora, necessidade de acesso a e-mail de seção e muitos outros.

Quando esses pedidos não ficam centralizados, é fácil perder o controle. Pode ficar difícil saber quais atendimentos estão pendentes, há quanto tempo um chamado está aguardando, quais locais precisam de mais suporte e quais problemas acontecem com maior frequência.

Este projeto foi pensado para dar uma visão organizada desse trabalho. A intenção é que o usuário consiga abrir um chamado de maneira simples e que o setor responsável consiga acompanhar o atendimento com mais clareza.

## O que o sistema deve ajudar a controlar

Quando estiver completo, o sistema deverá permitir acompanhar pontos como:

| Informação | Como isso pode ajudar o setor |
| --- | --- |
| Quantidade total de chamados | Entender o volume de trabalho por período. |
| Chamados abertos, em atendimento, fechados e cancelados | Saber a situação real da demanda. |
| Tempo médio de atendimento | Identificar quanto tempo os atendimentos costumam levar. |
| Chamados por usuário, unidade ou local | Encontrar locais e usuários que precisam de mais apoio. |
| Tipos de chamado mais frequentes | Descobrir os problemas mais repetidos para buscar uma solução definitiva. |
| Prioridades mais comuns | Entender se há muitas ocorrências urgentes e onde agir preventivamente. |
| Motivos de cancelamento | Ver se o problema foi resolvido no local, se deixou de ser necessário ou se houve duplicidade. |

Esses dados não servem apenas para gerar números. Eles podem ajudar a justificar compra de equipamentos, reforço de infraestrutura, criação de orientações internas e prevenção de falhas que se repetem.

## Situação atual do projeto

O projeto está na fase inicial da API, ou seja, a parte que recebe e organiza os dados. Ainda não existe uma tela pronta para o usuário final, login, fila visível ou painel de relatórios.

Mesmo assim, as regras principais de usuário e chamado já estão sendo montadas para que as próximas partes sejam criadas sobre uma base organizada.

### O que já está pronto

| Área | O que já foi implementado |
| --- | --- |
| Usuários | Cadastro com posto/graduação, nome e RE. |
| Validação de RE | Aceita somente números de 1 a 6 dígitos, sem traços ou dígito verificador. |
| Inativação | O usuário não é apagado pela aplicação; ele pode ser inativado para preservar o histórico. |
| Chamados | Cadastro com solicitante, descrição, local de atendimento e prioridade. |
| Prioridades | `BAIXA`, `MEDIA`, `ALTA` e `URGENTE`. |
| Status | `ABERTO`, `EM_ATENDIMENTO`, `FECHADO` e `CANCELADO`. |
| Atendimento | Um chamado pode ser iniciado e finalizado pelo sistema. |
| Cancelamento | O solicitante pode cancelar um chamado ainda aberto usando um motivo definido. |
| Histórico | Chamados não são excluídos, pois serão necessários para relatórios futuros. |
| Banco de dados | Banco H2 local para a fase de desenvolvimento. |

## Como funciona hoje

O fluxo atual de um chamado é o seguinte:

```text
Usuário é cadastrado
        ↓
Usuário abre um chamado
        ↓
Status inicial: ABERTO
        ↓
Técnico inicia o atendimento
        ↓
Status: EM_ATENDIMENTO
        ↓
Técnico finaliza o atendimento
        ↓
Status: FECHADO
```

Enquanto o chamado estiver `ABERTO`, o próprio solicitante pode cancelá-lo se não precisar mais do suporte. Um chamado cancelado não é apagado: ele continua registrado para manter o histórico correto.

## Regras importantes já definidas

### Usuários

- O RE é único e deve conter somente números, com no máximo seis dígitos.
- Usuários não devem ser excluídos pela aplicação, pois podem possuir chamados antigos vinculados ao seu cadastro.
- Ao inativar um usuário, os chamados que ainda estiverem `ABERTO` serão cancelados automaticamente.
- Chamados em `EM_ATENDIMENTO` ou `FECHADO` não são alterados ao inativar o usuário, pois fazem parte do histórico de trabalho já iniciado ou concluído.

### Chamados

- Todo chamado novo começa com o status `ABERTO`.
- O fluxo normal é: `ABERTO` → `EM_ATENDIMENTO` → `FECHADO`.
- Chamados não serão excluídos nem “inativados”. O histórico precisa continuar disponível para consultas e relatórios.
- O cancelamento é permitido somente para chamado `ABERTO`.
- Os motivos que o usuário pode escolher para cancelar são:
  - `RESOLVIDO_NO_LOCAL`
  - `NAO_HA_MAIS_NECESSIDADE`
  - `CHAMADO_DUPLICADO`
  - `OUTRO`
- O motivo `USUARIO_INATIVADO` é usado somente pelo sistema quando um usuário é inativado. Ele não aparece como opção para o usuário escolher.

### Fila por prioridade

A fila ainda será implementada, mas suas regras já foram definidas:

1. A fila considerará apenas chamados com status `ABERTO`.
2. Chamados em `EM_ATENDIMENTO` já estão sendo tratados e não devem ocupar uma posição de espera.
3. A ordem de prioridade será: `URGENTE` → `ALTA` → `MEDIA` → `BAIXA`.
4. Entre chamados da mesma prioridade, será respeitada a ordem de abertura.
5. Ao abrir um chamado, o solicitante deverá ver sua posição na fila e quantos chamados estão à frente.

## Rotas disponíveis na API

Por enquanto, os testes podem ser feitos pelo Postman. A aplicação inicia, por padrão, em `http://localhost:8080`.

### Usuários

| Ação | Método e rota |
| --- | --- |
| Cadastrar usuário | `POST /usuarios/cadastrar` |
| Buscar por RE | `GET /usuarios/buscar/{re}` |
| Atualizar dados | `PUT /usuarios/atualizar-dados/{re}` |
| Inativar usuário | `PATCH /usuarios/inativar/{re}` |

Exemplo de cadastro de usuário:

```json
{
  "postoGraduacao": "CB",
  "nome": "João da Silva",
  "re": "123456"
}
```

### Chamados

| Ação | Método e rota |
| --- | --- |
| Cadastrar chamado | `POST /chamados/cadastrar` |
| Buscar por ID | `GET /chamados/buscar/{chamadoId}` |
| Atualizar prioridade | `PUT /chamados/atualizar-dados/{chamadoId}?prioridade=ALTA` |
| Iniciar atendimento | `PATCH /chamados/iniciar-atendimento/{chamadoId}` |
| Finalizar atendimento | `PATCH /chamados/finalizar/{chamadoId}` |
| Cancelar chamado | `PATCH /chamados/cancelar/{chamadoId}?motivoCancelamento=RESOLVIDO_NO_LOCAL` |

Exemplo de cadastro de chamado:

```json
{
  "re": "123456",
  "descricao": "O monitor não apresenta imagem.",
  "localAtendimento": "Sala do P1",
  "prioridade": "MEDIA"
}
```

> Os nomes enviados pela API são técnicos, como `MEDIA`, `ABERTO` e `RESOLVIDO_NO_LOCAL`. Quando o frontend for criado, o usuário verá textos mais amigáveis.

## Como executar localmente

### Pré-requisitos

- Java 26 ou versão compatível com a configuração do projeto.
- IntelliJ IDEA ou outra IDE para Java.
- Maven Wrapper, que já está incluído no repositório.

### Configuração do banco local

Antes de iniciar, configure as variáveis de ambiente da execução da aplicação. Caso use um arquivo `.env` com sua IDE, ele deve permanecer fora do GitHub.

Exemplo para o banco H2 usado nesta fase:

```properties
DATABASE_URL=jdbc:h2:./Data;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DATABASE_USERNAME=sa
DATABASE_PASSWORD=
```

Depois, execute a classe `Helpdesk37bpmmApplication` pela IDE ou use o Maven Wrapper:

```bash
.\mvnw.cmd spring-boot:run
```

Com a aplicação em execução, o console do H2 fica disponível em:

```text
http://localhost:8080/h2-console
```

No console, utilize a mesma URL, usuário e senha configurados para a aplicação. O arquivo `Data.mv.db` é local e está ignorado pelo Git.

## Evolução planejada

O objetivo é transformar este projeto em um Helpdesk completo, útil no dia a dia e forte como projeto de portfólio. As etapas abaixo ainda não estão prontas; elas fazem parte do planejamento do sistema.

### 1. Melhorar a experiência e as respostas da API

- Criar mensagens de erro claras para campos inválidos ou dados não encontrados.
- Substituir os retornos `null` atuais por respostas que deixem claro o que aconteceu.
- Validar campos obrigatórios antes de salvar os dados.
- Criar testes automáticos para garantir que regras importantes continuem funcionando após mudanças.
- Criar filtros e páginas de consulta para não carregar listas grandes de uma vez.

### 2. Criar a fila de atendimento

- Aplicar a ordem de prioridade já definida.
- Mostrar a posição de cada chamado na fila.
- Informar quantos chamados estão à frente do solicitante.
- Registrar qual técnico assumiu o atendimento.
- Guardar datas de abertura, início, finalização e cancelamento para permitir análises corretas.

### 3. Classificar os chamados

Para descobrir os problemas mais frequentes, os chamados precisarão de uma classificação. A ideia é incluir categorias como rede, hardware, impressora, monitor, sistemas, acesso, e-mail e outras que façam sentido para a rotina do setor.

Essa classificação permitirá responder perguntas como: “qual tipo de problema mais gerou atendimento neste mês?” ou “quais equipamentos apresentam mais falhas?”. As categorias serão definidas com cuidado em uma etapa futura, para não criar opções que não sejam úteis na prática.

### 4. Criar relatórios e painel de acompanhamento

- Total de chamados por período.
- Quantidade por status e prioridade.
- Quantidade por usuário, local de atendimento e categoria.
- Tipos de problema mais recorrentes.
- Tempo médio entre abertura e início do atendimento.
- Tempo médio entre início e finalização.
- Chamados cancelados e seus motivos.
- Visão de chamados pendentes para apoiar a organização da equipe.

### 5. Criar o frontend

- Tela de login e tela inicial simples.
- Formulário de abertura de chamado pensado para quem não tem familiaridade com sistemas.
- Exemplos nos campos, como `Ex.: Sala do P1` no local de atendimento.
- Tela para acompanhar o próprio chamado e sua posição na fila.
- Tela de atendimento para técnicos.
- Painel de relatórios para responsáveis pelo setor.
- Textos amigáveis no lugar dos nomes técnicos usados pela API.

### 6. Integrar inteligência artificial de forma útil

Antes de abrir um chamado, a pessoa poderá conversar com um assistente simples. A intenção não é substituir o atendimento humano, mas resolver situações conhecidas antes que elas virem um chamado.

Exemplos de ajuda que poderão ser oferecidos:

- Orientações para verificar cabo de energia e cabo de imagem quando o monitor não liga.
- Passo a passo para acessar o e-mail de seção ou algum sistema interno.
- Perguntas com botões, como “resolveu?”, “sim”, “não” ou outras opções adequadas ao caso.
- Abertura normal do chamado caso as orientações não resolvam o problema.

As orientações serão baseadas em informações controladas pelo setor, e as chaves de integração ficarão apenas no backend, nunca no navegador do usuário.

### 7. Preparar para uso real

- Migrar do H2 para PostgreSQL, banco mais adequado para uma aplicação em uso contínuo.
- Usar Flyway para registrar as alterações feitas na estrutura do banco ao longo do tempo.
- Criar autenticação e separar permissões de solicitante, técnico e administrador.
- Registrar ações importantes, como início, finalização, cancelamento e alterações de prioridade.
- Organizar logs para facilitar a identificação de problemas.
- Dockerizar a aplicação e o banco para facilitar a execução em outro computador ou servidor.
- Criar documentação de instalação, uso e manutenção.
- Configurar testes automáticos no GitHub a cada atualização do projeto.

## Indicadores e relatórios planejados

| Indicador | O que será possível entender |
| --- | --- |
| Chamados recebidos no mês | Volume de demanda do setor. |
| Chamados por categoria | Quais problemas precisam de mais atenção. |
| Chamados por local | Quais salas ou unidades apresentam mais ocorrências. |
| Chamados por usuário | Onde há maior necessidade de suporte ou orientação. |
| Tempo médio para iniciar atendimento | Quanto tempo, em média, um chamado fica aguardando. |
| Tempo médio para finalizar | Quanto tempo os atendimentos costumam durar. |
| Chamados por prioridade | Se os casos urgentes estão sendo tratados com a atenção esperada. |
| Taxa de cancelamento | Quantos chamados foram resolvidos antes do atendimento ou deixaram de ser necessários. |

## Tecnologias utilizadas

- **Java** — linguagem usada no projeto.
- **Spring Boot** — base da aplicação e das rotas da API.
- **Spring Data JPA** — ajuda a salvar e buscar informações no banco de dados.
- **H2 Database** — banco local usado durante o desenvolvimento inicial.
- **Lombok** — reduz código repetitivo nas classes.
- **Maven** — gerencia as bibliotecas do projeto.

## Transparência sobre a fase atual

Este é um projeto em desenvolvimento. A estrutura principal de usuários e chamados já existe, mas recursos importantes para uma versão final — como tela, autenticação, fila, categorias, relatórios, banco PostgreSQL e testes automáticos — ainda serão construídos.

Parte das validações ainda retorna `null` quando algo não atende às regras. Isso foi mantido nesta fase porque acompanha o conteúdo estudado até o momento. A melhoria para mensagens de erro específicas já está registrada como próxima evolução.

O objetivo é evoluir o sistema passo a passo, mantendo o código legível e entendendo cada decisão antes de avançar.
