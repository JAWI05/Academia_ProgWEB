# Matriz de rastreabilidade

Fluxo exigido: **requisito original → problema/lacuna → requisito revisado ou novo → justificativa → implementação → teste/evidência.**

> ⚠ **PREENCHER:** a coluna "Requisito original (1ª entrega)" depende do levantamento do legado feito na 1ª entrega, que não está neste repositório.
> Copie o identificador do requisito legado correspondente de cada linha (ou escreva "novo" quando não houver equivalente).
> As demais colunas (lacuna, justificativa, implementação e testes) foram preenchidas a partir do código.

## A. Requisitos mínimos da evolução (enunciado, seção 2)

| # | Requisito mínimo | Implementação | Teste / evidência |
|---|---|---|---|
| 1 | Inventariar tabelas, relacionamentos, chaves, campos, regras e problemas do legado | `docs/MODELAGEM.md` §1 e relatório §2 (⚠ a preencher com o diagnóstico da 1ª entrega) | documento (1ª entrega) |
| 2 | Revisar identificadores, chaves, nomenclaturas; normalização, integridade, restrições e índices | entidades em `model/` (UNIQUE, FKs e índices nomeados, colunas em maiúsculas, chave composta de inscrição); `docs/MODELAGEM.md` §2 | `ProgwebApplicationTests` (esquema sobe com o mapeamento); `FluxoCompletoIT` e `ClienteServiceTest` (integridade em uso) |
| 3 | Reavaliar status, valores admissíveis e transições; prevenir estados inconsistentes | `StatusAula.proximos()`, `Aula.transitarPara`, `StatusEsporte` | `StatusAulaTest`, `AulaTest`, `AulaServiceTest`, `AulaControllerIT.transicaoInvalidaRetorna409`, `FluxoCompletoIT.cicloCompletoDeUmaAula...` |
| 4 | Matriz de rastreabilidade | este documento | — |
| 5 | Requisitos funcionais novos de valor e requisitos não funcionais | seção B abaixo | seção B |
| 6 | CRUD completo de entidade central, relacionamentos JPA efetivos, validações, erros, persistência real | `Aula` (N:1 com Esporte, Personal e Unidade; N:N com Cliente); `AulaService`; `AulaController` e `AulaWebController`; `GlobalExceptionHandler` | `AulaServiceTest`, `AulaControllerIT`, `SiteIT`, `FluxoCompletoIT` (confere no banco ao final), `ValidacaoFormulariosTest` |
| 7 | Autenticação e autorização com 2 perfis distintos, protegidas no servidor | `SecurityConfig`, `UsuarioDetailsService`, `PrincipalUsuario`, `UsuarioService` | `SegurancaIT` (13 casos), `FluxoCompletoIT`, `PrincipalUsuarioTest` |
| 8 | Páginas responsivas com Bootstrap e formulários/exibição dinâmica com Thymeleaf | `templates/` (fragmentos, listas, formulários, login), `custom.css` | `SiteIT` (renderiza as páginas de verdade por perfil); responsividade: roteiro manual em `docs/evidencias/ROTEIRO_EVIDENCIAS.md` |

## B. Requisitos novos e revisados implementados

