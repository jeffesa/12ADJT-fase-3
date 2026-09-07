# Collection — FIAP Fase 3 (Sistema Hospitalar)

Collection única para testar os 3 microsserviços do projeto. Compatível com **Postman**, **Bruno** e **Newman** (schema Postman Collection v2.1.0).

| Arquivo | Descrição |
|---|---|
| `fase3-hospital.postman_collection.json` | Collection única com todos os endpoints REST + GraphQL, variáveis embutidas e cenários de teste |

Não há arquivo de environment separado: as variáveis (URLs dos 3 serviços, token, IDs) ficam embutidas na própria collection (`collectionVariables`), no mesmo modelo da collection da fase 2.

## Serviços cobertos

| Serviço | Porta | Tipo | Pasta |
|---|---|---|---|
| scheduling-service | 8081 | REST (Auth JWT + CRUD consultas + usuários) | `Auth`, `Scheduling`, `Users` |
| notification-service | 8082 | REST (consulta de notificações) | `Notification` |
| history-service | 8083 | GraphQL (histórico de consultas) | `History` |

As pastas acima ficam dentro de `local` (ambiente Docker Compose). O ambiente de produção (`prod`) será adicionado na TASK-040, quando houver deploy.

## Como importar no Bruno

1. Bruno > **Import Collection** > escolha **Postman Collection** e selecione `fase3-hospital.postman_collection.json`.
2. Abra a pasta `local` e rode **"Selecionar ambiente local"** primeiro (aponta as variáveis para `localhost:8081/8082/8083` e limpa o token).
3. Rode a pasta `Auth` (o **Register DOCTOR** e o **Login DOCTOR** salvam o token; **Register PATIENT** salva o `patientId`).
4. Rode `Scheduling`, `Users`, `Notification`, `History`.

> O auto-login (script de pré-request no nível da collection) faz login automaticamente se o token estiver vazio e já houver credenciais de DOCTOR salvas, então requests isolados também funcionam.

## Como usar no Postman

Import > selecione o arquivo. Rode `local > Selecionar ambiente local` e depois as pastas na ordem `Auth → Scheduling → Users → Notification → History`.

## Como rodar com Newman (CLI)

Na pasta `docs/api-collection/`:

```bash
# ambiente local inteiro
npx newman run fase3-hospital.postman_collection.json --folder local
```

Última execução validada contra o docker-compose real: **25 requests, 40 assertions, 0 falhas**.

## Variáveis (embutidas na collection)

| Variável | Descrição |
|---|---|
| `localScheduling` / `localNotification` / `localHistory` | URLs locais (8081/8082/8083) |
| `prodScheduling` / `prodNotification` / `prodHistory` | URLs de produção (ajuste conforme seu deploy) |
| `baseUrl` / `notificationUrl` / `historyUrl` | URLs ativas, definidas pelo request "Selecionar ambiente" |
| `token` | JWT do DOCTOR (usado nos requests autenticados) |
| `patientToken` | JWT do PATIENT (cenários de ownership) |
| `doctorId` / `patientId` | IDs dos usuários registrados |
| `appointmentId` | ID da consulta criada |
| `appointmentDateTime` | data/hora futura gerada no pré-request |

## Cenários

**Sucesso:** register (DOCTOR/PATIENT), login, criar/buscar/atualizar/listar/cancelar consulta, listar por paciente e por médico, upcoming, listar usuários, listar notificações (com e sem filtro), queries GraphQL (`appointmentsByPatient`, `appointmentsByDoctor`, `allAppointmentHistories`, `appointmentHistory` por id, `upcomingAppointments`).

**Erro:** login com senha inválida, criar consulta sem token, buscar consulta inexistente, notificações sem token, GraphQL sem token.

> `notification-service` e `history-service` são populados de forma assíncrona por eventos RabbitMQ publicados pelo scheduling ao criar/atualizar consultas.
