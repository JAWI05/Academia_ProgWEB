# Histórico Git

Repositório individual, branch `main`. Cada entrega do cronograma está marcada com uma **tag**:

| Tag | Entrega | Prazo | Conteúdo |
|---|---|---|---|
| `entrega-2` | 2ª entrega (fases 5 a 8) | 06/11/2026 | Spring Boot executável, entidades JPA, repositórios, serviços, controllers, persistência real e testes |
| `entrega-3` | 3ª entrega (fases 9 e 10) | 13/11/2026 | login e logout, dois perfis com permissões distintas, páginas responsivas com Thymeleaf integrado |
| `entrega-4` | 4ª entrega (fases 11 a 13) | 20/11/2026 | sistema integrado, testes e evidências, documentação, histórico Git e instruções de execução |

A 1ª entrega (fases 1 a 4: diagnóstico, requisitos, modelos e justificativas) é documental e está em `docs/` (ver `MODELAGEM.md` e `MATRIZ_RASTREABILIDADE.md`).

## Como o histórico está organizado

Os commits seguem as camadas do projeto (configuração, modelo, repositórios, apoio, serviços, segurança, cargas, controllers, testes, páginas, estilos, documentação)
e o prefixo da etapa. Etapas, em ordem: projeto base (Spring Initializr) → **entrega 2** → site Thymeleaf → base de demonstração → **entrega 3** → filtros → **entrega 4**.

> **Transparência:** este histórico foi montado a partir das versões que o projeto teve em cada etapa, agrupando as mudanças por camada.
> As **datas dos commits são as de quando o repositório foi montado**, e não as datas em que cada etapa foi escrita.
> Se você tem commits reais do seu desenvolvimento em outro repositório, prefira o seu e use este como referência.

## Conferir

```bash
git log --oneline --decorate --graph
git tag -n
git diff entrega-2 entrega-3 --stat        # o que mudou entre as entregas
git show entrega-4 --stat
```

## Publicar no GitHub

1. Crie um repositório **vazio** no GitHub (sem README, sem .gitignore).
2. Na pasta do projeto:
```bash
git remote add origin https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git
git push -u origin main --tags
```
3. Confira a aba *Actions*: o workflow `CI` roda os testes e guarda os relatórios como artefato (evidência E17 do roteiro).

## Ajustar a autoria (faça antes do primeiro `git push`)

Os commits foram criados com o autor genérico `Aluno <aluno@users.noreply.github.com>`. Para usar o seu nome e e-mail (Git Bash):

```bash
git filter-branch -f --env-filter '
export GIT_AUTHOR_NAME="Seu Nome"
export GIT_AUTHOR_EMAIL="seu-email@exemplo.com"
export GIT_COMMITTER_NAME="Seu Nome"
export GIT_COMMITTER_EMAIL="seu-email@exemplo.com"
' --tag-name-filter cat -- --all
```

Para os commits novos, configure uma vez: `git config user.name "Seu Nome"` e `git config user.email "seu-email@exemplo.com"`.

## Convenção para os próximos commits

Commits pequenos, no imperativo, com o escopo no início (ex.: `Aulas: valida capacidade ao editar`), um assunto por commit, e testes junto da mudança.
Correções depois da entrega entram em commits novos; as tags não são movidas.
