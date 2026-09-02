# Helpdesk Telemática - PMESP

> Projeto em desenvolvimento para organizar os chamados de suporte de Telemática, facilitar o acompanhamento do atendimento e transformar os registros do dia a dia em informações úteis para o setor.

## Sobre o projeto

Este sistema nasceu para resolver uma situação comum em muitos batalhões: pedidos de suporte chegam por telefone, mensagem, conversa pessoal ou por vários canais diferentes. Com isso, fica difícil saber o que ainda está pendente, quem solicitou ajuda, qual problema deve ser atendido primeiro e quanto tempo os atendimentos estão levando.

A proposta é criar um Helpdesk simples de usar para quem pede suporte e organizado para quem atende. Cada unidade poderá ter a sua própria instalação, com os seus próprios dados e sua própria equipe de Telemática. O repositório servirá como uma base que outros batalhões poderão adaptar à sua realidade.

Além do Helpdesk, a visão de longo prazo é formar uma ferramenta de informática mais completa. Ela terá um módulo separado chamado **Mike IA**, voltado a dúvidas sobre procedimentos, documentos e normas. Esse módulo não substituirá a gestão de chamados: ele será uma área própria dentro da mesma plataforma.

## O que este sistema pretende melhorar

| Situação sem centralização | Como o sistema pode ajudar |
| --- | --- |
| Pedidos chegam por vários canais | Cada solicitação fica registrada em um único lugar. |
| Dificuldade para saber o que atender primeiro | A fila prioriza chamados urgentes e mantém ordem dentro de cada prioridade. |
| Histórico se perde com o tempo | Chamados fechados e cancelados continuam no banco para consultas futuras. |
| Falta de dados para planejar melhorias | Os registros poderão gerar relatórios de volume, tempo e problemas frequentes. |
| Atendimento depende de memória ou mensagens antigas | Técnico e solicitante poderão acompanhar o status pelo sistema. |

## Estado atual

O projeto está na fase de construção da API. A API é a parte que recebe os dados, aplica as regras e conversa com o banco de dados. As funcionalidades abaixo foram testadas manualmente no Postman.

Ainda não existe frontend, login, painel de técnico, painel de usuário ou relatório automático. Esses itens fazem parte das próximas etapas e estão descritos neste README para deixar claro o caminho do projeto.

### O que já funciona

| Área | Entrega atual |
| --- | --- |
| Cadastro de usuário | Registra posto/graduação, nome e RE. |
| Validação de RE | Aceita somente números, com 1 a 6 dígitos. |
| Usuário inativo | O usuário não é apagado pela aplicação, preservando o histórico. |
| Cadastro de chamado | Registra solicitante, descrição, categoria, local e prioridade. |
| Categorias | Computador, monitor, impressora, rede/internet, e-mail e outro. |
| Prioridades | Baixa, média, alta e urgente. |
| Status | Aberto, em atendimento, fechado e cancelado. |
| Cancelamento | Usuário pode cancelar chamado aberto com motivo predefinido. |
| Inativação automática | Ao inativar um usuário, seus chamados ainda abertos são cancelados com o motivo técnico `USUARIO_INATIVADO`. |
| Fila de atendimento | Mostra somente chamados abertos, na ordem: urgente, alta, média e baixa. Dentro da mesma prioridade, o mais antigo vem primeiro. |
| Organização do código | O projeto já está separado por área: controller, dto, mapper, model, repository, service e enums. |
| Banco de dados atual | H2 local, usado durante o desenvolvimento e os testes. |

## Como o chamado funciona hoje

```text
Usuário cadastrado e ativo
        |
        v
Abre um chamado
        |
        v
Status: ABERTO
        |
        +--> Pode ser cancelado pelo solicitante, com motivo
        |
        v
Técnico inicia o atendimento
        |
        v
Status: EM_ATENDIMENTO
        |
        v
Técnico finaliza
        |
        v
Status: FECHADO
```

### Regras já definidas

- Um chamado novo sempre nasce como `ABERTO`.
- Somente um chamado `ABERTO` entra na fila de atendimento.
- O técnico só pode iniciar um chamado que esteja `ABERTO`.
- O técnico só pode finalizar um chamado que esteja `EM_ATENDIMENTO`.
- Um chamado não é apagado nem inativado. Ele precisa ficar registrado para consultas e relatórios futuros.
- O solicitante só pode cancelar um chamado que ainda esteja `ABERTO`.
- Um usuário inativo não pode abrir novos chamados.
- Quando um usuário é inativado, seus chamados abertos são cancelados automaticamente. Chamados em atendimento, fechados e já cancelados não são alterados.

