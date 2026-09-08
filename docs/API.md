# Documentação da API — FIAP Fase 3 (Sistema Hospitalar)

Referência dos endpoints REST e das queries GraphQL dos três microsserviços, com exemplos de request/response (sucesso e erro), autenticação e códigos HTTP.

| Serviço | Porta | Tipo | Base |
|---|---|---|---|
| scheduling-service | 8081 | REST | `http://localhost:8081` |
| notification-service | 8082 | REST | `http://localhost:8082` |
| history-service | 8083 | GraphQL | `http://localhost:8083/graphql` |

## Sumário

- [Autenticação](#autenticação)
- [Códigos HTTP](#códigos-http)
- [Formato de erro](#formato-de-erro)
- [scheduling-service — Auth](#scheduling-service--auth)
- [scheduling-service — Appointments](#scheduling-service--appointments)
- [scheduling-service — Users](#scheduling-service--users)
- [notification-service — Notifications](#notification-service--notifications)
- [history-service — GraphQL](#history-service--graphql)

---

## Autenticação

A API usa **JWT (Bearer token)**. O token é emitido **apenas** pelo scheduling-service (endpoints de `register` e `login`); os demais serviços apenas validam o token.

### Como obter o token

1. Registre um usuário (`POST /api/v1/auth/register`) ou faça login (`POST /api/v1/auth/login`).
2. A resposta (`AuthResponse`) traz o campo `token`.

### Como usar o token

Envie em todas as requisições protegidas o header:

```
Authorization: Bearer <token>
```

Exemplo:

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyQGZpYXAuY29t...
```

### Roles

Existem três roles: `PATIENT`, `DOCTOR`, `NURSE` (não há ADMIN). No corpo do `register` a role é enviada **sem** prefixo (`DOCTOR`); nas respostas ela aparece **com** prefixo interno (`ROLE_DOCTOR`).

> Requisições sem token (ou com token inválido) em rotas protegidas recebem **403 Forbidden** (comportamento padrão do Spring Security, sem entry point customizado). No GraphQL, a falta de autenticação retorna HTTP 200 com um erro `UNAUTHORIZED` no array `errors`.

---

## Códigos HTTP

| Código | Significado | Quando ocorre |
|---|---|---|
| 200 OK | Sucesso | GET, PUT, PATCH bem-sucedidos |
| 201 Created | Recurso criado | `register`, criar consulta |
| 400 Bad Request | Requisição inválida | Falha de validação de campos; query param inválido |
| 401 Unauthorized | Não autenticado | `AuthenticationException` que chega ao controller |
| 403 Forbidden | Sem permissão | Sem token / token inválido, ou role/ownership insuficiente |
| 404 Not Found | Recurso não encontrado | ID inexistente |
| 422 Unprocessable Entity | Regra de negócio violada | Ex.: email já cadastrado, credenciais inválidas, tipo inválido |
| 500 Internal Server Error | Erro inesperado | Exceção não tratada |

---

## Formato de erro

Os serviços REST usam o padrão **RFC 7807 `ProblemDetail`** (`Content-Type: application/problem+json`). Não há um DTO de erro próprio.

Campos:

| Campo | Tipo | Descrição |
|---|---|---|
| `type` | String (URI) | Identificador do tipo de erro (ex.: `https://api.fiap.com/errors/business`) |
| `title` | String | Título curto (ex.: "Erro de negócio") |
| `status` | int | Código HTTP |
| `detail` | String | Mensagem do erro (ausente no 400 de validação) |
| `instance` | String | Path da requisição que gerou o erro (ex.: `/api/v1/auth/register`) |
| `timestamp` | String (ISO-8601) | Momento do erro |
| `errors` | String[] | **Apenas no 400 de validação** — lista `campo: mensagem` |

**400 — validação** (traz `errors`, sem `detail`):

```json
{
  "type": "https://api.fiap.com/errors/validation",
  "title": "Erro de validação",
  "status": 400,
  "instance": "/api/v1/auth/register",
  "errors": [
    "email: Email inválido",
    "password: Senha deve ter no mínimo 6 caracteres"
  ],
  "timestamp": "2026-09-07T12:34:56.789Z"
}
```

**404 — não encontrado:**

```json
{
  "type": "https://api.fiap.com/errors/not-found",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Consulta não encontrada",
  "instance": "/api/v1/appointments/00000000-0000-0000-0000-000000000000",
  "timestamp": "2026-09-07T12:34:56.789Z"
}
```

**422 — regra de negócio** (traz `detail`, sem `errors`):

```json
{
  "type": "https://api.fiap.com/errors/business",
  "title": "Erro de negócio",
  "status": 422,
  "detail": "Email já cadastrado: paciente@fiap.com",
  "instance": "/api/v1/auth/register",
  "timestamp": "2026-09-07T12:34:56.789Z"
}
```

---

## scheduling-service — Auth

Base: `http://localhost:8081/api/v1/auth` — **público** (sem token).

### POST /api/v1/auth/register

Registra um usuário e retorna o JWT. **201 Created**.

Headers: `Content-Type: application/json`

Body (`RegisterRequest`):

| Campo | Tipo | Validação |
|---|---|---|
| `name` | String | obrigatório |
| `email` | String | obrigatório, formato de email |
| `password` | String | obrigatório, mínimo 6 caracteres |
| `role` | String | obrigatório: `DOCTOR`, `NURSE` ou `PATIENT` |

Request:

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. House",
    "email": "house@fiap.com",
    "password": "senha123",
    "role": "DOCTOR"
  }'
```

Response 201 (`AuthResponse`):

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "userId": "34816130-ed9d-4a69-a8eb-6783311654d7",
  "name": "Dr. House",
  "email": "house@fiap.com",
  "role": "ROLE_DOCTOR"
}
```

Erros: **400** (validação), **422** (email já cadastrado).

### POST /api/v1/auth/login

Autentica e retorna o JWT. **200 OK**.

Body (`LoginRequest`):

| Campo | Tipo | Validação |
|---|---|---|
| `email` | String | obrigatório, formato de email |
| `password` | String | obrigatório |

Request:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "house@fiap.com", "password": "senha123" }'
```

Response 200: `AuthResponse` (igual ao register).

Erro **422** (credenciais inválidas):

```json
{
  "type": "https://api.fiap.com/errors/business",
  "title": "Erro de negócio",
  "status": 422,
  "detail": "Email ou senha inválidos",
  "instance": "/api/v1/auth/login",
  "timestamp": "2026-09-07T12:34:56.789Z"
}
```

---

## scheduling-service — Appointments

Base: `http://localhost:8081/api/v1/appointments` — **requer** `Authorization: Bearer <token>`.

`AppointmentResponse` (retornado pelos endpoints):

| Campo | Tipo |
|---|---|
| `id` | UUID |
| `patientId` | UUID |
| `doctorId` | UUID |
| `dateTime` | ISO-8601 (`2026-12-01T14:30:00`) |
| `status` | `SCHEDULED` \| `CONFIRMED` \| `CANCELLED` \| `COMPLETED` |
| `description` | String |
| `createdAt` | ISO-8601 |
| `updatedAt` | ISO-8601 |

### POST /api/v1/appointments

Cria uma consulta. Roles: **DOCTOR, NURSE**. **201 Created**.

Body (`CreateAppointmentRequest`):

| Campo | Tipo | Validação |
|---|---|---|
| `patientId` | UUID | obrigatório |
| `doctorId` | UUID | obrigatório |
| `dateTime` | ISO-8601 | obrigatório, deve ser futuro |
| `description` | String | obrigatório |

```bash
curl -X POST http://localhost:8081/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "patientId": "d4aa58e0-f377-43cc-a0a2-5dde188a0061",
    "doctorId": "34816130-ed9d-4a69-a8eb-6783311654d7",
    "dateTime": "2026-12-01T14:30:00",
    "description": "Consulta de rotina"
  }'
```

Response 201:

```json
{
  "id": "59bc1125-131d-4fbf-9d1e-ffa6b7168df5",
  "patientId": "d4aa58e0-f377-43cc-a0a2-5dde188a0061",
  "doctorId": "34816130-ed9d-4a69-a8eb-6783311654d7",
  "dateTime": "2026-12-01T14:30:00",
  "status": "SCHEDULED",
  "description": "Consulta de rotina",
  "createdAt": "2026-09-07T12:06:40",
  "updatedAt": "2026-09-07T12:06:40"
}
```

Erros: **400** (validação), **403** (sem token / role insuficiente), **404** (paciente/médico inexistente), **422** (regra de negócio).

### PUT /api/v1/appointments/{id}

Atualiza uma consulta. Roles: **DOCTOR, NURSE**. **200 OK**. Body (`UpdateAppointmentRequest`) — todos os campos opcionais (só os informados são atualizados): `patientId`, `doctorId`, `dateTime` (futuro), `description`.

```bash
curl -X PUT http://localhost:8081/api/v1/appointments/{id} \
  -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{ "description": "Consulta de retorno" }'
```

### PATCH /api/v1/appointments/{id}/cancel

Cancela uma consulta (sem body). Roles: **DOCTOR, NURSE, PATIENT** (paciente só as próprias). **200 OK** — retorna o `AppointmentResponse` com `status: "CANCELLED"`.

```bash
curl -X PATCH http://localhost:8081/api/v1/appointments/{id}/cancel \
  -H "Authorization: Bearer <token>"
```

### GET /api/v1/appointments/{id}

Busca por id. Autenticado (PATIENT só as suas). **200** / 403 / 404.

### GET /api/v1/appointments/patient/{patientId}

Lista as consultas de um paciente. DOCTOR/NURSE ou o próprio PATIENT. **200** (array).

### GET /api/v1/appointments/doctor/{doctorId}

Lista as consultas de um médico. Roles: DOCTOR, NURSE. **200** (array).

### GET /api/v1/appointments

Lista todas. Roles: DOCTOR, NURSE. Query param opcional `status` (`SCHEDULED`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, case-insensitive). **200** (array). Valor inválido → **400**.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8081/api/v1/appointments?status=SCHEDULED"
```

### GET /api/v1/appointments/upcoming

Consultas futuras, filtradas pela role do usuário autenticado. **200** (array).

---

## scheduling-service — Users

Base: `http://localhost:8081/api/v1/users` — **requer** token.

`UserResponse`: `id` (UUID), `name` (String), `email` (String), `role` (String, ex. `ROLE_DOCTOR`), `createdAt` (ISO-8601). A senha nunca é retornada.

### GET /api/v1/users

Lista usuários. Roles: DOCTOR, NURSE. Query param opcional `role` (`DOCTOR`, `NURSE`, `PATIENT`; aceita também `ROLE_*`). **200** (array). Inválido → **400**.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8081/api/v1/users?role=DOCTOR"
```

### GET /api/v1/users/{id}

Busca por id. Autenticado (PATIENT só o próprio perfil). **200** / 403 / 404.

---

## notification-service — Notifications

Base: `http://localhost:8082/api/v1/notifications` — **requer** token (qualquer role autenticada). As notificações são geradas de forma assíncrona pelos eventos publicados pelo scheduling-service.

`NotificationResponse`: `id` (UUID), `to` (String), `subject` (String), `body` (String), `type` (enum), `sentAt` (ISO-8601).

Tipos (`NotificationType`): `APPOINTMENT_CREATED`, `APPOINTMENT_UPDATED`, `APPOINTMENT_REMINDER`.

### GET /api/v1/notifications

Lista as notificações. Query param opcional `type` (case-insensitive). **200** (array). Tipo inválido → **422**.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8082/api/v1/notifications?type=APPOINTMENT_CREATED"
```

Response 200:

```json
[
  {
    "id": "8650fbcd-90f8-4d7c-b4b6-f1bdf5434350",
    "to": "patient:3706f4dd-c005-4988-9236-3481b488bb6a",
    "subject": "Consulta agendada",
    "body": "Sua consulta (id ...) foi agendada para 2026-09-14T15:05:11. Status atual: SCHEDULED.",
    "type": "APPOINTMENT_CREATED",
    "sentAt": "2026-09-07T12:05:11"
  }
]
```

---

## history-service — GraphQL

Endpoint: **`POST http://localhost:8083/graphql`** — **requer** `Authorization: Bearer <token>`. UI de testes (dev) em `GET /graphiql`.

O corpo é JSON com `query` e, opcionalmente, `variables`:

```json
{ "query": "query($patientId: ID!) { ... }", "variables": { "patientId": "..." } }
```

### Schema

```graphql
type Query {
    appointmentsByPatient(patientId: ID!): [AppointmentHistory]
    appointmentsByDoctor(doctorId: ID!): [AppointmentHistory]
    upcomingAppointments(patientId: ID!): [AppointmentHistory]
    appointmentHistory(id: ID!): AppointmentHistory
    allAppointmentHistories: [AppointmentHistory]
}

type Mutation {
    saveAppointmentHistory(input: AppointmentHistoryInput!): AppointmentHistory
}

input AppointmentHistoryInput {
    appointmentId: ID!
    patientId: ID!
    doctorId: ID!
    patientName: String
    doctorName: String
    dateTime: String
    status: String
    description: String
    eventType: String
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

### Controle de acesso (ownership por role)

- `appointmentsByPatient` / `upcomingAppointments`: PATIENT só o próprio `patientId`; DOCTOR/NURSE amplo.
- `appointmentsByDoctor`: PATIENT bloqueado; DOCTOR só o próprio `doctorId`; NURSE amplo.
- `appointmentHistory(id)`: PATIENT/DOCTOR só se forem parte do registro; NURSE vê qualquer.
- `allAppointmentHistories`: restrito a DOCTOR/NURSE.

### Exemplo — sucesso

```bash
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($patientId: ID!) { appointmentsByPatient(patientId: $patientId) { appointmentId status eventType dateTime } }",
    "variables": { "patientId": "d4aa58e0-f377-43cc-a0a2-5dde188a0061" }
  }'
```

```json
{
  "data": {
    "appointmentsByPatient": [
      {
        "appointmentId": "59bc1125-131d-4fbf-9d1e-ffa6b7168df5",
        "status": "SCHEDULED",
        "eventType": "CREATED",
        "dateTime": "2026-12-01T14:30:00"
      }
    ]
  }
}
```

### Exemplo — erro (GraphQL retorna HTTP 200 com `errors`)

Erros do GraphQL não usam ProblemDetail; vêm no array `errors`, com `extensions` contendo `code`, `status` e `timestamp`:

| Situação | `code` | `status` |
|---|---|---|
| Não autenticado | `UNAUTHORIZED` | 401 |
| Sem permissão (ownership/role) | `FORBIDDEN` | 403 |
| Registro não encontrado | `NOT_FOUND` | 404 |
| Regra de negócio | `BUSINESS_ERROR` | 422 |
| Validação de argumento | `BAD_REQUEST` | 400 |

```json
{
  "errors": [
    {
      "message": "Acesso negado",
      "extensions": {
        "code": "FORBIDDEN",
        "status": 403,
        "timestamp": "2026-09-07T12:34:56.789Z"
      }
    }
  ],
  "data": { "appointmentsByDoctor": null }
}
```

---

> Documentação interativa complementar: Swagger UI do scheduling em `http://localhost:8081/swagger-ui.html` e do notification em `http://localhost:8082/swagger-ui.html`. Collection pronta em [`docs/api-collection/`](api-collection/).
