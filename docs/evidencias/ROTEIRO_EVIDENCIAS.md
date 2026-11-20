# Roteiro de evidências

Objetivo: comprovar, com capturas de tela e registros, que os requisitos revisados/novos funcionam e **melhoram o legado**.
Salve as imagens em `docs/evidencias/img/` com o nome indicado e faça o commit.

Antes de começar: `.\mvnw.cmd spring-boot:run`, apague a pasta `database/` se quiser a base limpa e abra <http://localhost:8080/>.

## A. Capturas da versão proposta

| ID | O que fazer | O que a imagem deve mostrar | Arquivo |
|---|---|---|---|
| E01 | Abrir `/login` sem estar logado | tela de login | `E01-login.png` |
| E02 | Entrar com `admin` / senha errada | mensagem "Usuário ou senha inválidos" | `E02-login-invalido.png` |
| E03 | Entrar como `admin` | início com indicadores, próximas aulas, menu com **Usuários** e etiqueta *Admin* | `E03-inicio-admin.png` |
| E04 | Entrar como `operador` | menu **sem** Usuários, etiqueta *Operador*, sem botão "Nova aula" | `E04-inicio-operador.png` |
| E05 | Como operador, digitar `/aulas/nova` ou `/usuarios` na barra de endereços | página de **acesso negado** (proteção no servidor) | `E05-acesso-negado.png` |
| E06 | Em `/aulas`, filtrar por esporte + status + "só com vagas" | lista filtrada e contagem de resultados | `E06-filtro-aulas.png` |
| E07 | Em `/clientes`, filtrar por situação "Com inscrição" | lista filtrada | `E07-filtro-clientes.png` |
| E08 | Em `/aulas/nova`, enviar o formulário vazio | mensagens de validação por campo | `E08-validacao-aula.png` |
| E09 | Abrir uma aula `AGENDADA` e tentar **Concluir** sem iniciar (só há os botões válidos) / ou via `api.http` a transição inválida | apenas as transições permitidas na tela, ou o HTTP 409 na API | `E09-transicao.png` |
| E10 | Inscrever clientes até lotar e tentar mais um | mensagem "Aula lotada" | `E10-lotacao.png` |
| E11 | Criar duas aulas do mesmo personal no mesmo horário | mensagem de conflito de horário | `E11-conflito-horario.png` |
| E12 | Como admin, tentar excluir um cliente inscrito | mensagem de recusa | `E12-exclusao-cliente.png` |
| E13 | Em `/usuarios`, criar uma conta e desativar outra | lista de usuários com situação | `E13-usuarios.png` |
| E14 | Abrir o site no celular (ou DevTools modo responsivo) | menu colapsado e tabelas com rolagem | `E14-responsivo.png` |
| E15 | Rodar `scripts\executar-testes.ps1` | console com o resumo `APROVADO` e o arquivo `RESULTADOS_TESTES.md` gerado | `E15-testes.png` |
| E16 | Abrir `target/site/jacoco/index.html` | cobertura da suíte | `E16-cobertura.png` |
| E17 | Aba *Actions* do GitHub depois do push | execução do CI em verde | `E17-ci.png` |
| E18 | `git log --oneline --decorate --graph` no terminal | histórico com as tags das entregas | `E18-git-log.png` |

## B. Comparativo antes/depois (legado × proposta)

> ⚠ **PREENCHER a coluna "Legado"** com evidência do sistema original (captura, consulta ou trecho do esquema da 1ª entrega).

| Melhoria | Legado (evidência) | Proposta (evidência) |
|---|---|---|
| Relacionamentos e integridade referencial entre aula, esporte, personal e unidade | ⚠ | `docs/MODELAGEM.md` + E11 |
| Status com transições válidas | ⚠ | E09 |
| Controle de lotação e inscrição duplicada | ⚠ | E10 |
| E-mail único e exclusão protegida de cliente | ⚠ | E12 |
| Controle de acesso com dois perfis | ⚠ (sem autenticação) | E03, E04, E05 |
| Busca e filtros | ⚠ | E06, E07 |
| Validação de entrada | ⚠ | E08 |

## C. Registros automáticos

- Resultado dos testes: `docs/evidencias/RESULTADOS_TESTES.md` (gerado pelo script).
- Requisições da API prontas para executar: `docs/evidencias/api.http` (cliente HTTP do IntelliJ).
