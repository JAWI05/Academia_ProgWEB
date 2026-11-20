# Academia — Projeto Integrador Full Stack (2ª entrega, fases 5–8)

Base: projeto Spring Initializr (`progweb`) + padrão do exemplo da Aula 16 (BDJPA) do professor.
Spring Boot 4.1 · Java 21 · Spring Data JPA/Hibernate · H2 (arquivo, schema APP) · Bean Validation.
Thymeleaf, Nesta entrega o Spring Security está com uma configuração provisória que libera tudo (`config/SecurityConfig`); login, perfis e Bootstrap entram na 3ª entrega (fases 9–10).

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

## Endpoints (entidade central: Aula)
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

## Regras de negócio
- Status: `AGENDADA → EM_ANDAMENTO | CANCELADA`, `EM_ANDAMENTO → CONCLUIDA`; `CONCLUIDA` e `CANCELADA` são finais (`StatusAula`, validado na entidade).
- Inscrição: só em aula AGENDADA, sem duplicidade e respeitando a capacidade; `@Version` evita estourar a lotação em requisições simultâneas.
- Um personal não pode ter duas aulas (não canceladas) sobrepostas; esporte INATIVO não aceita novas aulas.
- Capacidade não pode ficar abaixo do número de inscritos; exclusão só sem inscritos (senão, cancelar).

## Erros (ProblemDetail)
400 validação (campo a campo) · 404 inexistente · 409 regra de negócio, concorrência ou integridade.

## Decisões e limitações
- Esquema gerado pelo Hibernate (`ddl-auto=update`), como no exemplo; faixas (duração 15–240 min, capacidade 1–100) são validadas no DTO, não por CHECK no banco.
- Ids `Long` (o exemplo usa `Integer`); sem autenticação (3ª entrega); sem telas; CRUD de Unidade/Esporte/Personal/Cliente ainda não exposto; carga da planilha legada pendente em `CargaInicial`.
