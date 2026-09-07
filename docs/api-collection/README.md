# Collection Postman — FIAP Fase 3 (Sistema Hospitalar)

Collection completa para testar os 3 microsserviços do projeto via Postman ou Newman.

| Arquivo | Descrição |
|---|---|
| `fase3-hospital.postman_collection.json` | Collection com todos os endpoints REST e queries GraphQL |
| `fase3-local.postman_environment.json` | Environment apontando para o ambiente local (Docker Compose) |

## Serviços cobertos

| Serviço | Porta | Tipo | Pasta na collection |
|---|---|---|---|
| scheduling-service | 8081 | REST (Auth JWT + CRUD consultas + usuários) | `Auth`, `Scheduling`, `Users` |
| notification-service | 8082 | REST (consulta de notificações) | `Notification` |
| history-service | 8083 | GraphQL (histórico de consultas) | `History` |

## Pré-requisitos

- Os 3 serviços no ar (via `docker-compose up --build` na raiz do projeto). Ver README principal.
- Para rodar via CLI: Node.js instalado (`npx newman`, sem instalação global necessária).

## Como usar no Postman

1. **Import** > selecione os dois arquivos JSON (collection + environment).
2. No canto superior direito, selecione o environment **FIAP Fase 3 - Local**.
3. Rode a pasta **Auth** primeiro. O request "Register DOCTOR" e "Login DOCTOR" salvam o token JWT na variável `{{token}}`, e "Register PATIENT" salva `{{patientId}}`.
4. Rode as demais pastas (**Scheduling**, **Users**, **Notification**, **History**). Todos os requests autenticados usam `Authorization: Bearer {{token}}`.

### Auto-login (pré-request)

A collection tem um script de **pré-request no nível da collection** que faz login automático se `{{token}}` estiver vazio e já existirem `doctorEmail`/`doctorPassword` no environment. Assim, mesmo executando um request isolado, o token é obtido automaticamente.

## Como rodar com Newman (CLI)

Na pasta `docs/api-collection/`:

```bash
npx newman run fase3-hospital.postman_collection.json \
  -e fase3-local.postman_environment.json
```

O Newman executa os requests na ordem das pastas, encadeando os dados (registra usuários → login → cria consulta → consulta histórico). Os scripts de teste validam status codes e o corpo das respostas.

> Dica: os e-mails de registro usam timestamp (`doctor_<ts>@fiap.com`) para evitar conflito de duplicidade no Postgres, que tem volume persistente. Você pode rodar a collection várias vezes sem limpar o banco.

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `schedulingUrl` / `notificationUrl` / `historyUrl` | URLs base dos 3 serviços |
| `token` | JWT do DOCTOR, populado no login (usado nos requests autenticados) |
| `patientToken` | JWT do PATIENT (para cenários de ownership) |
| `doctorId` / `patientId` | IDs dos usuários registrados |
| `appointmentId` | ID da consulta criada (usado em get/update/cancel) |
| `appointmentDateTime` | Data/hora futura gerada no pré-request da criação |

## Cenários incluídos

**Sucesso:** register (DOCTOR/PATIENT), login, criar/buscar/atualizar/listar/cancelar consulta, listar por paciente e por médico, upcoming, listar usuários, listar notificações (com e sem filtro de tipo), queries GraphQL (`appointmentsByPatient`, `appointmentsByDoctor`, `allAppointmentHistories`, `upcomingAppointments`).

**Erro:** login com senha inválida, criar consulta sem token (401/403), buscar consulta inexistente (404), listar notificações sem token, GraphQL sem token.

## Fluxo de dados entre requests

```
Register DOCTOR ─► token, doctorId
Register PATIENT ─► patientId, patientToken
       │
Create appointment (patientId + doctorId) ─► appointmentId
       │
Get / Update / Cancel appointment (usa appointmentId)
       │
History GraphQL (usa patientId / doctorId)  ◄── populado via evento RabbitMQ
```

> O history-service e o notification-service são populados de forma assíncrona (eventos RabbitMQ publicados pelo scheduling ao criar/atualizar consultas). Ao rodar tudo em sequência rápida, pode haver um pequeno atraso até o evento ser consumido; as queries de histórico retornam `200` mesmo que a lista ainda esteja vazia nesse instante.
