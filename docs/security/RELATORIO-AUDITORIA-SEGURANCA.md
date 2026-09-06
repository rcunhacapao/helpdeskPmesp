# Relatório de auditoria de segurança — HelpDesk Telemática

Data da auditoria: 06/09/2026
Branch de trabalho: `codex/security-hardening`

## 1. Resumo executivo

O projeto foi auditado como uma aplicação Spring Boot 4.1.1 com frontend estático em HTML, CSS e JavaScript, API REST, autenticação por sessão HTTP, PostgreSQL em execução normal e H2 apenas em testes.

Antes das correções, o nível de risco era **alto** porque operações autenticadas por cookie não exigiam proteção CSRF e algumas ações técnicas aceitavam uma identidade informada pelo cliente sem confirmar que ela correspondia à sessão. Também faltavam limitação de tentativas, rotação explícita do identificador da sessão, limites em diversos campos, separação segura do Swagger e uma migração completa para bancos novos.

Depois das correções e da segunda revisão, não foi identificada vulnerabilidade crítica conhecida no código analisado. As falhas altas encontradas foram corrigidas e receberam testes negativos. Permanecem riscos de implantação e governança que precisam ser resolvidos antes de produção, principalmente HTTPS real, validação em PostgreSQL real, auditoria automatizada completa de componentes e definição mais granular das permissões técnicas.

## 2. Vulnerabilidades encontradas

| Severidade | Vulnerabilidade | Local | Impacto | Correção |
|---|---|---|---|---|
| ALTO | Ausência de CSRF em autenticação por cookie | `SecurityConfig` e frontend | Um site externo poderia induzir o navegador autenticado a enviar uma operação de alteração | CSRF do Spring Security habilitado; token obtido pelo frontend e exigido em métodos mutáveis |
| ALTO | Identidade técnica confiada a parâmetro do cliente | `ChamadoService` e `TecnicoService` | Um técnico poderia registrar uma ação operacional em nome de outro | A identidade informada agora deve corresponder à identidade autenticada; testes negativos adicionados |
| MÉDIO | Identificador de sessão não era trocado após autenticação | `AutenticacaoController` | Possibilidade de reutilização de um identificador conhecido antes da autenticação | `changeSessionId()` antes de persistir o contexto autenticado |
| MÉDIO | Ausência de limitação de tentativas de autenticação | `/auth/login` | Facilita força bruta automatizada | Bucket4j + Caffeine: cinco tentativas por minuto por origem e identificador, com resposta 429 e `Retry-After` |
| MÉDIO | Banco Docker aceitava credencial previsível e publicava a porta em todas as interfaces | `docker-compose.yml` | Acesso indevido ao banco em uma máquina alcançável pela rede | Senha passou a ser obrigatória por variável e porta limitada a `127.0.0.1` |
| MÉDIO | Hibernate alterava o esquema automaticamente | `application.properties` | Mudanças acidentais e não auditadas na estrutura do banco | `ddl-auto=validate` e migração Flyway V9 com o esquema completo |
| MÉDIO | Swagger habilitado fora de um perfil específico e springdoc 2.x incompatível com Spring Boot 4 | configuração e `pom.xml` | Exposição desnecessária do inventário da API e risco de falha da ferramenta | Swagger desligado por padrão, ligado apenas em `dev`; springdoc atualizado para 3.1.0 e testado |
| MÉDIO | Campos sem limites máximos e parâmetros técnicos sem validação declarativa | DTOs e `ChamadoController` | Consumo excessivo de memória, armazenamento abusivo e dados fora do formato esperado | `@Size`, `@Pattern`, validação de parâmetros e resposta 400 controlada |
| BAIXO | Headers defensivos incompletos | `SecurityConfig` | Menor proteção do navegador contra conteúdo injetado, enquadramento e vazamento de referência | CSP, Referrer-Policy e Permissions-Policy adicionados; headers padrão do Spring preservados |
| BAIXO | Rota inexistente era transformada em 500 | `TratadorDeExcecoes` | Confunde monitoramento e revela comportamento interno inconsistente | `NoResourceFoundException` agora produz 404 controlado |
| BAIXO | Padrões de arquivos secretos incompletos no Git | `.gitignore` | Maior chance de versionar chaves, certificados ou configuração local | Regras para `.env.*`, chaves, keystores, `secrets/` e configuração local |

