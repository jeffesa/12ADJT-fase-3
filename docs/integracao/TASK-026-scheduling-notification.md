# TASK-026 — Integração scheduling → RabbitMQ → notification (end-to-end)

Evidência da validação end-to-end do fluxo assíncrono: criar consulta no
scheduling-service publica um evento no RabbitMQ, consumido pelo
notification-service, que envia (simula) a notificação.

## Ambiente

Subido via Docker Compose:

```bash
docker-compose up --build rabbitmq postgres-scheduling scheduling-service notification-service
```

Containers healthy: `fase3-rabbitmq`, `fase3-postgres-scheduling`,
`fase3-scheduling-service`, `fase3-notification-service`.

## Passo a passo do teste

1. Registrar um médico e um paciente (POST `/api/v1/auth/register`).
2. Autenticar como médico (o token vem no registro/login).
3. Criar uma consulta:

```bash
curl -X POST http://localhost:8081/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_DOCTOR>" \
  -d '{"patientId":"<PAT_ID>","doctorId":"<DOC_ID>","dateTime":"2099-12-01T14:30:00","description":"Consulta"}'
```

## Resultado observado (critérios de aceitação)

| # | Critério | Resultado |
|---|----------|-----------|
| 1 | Criar consulta via API REST | `HTTP 201` com a consulta criada |
| 2 | Mensagem publicada na exchange | `appointment.exchange`: `publish_in=1`, `publish_out=2` (roteou para as filas notification e history) |
| 3 | Notification consome e loga | log `[NOTIFICAÇÃO ENVIADA] type=APPOINTMENT_CREATED ... subject='Consulta agendada'` |
| 4 | Fluxo funcional via Docker Compose | 4 containers `healthy` |
| 5 | Log de confirmação no notification | body da notificação contém o `appointmentId` da consulta criada |

## Log de confirmação (exemplo real capturado)

```
c.f.n.i.messaging.LogNotificationSender : [NOTIFICAÇÃO ENVIADA]
  type=APPOINTMENT_CREATED | to=patient:355816de-...
  subject='Consulta agendada'
  body='Sua consulta (id 40c9754c-...) com o médico 876782d6-... foi agendada
        para 2099-12-01T14:30. Status atual: SCHEDULED.'
```

## Como verificar a publicação no RabbitMQ

RabbitMQ Management UI: http://localhost:15672 (guest/guest) → aba
**Exchanges** → `appointment.exchange` → seção *Message rates* mostra as
publicações; aba **Queues** mostra o consumo em `appointment.notification.queue`.

## Conclusão

Fluxo scheduling → RabbitMQ → notification validado end-to-end com sucesso.
Uma publicação foi roteada para 2 filas (notification + history) via exchange
do tipo *topic*, confirmando o desacoplamento e o fan-out por routing key.
