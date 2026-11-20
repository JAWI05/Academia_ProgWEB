# Relatório técnico — Projeto Integrador Full Stack (Academia Fitness)

Disciplina: Programação Web — Engenharia de Computação, ESEG — 2º semestre de 2026 — projeto individual.
Documentos de apoio: [MODELAGEM](MODELAGEM.md) · [DECISÕES](DECISOES_TECNICAS.md) · [MATRIZ](MATRIZ_RASTREABILIDADE.md) · [TESTES](TESTES.md) · [EVIDÊNCIAS](evidencias/ROTEIRO_EVIDENCIAS.md)

## 1. Contexto

O trabalho moderniza o sistema legado de uma rede de academias, substituindo a base original por uma aplicação web em Java/Spring Boot com
persistência relacional (JPA/Hibernate), interface em Thymeleaf/Bootstrap e controle de acesso com dois perfis.
O foco não é copiar tabelas nem fazer telas de cadastro, e sim evoluir o domínio: relacionamentos explícitos, status com transições válidas,
regras de negócio verificáveis e segurança no servidor.

## 2. Problemas identificados

### 2.1 No legado (estrutura e requisitos)

> ⚠ **PREENCHER** com o diagnóstico da 1ª entrega (3 a 6 itens objetivos). Sugestão de formato:
> - *Problema* — onde aparece no legado — *consequência* (ex.: identificador sem restrição de unicidade → registros duplicados).

### 2.2 Riscos e problemas identificados durante o desenvolvimento e tratados

| Problema / risco | Como foi tratado | Onde |
|---|---|---|
| Entidades expostas diretamente pelos formulários e pela API (envio de `id`, `status`, `versao` pelo cliente; vazamento do hash da senha) | formulários e DTOs próprios, com validação | D-09 |
| Status sem controle de transições (concluir uma aula que não começou; reabrir aula cancelada) | `enum` com mapa de transições validado na entidade | D-06 |
| Superlotação e inscrição duplicada (inclusive com requisições simultâneas) | regra na entidade + chave composta + `@Version` | D-05, D-07 |
| Agenda impossível (mesmo personal em duas aulas ao mesmo tempo) | verificação de sobreposição de intervalos no serviço | D-08 |
| Ações restritas acessíveis digitando a URL (esconder botão não protege) | autorização por perfil no servidor (`SecurityConfig`) + testes | D-11 |
| Console do banco exposto com o login ligado; senhas padrão no repositório | console desligado; senhas por propriedade/variável com BCrypt, documentadas como somente de demonstração | D-13, D-15 |
| Sessão aberta continuava válida depois de desativar a conta | `EstadoContaFilter` | D-16 |

## 3. Soluções adotadas e justificativas

Resumo (detalhes, alternativas e impactos em [DECISOES_TECNICAS.md](DECISOES_TECNICAS.md)):

- **Modelo**: sete tabelas com chaves, restrições e índices nomeados; `Aula` liga Esporte, Personal e Unidade (N:1) e Cliente (N:N); `USUARIO` separado do domínio. Diagrama em [MODELAGEM.md](MODELAGEM.md).
- **Regras de negócio na entidade e orquestração no serviço**, cobertas por testes unitários que não dependem de Spring.
- **API REST e site Thymeleaf compartilham os mesmos serviços**, então as regras valem igualmente nos dois caminhos.
- **Segurança**: login/logout, BCrypt, CSRF no site, ADMIN e OPERADOR com permissões distintas verificadas no servidor.
- **Usabilidade**: filtros combináveis (aulas e clientes), mensagens por campo, selos de status e de vagas, interface responsiva com contraste conferido.
- **Reprodutibilidade**: Maven Wrapper, banco criado e populado automaticamente, README com instruções, integração contínua.

## 4. Testes realizados e resultados

Estratégia, catálogo e cenários em [TESTES.md](TESTES.md): **17 classes de teste e 131 métodos**, divididos em unitários puros, unitários com Mockito,
integração com Spring/H2 e fluxos ponta a ponta, com cenários válidos, inválidos e casos-limite.

> ⚠ **PREENCHER após rodar `scripts/executar-testes.ps1`** (copie do arquivo gerado `docs/evidencias/RESULTADOS_TESTES.md`):

| Data da execução | Testes | Falhas | Erros | Ignorados | Cobertura de linhas (JaCoCo) |
|---|---:|---:|---:|---:|---:|
| ⚠ | ⚠ | ⚠ | ⚠ | ⚠ | ⚠ |

Evidências visuais (antes/depois, perfis, validações, filtros) seguem o roteiro de [evidencias/ROTEIRO_EVIDENCIAS.md](evidencias/ROTEIRO_EVIDENCIAS.md).

## 5. Limitações conhecidas, riscos residuais e melhorias futuras

| Tema | Limitação / risco residual | Melhoria futura |
|---|---|---|
| Migração do legado | a base atual é gerada por carga com dados fictícios; **a importação dos dados reais do legado não foi implementada** | carga a partir da planilha original com validação e relatório de rejeitados |
| Esquema do banco | gerado pelo Hibernate (`update`), sem migrações versionadas nem `CHECK` no banco | Flyway/Liquibase e restrições `CHECK` |
| Banco | H2 em arquivo, adequado à demonstração | PostgreSQL/MySQL em contêiner |
| Concorrência | `@Version` protege a lotação, mas não há teste com threads; há janela de corrida na checagem de conflito de horário | teste de concorrência e restrição no banco (ou bloqueio) para conflito |
| Autenticação da API | HTTP Basic | tokens (JWT/OAuth2) e HTTPS |
| Contas | senhas padrão de demonstração; sem recuperação de senha, sem política de expiração, sem bloqueio por tentativas | troca obrigatória no primeiro acesso, política de senha, *rate limit* |
| Desempenho | listas sem paginação; uma consulta extra por requisição para validar o estado da conta | paginação e cache curto |
| Auditoria | não há registro de quem alterou o quê | trilha de auditoria (ex.: Hibernate Envers) |
| Funcionalidades de negócio | sem pagamentos, planos, check-in e avaliações físicas | evoluir o domínio conforme o levantamento do legado |
| Interface | Bootstrap por CDN (exige internet); sem testes automatizados de interface | WebJars e testes de UI (Selenium/Playwright) |
| Testes | lacunas listadas em [TESTES.md §5](TESTES.md) | cobrir os caminhos felizes restantes via HTTP |

## 6. Declaração de uso de ferramentas de apoio

> ⚠ **CONFIRA E AJUSTE** conforme as regras de uso de ferramentas (inclusive IA) comunicadas pelo professor, e escreva apenas o que for verdadeiro.

Durante o projeto foram utilizadas ferramentas de IA generativa como apoio: Claude (Anthropic) para geração e revisão de código, testes e
documentação, e Gemini (Google) para análise de exemplo e sugestões de interface. O autor executou o sistema e os testes, revisou os
componentes entregues e responde pela compreensão, correção e defesa de todos eles. O projeto de referência do professor (Aula 16, `BDJPA`)
foi usado como base de padrões de mapeamento JPA e configuração do banco.