## 3. Vulnerabilidades críticas e altas

### 3.1 CSRF em operações com sessão

A autenticação utiliza cookie de sessão. Nesse modelo, o navegador envia o cookie automaticamente, inclusive quando a requisição é provocada por outro site. Com CSRF desabilitado, esconder botões no frontend não impedia uma requisição forjada diretamente contra a API.

A correção reativou a proteção padrão do Spring Security e criou `GET /auth/csrf` para o frontend obter o token vinculado à sessão. Toda chamada `POST`, `PUT`, `PATCH` ou `DELETE` feita pelo cliente inclui o header indicado pelo backend. Um teste confirma que uma operação sem token recebe 403.

### 3.2 Ações técnicas com identidade controlada pelo cliente

As rotas para iniciar atendimento e alterar disponibilidade recebem um RE na URL ou query string. O perfil técnico era verificado, mas o servidor não confirmava em todos esses pontos se o RE informado era o mesmo da sessão. Alterar manualmente a requisição permitia atribuir a ação a outro técnico.

A camada de serviço agora compara o parâmetro com `SecurityContextHolder.getContext().getAuthentication().getName()`. A validação ocorre no backend e possui testes negativos específicos.

## 4. Arquivos modificados

- `.gitignore`: bloqueio preventivo de arquivos de segredo e configuração local.
- `docker-compose.yml`: senha obrigatória e PostgreSQL publicado apenas no loopback.
- `pom.xml`: springdoc compatível com Spring Boot 4, Bucket4j, Caffeine, testes do Spring Security e OWASP Dependency-Check.
- `SecurityConfig.java`: CSRF, autorização, erros JSON, headers defensivos e limpeza do cookie no logout.
- `AutenticacaoController.java`: endpoint CSRF, limite de tentativas e rotação do ID de sessão.
- `LimiteTentativasLogin.java` e `MuitasTentativasException.java`: controle e resposta para excesso de tentativas.
- DTOs de autenticação, usuário, técnico, chamado, Mike IA e relato de erro: formatos e tamanhos máximos.
- `ChamadoController.java`, `ChamadoService.java` e `TecnicoService.java`: validação de parâmetros e identidade técnica no servidor.
- `UsuarioServices.java`: validação exata do domínio institucional do e-mail.
- `TratadorDeExcecoes.java`: respostas controladas para 404, 429 e violações de parâmetros.
- `application.properties`, `application-dev.properties` e `application-prod.properties`: separação de ambiente, cookies, erros públicos, JPA e Swagger.
- `V9__criar_schema_base.sql`: esquema completo, chaves estrangeiras, unicidade e índices para instalações novas.
- `static/app.js`: envio do token CSRF e cookies limitados à mesma origem.
- testes de integração, serviços e frontend: CSRF, 401/403/404/429, sessão, headers, Swagger, limites e autorização negativa.

## 5. Segurança da autenticação

- As credenciais persistidas usam `BCryptPasswordEncoder`, que inclui salt no próprio hash.
- O hash não aparece nos DTOs de resposta nem foi encontrado em logs de aplicação.
- Falhas de autenticação retornam uma mensagem genérica, sem confirmar qual parte estava incorreta.
- O ID da sessão é rotacionado quando a autenticação é concluída.
- Logout invalida a sessão e solicita a remoção de `JSESSIONID`.
- Cookies estão configurados com `HttpOnly` e `SameSite=Strict`; no perfil `prod`, também com `Secure`.
- Tentativas repetidas recebem 429 após o limite definido.
- Senhas e identificadores de entrada possuem tamanho/formato máximo antes do trabalho criptográfico.

