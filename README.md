# Academia Fitness — Projeto Integrador Full Stack

Sistema web de gestão de academia (aulas, inscrições, clientes e usuários), desenvolvido como **modernização de um sistema legado**
para a disciplina de Programação Web (Engenharia de Computação, ESEG, 2º semestre de 2026). Projeto individual.

| | |
|---|---|
| Linguagem / plataforma | Java 21, Spring Boot 4 (Spring MVC, Spring Data JPA, Spring Security) |
| Persistência | JPA/Hibernate, banco relacional H2 em arquivo (schema `APP`) |
| Interface | Thymeleaf, Bootstrap 5, tema próprio (`static/css/custom.css`) |
| Segurança | Login/logout, 2 perfis (ADMIN e OPERADOR), BCrypt, CSRF, autorização no servidor |
| Testes | JUnit 5, Mockito, Spring Test/MockMvc, Spring Security Test (131 métodos de teste) |

## 1. Pré-requisitos

- **JDK 21** (`java -version` deve mostrar 21). Nada mais é necessário: o Maven vem junto pelo *wrapper* (`mvnw`).
- **Git** (para clonar) e acesso à internet na primeira execução (baixa dependências; as telas carregam Bootstrap por CDN).
- Porta **8080** livre.

## 2. Instalação

```bash
git clone <URL-DO-REPOSITORIO>
cd <pasta-do-repositorio>        # a pasta que contém o pom.xml
```

## 3. Execução

**Windows (PowerShell)**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**
```bash
./mvnw spring-boot:run
```

Abra <http://localhost:8080/> — você será levado ao login.

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Operador | `operador` | `operador123` |

> As contas e senhas acima são **somente de demonstração**; são criadas na primeira execução e gravadas em BCrypt.
> Para trocar, defina antes de subir a aplicação as variáveis `ADMIN_SENHA` e `OPERADOR_SENHA` (ou edite `application.properties`).

Na primeira execução o sistema cria o esquema do banco e carrega dados fictícios: 6 unidades, 10 esportes, 12 personal trainers,
63 clientes e cerca de 45 aulas (passadas e futuras, com inscrições). A carga só roda com o banco vazio; reiniciar não duplica nada.

**Recomeçar do zero:** pare a aplicação e apague a pasta `database/` (o banco é o arquivo `database/academia.mv.db`).

### Configuração

| Chave / variável | Padrão | Para que serve |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./database/academia;AUTO_SERVER=TRUE;INIT=...` | banco em arquivo; `AUTO_SERVER` permite abrir o mesmo banco em outra ferramenta com a aplicação rodando |
| `DB_USER` / `DB_PASSWORD` | `sa` / vazio | credenciais do banco |
| `ADMIN_SENHA` / `OPERADOR_SENHA` | `admin123` / `operador123` | senhas das contas de demonstração (usadas só quando não existe nenhum usuário) |
| `academia.carga.inicial` | `true` | garante cadastros básicos e as duas contas |
| `academia.carga.demo` | `true` | gera a base de demonstração (use `false` para começar com a base mínima) |
| `H2_CONSOLE` | `false` | console web do H2; **mantenha desligado** (veja *Segurança* em `docs/DECISOES_TECNICAS.md`). Para inspecionar o banco use a ferramenta de banco de dados do IntelliJ com a URL JDBC acima |

## 4. Como usar (roteiro rápido)

1. Entre como **admin**: `Início` mostra os indicadores e as próximas aulas.
2. `Aulas`: filtre por texto, esporte, personal, unidade, status, período e "só com vagas"; abra uma aula para ver inscritos.
3. Crie uma aula (`Nova aula`), inscreva clientes e avance o status (`AGENDADA → EM_ANDAMENTO → CONCLUIDA`; `CANCELADA` só a partir de `AGENDADA`).
4. `Clientes`: cadastre, edite, filtre (inclusive "com/sem inscrição") e tente excluir um cliente inscrito (o sistema recusa).
5. `Usuários` (só admin): crie contas, defina o perfil e ative/desative.
6. Saia e entre como **operador** para ver o que muda: sem criar/editar aula, sem alterar status, sem excluir cliente, sem `Usuários`.
   Digitar uma URL restrita na barra de endereços leva à página de acesso negado (HTTP 403).

### Permissões

| Operação | ADMIN | OPERADOR |
|---|:---:|:---:|
| Ver aulas, detalhes e clientes | sim | sim |
| Cadastrar e editar clientes | sim | sim |
| Inscrever / cancelar inscrição em aula | sim | sim |
| Criar, editar, excluir aula e alterar status | sim | **não** |
| Excluir cliente | sim | **não** |
| Gerenciar usuários | sim | **não** |

## 5. API REST

Autenticação básica (mesmos usuários). Exemplos:

```bash
curl -u admin:admin123 "http://localhost:8080/api/aulas?status=AGENDADA&apenasComVagas=true"
curl -u admin:admin123 -X POST http://localhost:8080/api/aulas -H "Content-Type: application/json" \
     -d '{"esporteId":2,"personalId":2,"unidadeId":1,"inicio":"2027-01-10T09:00:00","duracaoMin":45,"capacidade":15}'
