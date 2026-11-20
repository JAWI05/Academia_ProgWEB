# Testes

## 1. Estratégia

Quatro camadas, da mais rápida para a mais ampla:

1. **Unitários puros** (sem Spring): regras da entidade `Aula`, tabela de transições, filtros, padrão LIKE, autoridades do usuário.
2. **Unitários com Mockito**: serviços (`AulaService`, `ClienteService`, `UsuarioService`) com repositórios simulados; filtro de estado da conta com requisição simulada.
3. **Integração com Spring + H2 em memória**: mapeamento JPA e esquema, filtros contra o banco, API REST, páginas Thymeleaf por perfil, segurança.
4. **Fluxos ponta a ponta**: sequência completa de uma aula, conflito de horário, exclusão bloqueada de cliente e ciclo de vida de um usuário com sessão.

Cada classe de integração com dados próprios usa um banco em memória **separado** (`mem:fluxo`, `mem:filtros`, `mem:demotest`), então um teste não interfere em outro.

## 2. Como executar e onde está o registro

```powershell
.\scripts\executar-testes.ps1      # Windows: roda a suíte com cobertura e gera o registro
```
```bash
./scripts/executar-testes.sh       # Linux/macOS
```

| Saída | Local | Origem |
|---|---|---|
| **Registro dos resultados** | `docs/evidencias/RESULTADOS_TESTES.md` | gerado por `scripts/RelatorioTestes.java` a partir dos relatórios reais do Surefire |
| Relatórios detalhados | `target/surefire-reports/` | Maven Surefire |
| Cobertura | `target/site/jacoco/index.html` | JaCoCo (`-Pcobertura`) |
| Execução no GitHub | aba *Actions* do repositório | `.github/workflows/ci.yml` |

> ⚠ **O arquivo `RESULTADOS_TESTES.md` só existe depois de a suíte ser executada.** Rode o script, confira o resultado e faça o commit do arquivo
> (e, se quiser, de um print do relatório de cobertura em `docs/evidencias/img/`). Nada neste repositório afirma resultados que não foram executados.

## 3. Catálogo de testes (gerado a partir do código)

| Classe | Tipo | Métodos de teste | O que cobre |
|---|---|---:|---|
| `model.StatusAulaTest` | Unitário puro | 3 | Tabela de transições de status |
| `model.AulaTest` | Unitário puro | 14 | Regras da entidade Aula: lotação, duplicidade, status, edição |
| `filtro.FiltrosTest` | Unitário puro | 6 | Objetos de filtro de aulas e clientes |
| `repository.AulaSpecsTest` | Unitário puro | 4 | Padrão LIKE seguro (curingas escapados) |
| `form.ValidacaoFormulariosTest` | Unitário (Bean Validation) | 13 | Validação de formulários e DTO: válidos, inválidos, limites exatos |
| `security.PrincipalUsuarioTest` | Unitário puro | 3 | Autoridades ROLE_*, hash e conta habilitada |
| `security.EstadoContaFilterTest` | Unitário (Mockito + mocks do Spring) | 5 | Encerramento de sessão de conta desativada |
| `service.AulaServiceTest` | Unitário (Mockito) | 16 | Regras de negócio do serviço de aulas |
| `service.ClienteServiceTest` | Unitário (Mockito) | 5 | Regras do serviço de clientes |
| `service.UsuarioServiceTest` | Unitário (Mockito) | 10 | Regras do serviço de usuários (BCrypt, último admin) |
| `service.FiltrosIT` | Integração (Spring + H2) | 12 | Filtros de aulas e clientes contra o banco |
| `service.CargaDemonstracaoIT` | Integração (Spring + H2) | 3 | Base de demonstração: volume, status, lotação, conflitos |
| `controller.AulaControllerIT` | Integração (Spring + H2) | 5 | API REST: segurança, validação, erros e filtros |
| `controller.SiteIT` | Integração (Spring + H2) | 13 | Renderização das páginas Thymeleaf por perfil e filtros do site |
| `controller.SegurancaIT` | Integração (Spring + H2) | 13 | Login, logout, CSRF, permissões por perfil e acesso negado |
| `controller.FluxoCompletoIT` | Integração ponta a ponta | 5 | Fluxos completos: ciclo da aula, conflito, exclusão de cliente, ciclo de usuário |
| `ProgwebApplicationTests` | Integração (Spring + H2) | 1 | Contexto sobe: mapeamento JPA, esquema e cargas |
| **Total** | | **131** | (métodos; os parametrizados executam várias vezes) |