### Motivos de cancelamento disponíveis ao solicitante

- `RESOLVIDO_NO_LOCAL`
- `NAO_HA_MAIS_NECESSIDADE`
- `CHAMADO_DUPLICADO`
- `OUTRO`

`USUARIO_INATIVADO` é reservado ao sistema e não deve aparecer como opção para o usuário.

## Fila de atendimento atual

A fila já está pronta na API e segue esta ordem:

1. `URGENTE`
2. `ALTA`
3. `MEDIA`
4. `BAIXA`

Se dois chamados tiverem a mesma prioridade, o chamado aberto primeiro aparece antes. A posição numérica para o solicitante, por exemplo "você está em 3º na fila", ainda será implementada em uma etapa futura. Essa função é obrigatória para a versão final.

## Estrutura do código

O código foi organizado por assunto para facilitar a leitura e a manutenção:

```text
src/main/java/pmesp/helpdesk37bpmm
|
|-- Chamado
|   |-- controller   # Rotas de chamado
|   |-- dto          # Dados recebidos e dados devolvidos pela API
|   |-- enums        # Status, prioridade e categoria
|   |-- mapper       # Conversão entre DTO e Model
|   |-- model        # Tabela de chamados no banco
|   |-- repository   # Consultas ao banco
|   `-- service      # Regras de negócio do chamado
|
`-- Usuario
    |-- controller   # Rotas de usuário
    |-- dto          # Dados recebidos e dados devolvidos pela API
    |-- enums        # Postos e graduações
    |-- mapper       # Conversão entre DTO e Model
    |-- model        # Tabela de usuários no banco
    |-- repository   # Consultas ao banco
    `-- service      # Regras de negócio do usuário
