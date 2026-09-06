# Regras de trabalho — projeto FIAP fase-3

Regras obrigatórias para qualquer sessão. Nascem de erros reais cometidos em sessões
anteriores. Seguir à risca para não repetir os problemas e não desperdiçar créditos.

## 1. Verificação de estado no INÍCIO de cada sessão (antes de qualquer coisa)

Antes de assumir que uma task está pendente/feita ou que um arquivo está num
determinado estado, SEMPRE rodar:

```
git branch --show-current
git status --short
git fetch origin && git log --oneline -3 origin/develop origin/main
```

- Confirmar em qual branch estou e se develop/main locais == origin.
- NUNCA assumir que uma branch está atualizada. Fazer `git pull` na develop antes de
  criar branch nova.
- Verificar o estado REAL da task no kanban/GitHub (issue OPEN/CLOSED, coluna) antes de
  dizer que está pendente. Não confiar na memória do resumo.

## 2. Edições de arquivo — SEMPRE confirmar que persistiram

Erro real cometido: editei o `.github/workflows/ci-cd.yml` e a mudança não persistiu
(sessão encerrou / arquivo estava em estado diferente do lembrado), gerando workflow
desatualizado e retrabalho.

Regra:
- Depois de editar QUALQUER arquivo, se houver dúvida se persistiu, reler o arquivo
  (ou `git diff <arquivo>`) para confirmar o conteúdo real ANTES de commitar.
- Antes de commitar, SEMPRE rodar `git status --short` e `git diff --staged` para
  confirmar exatamente o que está indo. Se aparecer "nothing to commit" quando eu
  esperava mudanças, PARAR e investigar — não seguir como se estivesse tudo certo.
- Nunca descrever uma mudança como aplicada sem ter visto o diff real.

## 3. Fluxo de branch/PR/merge (padrão do projeto)

- Branch a partir da develop atualizada: `feature/task-NNN-...` ou `fix/task-NNN-...`.
- Validar 100% (build + testes + e2e quando aplicável) ANTES de commit → PR.
- `gh pr create --base develop --body-file /tmp/x.md` (aspas inline quebram o wrapper).
- `gh pr merge N --merge`.
- "Closes #N" NÃO fecha issue auto neste repo (merge via API) → fechar manual:
  `gh issue close N --reason completed`.
- Sincronizar main após merge: checkout main, pull, merge develop, push. Confirmar com
  `git diff develop main --stat` (vazio = idênticos).
- Mover card no kanban para Done e deletar as branches (local + remota).
- Após qualquer merge, atualizar develop e main locais com `git pull` antes de seguir.

## 4. Validação end-to-end obrigatória (regra do usuário)

- SEMPRE testar e confirmar que está 100% (subir docker-compose real, não só testes
  unitários) ANTES de commitar, abrir PR e finalizar a task no kanban.
- Um comando terminar sem erro NÃO é prova de sucesso — verificar o resultado real
  contra os critérios de aceitação da issue, item por item.

## 5. Comunicação (regra do usuário)

- Ser DIRETO. Explicações longas confundem e gastam créditos.
- NÃO fazer perguntas desnecessárias. Quando o caminho for claro, executar e reportar.
  Só perguntar quando houver decisão real de negócio/tradeoff que dependa do usuário.
- Não re-verificar coisas já confirmadas na mesma sessão.

## 6. Contexto técnico do ambiente

- Dev local: Java 25 (projeto declara Java 17; containers Docker usam Java 17 temurin).
- JaCoCo 0.8.13 (suporta Java 25; resolve "Unsupported class file major version 69").
  Check de cobertura ativo: LINE >= 80% por módulo, roda em `mvn verify`.
- Docker via Colima (`colima start`). Usar `docker-compose` (v2 standalone), NÃO
  `docker compose`.
- Portas: scheduling 8081, notification 8082, history 8083; rabbit 5672/15672
  (guest/guest); postgres-scheduling 5433, postgres-history 5434.
- Emails/registros duplicam no Postgres (volume persistente) → usar timestamp único.
- Encadeamentos longos de curl travam o wrapper → fazer passo a passo com `--max-time 10`.
- SonarCloud: organization `jeffesa`, projectKey `jeffesa_12ADJT-fase-3`. Scanner exige
  Java 21+. Scan roda no CI só se o secret `SONAR_TOKEN` existir.
