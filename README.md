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

O projeto está na fase de construção e teste da API. As funcionalidades abaixo já foram testadas manualmente no Postman.

### Funcionalidades disponíveis

- Cadastro de usuários com posto/graduação, nome e RE.
- Validação de RE: somente números, de 1 a 6 dígitos.
- Inativação de usuários sem apagar seu histórico.
- Cancelamento automático de chamados abertos quando o usuário é inativado.
- Abertura de chamados com descrição, categoria, local de atendimento e prioridade.
- Categorias: computador, monitor, impressora, rede/internet, e-mail e outro.
- Prioridades: baixa, média, alta e urgente.
- Fluxo de status: aberto, em atendimento, fechado e cancelado.
- Cancelamento de chamado aberto com motivo registrado.
- Fila de atendimento: urgentes primeiro, depois alta, média e baixa prioridade. Dentro da mesma prioridade, o chamado mais antigo vem antes.
- Consulta de chamados em atendimento, separada da fila de chamados abertos.
- Login com RE e senha, primeiro acesso com confirmação de e-mail funcional, e perfis de
  usuário comum/técnico (veja [Autenticação](#autenticação)).
- Banco H2 local para desenvolvimento e testes.

### Fluxo do chamado

```text
Usuário ativo -> abre chamado -> ABERTO -> EM_ATENDIMENTO -> FECHADO
                              |
                              `-> CANCELADO, quando necessário
```

Regras importantes:

- Um chamado novo sempre começa como `ABERTO`.
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
| `GET` | `/tecnicos/buscar/{re}` | Busca técnico pelo RE do usuário. | técnico |
| `GET` | `/tecnicos/disponiveis` | Lista técnicos disponíveis no momento. | técnico |
| `PATCH` | `/tecnicos/ficar-disponivel/{re}` | Marca o técnico como disponível. | técnico |
| `PATCH` | `/tecnicos/ficar-indisponivel/{re}` | Marca o técnico como indisponível. | técnico |
| `POST` | `/chamados/cadastrar` | Abre um chamado. | logado |
| `GET` | `/chamados/buscar/{id}` | Busca chamado pelo identificador (usuário comum só vê os próprios). | logado |
| `GET` | `/chamados/fila` | Mostra a fila de chamados abertos. | técnico |
| `GET` | `/chamados/em-atendimento` | Mostra os chamados que já estão sendo atendidos. | técnico |
| `PUT` | `/chamados/atualizar-dados/{id}?prioridade=ALTA` | Altera a prioridade de um chamado aberto. | técnico |
| `PATCH` | `/chamados/iniciar-atendimento/{id}?reTecnico={re}` | Técnico assume e inicia o atendimento. | técnico |
| `PATCH` | `/chamados/transferir-responsavel/{id}?reTecnico={re}` | Transfere o chamado para outro técnico. | técnico |
| `PATCH` | `/chamados/finalizar/{id}` | Finaliza um chamado em atendimento. | técnico |
| `PATCH` | `/chamados/cancelar/{id}?motivoCancelamento=OUTRO` | Cancela um chamado aberto (usuário comum só o próprio). | logado |

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

## Próximas etapas

1. ~~Melhorar as mensagens de erro da API.~~ ✅ concluído.
2. ~~Criar testes automatizados para as regras principais.~~ ✅ concluído.
3. Criar filtros de chamados por status, prioridade, categoria e período.
4. Implementar a posição do solicitante na fila.
5. Registrar solução ou observação ao finalizar um chamado.
6. Conectar o frontend (hoje um protótipo visual) à API real.
7. ~~Criar login com primeiro acesso por RE e senha própria do Helpdesk.~~ ✅ concluído.
8. Migrar o banco de H2 para PostgreSQL e preparar a aplicação para Docker.
9. Criar relatórios de volume, tempo médio de atendimento, categorias mais frequentes e chamados por usuário/local.

## Visão futura

O planejamento pode evoluir conforme os testes e as necessidades do setor, mas a direção atual do projeto inclui:

- Tela para abertura e acompanhamento de chamados.
- Painel do técnico com fila, chamados em atendimento, finalizados e filtros.
- Indicadores para ajudar o setor a entender volume de trabalho, tempo médio e problemas mais frequentes.
- Instalação independente para outras unidades.
- Assistente com sugestões simples antes da abertura do chamado.
- **Mike IA**, módulo separado para consultas sobre documentos, normas e procedimentos, sempre com fontes revisadas.

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