## 6. Segurança da autorização

Matriz efetiva das rotas:

| Acesso | Rotas |
|---|---|
| Público | arquivos estáticos, `GET /auth/csrf`, `POST /auth/login` |
| Autenticado | sessão/logout, operações próprias de chamados, fluxo do Mike IA e criação de relato de erro |
| Técnico | `/usuarios/**`, `/tecnicos/**`, fila/resumo/ações técnicas de chamados, consulta de relatos, histórico e métricas do Mike IA |
| Administrador | não existe perfil nem endpoint administrativo separado na implementação atual |
| Desenvolvimento | Swagger UI e `/v3/api-docs`, existentes somente com o perfil `dev` |

As consultas de chamado e de atendimento do Mike IA verificam propriedade na camada de serviço. DTOs de entrada não possuem campo de role, status interno, hash ou permissões, reduzindo risco de mass assignment. Um usuário comum acessando rota técnica recebe 403, e um usuário não autenticado acessando rota protegida recebe 401.

## 7. Variáveis de ambiente e segredos

- O frontend não usa Next.js e não existem variáveis `NEXT_PUBLIC_*`.
- Não há `.env` versionado.
- URL, usuário e senha do banco são lidos de variáveis de ambiente.
- O bootstrap operacional também lê seus valores de variáveis, sem credencial embutida no código de produção.
- A busca no estado atual e no histórico Git, com valores sensíveis omitidos, não encontrou padrões fortes de API key, token ou chave privada.
- `.gitignore` foi ampliado para reduzir reincidência.

Nenhum valor real de segredo foi copiado para este relatório.

## 8. Segurança das APIs

- A aplicação permanece same-origin e não define uma política CORS permissiva. Não foi encontrado `allowedOrigins("*")`.
- Operações que modificam estado exigem CSRF.
- Respostas 401, 403, 404, 429 e 500 usam JSON controlado.
- Stack traces, detalhes de binding, nomes de exceções e mensagens internas estão desabilitados nas respostas padrão.
- O acesso por ID é revalidado no serviço nos recursos privados auditados.
- Não foram encontrados endpoints Actuator, H2 Console, debug, métricas de infraestrutura ou health expostos.
- Não existem uploads ou manipulação de caminhos nesta versão.

## 9. Frontend

O frontend é JavaScript puro, sem pacote de runtime npm e sem etapa de build. Não há token de autenticação em `localStorage` ou `sessionStorage`; a autenticação permanece em cookie `HttpOnly`. O `sessionStorage` guarda apenas o estado temporário da triagem e o teste confirma que o CPF não é copiado para esse resumo do navegador.

Conteúdo vindo da API é renderizado com `textContent`. Os usos de `innerHTML` encontrados limpam contêineres ou montam rótulos com textos constantes controlados pelo próprio código; não foi encontrado `dangerouslySetInnerHTML` nem interpolação de entrada livre nesses pontos. A CSP adiciona uma segunda barreira defensiva.

## 10. Backend

- Controllers recebem DTOs, não entidades JPA diretamente.
- O servidor define status, relações, identidade e permissões sensíveis.
- A consulta JPQL localizada usa parâmetro nomeado; não há SQL concatenado a partir de entrada do usuário.
- Não há `Runtime.exec`, `ProcessBuilder`, execução de shell, SSRF ou upload.
- O domínio do e-mail é separado depois de validar a existência de um único `@` e comparado por igualdade case-insensitive.
- `spring.jpa.open-in-view=false` reduz consultas tardias e serialização acidental fora da camada de serviço.

## 11. Banco de dados e infraestrutura

PostgreSQL é o banco de execução e H2 fica no escopo de testes. O Flyway agora possui uma migração de base completa e o Hibernate apenas valida o esquema. A migração foi exercitada repetidamente a partir de bancos H2 vazios e passou pela validação do Hibernate.

