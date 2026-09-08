# 📚 Documentação — Tech Challenge Fase 3

Índice geral de toda a documentação do projeto (sistema hospitalar em microsserviços).

---

## 🗺️ Quick Start — Ordem de leitura sugerida

1. [README principal](../README.md) — Visão geral, arquitetura, como executar, endpoints
2. [API.md](API.md) — Documentação detalhada dos endpoints (DTOs, exemplos, códigos HTTP)
3. [BACKLOG.md](planejamento/BACKLOG.md) — Todas as tasks e critérios de aceitação
4. [Collection Postman](api-collection/README.md) — Como importar e rodar os testes (Postman, Bruno, Newman)

---

## 📁 Estrutura

```
docs/
├── README.md                                   ← (este arquivo)
├── API.md                                      ← Documentação detalhada da API (REST + GraphQL)
├── ADJT - BB - Tech Challenge - Fase 3.pdf     ← Enunciado do desafio
├── api-collection/
│   ├── README.md                               ← Como importar/executar (Postman, Bruno, Newman)
│   └── fase3-hospital.postman_collection.json  ← Collection única (Auth, Scheduling, Users, Notification, History)
├── integracao/
│   └── TASK-026-scheduling-notification.md     ← Fluxo de integração scheduling → notification
└── planejamento/
    ├── BACKLOG.md                              ← Backlog completo (tasks, épicos, critérios)
    ├── RESUMO_CONVERSA.md                      ← Contexto e decisões técnicas
    └── HANDOFF_SESSION.md                      ← Handoff entre sessões
```

---

## 📄 Documentos

### Projeto e API

| Documento | Descrição |
|-----------|-----------|
| [README principal](../README.md) | Visão geral, tecnologias, arquitetura, como executar |
| [API.md](API.md) | Endpoints REST e GraphQL, DTOs, exemplos request/response, códigos HTTP |
| [Collection Postman](api-collection/README.md) | Collection única com cenários de sucesso/erro; Postman, Bruno, Newman |

### Planejamento

| Documento | Descrição |
|-----------|-----------|
| [BACKLOG.md](planejamento/BACKLOG.md) | Tasks, épicos e critérios de aceitação |
| [RESUMO_CONVERSA.md](planejamento/RESUMO_CONVERSA.md) | Contexto do projeto e decisões técnicas |
| [HANDOFF_SESSION.md](planejamento/HANDOFF_SESSION.md) | Handoff entre sessões de desenvolvimento |

### Integração

| Documento | Descrição |
|-----------|-----------|
| [TASK-026-scheduling-notification.md](integracao/TASK-026-scheduling-notification.md) | Fluxo de integração scheduling → RabbitMQ → notification |
