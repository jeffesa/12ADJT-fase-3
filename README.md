# Tech Challenge Fase 3 — Sistema Hospitalar de Agendamento de Consultas

[![CI/CD](https://github.com/jeffesa/12ADJT-fase-3/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/jeffesa/12ADJT-fase-3/actions/workflows/ci-cd.yml)

Sistema hospitalar baseado em microsserviços para agendamento de consultas médicas, com comunicação assíncrona (RabbitMQ + DLQ), segurança com JWT, API REST e GraphQL, seguindo Clean Architecture.

**Repositório:** https://github.com/jeffesa/12ADJT-fase-3

> Badge de cobertura de testes (SonarCloud) será adicionado na TASK-031 (configuração de JaCoCo + SonarCloud).

> Continuação da Fase 2. Este projeto evolui a arquitetura para microsserviços com mensageria, mantendo o padrão de Clean Architecture (domain → application → infra).

---

## Sumário

- [Objetivo](#objetivo)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Como executar](#como-executar)
- [Endpoints REST (scheduling-service)](#endpoints-rest-scheduling-service)
- [GraphQL (history-service)](#graphql-history-service)
- [Mensageria (RabbitMQ)](#mensageria-rabbitmq)
- [Como rodar os testes](#como-rodar-os-testes)
- [Status de implementação](#status-de-implementação)

---

## Objetivo

Permitir que profissionais de saúde (médicos e enfermeiros) e pacientes gerenciem consultas médicas de forma segura e desacoplada:

- **Médicos e enfermeiros** criam, editam e visualizam consultas.
- **Pacientes** visualizam e cancelam apenas as suas consultas.
- Cada consulta criada/editada dispara um evento assíncrono que alimenta o serviço de notificações (lembretes) e o serviço de histórico (consultável via GraphQL).

---

## Arquitetura

Três microsserviços independentes, comunicando-se de forma assíncrona via RabbitMQ. Cada serviço com seu próprio banco quando aplicável.

```
                           ┌──────────────────────────┐
        REST + JWT         │    scheduling-service     │
   cliente ───────────────►│      (porta 8081)         │
                           │  Auth + Appointments      │
                           │  PostgreSQL (5433)        │
                           └────────────┬─────────────┘
                                        │ publica eventos (JSON)
                                        ▼
                           ┌──────────────────────────┐
                           │        RabbitMQ           │
                           │   appointment.exchange    │
                           │   (topic) 5672 / 15672    │
                           └──────┬──────────────┬─────┘
              appointment.*       │              │      appointment.*
                                  ▼              ▼
             ┌────────────────────────┐  ┌────────────────────────┐
             │  notification-service  │  │    history-service     │
             │      (porta 8082)      │  │      (porta 8083)      │
             │  consome eventos e     │  │  consome eventos e     │
             │  envia notificações    │  │  persiste histórico    │
             │                        │  │  GraphQL + PostgreSQL  │
             │                        │  │       (5434)           │
             └────────────────────────┘  └────────────────────────┘
```

- **Exchange:** `appointment.exchange` (tipo topic)
- **Filas:** `appointment.notification.queue`, `appointment.history.queue` (cada uma com sua DLQ)
- **Routing keys:** `appointment.created`, `appointment.updated`
- **DLQ:** mensagens que falham após retry (3 tentativas, backoff exponencial) vão para a fila `.dlq` correspondente (TTL de 24h)

---

## Tecnologias

| Categoria | Tecnologia |
|-----------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2.x |
| Build | Maven (multi-module) |
| Segurança | Spring Security + JWT (jjwt 0.12.5) |
| Mensageria | RabbitMQ (Spring AMQP) + DLQ |
| Persistência | Spring Data JPA + PostgreSQL (prod) / H2 (dev/test) |
| API | REST (scheduling) + GraphQL (history) |
| Documentação | SpringDoc OpenAPI / Swagger (scheduling) |
| Testes | JUnit 5 + Mockito |
| Containerização | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Como executar

### Pré-requisitos

- **Docker** e **Docker Compose** instalados e em execução
  - macOS: Docker Desktop, Colima ou Rancher Desktop
  - Linux: Docker Engine + plugin docker compose
- Portas livres: `8081`, `8082`, `8083`, `5433`, `5434`, `5672`, `15672`

> Não é necessário ter Java ou Maven instalados para executar via Docker — o build acontece dentro dos containers (multi-stage).

### Subindo a aplicação

Na raiz do projeto:

```bash
./run.sh
```

ou, diretamente:

```bash
docker-compose up --build
```

Isso sobe **todos** os serviços de uma vez: RabbitMQ, os dois bancos PostgreSQL e os três microsserviços. Os serviços aguardam os healthchecks das dependências (banco e broker) antes de iniciar.

### Verificando

Após a subida (aguarde os containers ficarem `healthy`):

| Serviço | URL |
|---------|-----|
| Scheduling — Swagger UI | http://localhost:8081/swagger-ui.html |
| Scheduling — Health | http://localhost:8081/actuator/health |
| Notification — Swagger UI | http://localhost:8082/swagger-ui.html |
| Notification — Health | http://localhost:8082/actuator/health |
| History — Health | http://localhost:8083/actuator/health |
| History — GraphiQL | http://localhost:8083/graphiql |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |

### Parando

```bash
docker-compose down
```

Para remover também os volumes (dados dos bancos):

```bash
docker-compose down -v
```

---

## Endpoints REST (scheduling-service)

Base: `http://localhost:8081`

### Autenticação (público)

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| POST | `/api/v1/auth/register` | Registra usuário (role: DOCTOR, NURSE, PATIENT) e retorna JWT | 201 |
| POST | `/api/v1/auth/login` | Autentica e retorna JWT | 200 |

### Consultas (requer JWT — header `Authorization: Bearer <token>`)

| Método | Rota | Acesso | Status |
|--------|------|--------|--------|
| POST | `/api/v1/appointments` | DOCTOR, NURSE | 201 |
| PUT | `/api/v1/appointments/{id}` | DOCTOR, NURSE | 200 |
| PATCH | `/api/v1/appointments/{id}/cancel` | DOCTOR, NURSE, PATIENT (dono) | 200 |
| GET | `/api/v1/appointments/{id}` | Autenticado (PATIENT só as suas) | 200 / 403 / 404 |
| GET | `/api/v1/appointments/patient/{patientId}` | DOCTOR, NURSE ou próprio PATIENT | 200 |
| GET | `/api/v1/appointments/doctor/{doctorId}` | DOCTOR, NURSE | 200 |
| GET | `/api/v1/appointments/upcoming` | Autenticado (filtrado por role) | 200 |

> A documentação interativa completa (com schemas e "Try it out") está no Swagger UI: http://localhost:8081/swagger-ui.html
>
> Fluxo de uso: registre um usuário → copie o `token` da resposta → clique em **Authorize** no Swagger e cole o token → use os endpoints protegidos.

---

## GraphQL (history-service)

Endpoint: `http://localhost:8083/graphql` — UI de testes em `/graphiql`.

Schema (`history-service/src/main/resources/graphql/schema.graphqls`):

```graphql
type Query {
    appointmentsByPatient(patientId: ID!): [AppointmentHistory]
    appointmentsByDoctor(doctorId: ID!): [AppointmentHistory]
    upcomingAppointments(patientId: ID!): [AppointmentHistory]
    appointmentHistory(id: ID!): AppointmentHistory
}

type AppointmentHistory {
    id: ID!
    appointmentId: ID!
    patientId: ID!
    doctorId: ID!
    patientName: String
    doctorName: String
    dateTime: String
    status: String
    description: String
    eventType: String
    receivedAt: String
}
```

---

## Mensageria (RabbitMQ)

Quando uma consulta é criada ou editada, o scheduling-service publica um evento JSON na exchange `appointment.exchange`:

- **`appointment.created`** — nova consulta
- **`appointment.updated`** — consulta editada

O evento (`AppointmentEvent`) contém: `appointmentId`, `patientId`, `doctorId`, `dateTime`, `status`, `eventType`.

Os serviços de notificação e histórico consomem esses eventos por meio de filas próprias com Dead Letter Queue (DLQ) e política de retry (3 tentativas com backoff exponencial). Mensagens que falham no processamento são encaminhadas para a `.dlq` correspondente.

Inspeção via RabbitMQ Management UI: http://localhost:15672 (guest / guest).

### Como testar a DLQ (fluxo de falha)

Para demonstrar o retry + Dead Letter Queue de ponta a ponta, o notification-service
tem um gatilho de teste: se uma consulta for criada com `description` igual a
`FORCE_ERROR`, o processamento da notificação falha de propósito, exercitando os
3 retries e o envio para a DLQ.

**Passo a passo:**

1. Suba o ambiente: `./run.sh docker` (ou `docker-compose up --build`).
2. Registre um usuário e faça login para obter o token (endpoints de auth).
3. Crie uma consulta com a descrição `FORCE_ERROR`:

```bash
curl -X POST http://localhost:8081/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_DOCTOR>" \
  -d '{"patientId":"<PAT_ID>","doctorId":"<DOC_ID>","dateTime":"2099-12-01T14:30:00","description":"FORCE_ERROR"}'
```

4. Observe o log do notification-service (`docker logs fase3-notification-service`):

```
RetryExecutor  : Tentativa 1/3 falhou: Falha simulada (FORCE_ERROR) ...
RetryExecutor  : Tentativa 2/3 falhou: ...
RetryExecutor  : Tentativa 3/3 falhou: ...
RetryExecutor  : Todas as 3 tentativas falharam.
AppointmentNotificationListener : ... enviando para a DLQ.
DlqMonitorListener : [DLQ] Mensagem na appointment.notification.dlq: headers={x-death=...} | payload={...}
```

Os intervalos entre as tentativas seguem backoff exponencial (~1s, ~2s).

**Ver a mensagem parada na fila DLQ (RabbitMQ Management UI):**

Por padrão, o `DlqMonitorListener` consome a DLQ para logar as mensagens — então
a fila esvazia logo após o log. Para inspecionar a mensagem **parada** na
`appointment.notification.dlq` pela UI (http://localhost:15672, guest/guest, aba
**Queues**), desative o monitor ao subir:

```bash
NOTIFICATION_DLQ_MONITOR_ENABLED=false docker-compose up --build
```

(ou defina `app.notification.dlq.monitor.enabled=false`). Assim a mensagem
permanece visível na DLQ para inspeção manual e eventual reprocessamento.

### Listar notificações processadas

O notification-service persiste as notificações enviadas e expõe um endpoint REST
para consultá-las (útil para comprovar que os eventos foram consumidos):

```bash
# Todas as notificações (requer JWT — use um token obtido no scheduling-service)
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8082/api/v1/notifications

# Filtro opcional por tipo
curl -H "Authorization: Bearer <TOKEN>" \
  "http://localhost:8082/api/v1/notifications?type=APPOINTMENT_CREATED"
```

Tipos válidos: `APPOINTMENT_CREATED`, `APPOINTMENT_UPDATED`, `APPOINTMENT_REMINDER`.
Documentação interativa em http://localhost:8082/swagger-ui.html.

---

## Como rodar os testes

Requer Java 17 e Maven instalados localmente (ou use o Maven do container).

```bash
# Todos os testes (perfil de teste, H2 em memória, sem RabbitMQ)
mvn clean test -Dspring.profiles.active=test

# Testes de um módulo específico
mvn clean test -pl scheduling-service -Dspring.profiles.active=test
```

Os testes usam H2 em memória e não dependem de RabbitMQ/PostgreSQL (o profile `test` exclui a auto-configuração do RabbitMQ).

---

## Status de implementação

Este projeto está em desenvolvimento incremental (por sprints). Status atual por serviço:

| Serviço | Status |
|---------|--------|
| **scheduling-service** | Auth (JWT), CRUD de consultas, publicação de eventos, Swagger — implementados |
| **notification-service** | Infraestrutura de mensageria e segurança configuradas; consumer de notificações em desenvolvimento |
| **history-service** | Infraestrutura de mensageria, segurança e schema GraphQL configurados; consumer e resolvers em desenvolvimento |

Consulte o backlog completo em [`docs/planejamento/BACKLOG.md`](docs/planejamento/BACKLOG.md) e o quadro de tarefas em [GitHub Projects](https://github.com/users/jeffesa/projects/9).
