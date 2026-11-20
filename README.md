# Academia — Projeto Integrador Full Stack (backend + site + login)

Base: projeto Spring Initializr (`progweb`) + padrão do exemplo da Aula 16 (BDJPA) do professor.
Spring Boot 4.1 · Java 21 · Spring Data JPA/Hibernate · H2 (arquivo, schema APP) · Bean Validation.
Thymeleaf · Bootstrap · Spring Security (login, logout, dois perfis, BCrypt, CSRF).

## O que veio do exemplo do professor
| Exemplo (BDJPA) | Aqui |
|---|---|
| `persistence.xml` (H2 em arquivo, schema APP, `update`, show/format/highlight SQL) | mesmas chaves em `application.properties` (o Spring Boot gerencia a unidade de persistência) |
| Entidades com `@Table` (UNIQUE e índices nomeados), colunas em maiúsculas, `@ForeignKey` nomeada, `LAZY` | todas as entidades em `model/` seguem o mesmo padrão |
| `CargaDepartamentos` (upsert por chave natural) | `carga/CargaInicial` (idempotente, por UNIQUE) |
| `H2Console` | console embutido do Spring Boot em `/h2-console` (starter `spring-boot-h2console`) |
| `.gitignore`, Java 21 | mantidos (+ `database/`); pacote do projeto: `br.com.academiafitness.progweb` |

## Como executar
```bash
./mvnw spring-boot:run    # http://localhost:8080 ; banco em ./database/academia (persiste entre execuções)
./mvnw test               # unitários + integração (H2 em memória)
```
Console do banco: http://localhost:8080/h2-console (URL `jdbc:h2:file:./database/academia;AUTO_SERVER=TRUE`, usuário `sa`, senha vazia; tabelas no schema `APP`).
Opcional: `DB_USER`, `DB_PASSWORD`. O Maven Wrapper já está no projeto.

## Segurança (3ª entrega)
Usuários de demonstração, criados por `carga/CargaUsuarios` na primeira execução (quando não existe nenhum usuário), com senhas gravadas em BCrypt. A `CargaDemonstracao` só gera dados fictícios (clientes, aulas, personais...) e não cria contas:

| Perfil | Login | Senha |
|---|---|---|
| ADMIN | `admin` | `admin123` |
| OPERADOR | `operador` | `operador123` |

Valores só para demonstração: troque em `application.properties` ou pelas variáveis `ADMIN_SENHA` e `OPERADOR_SENHA`.

| Permissão | ADMIN | OPERADOR |
|---|---|---|
| Ver aulas, detalhes e clientes | sim | sim |
| Cadastrar/editar clientes | sim | sim |
| Inscrever / cancelar inscrição | sim | sim |
| Criar, editar, excluir aula e alterar status | sim | **não** |
| Excluir cliente | sim | **não** |
| Gerenciar usuários (`/usuarios`) | sim | **não** |

As regras ficam em `config/SecurityConfig` e valem no servidor: o operador que digitar a URL restrita cai em `/acesso-negado` (HTTP 403), e a API REST segue as mesmas regras (use autenticação básica no curl, ex.: `curl -u admin:admin123 localhost:8080/api/aulas`).
Os botões restritos também são ocultados nas telas com `sec:authorize`, mas isso é só conforto visual.
Logout é por `POST /logout` com token CSRF. `EstadoContaFilter` encerra a sessão de quem foi desativado por um administrador.
Regras de usuário: não é possível desativar a própria conta nem deixar o sistema sem um administrador ativo.
O console H2 está desligado por padrão (`H2_CONSOLE=true` só para depuração local).

## Site (Thymeleaf + Bootstrap + tema próprio)
| Página | Rota |
|---|---|
| Login / acesso negado | `/login`, `/acesso-negado` |
| Início (painel e próximas aulas) | `/home` (e `/`) |
| Usuários (somente ADMIN) | `/usuarios` |
| Aulas: lista com filtro por status | `/aulas` |
| Aula: detalhe, inscrições e troca de status | `/aulas/{id}` |
| Aula: criar / editar | `/aulas/nova`, `/aulas/{id}/editar` |
| Clientes: lista, criar, editar, excluir | `/clientes`, `/clientes/novo`, `/clientes/{id}/editar` |

