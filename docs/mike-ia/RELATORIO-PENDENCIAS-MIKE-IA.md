# Mike IA — Relatório de observações para resolução posterior

> **Situação:** registro de decisões adiadas durante a definição do MVP.  
> **Decisão já tomada:** o Mike IA usará um modelo da família **Google Gemini Flash**.  
> **Objetivo deste relatório:** deixar explícito o que ainda não foi definido, sem bloquear a documentação, o desenho técnico e os testes locais do MVP.

## Decisões já consolidadas

- O Mike será um assistente interno integrado ao Helpdesk de Telemática, e não um agente autônomo.
- A IA não acessará o banco de dados, infraestrutura ou credenciais diretamente.
- Toda consulta e alteração continuará passando por regras e APIs controladas do backend.
- O MVP se limita a orientação baseada em material aprovado, triagem, criação confirmada de chamados e consulta autorizada de chamados.
- O provedor de IA será o Gemini API, com um modelo Gemini Flash.

## Observação sobre a escolha do modelo

O identificador exato do modelo Flash não será gravado diretamente em classes Java. Ele deverá vir de uma configuração externa, por exemplo `MIKE_IA_MODEL`, junto da chave `GEMINI_API_KEY`.

Isso permite testar e atualizar o modelo aprovado sem expor segredo nem obrigar uma alteração de código apenas para trocar a versão. Antes do primeiro acesso real à API, a equipe deverá confirmar qual identificador Gemini Flash estará habilitado na conta/ambiente usado pelo Helpdesk.

## Pendências adiadas

| Tema | O que ainda precisa ser decidido | O que pode ser feito agora | Momento em que se torna obrigatório |
| --- | --- | --- | --- |
| Retenção de conversas e logs | Prazo de guarda, responsáveis, forma de descarte e quem pode consultar os registros. | Modelar o ponto de auditoria sem persistir conversas reais. | Antes de qualquer piloto com usuários reais. |
| Base de conhecimento aprovada | Repositório dos documentos, responsáveis técnicos, revisão, classificação de acesso e publicação. | Definir a interface de busca e usar dados fictícios nos testes. | Antes de habilitar respostas baseadas em documentos reais. |
| Modelo Flash específico | Qual versão/identificador de Gemini Flash será aprovado para o ambiente. | Tornar o nome do modelo configurável e criar um adaptador sem chave real. | Antes da primeira chamada ao Gemini API. |
| Credencial e faturamento da API | Conta responsável, limite de uso, orçamento, armazenamento seguro da chave e rotação. | Preparar variáveis de ambiente documentadas, sem criar nem armazenar credenciais. | Antes de integrar com o serviço externo. |
| Perfis e permissões finais | Matriz real de perfis, campos e operações que cada perfil poderá consultar. | Mapear as permissões existentes e desenhar as verificações no backend. | Antes de expor qualquer consulta de chamado ou equipamento ao Mike. |
| Categorias do piloto | Quais problemas recorrentes terão triagem inicial e quais perguntas serão aprovadas. | Criar estrutura extensível de fluxos e exemplos de teste. | Antes de disponibilizar a triagem para usuários. |
| Ambiente de piloto | Unidade participante, usuários de teste, responsável pelo feedback e procedimento de interrupção. | Preparar testes locais e uma demonstração com dados fictícios. | Antes da disponibilização interna. |
| Métricas e auditoria | Indicadores de sucesso, formato dos eventos e acesso aos painéis. | Definir eventos técnicos mínimos sem coletar dados reais. | Antes do piloto, para haver linha de base e rastreabilidade. |
| Avaliação de privacidade e segurança | Classificação formal dos dados, limites de envio ao provedor e aprovação institucional. | Aplicar minimização de dados e bloqueios técnicos desde o início. | Antes de enviar qualquer dado real do Helpdesk ao Gemini. |

## Regras que permanecem obrigatórias mesmo antes dessas decisões

- `GEMINI_API_KEY` e qualquer outro segredo devem permanecer somente em variáveis de ambiente ou cofre de segredos; nunca em código, documentação com valores reais, commits ou respostas da API.
- O backend deve validar autenticação, autorização e todos os dados de entrada antes de executar uma operação sugerida pelo modelo.
- O modelo só poderá solicitar ferramentas previamente declaradas. Uma resposta em linguagem natural nunca terá permissão de executar consulta SQL, comando de sistema ou alteração administrativa.
- Criar ou alterar um chamado continuará exigindo confirmação explícita do usuário e validação pelo backend.
- Dados reais só poderão ser enviados ao Gemini após a decisão de privacidade, segurança e ambiente de piloto.

## Critério para encerrar este relatório

Cada linha da tabela pode ser marcada como resolvida quando houver uma decisão registrada, um responsável identificado e, quando aplicável, uma verificação técnica ou aprovação correspondente. Essas definições deverão alimentar a especificação do MVP antes do piloto.
