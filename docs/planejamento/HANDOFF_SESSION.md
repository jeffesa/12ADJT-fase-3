# Handoff para Nova Sessão - Tech Challenge Fase 3

## Contexto do Projeto

Sistema hospitalar de agendamento de consultas com microsserviços, comunicação assíncrona (RabbitMQ + DLQ), segurança (Spring Security + JWT) e GraphQL, utilizando Clean Architecture.

**Repositório:** https://github.com/jeffesa/12ADJT-fase-3
**Kanban:** https://github.com/users/jeffesa/projects/9
**Branch principal de desenvolvimento:** `develop`

---

## O que já foi feito

### Tasks Concluídas (verificar no kanban como "Done/Closed"):

1. **TASK-001** (Issue #1 - CLOSED): Estrutura multi-module Maven
   - Parent pom.xml com 3 modules
   - scheduling-service (porta 8081): Web, JPA, Security, AMQP, JWT, OpenAPI
   - notification-service (porta 8082): Web, AMQP, Actuator
   - history-service (porta 8083): Web, JPA, Security, AMQP, GraphQL, JWT
   - Profiles: dev (H2), test (H2), prod (PostgreSQL + RabbitMQ)
   - `mvn clean install -DskipTests` → BUILD SUCCESS

2. **TASK-042** (Issue #42 - CLOSED): CI/CD básico
   - `.github/workflows/ci-cd.yml`: build + test (mvn -B clean verify)
   - `.github/workflows/branch-naming.yml`: validação de nomes de branch
   - `.github/workflows/auto-pr-to-main.yml`: sync develop → main

3. **Bugfix adicional**: `actions/checkout` atualizado para @v5 (Node.js deprecation warning)

### Trabalho em andamento que pode já existir na develop:
- Docker Compose, Dockerfiles e .dockerignore foram detectados na develop (possivelmente TASK-003 parcial ou completa)

---

## Instruções para a nova sessão

### 1. VALIDAR O ESTADO ATUAL

Antes de continuar o desenvolvimento, faça:

```
# Verificar issues fechadas no kanban
gh issue list --repo jeffesa/12ADJT-fase-3 --state closed

# Verificar issues abertas
gh issue list --repo jeffesa/12ADJT-fase-3 --state open

# Verificar o estado do repositório
git log --oneline develop -20

# Verificar se o build passa
cd /Users/jefferson/Documents/FIAP/fase-3
git checkout develop && git pull
mvn clean install -DskipTests
```

Cruzar o que está na develop (commits/PRs merged) com o que está fechado no kanban. Se houver divergência, ajustar.

### 2. IDENTIFICAR PRÓXIMA TASK

Consultar o BACKLOG.md em `docs/planejamento/BACKLOG.md` para a ordem de execução.

Sprint 1 (Fundação) - ordem sugerida:
- [x] TASK-001: Estrutura multi-module Maven
- [x] TASK-042: CI/CD básico
- [ ] TASK-002: Clean Architecture em cada módulo
- [ ] TASK-003: Docker Compose multi-serviço (VERIFICAR - pode já estar feita)
- [ ] TASK-004: RabbitMQ com exchanges, queues e DLQ
- [ ] TASK-005: Spring Security + JWT
- [ ] TASK-006: OpenAPI/Swagger
- [ ] TASK-007: GlobalExceptionHandler (ProblemDetail)

### 3. FLUXO DE DESENVOLVIMENTO (seguir sempre)

Para cada task:
1. Mover issue para "In Progress" no kanban:
   ```
   # Obter item ID
   gh project item-list 9 --owner jeffesa --format json --jq '.items[] | select(.content.number == NUMERO_ISSUE) | .id'
   # Mover para In Progress (option-id: 47fc9ee4)
   gh project item-edit --project-id PVT_kwHOAH0ixs4Bgj3c --id ITEM_ID --field-id PVTSSF_lAHOAH0ixs4Bgj3czhfi90Q --single-select-option-id 47fc9ee4
   ```
2. Criar branch: `git checkout develop && git checkout -b feature/task-XXX-descricao`
3. Implementar conforme critérios de aceitação no BACKLOG.md
4. Commitar com: `feature(task-XXX): descrição` ou `ci(task-XXX):` ou `docs(task-XXX):`
5. Push: `git push -u origin feature/task-XXX-descricao`
6. PR para develop: `gh pr create --base develop`
7. Merge: `gh pr merge --merge`
8. Fechar issue e mover para Done:
   ```
   gh issue close NUMERO --repo jeffesa/12ADJT-fase-3 --reason completed
   # Mover para Done (option-id: 98236657)
   gh project item-edit --project-id PVT_kwHOAH0ixs4Bgj3c --id ITEM_ID --field-id PVTSSF_lAHOAH0ixs4Bgj3czhfi90Q --single-select-option-id 98236657
   ```
9. Voltar para develop: `git checkout develop && git pull`

### 4. IDs IMPORTANTES DO KANBAN

- **Project ID:** PVT_kwHOAH0ixs4Bgj3c
- **Status Field ID:** PVTSSF_lAHOAH0ixs4Bgj3czhfi90Q
- **Status Options:**
  - Backlog: f75ad846
  - To Do: 61e4505c
  - In Progress: 47fc9ee4
  - Done: 98236657

### 5. REFERÊNCIAS

- **Fase-2 (referência de arquitetura):** /Users/jefferson/Documents/FIAP/fase-2
- **BACKLOG completo:** /Users/jefferson/Documents/FIAP/fase-3/docs/planejamento/BACKLOG.md
- **Decisões técnicas:** /Users/jefferson/Documents/FIAP/fase-3/docs/planejamento/RESUMO_CONVERSA.md
- **PDF requisitos:** /Users/jefferson/Documents/FIAP/fase-3/docs/ADJT - BB - Tech Challenge - Fase 3.pdf

### 6. PADRÕES A SEGUIR

- Clean Architecture: domain → application → infra (mesmo padrão fase-2)
- Pacotes: `com.fiap.{scheduling,notification,history}.{domain,application,infra}`
- Commits: conventional commits (`feature()`, `fix()`, `ci()`, `docs()`)
- Branches: `feature/task-XXX-descricao`, `bugfix/descricao`
- Testes: JUnit 5 + Mockito, cobertura >= 80% (JaCoCo)
- Sem SonarCloud até Sprint 6 (TASK-031/032)

---

*Criado em: Agosto/2026*