```

### Por que existem DTOs e Mappers?

O `Model` representa como a informação fica guardada no banco. O `DTO` representa somente os dados que entram ou saem da API. O `Mapper` faz a conversão entre os dois.

Na prática, isso evita que campos definidos pelo sistema, como `id`, `ativo`, `status` e datas, sejam enviados livremente por quem usa a API. Também deixa cada classe com uma responsabilidade mais clara.

## Rotas disponíveis para teste

| Método | Rota | O que faz |
| --- | --- | --- |
| `POST` | `/usuarios/cadastrar` | Cadastra um usuário. |
| `GET` | `/usuarios/buscar/{re}` | Busca um usuário pelo RE. |
| `PUT` | `/usuarios/atualizar-dados/{re}` | Atualiza nome e posto/graduação. |
| `PATCH` | `/usuarios/inativar/{re}` | Inativa o usuário e cancela os chamados abertos dele. |
| `POST` | `/chamados/cadastrar` | Abre um novo chamado. |
| `GET` | `/chamados/buscar/{id}` | Busca um chamado pelo identificador. |
| `GET` | `/chamados/fila` | Mostra a fila de chamados abertos. |
| `PUT` | `/chamados/atualizar-dados/{id}?prioridade=ALTA` | Altera a prioridade de um chamado aberto. |
| `PATCH` | `/chamados/iniciar-atendimento/{id}` | Move o chamado de aberto para em atendimento. |
| `PATCH` | `/chamados/finalizar/{id}` | Fecha um chamado que está em atendimento. |
| `PATCH` | `/chamados/cancelar/{id}?motivoCancelamento=OUTRO` | Cancela um chamado aberto. |

### Exemplos de JSON

Cadastro de usuário:

```json
{
  "postoGraduacao": "SD PM",
  "nome": "Nome de teste",
  "re": "123456"
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

> Os valores de categoria, prioridade, status e motivo são técnicos na API. No frontend, eles serão mostrados com nomes mais amigáveis para facilitar o uso.

## Como executar localmente

### Pré-requisitos

- Java instalado
- Uma IDE Java, como IntelliJ IDEA
- Postman para testar a API, se desejar

### Configuração local

As configurações privadas do banco ficam no arquivo `.env`, que não deve ser enviado ao GitHub. Use valores locais e não publique senhas, dados reais de usuários ou o arquivo do banco.

Exemplo de estrutura esperada:

```env
DATABASE_URL=jdbc:h2:./Data/helpdesk
DATABASE_USERNAME=seu_usuario_local
DATABASE_PASSWORD=sua_senha_local
```

O banco H2 é temporário para o aprendizado e os testes. A migração para PostgreSQL será feita mais à frente, quando a base estiver madura.

## Próximas tarefas sugeridas

Estas são as próximas prioridades para continuar o projeto sem pular etapas:

1. **Melhorar respostas de erro da API** - trocar retornos vazios por mensagens claras, por exemplo: "RE deve conter apenas números de 1 a 6 dígitos".
2. **Criar testes automatizados** - garantir que as regras de abertura, cancelamento, fila e inativação continuem funcionando depois de futuras alterações.
3. **Criar consulta por status** - separar chamadas abertos, em atendimento, finalizados e todos os chamados para preparar as telas do técnico.
4. **Implementar posição na fila** - informar ao solicitante quantos chamados estão à frente dele, respeitando a prioridade.
5. **Planejar a conclusão do chamado** - registrar uma solução simples ou observação de encerramento para ajudar nos relatórios futuros.
6. **Começar o frontend** - criar as telas de abertura, meus chamados e painel do técnico usando a identidade visual definida para o projeto.

## Visão de evolução

O caminho abaixo representa o planejamento atual. Ele pode mudar conforme surgirem testes, ideias melhores ou novas necessidades do setor.

### 1. API mais completa e segura

- Mensagens de erro claras e padronizadas.
- Testes automatizados das principais regras.
- Filtros por status, prioridade, período, categoria, solicitante e local.
- Histórico de solução e atendimento.
- Posição do chamado na fila.
- Controle de permissões para solicitante e técnico.

### 2. Login e primeiro acesso

- Usuários serão cadastrados inicialmente pela equipe responsável.
- No primeiro acesso, o policial informará o RE e criará uma senha própria para o Helpdesk.
- A senha será salva de forma protegida no banco, nunca em texto visível.
- Em uma etapa posterior, será avaliado envio de código para o e-mail corporativo, aumentando a segurança.
- Para a versão de portfólio, haverá dados fictícios e uma autenticação separada da instalação real.

### 3. Frontend para usuário e técnico

A interface seguirá uma identidade visual sóbria e institucional:

| Elemento | Cor |
| --- | --- |
| Menu lateral e navegação | `#1A1D20` |
| Fundo principal | `#F8F9FA` |
| Cards e áreas de leitura | `#FFFFFF` |
| Ações principais | `#8B0000` |
| Alertas e urgências | `#DC3545` |

Telas planejadas:

- Abertura de chamado com linguagem simples e exemplos de local de atendimento.
- Meus chamados, para o solicitante acompanhar situação, horário e posição na fila.
- Fila do técnico, mostrando somente chamados abertos.
- Chamados em atendimento.
- Todos os chamados, com filtros.
- Chamados finalizados, reunindo fechados e cancelados.
- Painel com indicadores do setor.

### 4. Relatórios e indicadores

O objetivo é gerar informação que ajude a gestão, não somente listar chamados. Alguns indicadores planejados são:

- Quantidade de chamados por período.
- Quantidade de chamados por usuário, local e categoria.
- Problemas mais frequentes.
- Tempo médio entre abertura, início e finalização.
- Volume de chamados por prioridade.
- Motivos de cancelamento.
- Chamados recorrentes por equipamento ou ambiente, quando esses dados forem incluídos.

### 5. Banco e entrega da aplicação

- Migrar de H2 para PostgreSQL.
- Usar migrações de banco para manter cada instalação organizada.
- Aprender e adicionar Docker para facilitar a execução em outras unidades.
- Registrar logs e preparar uma forma segura de configurar cada instalação.
- Criar instruções para que outro batalhão consiga baixar, configurar e usar sua própria instância.

### 6. IA dentro da plataforma

O uso de IA será dividido em duas ideias:

| Módulo futuro | Objetivo |
| --- | --- |
| Assistente antes do chamado | Sugerir verificações simples, como cabos, energia ou orientações iniciais, antes de abrir um chamado. |
| Mike IA | Responder dúvidas sobre documentos, normas e procedimentos, em uma área separada do Helpdesk. |

O Mike IA deverá trabalhar somente com fontes revisadas. Quando for criado, a ideia é que ele informe de qual documento, versão e página veio a resposta, evitando respostas sem referência.

## Tecnologias usadas até agora

- Java
- Spring Boot
- Spring Data JPA
- H2 Database
- Lombok
- Maven
- Postman para testes manuais
- Git e GitHub para versionamento

## Transparência sobre o estágio do projeto

Este é um projeto em evolução, feito de forma gradual para que cada decisão possa ser entendida, testada e mantida no futuro. O objetivo não é apenas chegar a uma aplicação pronta: é construir uma base confiável, aprender as escolhas feitas e produzir um projeto de portfólio que represente uma solução real para a rotina de Telemática.

As telas, regras secundárias e integrações futuras podem ser ajustadas durante o desenvolvimento. As regras já implementadas e testadas são a base atual do sistema.