No Docker Compose, a senha é obrigatória e a porta do PostgreSQL fica ligada apenas a `127.0.0.1`. O executável Docker não está instalado ou não está no `PATH` deste host, portanto `docker compose config` e o teste real contra PostgreSQL não puderam ser executados nesta auditoria.

Antes de produção, a infraestrutura ainda deve fornecer TLS, proxy reverso configurado para substituir headers encaminhados, backup restaurável, controle de acesso ao banco e armazenamento seguro dos segredos.

## 12. Dependências

O Maven resolveu, entre outras, as seguintes versões relevantes:

- Spring Boot 4.1.1;
- Spring Security 7.1.1;
- Spring Data JPA 4.1.1;
- Spring Framework 7.0.9;
- PostgreSQL Driver 42.7.13;
- Flyway 12.4.0;
- Tomcat 11.0.24;
- Jackson Databind 2.21.5;
- springdoc-openapi 3.1.0;
- Bucket4j 8.19.0;
- Caffeine 3.2.4.

O springdoc foi atualizado de 2.8.5 para 3.1.0. A documentação oficial informa que a linha 3.x é a compatível com Spring Boot 4 e aponta 3.1.0 como versão estável atual: <https://springdoc.org/>.

Os avisos oficiais do Spring publicados em agosto de 2026 indicam 7.1.1 como correção para vulnerabilidades recentes do Spring Security e 4.1.1 como correção para o bypass de validação de `Sort` no Spring Data JPA. As versões efetivamente resolvidas já são essas versões corrigidas:

- <https://spring.io/security/cve-2026-41707/>
- <https://spring.io/security/cve-2026-47841/>
- <https://spring.io/security/cve-2026-47842/>
- <https://spring.io/security/cve-2026-47877/>
- <https://spring.io/security/cve-2026-47834/>

O OWASP Dependency-Check 12.2.2 foi adicionado ao Maven. Duas execuções tentaram atualizar a base NVD, mas o Java 26 deste host falhou ao abrir o seletor de I/O local (`Unable to establish loopback connection`), inclusive com seletor alternativo. Como a base não foi obtida, **não existe relatório automatizado completo para afirmar ausência de CVEs em todas as dependências transitivas**. O plugin permanece configurado para nova execução em CI ou em um host compatível.

O frontend não possui dependências npm de runtime; `package.json` contém apenas o comando de testes com o Node nativo.

## 13. Testes executados

| Verificação | Comando | Resultado |
|---|---|---|
| Suíte Java completa | Maven `test` com H2 em memória | 69 testes, 0 falhas |
| Build do backend | `mvn -DskipTests package` | `BUILD SUCCESS`; JAR executável gerado |
| Segurança focada | `mvn -Dtest=SegurancaHttpIntegrationTest,LimiteTentativasLoginTest,FrontendEstaticoTest test` | 12 testes, 0 falhas |
| Perfil de desenvolvimento/OpenAPI | `mvn -Dtest=SpringdocDevProfileIntegrationTest test` | 1 teste, 0 falhas |
| JavaScript | `npm test` | 28 testes, 0 falhas |
| Árvore de dependências | `mvn dependency:tree -Dscope=runtime` | concluída; versões efetivas registradas |
| OWASP Dependency-Check | `mvn org.owasp:dependency-check-maven:12.2.2:check` | bloqueado pelo runtime Java/loopback antes da análise NVD |
| Segredos no histórico | `git log -p --all --no-textconv` + padrões fortes com saída mascarada | nenhum padrão forte encontrado |
| Integridade do diff | `git diff --check` limitado aos arquivos da auditoria | sem erro nas alterações da auditoria |
| Docker Compose | `docker compose config --quiet` | não executado: Docker ausente do `PATH` |

