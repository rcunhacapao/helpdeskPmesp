# Relatório de segurança — HelpDesk Telemática

Data da revisão: 06/09/2026
Branch de trabalho: `codex/security-hardening`

## 1. Escopo considerado

Esta conclusão considera o cenário informado para o projeto:

- sistema simples para uso exclusivamente interno;
- acesso apenas pela intranet da mesma organização;
- nenhuma publicação direta na Internet;
- uma única instância da aplicação e um PostgreSQL local;
- dados operacionais de chamados;
- todos os técnicos pertencem ao mesmo nível operacional e podem administrar chamados e a disponibilidade da equipe.

Se algum desses pontos mudar, principalmente com acesso remoto, Internet, integração externa ou armazenamento de dados sensíveis, a avaliação deverá ser refeita.

## 2. Conclusão

**ADEQUADO PARA HOMOLOGAÇÃO E USO INTERNO NO ESCOPO DECLARADO.**

As falhas relevantes encontradas no código foram tratadas sem introduzir infraestrutura desnecessária. O projeto mantém proteções simples, locais e transparentes para os usuários. HTTPS, proxy reverso, papéis técnicos adicionais, rate limiting distribuído, Redis e ferramentas externas de monitoramento não são requisitos para esta implantação isolada.

O requisito de permissão ampla da equipe técnica foi confirmado: qualquer usuário com perfil técnico pode operar sobre os chamados e alterar a disponibilidade de qualquer técnico. Usuários comuns continuam separados das rotas técnicas.

## 3. Proteções mantidas

| Proteção | Motivo |
|---|---|
| CSRF em operações de alteração | Proteção nativa do Spring para sessões por cookie, sem serviço externo |
| Rotação da sessão após autenticação | Evita reutilização do identificador anterior e não muda o uso do sistema |
| Validação de formato e tamanho | Impede dados inválidos e textos excessivos no banco |
| Respostas 401, 403, 404 e 500 controladas | Evita expor detalhes internos e facilita entender erros |
| Headers básicos do navegador | CSP, proteção contra enquadramento, referência e tipos incorretos |
| Separação entre usuário e técnico | Usuários comuns não acessam as funções reservadas à equipe |
| Verificação de propriedade para usuários comuns | Cada usuário consulta apenas os próprios chamados |
| Consultas JPA parametrizadas | Não foi encontrada montagem de SQL com entrada do usuário |
| Swagger somente no perfil `dev` | A documentação não fica exposta no uso normal |
| Flyway e `ddl-auto=validate` | O banco evolui por migrações conhecidas e o Hibernate apenas confere |
| PostgreSQL em `127.0.0.1` | O banco não é publicado diretamente para outras máquinas da intranet |
| Senha do PostgreSQL por variável | Nenhum valor operacional precisa ficar gravado no repositório |

## 4. Simplificações realizadas após a definição do escopo

Foram retiridos componentes que não se justificam para a instalação atual:

- Bucket4j e Caffeine, que existiam apenas para limitação de tentativas;
- classes, tratamento 429 e testes exclusivos desse limitador;
- plugin OWASP Dependency-Check, que dependia de base externa e aumentava o processo de build;
- perfil específico para HTTPS/proxy reverso;
- bloqueios que impediam um técnico de operar em nome de outro técnico.

Os testes agora registram explicitamente a regra real: a equipe técnica pode gerenciar todo o ambiente técnico.

## 5. Docker e PostgreSQL reais

A validação foi executada com o Docker Desktop aberto:

| Verificação | Resultado |
|---|---|
| Docker Desktop | 4.89.0, mecanismo 29.7.2 funcionando |
| `docker compose config --quiet` | configuração válida |
| Contêiner PostgreSQL | iniciado e aceitando conexões |
| Porta publicada | `127.0.0.1:5432`, acessível somente na própria máquina |
| PostgreSQL | versão 16.15 |
| Banco vazio temporário | criado exclusivamente para o teste |
| Migrações Flyway | 9 de 9 aplicadas com sucesso |
| Esquema resultante | 6 tabelas e versão 9 confirmadas |
| Hibernate | conectou ao PostgreSQL e validou o mapeamento |
| Limpeza | banco temporário removido; banco e volume normais preservados |

O contêiner antigo ainda usava a publicação `0.0.0.0:5432`. Ele foi recriado com o Compose atual, preservando o volume, e passou a usar apenas `127.0.0.1:5432`.

A abertura de uma porta HTTP pelo processo Java iniciado dentro do ambiente do Codex encontrou a limitação local `Unable to establish loopback connection` do JDK 26. Isso ocorreu depois de o PostgreSQL, o Flyway e o Hibernate concluírem com sucesso. A camada HTTP foi validada separadamente por MockMvc, sem abrir uma porta real.

## 6. Testes finais

| Verificação | Resultado |
|---|---|
| Suíte Java completa | 66 testes, 0 falhas |
| Testes JavaScript | 28 testes, 0 falhas |
| Serviços e segurança HTTP alterados | 27 testes, 0 falhas |
| PostgreSQL real em banco vazio | 9 migrações, 6 tabelas, schema na versão 9 |
| Swagger no perfil `dev` | resposta validada por teste |
| Integridade das alterações | verificada com `git diff --check` nos arquivos do trabalho |

Os testes cobrem autenticação, sessão, CSRF, separação entre usuário e técnico, consultas de chamados, concorrência, validações, erros da API, migrações, frontend e o acesso amplo intencional da equipe técnica.

## 7. Dependências

O runtime permanece baseado principalmente em Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Flyway e springdoc. O springdoc foi atualizado para a linha 3.1.0, compatível com Spring Boot 4.

As bibliotecas Bucket4j e Caffeine não fazem mais parte do projeto. O frontend não possui dependências npm de runtime.

Uma varredura completa baseada na base pública de vulnerabilidades não foi incluída no build local porque exige atualização pela Internet. Para este ambiente isolado isso não impede o uso interno, mas é saudável conferir atualizações das dependências periodicamente em uma máquina de desenvolvimento com acesso externo.

## 8. HTTPS e proxy

HTTPS e proxy reverso não são necessários para a arquitetura atualmente declarada: uma aplicação local, restrita a uma intranet isolada e sem acesso público.

Essa decisão deve ser revista apenas se ocorrer pelo menos uma destas mudanças:

- acesso por Wi-Fi compartilhado ou rede não totalmente confiável;
- ligação da intranet com outras redes;
- acesso remoto, VPN de terceiros ou publicação na Internet;
- armazenamento de informações sensíveis;
- exigência formal da área de infraestrutura ou segurança da organização.

Enquanto o cenário permanecer como informado, HTTP interno pode ser usado sem adicionar proxy ao projeto.

## 9. Cuidados operacionais simples

Não exigem novas bibliotecas nem novos serviços:

1. manter o Windows, Java, Docker e PostgreSQL atualizados;
2. restringir o acesso físico e as contas da máquina que hospeda o sistema;
3. fazer cópia periódica do volume ou backup do PostgreSQL e testar a restauração;
4. não registrar informações pessoais desnecessárias no texto dos chamados;
5. não publicar a porta da aplicação ou do banco na Internet;
6. executar os testes antes de instalar uma nova versão.

## 10. Reavaliação

No cenário atual, não há necessidade de transformar o HelpDesk em uma arquitetura corporativa complexa. A prioridade deve continuar sendo estabilidade, facilidade de manutenção e funcionamento correto para a equipe local.

Uma nova revisão de segurança só é necessária quando houver mudança real de escopo, dados, rede, integrações ou quantidade de instâncias.