As regras vêm da camada de serviço (as mesmas da API): transições de status, lotação, conflito de horário do personal, esporte inativo, e-mail único de cliente e bloqueio de exclusão de cliente inscrito.
Estilos: `static/css/custom.css` (paleta #1B365D, #005A9C, #60C1E5, #A0E0EF); prévia dos componentes em `/design-preview.html`.

## Filtros (aulas e clientes)
Os filtros rodam no banco (Criteria API / Specifications), combinam com **E** e ignoram o que estiver em branco; texto sem diferenciar maiúsculas e com `%`/`_` tratados como texto comum.

| Tela | Filtros |
|---|---|
| `/aulas` | texto (esporte, personal ou unidade), esporte, personal, unidade, status, período (de/até) e "só aulas com vagas" |
| `/clientes` | texto (nome, e-mail ou objetivo), objetivo, situação (com/sem inscrição em aula) e período de cadastro |

A API aceita os mesmos parâmetros na query string, por exemplo `GET /api/aulas?status=AGENDADA&apenasComVagas=true&de=2026-11-20&q=carla`.
Período invertido ou valor inválido não derruba a página: o filtro é ignorado e aparece um aviso.

## Endpoints da API REST (entidade central: Aula)
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/aulas?status=AGENDADA` | lista (filtro opcional) |
| GET | `/api/aulas/{id}` | detalhe |
| POST | `/api/aulas` | cria (nasce AGENDADA) |
| PUT | `/api/aulas/{id}` | atualiza (só AGENDADA) |
| PATCH | `/api/aulas/{id}/status` | transição de status |
| DELETE | `/api/aulas/{id}` | exclui (só AGENDADA sem inscritos) |
| POST/DELETE | `/api/aulas/{id}/inscricoes/{clienteId}` | inscreve / cancela inscrição |
| GET | `/api/unidades`, `/esportes`, `/personais`, `/clientes` | consultas de apoio |

## Base de dados de demonstração
`carga/CargaInicial` garante os cadastros básicos e `carga/CargaDemonstracao` gera dados fictícios com semente fixa:
6 unidades, 10 esportes (2 inativos), 12 personal trainers, 63 clientes e cerca de 45 aulas entre 3 semanas atrás e 3 semanas à frente,
com inscrições, aulas concluídas/canceladas/agendadas, sem estourar a lotação e sem conflito de horário do personal.
Só roda se o banco estiver quase vazio (não duplica ao reiniciar). Para recriar a base do zero, apague a pasta `database/` e suba de novo.
Para desligar: `academia.carga.demo=false`.

## Regras de negócio
- Status: `AGENDADA → EM_ANDAMENTO | CANCELADA`, `EM_ANDAMENTO → CONCLUIDA`; `CONCLUIDA` e `CANCELADA` são finais (`StatusAula`, validado na entidade).
- Inscrição: só em aula AGENDADA, sem duplicidade e respeitando a capacidade; `@Version` evita estourar a lotação em requisições simultâneas.
- Um personal não pode ter duas aulas (não canceladas) sobrepostas; esporte INATIVO não aceita novas aulas.
- Capacidade não pode ficar abaixo do número de inscritos; exclusão só sem inscritos (senão, cancelar).

## Erros (ProblemDetail)
400 validação (campo a campo) · 404 inexistente · 409 regra de negócio, concorrência ou integridade.

## Decisões e limitações
- Esquema gerado pelo Hibernate (`ddl-auto=update`), como no exemplo; faixas (duração 15–240 min, capacidade 1–100) são validadas no DTO, não por CHECK no banco.
- Ids `Long` (o exemplo usa `Integer`); CRUD de Unidade/Esporte/Personal/Cliente ainda não exposto; carga da planilha legada pendente em `CargaInicial`.