<details><summary><code>StatusAulaTest</code> — 3 método(s)</summary>

- `transicoesValidas`
- `transicoesInvalidas`
- `estadosFinaisNaoTemProximos`

</details>

<details><summary><code>AulaTest</code> — 14 método(s)</summary>

- `novaAulaNasceAgendadaSemInscritosECalculaOFim`
- `inscreveAteALotacaoEBloqueiaOExcedente`
- `capacidadeUmEhCasoLimiteValido`
- `naoPermiteInscricaoDuplicada`
- `soAceitaInscricaoEmAulaAgendada`
- `removeInscricaoLiberandoVaga`
- `naoRemoveInscricaoDeQuemNaoEstaInscrito`
- `naoRemoveInscricaoDeAulaJaIniciada`
- `percorreOCicloDeVidaCompleto`
- `transicaoInvalidaInformaOrigemEDestino`
- `estadosFinaisNaoSaemDoLugar`
- `capacidadeIgualAosInscritosEhPermitida`
- `capacidadeMenorQueOsInscritosEhRejeitada`
- `soEditaAulaAgendada`

</details>

<details><summary><code>FiltrosTest</code> — 6 método(s)</summary>

- `textoEmBrancoNaoContaComoFiltro`
- `periodoDeAulasInvertidoSoQuandoInicioPosteriorAoFim`
- `situacaoNulaDeClienteViraTodos`
- `objetivoEmBrancoNaoContaComoFiltro`
- `periodoDeCadastroInvertido`
- `cadaSituacaoTemRotuloParaATela`

</details>

<details><summary><code>AulaSpecsTest</code> — 4 método(s)</summary>

- `usaMinusculasETiraEspacosDasPontas`
- `escapaPorcentagem`
- `escapaSublinhado`
- `escapaABarraInvertida`

</details>

<details><summary><code>ValidacaoFormulariosTest</code> — 13 método(s)</summary>

- `aulaValidaNaoTemViolacoes`
- `aulaVaziaViolaTodosOsCamposObrigatorios`
- `duracaoRespeitaOsLimitesExatos`
- `capacidadeRespeitaOsLimitesExatos`
- `inicioNoPassadoEhRejeitado`
- `dtoDaApiTemAsMesmasRegrasDoFormulario`
- `clienteValidoEObjetivoOpcional`
- `clienteExigeNomeEEmailValido`
- `clienteRespeitaTamanhosMaximos`
- `usuarioValidoESenhaOpcionalNaEdicao`
- `loginTemTamanhoMinimoECaracteresPermitidos`
- `senhaTemLimitesDeTamanho`
- `usuarioExigePerfil`

</details>

<details><summary><code>PrincipalUsuarioTest</code> — 3 método(s)</summary>

- `autoridadeTemOPrefixoROLEEOPerfil`
- `expoeLoginHashEIdentificador`
- `contaDesativadaNaoEstaHabilitada`

</details>

<details><summary><code>EstadoContaFilterTest</code> — 5 método(s)</summary>

- `contaAtivaSegueNormalmente`
- `contaDesativadaTemASessaoEncerradaERedirecionaParaOLogin`
- `contaRemovidaDoBancoTambemEEncerrada`
- `visitanteSemLoginNaoConsultaOBanco`
- `arquivosEstaticosNaoSaoFiltrados`

</details>

<details><summary><code>AulaServiceTest</code> — 16 método(s)</summary>