curl -u admin:admin123 -X PATCH http://localhost:8080/api/aulas/1/status -H "Content-Type: application/json" -d '{"status":"CONCLUIDA"}'   # 409: transição inválida
```

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/aulas` | lista com filtros (`q`, `esporteId`, `personalId`, `unidadeId`, `status`, `de`, `ate`, `apenasComVagas`) |
| GET | `/api/aulas/{id}` | detalhe |
| POST | `/api/aulas` | cria (nasce `AGENDADA`) |
| PUT | `/api/aulas/{id}` | atualiza (só `AGENDADA`) |
| PATCH | `/api/aulas/{id}/status` | transição de status |
| DELETE | `/api/aulas/{id}` | exclui (só `AGENDADA` sem inscritos) |
| POST / DELETE | `/api/aulas/{id}/inscricoes/{clienteId}` | inscreve / cancela inscrição |
| GET | `/api/unidades`, `/api/esportes`, `/api/personais`, `/api/clientes` | consultas de apoio |

Erros seguem o formato `ProblemDetail`: `400` validação (com `erros` por campo), `404` inexistente, `409` regra de negócio, concorrência ou integridade.
Um arquivo pronto para o cliente HTTP do IntelliJ está em `docs/evidencias/api.http`.

## 6. Testes

```powershell
.\mvnw.cmd test                      # roda a suíte
.\scripts\executar-testes.ps1        # roda com cobertura e gera o registro em docs\evidencias\RESULTADOS_TESTES.md
```
```bash
./mvnw test
./scripts/executar-testes.sh         # idem, em Linux/macOS
```

- O **registro dos resultados** é gerado a partir dos relatórios reais do Surefire (`docs/evidencias/RESULTADOS_TESTES.md`).
- A **cobertura** (JaCoCo) sai em `target/site/jacoco/index.html` ao usar `-Pcobertura`.
- Os testes usam banco **em memória** e não dependem de nada externo.
- No GitHub, o workflow `.github/workflows/ci.yml` roda tudo a cada `push` e guarda os relatórios como artefato.
- Catálogo, cenários e o que **não** está coberto: `docs/TESTES.md`.

## 7. Estrutura do projeto

```
src/main/java/br/com/academiafitness/
  model/        entidades JPA e enums (Aula, Cliente, Esporte, PersonalTrainer, Unidade, Usuario, StatusAula, Perfil...)
  repository/   Spring Data JPA + Specifications dos filtros (AulaSpecs, ClienteSpecs)
  service/      regras de negócio e transações
  controller/   páginas Thymeleaf (*WebController) e API REST (AulaController, CatalogoController)
  form/ dto/    formulários HTML e DTOs da API, com validação
  filtro/       critérios de pesquisa (AulaFiltro, ClienteFiltro)
  security/     UserDetailsService, principal e filtro de estado da conta
  config/       SecurityConfig (rotas, perfis, login, CSRF)
  exception/    exceções de negócio e tratamento de erros (API e site)
  carga/        carga inicial, usuários e base de demonstração
src/main/resources/templates/   páginas Thymeleaf (fragments/layout.html, aulas/, clientes/, usuarios/...)
src/main/resources/static/css/  tema visual
src/test/                       testes unitários e de integração
docs/                           documentação técnica e evidências
scripts/                        execução de testes e geração do registro de resultados
.github/workflows/ci.yml        integração contínua
```

## 8. Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/RELATORIO_TECNICO.md`](docs/RELATORIO_TECNICO.md) | problemas, soluções, testes e resultados, limitações e melhorias futuras |
| [`docs/MODELAGEM.md`](docs/MODELAGEM.md) | modelo relacional (legado e evoluído) e diagrama de classes |
| [`docs/DECISOES_TECNICAS.md`](docs/DECISOES_TECNICAS.md) | decisões, alternativas, justificativas e impactos |
| [`docs/MATRIZ_RASTREABILIDADE.md`](docs/MATRIZ_RASTREABILIDADE.md) | requisito → lacuna → requisito revisado/novo → implementação → teste |
| [`docs/TESTES.md`](docs/TESTES.md) | estratégia, catálogo de testes, cenários e funcionalidades não cobertas |
| [`docs/evidencias/`](docs/evidencias/) | roteiro de evidências (antes/depois), registro dos testes, requisições de exemplo |
| [`docs/HISTORICO_GIT.md`](docs/HISTORICO_GIT.md) | organização do repositório, tags das entregas e como publicar |

## 9. Solução de problemas

| Sintoma | O que fazer |
|---|---|
| `JAVA_HOME ... not defined` / versão do Java errada | instale o JDK 21 e confira `java -version` |
| `Port 8080 was already in use` | feche o outro processo ou use outra porta (PowerShell): `.\\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` |
| `Database may be already in use` | feche a outra instância da aplicação (o `AUTO_SERVER` já permite abrir em ferramenta de banco) |
| Login recusado depois de mudar as senhas por variável | as contas só são criadas com a tabela de usuários vazia: apague `database/` e suba de novo |
| Telas sem estilo | o Bootstrap vem de CDN; verifique a conexão com a internet |
| Quero a base mínima | apague `database/` e suba com `"-Dspring-boot.run.arguments=--academia.carga.demo=false"` (ou ponha `academia.carga.demo=false` no `application.properties`) |

## 10. Autoria

Projeto individual. A seção de uso de ferramentas de apoio está no relatório técnico (`docs/RELATORIO_TECNICO.md`).