| ID | Requisito | Lacuna endereçada | Req. original (1ª entrega) | Justificativa | Implementação | Teste / evidência |
|---|---|---|---|---|---|---|
| RF-N01 | Agendar e manter aulas ligadas a esporte, personal e unidade | relacionamentos explícitos e consultáveis | ⚠ preencher | integridade referencial e consultas por associação | `Aula`, `AulaService`, `AulaController`, `AulaWebController` | `AulaServiceTest`, `AulaControllerIT`, `SiteIT` |
| RF-N02 | Status da aula com transições válidas (`AGENDADA → EM_ANDAMENTO → CONCLUIDA`; `CANCELADA` só de `AGENDADA`) | estados inconsistentes | ⚠ preencher | impedir, por exemplo, concluir uma aula que nem começou | `StatusAula`, `Aula.transitarPara` | `StatusAulaTest`, `AulaTest`, `FluxoCompletoIT` |
| RF-N03 | Inscrição de clientes com controle de lotação e duplicidade; só em aula agendada | superlotação e inscrição repetida | ⚠ preencher | regra de negócio central da academia | `Aula.inscrever/removerInscricao`, `@Version` | `AulaTest`, `AulaServiceTest`, `FluxoCompletoIT` |
| RF-N04 | Um personal não pode ter duas aulas sobrepostas | conflito de agenda | ⚠ preencher | evita agenda impossível | `AulaService.validarConflito` | `AulaServiceTest` (inclui o limite fim = início), `FluxoCompletoIT.personalNaoPodeTer...`, `CargaDemonstracaoIT` |
| RF-N05 | Esporte inativo não aceita novas aulas | oferta de modalidade encerrada | ⚠ preencher | coerência do cadastro | `AulaService.buscarEsporteAtivo`, `StatusEsporte` | `AulaServiceTest.rejeitaEsporteInativo` |
| RF-N06 | Cliente com e-mail único; cliente inscrito não pode ser excluído | duplicidade de cadastro e perda de histórico | ⚠ preencher | integridade e rastreabilidade | `ClienteService`, `UQ_CLIENTE_EMAIL` | `ClienteServiceTest`, `FluxoCompletoIT.clienteInscritoNaoPodeSerExcluido` |
| RF-N07 | Autenticação com dois perfis (ADMIN e OPERADOR) e permissões distintas | ausência de controle de acesso | ⚠ preencher | exigência do enunciado e proteção de dados | `SecurityConfig` | `SegurancaIT`, `FluxoCompletoIT` |
| RF-N08 | Gestão de usuários (criar, editar, ativar/desativar); nunca ficar sem administrador ativo | contas sem governança | ⚠ preencher | administração sem alterar o banco à mão | `UsuarioService`, `UsuarioWebController` | `UsuarioServiceTest`, `FluxoCompletoIT.cicloDeVida...` |
| RF-N09 | Conta desativada perde a sessão aberta | sessão continua válida após desativar | ⚠ preencher | resposta imediata a um acesso que deve ser cortado | `EstadoContaFilter` | `EstadoContaFilterTest`, `FluxoCompletoIT` |
| RF-N10 | Filtros de aulas (texto, esporte, personal, unidade, status, período, só com vagas) e de clientes (texto, objetivo, com/sem inscrição, período de cadastro) | encontrar registros em listas longas | ⚠ preencher | usabilidade com ~60 clientes e ~45 aulas | `AulaSpecs`, `ClienteSpecs`, `*Filtro` | `FiltrosIT`, `FiltrosTest`, `AulaSpecsTest`, `SiteIT`, `AulaControllerIT` |
| RF-N11 | Base de demonstração coerente com as regras | demonstrar sem digitar dados | ⚠ preencher | apresentação e testes reproduzíveis | `CargaInicial`, `CargaDemonstracao` | `CargaDemonstracaoIT` |
| RNF-01 | Validação de entrada com mensagens por campo | dados inválidos chegando ao banco | — | primeira barreira de qualidade | `form/`, `dto/`, `@Valid` | `ValidacaoFormulariosTest`, `AulaControllerIT.corpoInvalido...`, `SiteIT` |
| RNF-02 | Segurança: BCrypt, CSRF, logout por POST, console H2 desligado, sem *mass assignment* | exposição de dados e ações forjadas | — | boas práticas (OWASP) | `SecurityConfig`, DTOs/forms | `SegurancaIT`, `FluxoCompletoIT` |
| RNF-03 | Tratamento uniforme de erros (API em `ProblemDetail`; site com mensagens e página de erro) | respostas de erro inconsistentes | — | previsibilidade para quem consome e para o usuário | `GlobalExceptionHandler`, `WebExceptionHandler`, `error.html` | `AulaControllerIT`, `SiteIT` |
| RNF-04 | Reprodutibilidade: instruções, wrapper, base automática, CI | "na minha máquina funciona" | — | exigência de entrega | `README.md`, `mvnw`, cargas, `.github/workflows/ci.yml` | execução do CI e `docs/evidencias/RESULTADOS_TESTES.md` |
| RNF-05 | Interface responsiva e com contraste legível | uso em telas pequenas e legibilidade | — | acessibilidade básica | Bootstrap + `custom.css` (contrastes conferidos) | roteiro manual (`docs/evidencias`) |