Os cenários de segurança incluem CSRF ausente, autenticação ausente, perfil insuficiente, tentativa de agir por outro técnico, acesso a recurso de outro usuário, formato malicioso, rate limiting, rotação de sessão, headers e exposição do Swagger.

## 14. Riscos restantes

| Severidade | Risco restante | Tratamento necessário |
|---|---|---|
| MÉDIO | Auditoria automatizada de todas as dependências transitivas não foi concluída | Executar o Dependency-Check em CI com JDK compatível, cache seguro e chave NVD armazenada como secret |
| MÉDIO | Técnicos ainda formam um grupo operacional amplo para transferência, priorização e encerramento de chamados | Formalizar a regra; se não for intencional, criar papéis de responsável, supervisor ou despachante e respectivos testes |
| MÉDIO | Não existe trilha persistente e imutável de ações sensíveis | Definir eventos, retenção, acesso e correlação antes de implementar logs de auditoria |
| MÉDIO | O resumo técnico pode persistir CPF quando necessário ao atendimento | Definir base legal, prazo de retenção, mascaramento na interface e descarte após o uso operacional |
| MÉDIO | HTTPS e proxy reverso não existem neste repositório | Bloquear HTTP externamente, configurar certificado e aceitar headers encaminhados somente de proxy confiável |
| MÉDIO | Migrações não foram executadas contra uma instância PostgreSQL real nesta máquina | Validar instalação vazia e atualização de cópia anonimizada antes do deploy |
| BAIXO | Rate limiting é local à instância | Migrar os buckets para armazenamento compartilhado antes de escalar horizontalmente |
| BAIXO | O OpenAPI em `dev` gera avisos de conversão de alguns schemas, embora responda 200 | Acompanhar atualização do springdoc/Swagger e validar visualmente a documentação usada pela equipe |
| BAIXO | Não há funcionalidade de anexos hoje | Antes de criar upload, definir tamanho, MIME real, armazenamento fora do webroot, nomes gerados e autorização de download |

## 15. Recomendações futuras

### Antes do deploy

1. Executar a suíte completa novamente no commit candidato.
2. Subir PostgreSQL isolado, aplicar as nove migrações em banco vazio e testar uma atualização a partir de backup anonimizado.
3. Executar o OWASP Dependency-Check em um host compatível e bloquear CVSS igual ou superior a 7.
4. Validar o `docker compose config` e confirmar que o PostgreSQL não está acessível pela rede externa.
5. Fazer uma revisão funcional da matriz de poderes dos técnicos.

### Antes de produção

1. Publicar somente por HTTPS e validar `Secure`, HSTS e os headers no proxy real.
2. Armazenar segredos em mecanismo próprio da infraestrutura, nunca em arquivo versionado ou linha de comando compartilhada.
3. Implantar logs de auditoria para ações sensíveis, sem conteúdo de credenciais e com acesso restrito.
4. Definir retenção e descarte para descrições de chamados, triagens, CPF e logs.
5. Testar restauração de backup e resposta a incidente.

### Melhorias futuras

1. Usar armazenamento compartilhado para rate limiting quando houver mais de uma instância.
2. Adicionar SAST e secret scanning ao CI.
3. Criar papel de supervisor/administrador somente quando houver requisito real, aplicando privilégio mínimo.
4. Para futura IA externa, manter chamadas no backend, aplicar autorização por usuário, minimizar contexto, impedir ações autônomas e proteger contra prompt injection e vazamento entre usuários.

## 16. Recomendação de produção

**NÃO.**

O código está consideravelmente mais seguro e as falhas altas identificadas foram corrigidas, mas ainda não é recomendável colocar o sistema em produção hoje porque quatro controles essenciais não foram comprovados no ambiente real: HTTPS/proxy, PostgreSQL com as migrações, varredura automatizada completa das dependências e trilha persistente de auditoria. Após essas validações e a decisão formal sobre o alcance das permissões técnicas, a recomendação pode ser reavaliada.
