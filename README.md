# Helpdesk 37º BPM/M

Sistema de Helpdesk para registrar, organizar e acompanhar chamados de suporte.

O projeto está sendo construído de forma gradual, com código simples e comentado para facilitar o aprendizado, a manutenção e a evolução futura.

## O que já existe

- Cadastro de usuários com posto/graduação, nome e RE.
- RE aceito somente com números, entre 1 e 6 dígitos.
- Usuários não são excluídos pela aplicação: podem ser inativados.
- Cadastro de chamados com solicitante, descrição, local de atendimento e prioridade.
- Prioridades técnicas: `BAIXA`, `MEDIA`, `ALTA` e `URGENTE`.
- Status técnicos: `ABERTO`, `EM_ATENDIMENTO`, `FECHADO` e `CANCELADO`.
- Um chamado novo começa sempre como `ABERTO`.
- Fluxo de atendimento: `ABERTO` → `EM_ATENDIMENTO` → `FECHADO`.
- Cancelamento permitido somente quando o chamado está `ABERTO`.
- Motivos de cancelamento disponíveis ao usuário: `RESOLVIDO_NO_LOCAL`, `NAO_HA_MAIS_NECESSIDADE`, `CHAMADO_DUPLICADO` e `OUTRO`.
- Ao inativar um usuário, seus chamados ainda `ABERTO` são cancelados automaticamente com o motivo técnico `USUARIO_INATIVADO`.
- Chamados não são excluídos nem inativados, preservando o histórico para relatórios futuros.
- DTOs para receber os dados de cadastro de usuário e chamado.
- Banco H2 local para a etapa atual de desenvolvimento.

## Regras já definidas para a fila

Esta funcionalidade será construída em uma próxima etapa e é obrigatória para a versão final.

- A fila considera somente chamados com status `ABERTO`.
- Chamados `EM_ATENDIMENTO` já estão sendo tratados e não entram na espera da fila.
- Ordem de prioridade: `URGENTE`, `ALTA`, `MEDIA` e `BAIXA`.
- Dentro da mesma prioridade, a ordem será a de abertura do chamado.
- Ao criar um chamado, o solicitante deverá visualizar sua posição na fila e quantos chamados estão à frente.

## Próximas etapas do projeto

### Qualidade da API

- Criar mensagens de erro claras para dados inválidos, em vez de retornar `null`.
- Validar todos os campos obrigatórios e formatos de entrada.
- Criar testes automatizados para usuários, chamados e regras de negócio.
- Padronizar as respostas da API para facilitar o uso pelo frontend.
- Criar consultas de chamados por status, prioridade, usuário e período.
- Adicionar paginação para listas grandes de chamados.

### Fila e atendimento

- Implementar a fila de chamados com a ordem de prioridade definida acima.
- Exibir a posição atual na fila.
- Registrar qual técnico iniciou e finalizou cada atendimento.
- Registrar data e horário de início, finalização e cancelamento.
- Permitir observações de atendimento e histórico de alterações.
- Criar relatórios de chamados abertos, fechados, cancelados e tempo de atendimento.

### Banco de dados

- Migrar do H2 para PostgreSQL.
- Usar Flyway para controlar, versionar e aplicar alterações no banco de dados.
- Separar as configurações de desenvolvimento, testes e produção.
- Proteger dados sensíveis por variáveis de ambiente e manter segredos fora do GitHub.

### Frontend

- Criar interface simples, acessível e intuitiva para quem solicita suporte.
- Mostrar exemplos nos campos, como `Ex.: Sala do P1` para local de atendimento.
- Mostrar nomes amigáveis das prioridades e dos status, mantendo os nomes técnicos na API.
- Criar telas de cadastro, abertura, acompanhamento, fila, atendimento e relatórios.
- Impedir no próprio formulário o envio de RE com letras ou mais de seis dígitos.

### Inteligência artificial

- Antes de abrir um chamado, oferecer um chatbot com orientações simples e respostas por botões.
- Usar uma base de conhecimento controlada para problemas comuns, como monitor sem imagem ou acesso a sistemas.
- Permitir que a pessoa abra um chamado quando as orientações não resolverem o problema.
- Manter as chaves e integrações de IA somente no backend.

### Segurança, operação e portfólio

- Implementar autenticação e níveis de acesso: solicitante, técnico e administrador.
- Garantir que cada perfil veja e execute apenas o que lhe é permitido.
- Criar registro de auditoria para ações importantes.
- Adicionar logs organizados para diagnóstico de problemas.
- Dockerizar a aplicação e o banco PostgreSQL para facilitar a execução em qualquer computador.
- Criar documentação de instalação, uso da API e arquitetura do sistema.
- Configurar integração contínua para executar testes automaticamente a cada alteração no GitHub.

## Observação sobre a etapa atual

O projeto usa retornos `null` quando uma regra não é atendida, pois essa é a forma estudada até o momento. Em uma etapa futura, isso será substituído por respostas de erro específicas e fáceis de entender.