- `criaAulaValidaComStatusAgendada`
- `rejeitaEsporteInativo`
- `rejeitaConflitoDeHorarioDoPersonal`
- `aceitaAulaLogoAposOFimDaAnterior`
- `inscreveClienteEReduzVagas`
- `rejeitaInscricaoDuplicada`
- `rejeitaInscricaoEmAulaLotada`
- `rejeitaInscricaoEmAulaCancelada`
- `clienteInexistenteGera404`
- `bloqueiaPularDeAgendadaParaConcluida`
- `permiteFluxoCompletoDeStatus`
- `estadoFinalNaoPodeMudar`
- `naoExcluiAulaComInscritos`
- `excluiAulaAgendadaSemInscritos`
- `naoReduzCapacidadeAbaixoDosInscritos`
- `aulaInexistenteGera404`

</details>

<details><summary><code>ClienteServiceTest</code> — 5 método(s)</summary>

- `criaClienteNormalizandoEmail`
- `rejeitaEmailDuplicado`
- `permiteManterOProprioEmailAoAtualizar`
- `naoExcluiClienteInscritoEmAula`
- `excluiClienteSemInscricoes`

</details>

<details><summary><code>UsuarioServiceTest</code> — 10 método(s)</summary>

- `criaUsuarioComSenhaCriptografadaELoginNormalizado`
- `rejeitaLoginDuplicado`
- `exigeSenhaNaCriacao`
- `atualizarSemSenhaMantemOHash`
- `atualizarComSenhaGravaNovoHash`
- `naoRebaixaOUltimoAdminAtivo`
- `naoDesativaAPropriaConta`
- `naoDesativaOUltimoAdmin`
- `desativaAdminQuandoHaOutroAtivo`
- `reativaUsuarioInativo`

</details>

<details><summary><code>FiltrosIT</code> — 12 método(s)</summary>

- `semFiltroRetornaTodasOrdenadasPorInicio`
- `filtraPorStatus`
- `filtraPorEsportePersonalEUnidade`
- `apenasComVagasExcluiAulasLotadas`
- `filtraPorPeriodoIncluindoOsDoisExtremos`
- `buscaPorTextoIgnoraMaiusculasEProcuraEmEsportePersonalEUnidade`
- `curingasDoUsuarioSaoTratadosComoTexto`
- `combinaFiltrosComE`
- `clientesPorTexto`
- `clientesPorObjetivo`
- `clientesComESemInscricaoSeComplementam`
- `clientesPorPeriodoDeCadastro`

</details>

<details><summary><code>CargaDemonstracaoIT</code> — 3 método(s)</summary>

- `populaUmaBaseVolumosa`
- `geraAulasEmVariosStatus`
- `respeitaLotacaoESemConflitoDeHorarioDoPersonal`

</details>

<details><summary><code>AulaControllerIT</code> — 5 método(s)</summary>

- `listaAulasDoBanco`
- `aulaInexistenteRetorna404`
- `corpoInvalidoRetorna400ComErrosPorCampo`
- `transicaoInvalidaRetorna409`
- `apiAceitaOsMesmosFiltrosDoSite`

</details>

<details><summary><code>SiteIT</code> — 13 método(s)</summary>

- `paginaInicialRenderiza`
- `listaDeAulasMostraACargaInicial`
- `detalheMostraInscritos`
- `formulariosCarregam`
- `listaDeClientesRenderiza`
- `listaDeUsuariosRenderizaParaAdmin`
- `aulaInexistenteMostraPaginaDeErro404`
- `formularioVazioVoltaComMensagensDeValidacao`
- `menuMostraUsuariosEBotoesDeAdminSoParaAdmin`
- `filtroDeAulasSemResultadoMostraMensagem`
- `filtroDeAulasPorEsporteMantemOResultado`
- `periodoInvertidoEValorInvalidoGeramAvisoSemQuebrarAPagina`
- `filtroDeClientes`

</details>

<details><summary><code>SegurancaIT</code> — 13 método(s)</summary>

- `anonimoEhRedirecionadoParaLogin`
- `paginaDeLoginEhPublica`
- `loginComUsuarioDaCargaAutentica`
- `loginComSenhaErradaFalha`
- `logoutExigeCsrfEEncerraASessao`
- `operadorConsultaAulasEClientes`
- `operadorNaoCriaNemEditaAula`
- `operadorNaoAlteraStatusNemExcluiAulaOuCliente`
- `operadorNaoAcessaUsuarios`
- `operadorPodeInscreverClientes`
- `apiOperadorLeMasNaoExclui`
- `adminAcessaAreasRestritas`
- `postNoSiteSemCsrfEhBloqueado`

</details>

<details><summary><code>FluxoCompletoIT</code> — 5 método(s)</summary>

- `cicloCompletoDeUmaAulaDaCriacaoAConclusao`
- `personalNaoPodeTerDuasAulasNoMesmoHorario`
- `clienteInscritoNaoPodeSerExcluido`
- `cicloDeVidaDeUmUsuarioComSessaoEncerradaAoDesativar`
- `loginDuplicadoVoltaComMensagemNoFormulario`

</details>

<details><summary><code>ProgwebApplicationTests</code> — 1 método(s)</summary>

- `contextLoads`

</details>

## 4. Cenários cobertos

| Tipo de cenário | Exemplos |
|---|---|
| **Válidos** | aula criada nasce `AGENDADA`; inscrição até a lotação; ciclo `AGENDADA → EM_ANDAMENTO → CONCLUIDA`; criar usuário e entrar com ele; filtros combinados devolvem só o que casa |
| **Inválidos** | transição `AGENDADA → CONCLUIDA`; inscrição duplicada, em aula lotada, cancelada ou já iniciada; esporte inativo; e-mail ou login repetido; formulário vazio; operador tentando criar aula/alterar status/excluir cliente/abrir `/usuarios`; POST sem token CSRF |
| **Casos-limite** | duração 14/15/240/241 min; capacidade 0/1/100/101; capacidade igual ao nº de inscritos (permitida) e menor (recusada); nome com 100/101 e objetivo com 120/121 caracteres; senha com 5/6/60/61 caracteres; aula que começa exatamente quando outra termina; período de filtro com início = fim; `%` e `_` digitados no filtro; último administrador ativo |
| **Segurança** | login certo/errado, hash BCrypt gravado, logout só por POST com CSRF, 403 + página de acesso negado, sessão encerrada ao desativar a conta, API com os mesmos limites do site |

## 5. Funcionalidades **não** cobertas por testes automatizados

Registradas de forma transparente (risco residual):

| Item | Situação | Como é tratado |
|---|---|---|
| Aparência, responsividade e contraste das telas | sem teste automatizado | roteiro manual com capturas em `docs/evidencias/ROTEIRO_EVIDENCIAS.md` |
| Concorrência (`@Version`): duas inscrições simultâneas na última vaga | sem teste | a regra existe e o conflito vira HTTP 409; falta um teste com threads |
| Janela de corrida na verificação de conflito de horário do personal | sem teste e sem trava no banco | risco baixo para o porte; veja D-08 |
| Edição de aula, cliente e usuário pelos formulários (caminho feliz via HTTP) | só validação (unitária) e regras (serviço); a tela de edição é apenas aberta no `SiteIT` | a regra de edição é testada em `AulaTest`/`ClienteServiceTest`/`UsuarioServiceTest` |
| Criação, atualização e exclusão de aula pela **API** (caminho feliz via HTTP) | só leitura, validação e transição inválida | as mesmas regras são exercitadas pelo site e pelos testes de serviço |
| Cadastro de cliente pelo formulário (caminho feliz) | sem teste de integração | regra de e-mail único em `ClienteServiceTest` |
| Banco **em arquivo** (`AUTO_SERVER`, `INIT`) | os testes usam memória | verificado manualmente na execução da aplicação |
| Reexecução das cargas sobre banco já populado | só a primeira carga é testada | a condição "banco quase vazio" é simples; verificação manual |
| Página de erro para falhas inesperadas (HTTP 500) | sem teste | |
| Desempenho e carga | fora do escopo | sem paginação nas listas (veja o relatório) |
