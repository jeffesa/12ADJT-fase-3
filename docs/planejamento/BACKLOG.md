# BACKLOG - Tech Challenge Fase 3
## Sistema Hospitalar de Agendamento de Consultas

> **Continuação da Fase 2** — Este projeto implementa um sistema hospitalar com microsserviços,
> comunicação assíncrona (RabbitMQ + DLQ), segurança (Spring Security + JWT) e GraphQL,
> utilizando Clean Architecture conforme padrão estabelecido na Fase 2.

---

## 📋 ÉPICO 1: Configuração Inicial do Projeto e Infraestrutura

### [TASK-001: Criar estrutura multi-module Maven](#task-001)

**Labels:** `priority: high`, `épico: setup`, `pontos: 5`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Configurar o projeto Maven multi-module com parent POM e 3 módulos filhos (scheduling-service, notification-service, history-service). Cada módulo com seu próprio pom.xml, seguindo o padrão Spring Boot 3.2.x com Java 17.

## ✅ Critérios de Aceitação
- [ ] Parent POM na raiz com `<modules>` definidos
- [ ] Módulo `scheduling-service` com pom.xml próprio
- [ ] Módulo `notification-service` com pom.xml próprio
- [ ] Módulo `history-service` com pom.xml próprio
- [ ] Dependências comuns gerenciadas no parent (Spring Boot BOM, Java 17)
- [ ] Cada módulo inicia sem erros (`mvn clean install -DskipTests`)
- [ ] .gitignore configurado (target/, .idea/, .DS_Store, .env, etc.)
- [ ] Profiles configurados por módulo (dev com H2, test com H2, prod com PostgreSQL)

## 🔧 Dependências Técnicas
- Java 17+
- Spring Boot 3.2.x
- Maven 3.8+

---

### [TASK-002: Configurar Clean Architecture em cada módulo](#task-002)

**Labels:** `priority: high`, `épico: setup`, `pontos: 3`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Organizar pacotes seguindo Clean Architecture em cada módulo, reutilizando o padrão da Fase 2: domain → application → infra. Cada serviço terá pacotes base com seu domínio específico.

## ✅ Critérios de Aceitação
- [ ] scheduling-service: `com.fiap.scheduling.{domain,application,infra}`
- [ ] notification-service: `com.fiap.notification.{domain,application,infra}`
- [ ] history-service: `com.fiap.history.{domain,application,infra}`
- [ ] Cada módulo com subpacotes: domain/{entity,gateway,shared}, application/usecase, infra/{web,persistence,config,messaging}
- [ ] Dependências fluem de fora para dentro (infra → application → domain)

## 🔧 Dependências Técnicas
- [TASK-001](#task-001) concluída

---

### [TASK-003: Configurar Docker Compose multi-serviço](#task-003)

**Labels:** `priority: high`, `épico: setup`, `pontos: 5`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Criar docker-compose.yml na raiz com todos os serviços: 3 apps + PostgreSQL (scheduling e history) + RabbitMQ. Cada app com seu Dockerfile multi-stage.

## ✅ Critérios de Aceitação
- [ ] Dockerfile multi-stage para cada módulo (Maven build + JRE runtime)
- [ ] docker-compose.yml com serviços:
  - [ ] `postgres-scheduling` (porta 5433)
  - [ ] `postgres-history` (porta 5434)
  - [ ] `rabbitmq` (portas 5672 + 15672 para management UI)
  - [ ] `scheduling-service` (porta 8081)
  - [ ] `notification-service` (porta 8082)
  - [ ] `history-service` (porta 8083)
- [ ] .dockerignore configurado
- [ ] Networks configuradas entre serviços
- [ ] Health checks implementados em todos os serviços
- [ ] Volumes para persistência dos bancos
- [ ] Variáveis de ambiente configuráveis
- [ ] Sobe com um único comando (`docker-compose up --build`)

## 🔧 Dependências Técnicas
- [TASK-001](#task-001) concluída

---

### [TASK-004: Configurar RabbitMQ com exchanges, queues e DLQ](#task-004)

**Labels:** `priority: high`, `épico: setup`, `pontos: 5`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Configurar RabbitMQ com Spring AMQP, incluindo exchanges, queues, bindings e Dead Letter Queue (DLQ) para mensagens que falharem no processamento.

## ✅ Critérios de Aceitação
- [ ] Dependência `spring-boot-starter-amqp` nos módulos scheduling e notification
- [ ] Exchange: `appointment.exchange` (topic)
- [ ] Queue principal: `appointment.notification.queue`
- [ ] Queue DLQ: `appointment.notification.dlq`
- [ ] Binding: routing key `appointment.created` e `appointment.updated`
- [ ] Configuração de retry (3 tentativas antes de enviar para DLQ)
- [ ] TTL na DLQ (mensagens expiram após X tempo ou são reprocessadas)
- [ ] RabbitConfig class em cada módulo que usa mensageria
- [ ] Classe de configuração com `@Configuration` e declarações de Queue/Exchange/Binding
- [ ] Connection factory configurada via application.yml (host, port, user, pass)

## 🔧 Dependências Técnicas
- [TASK-003](#task-003) concluída (RabbitMQ rodando no Docker)

---

### [TASK-005: Configurar Spring Security + JWT](#task-005)

**Labels:** `priority: high`, `épico: setup`, `pontos: 5`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Implementar autenticação e autorização com Spring Security e JWT. O scheduling-service será o responsável pelo login/registro e geração de tokens. Os demais serviços validam o token.

## ✅ Critérios de Aceitação
- [ ] Dependência `spring-boot-starter-security` em todos os módulos
- [ ] Dependência `jjwt` (io.jsonwebtoken) para geração/validação de JWT
- [ ] Endpoint POST `/api/v1/auth/register` (criar usuário com role)
- [ ] Endpoint POST `/api/v1/auth/login` (retorna JWT token)
- [ ] Roles definidas: `ROLE_DOCTOR`, `ROLE_NURSE`, `ROLE_PATIENT`
- [ ] JwtTokenProvider: gera e valida tokens
- [ ] JwtAuthenticationFilter: intercepta requests e valida Bearer token
- [ ] SecurityConfig: define quais endpoints exigem quais roles
- [ ] Módulo compartilhado ou duplicação da validação JWT nos 3 serviços
- [ ] Token contém: userId, email, role, expiração

## 🔧 Dependências Técnicas
- [TASK-002](#task-002) concluída

---

### [TASK-006: Configurar OpenAPI/Swagger no scheduling-service](#task-006)

**Labels:** `priority: high`, `épico: setup`, `pontos: 2`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Configurar SpringDoc OpenAPI no scheduling-service com suporte a Bearer token no Swagger UI.

## ✅ Critérios de Aceitação
- [ ] Swagger UI acessível em `/swagger-ui.html`
- [ ] OpenApiConfig com título, descrição, versão e contato
- [ ] Configuração de SecurityScheme (Bearer JWT) no Swagger
- [ ] Tags organizadas por domínio (Auth, Appointments, Users)
- [ ] Endpoints documentados com @Operation, @ApiResponse

## 🔧 Dependências Técnicas
- [TASK-005](#task-005) concluída

---

### [TASK-007: Configurar tratamento global de erros (ProblemDetail)](#task-007)

**Labels:** `priority: high`, `épico: setup`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Implementar GlobalExceptionHandler com ProblemDetail (RFC 7807) em cada módulo, reutilizando o padrão da Fase 2.

## ✅ Critérios de Aceitação
- [ ] @RestControllerAdvice em cada módulo
- [ ] Respostas de erro seguem RFC 7807
- [ ] Tratamento de MethodArgumentNotValidException (400)
- [ ] Tratamento de EntityNotFoundException (404)
- [ ] Tratamento de BusinessException (422)
- [ ] Tratamento de AccessDeniedException (403)
- [ ] Tratamento de AuthenticationException (401)
- [ ] Logs de erros apropriados

## 🔧 Dependências Técnicas
- [TASK-002](#task-002) concluída

---

### [TASK-042: Configurar CI/CD básico (build + test + branch naming)](#task-042)

**Labels:** `priority: high`, `épico: setup`, `pontos: 3`, `type: infra`  
**Milestone:** Sprint 1 - Fundação

## 📋 Descrição
Configurar GitHub Actions com pipeline básico de build e testes desde o início do projeto. Reutilizar os workflows da Fase 2 adaptados para multi-module Maven. O refinamento com SonarCloud e badges fica na TASK-032 (Sprint 6).

## ✅ Critérios de Aceitação
- [ ] Workflow `ci-cd.yml`: build + test em push/PR para develop e main
  - [ ] Maven build multi-module (`mvn -B clean verify`)
  - [ ] Cache de Maven configurado
  - [ ] Java 17 (temurin)
- [ ] Workflow `branch-naming.yml`: validação de nomes de branch em PRs
  - [ ] Prefixos permitidos: feature/, bugfix/, hotfix/, release/, chore/, docs/, test/, refactor/, perf/, ci/, style/
- [ ] Workflow `auto-pr-to-main.yml`: cria PR automático develop → main
  - [ ] Auto-merge habilitado
- [ ] Build falha se testes falharem
- [ ] Pipelines validados com push de teste

## 🔧 Dependências Técnicas
- [TASK-001](#task-001) concluída (projeto compila)

---

## 📋 ÉPICO 2: Serviço de Agendamento (scheduling-service)

### [TASK-008: Criar entidade de domínio User e gateway](#task-008)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Criar entidade User no domain do scheduling-service (POJO puro) e interface UserGateway. Usuários possuem roles (DOCTOR, NURSE, PATIENT).

## ✅ Critérios de Aceitação
- [ ] Classe User com campos: id (UUID), name, email, password (hash), role (enum: DOCTOR, NURSE, PATIENT), createdAt
- [ ] Enum UserRole: DOCTOR, NURSE, PATIENT
- [ ] POJO puro sem anotações de framework
- [ ] Interface UserGateway: create, findById, findByEmail, findAll, findByRole
- [ ] Validações de domínio (email válido, nome não vazio)

## 🔧 Dependências Técnicas
- [TASK-002](#task-002) concluída

---

### [TASK-009: Criar entidade de domínio Appointment e gateway](#task-009)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Criar entidade Appointment no domain do scheduling-service. Representa uma consulta médica agendada.

## ✅ Critérios de Aceitação
- [ ] Classe Appointment com campos: id (UUID), patientId (UUID), doctorId (UUID), dateTime (LocalDateTime), status (enum), description, createdAt, updatedAt
- [ ] Enum AppointmentStatus: SCHEDULED, CONFIRMED, CANCELLED, COMPLETED
- [ ] POJO puro sem anotações de framework
- [ ] Interface AppointmentGateway: create, update, delete, findById, findByPatientId, findByDoctorId, findByDateRange, findUpcoming
- [ ] Validações de domínio (dateTime no futuro para criação, status transitions válidos)

## 🔧 Dependências Técnicas
- [TASK-008](#task-008) concluída

---

### [TASK-010: Criar casos de uso de Auth (Register/Login)](#task-010)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 5`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Implementar use cases de autenticação: registro de usuário e login com geração de JWT.

## ✅ Critérios de Aceitação
- [ ] RegisterUserUseCase — cria usuário com validação de email único, hash da senha (BCrypt), role obrigatória
- [ ] LoginUseCase — valida credenciais, retorna JWT token com role e userId
- [ ] Interface PasswordHasher no domain (implementação BCrypt na infra)
- [ ] Interface TokenProvider no domain (implementação JWT na infra)
- [ ] Use cases dependem apenas de interfaces do domain

## 🔧 Dependências Técnicas
- [TASK-008](#task-008) concluída
- [TASK-005](#task-005) concluída

---

### [TASK-011: Criar casos de uso de Appointment](#task-011)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 5`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Implementar use cases de agendamento de consultas com regras de acesso por role.

## ✅ Critérios de Aceitação
- [ ] CreateAppointmentUseCase — médicos e enfermeiros criam consultas; valida que paciente e médico existem; dateTime deve ser futuro
- [ ] UpdateAppointmentUseCase — médicos e enfermeiros editam; valida existência e permissões
- [ ] CancelAppointmentUseCase — médicos, enfermeiros e o próprio paciente podem cancelar
- [ ] FindAppointmentByIdUseCase — qualquer role autenticada (paciente só vê as suas)
- [ ] FindAppointmentsByPatientUseCase — paciente vê as suas; médico/enfermeiro vê de qualquer paciente
- [ ] FindAppointmentsByDoctorUseCase — médico vê as suas; enfermeiro vê de qualquer médico
- [ ] FindUpcomingAppointmentsUseCase — consultas futuras filtradas por role
- [ ] Após criar/editar, disparar evento para mensageria (interface EventPublisher no domain)

## 🔧 Dependências Técnicas
- [TASK-009](#task-009) concluída
- [TASK-010](#task-010) concluída

---

### [TASK-012: Criar persistência JPA do scheduling-service](#task-012)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 5`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Implementar adapters de saída: entidades JPA, repositories Spring Data, implementações de gateways.

## ✅ Critérios de Aceitação
- [ ] UserJpaEntity com anotações JPA (@Entity, @Table, @Id)
- [ ] AppointmentJpaEntity com anotações JPA e relacionamentos ManyToOne
- [ ] UserRepository (Spring Data JPA)
- [ ] AppointmentRepository (Spring Data JPA) com queries customizadas
- [ ] UserJpaGateway implementando UserGateway
- [ ] AppointmentJpaGateway implementando AppointmentGateway
- [ ] Mapeamento bidirecional entre entidades de domínio e JPA
- [ ] Tabelas: `users`, `appointments`
- [ ] Flyway ou DDL-auto para criação de schema

## 🔧 Dependências Técnicas
- [TASK-009](#task-009) concluída

---

### [TASK-013: Criar controllers REST do scheduling-service](#task-013)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 5`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Implementar controllers REST com DTOs, validações Bean Validation e documentação Swagger. Proteger endpoints por role.

## ✅ Critérios de Aceitação
- [ ] AuthController:
  - [ ] POST `/api/v1/auth/register` (público) — 201
  - [ ] POST `/api/v1/auth/login` (público) — 200
- [ ] AppointmentController:
  - [ ] POST `/api/v1/appointments` (DOCTOR, NURSE) — 201
  - [ ] PUT `/api/v1/appointments/{id}` (DOCTOR, NURSE) — 200
  - [ ] PATCH `/api/v1/appointments/{id}/cancel` (DOCTOR, NURSE, PATIENT) — 200
  - [ ] GET `/api/v1/appointments/{id}` (autenticado) — 200, 403, 404
  - [ ] GET `/api/v1/appointments/patient/{patientId}` (DOCTOR, NURSE, ou próprio PATIENT) — 200
  - [ ] GET `/api/v1/appointments/doctor/{doctorId}` (DOCTOR, NURSE) — 200
  - [ ] GET `/api/v1/appointments/upcoming` (autenticado, filtrado por role) — 200
- [ ] DTOs como records com Bean Validation e Swagger annotations
- [ ] SecurityConfig: proteção por role em cada endpoint
- [ ] Paciente só vê suas próprias consultas (validação no use case ou controller)

## 🔧 Dependências Técnicas
- [TASK-011](#task-011) e [TASK-012](#task-012) concluídas
- [TASK-005](#task-005) concluída (JWT filter)

---

### [TASK-014: Implementar publicação de eventos no RabbitMQ](#task-014)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Implementar o EventPublisher que publica mensagens no RabbitMQ quando uma consulta é criada ou editada.

## ✅ Critérios de Aceitação
- [ ] Interface EventPublisher no domain (método publish(AppointmentEvent))
- [ ] Classe AppointmentEvent no domain (DTO de evento: appointmentId, patientId, doctorId, dateTime, status, eventType)
- [ ] RabbitEventPublisher na infra implementando EventPublisher
- [ ] Publica mensagem JSON na exchange `appointment.exchange`
- [ ] Routing key: `appointment.created` para criação, `appointment.updated` para edição
- [ ] Mensagem contém dados suficientes para o notification-service enviar lembrete
- [ ] Serialização com Jackson (MessageConverter configurado)

## 🔧 Dependências Técnicas
- [TASK-004](#task-004) concluída (RabbitMQ configurado)
- [TASK-011](#task-011) concluída (use cases chamam EventPublisher)

---

### [TASK-015: Testes do scheduling-service](#task-015)

**Labels:** `priority: high`, `épico: scheduling`, `pontos: 5`, `type: test`  
**Milestone:** Sprint 2 - Scheduling Core

## 📋 Descrição
Testes unitários e de integração para o scheduling-service.

## ✅ Critérios de Aceitação
- [ ] Testes unitários dos use cases com mocks (JUnit 5 + Mockito)
- [ ] Testes do controller com MockMvc (incluindo validação de roles)
- [ ] Testes de integração com @SpringBootTest e H2
- [ ] Testes do JWT filter (token válido, inválido, expirado)
- [ ] Testes de autorização (role correta/incorreta)
- [ ] Cenários de sucesso e erro cobertos
- [ ] Cobertura ≥ 80% no módulo

## 🔧 Dependências Técnicas
- [TASK-013](#task-013) e [TASK-014](#task-014) concluídas

---

## 📋 ÉPICO 3: Serviço de Notificações (notification-service)

### [TASK-016: Criar consumer RabbitMQ no notification-service](#task-016)

**Labels:** `priority: high`, `épico: notification`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 3 - Notification

## 📋 Descrição
Implementar o consumer que escuta a fila `appointment.notification.queue` e processa eventos de consulta para enviar notificações.

## ✅ Critérios de Aceitação
- [ ] Classe AppointmentNotificationListener com `@RabbitListener`
- [ ] Deserializa mensagem JSON para AppointmentEvent
- [ ] Delega para use case de processamento
- [ ] Configuração de concurrency (quantos consumers simultâneos)
- [ ] Acknowledgment manual (para garantir processamento)

## 🔧 Dependências Técnicas
- [TASK-004](#task-004) concluída (filas configuradas)

---

### [TASK-017: Criar use case de envio de notificação](#task-017)

**Labels:** `priority: high`, `épico: notification`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 3 - Notification

## 📋 Descrição
Implementar lógica de processamento de notificação. O envio real pode ser simulado (log ou mock de email), mas a estrutura deve ser extensível.

## ✅ Critérios de Aceitação
- [ ] SendNotificationUseCase — recebe evento, monta mensagem, envia via NotificationSender
- [ ] Interface NotificationSender no domain (método send(Notification))
- [ ] Classe Notification no domain (to, subject, body, type, sentAt)
- [ ] Enum NotificationType: APPOINTMENT_CREATED, APPOINTMENT_UPDATED, APPOINTMENT_REMINDER
- [ ] Implementação LogNotificationSender na infra (loga no console simulando envio)
- [ ] Opcionalmente: EmailNotificationSender com JavaMailSender (mock ou MailHog)
- [ ] Registro de notificações enviadas (persistir ou logar)

## 🔧 Dependências Técnicas
- [TASK-016](#task-016) concluída

---

### [TASK-018: Implementar DLQ e retry no notification-service](#task-018)

**Labels:** `priority: high`, `épico: notification`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 3 - Notification

## 📋 Descrição
Configurar retry policy e Dead Letter Queue para mensagens que falharem no processamento.

## ✅ Critérios de Aceitação
- [ ] Retry: 3 tentativas com backoff exponencial (1s, 2s, 4s)
- [ ] Após 3 falhas, mensagem vai para `appointment.notification.dlq`
- [ ] DLQ com TTL opcional (mensagens expiram ou ficam para reprocessamento manual)
- [ ] Endpoint (ou log) para visualizar mensagens na DLQ
- [ ] Teste que simula falha e verifica que mensagem vai para DLQ
- [ ] Tratamento de exceções no listener (não perde mensagem)

## 🔧 Dependências Técnicas
- [TASK-017](#task-017) concluída

---

### [TASK-019: Testes do notification-service](#task-019)

**Labels:** `priority: high`, `épico: notification`, `pontos: 3`, `type: test`  
**Milestone:** Sprint 3 - Notification

## 📋 Descrição
Testes unitários e de integração para o notification-service.

## ✅ Critérios de Aceitação
- [ ] Testes unitários do use case com mock do NotificationSender
- [ ] Testes do listener com mensagem simulada
- [ ] Teste de DLQ (mensagem rejeitada vai para DLQ)
- [ ] Testes de integração com RabbitMQ embarcado (ou Testcontainers)
- [ ] Cenários de sucesso e erro cobertos
- [ ] Cobertura ≥ 80% no módulo

## 🔧 Dependências Técnicas
- [TASK-018](#task-018) concluída

---

## 📋 ÉPICO 4: Serviço de Histórico (history-service) com GraphQL

### [TASK-020: Criar entidade de domínio AppointmentHistory e gateway](#task-020)

**Labels:** `priority: high`, `épico: history`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Criar entidade que armazena o histórico de consultas no history-service. Dados recebidos via eventos do RabbitMQ.

## ✅ Critérios de Aceitação
- [ ] Classe AppointmentHistory com campos: id (UUID), appointmentId (UUID), patientId (UUID), doctorId (UUID), patientName, doctorName, dateTime, status, description, eventType, receivedAt
- [ ] POJO puro sem anotações de framework
- [ ] Interface AppointmentHistoryGateway: save, findByPatientId, findByDoctorId, findUpcomingByPatientId, findAll

## 🔧 Dependências Técnicas
- [TASK-002](#task-002) concluída

---

### [TASK-021: Criar consumer RabbitMQ no history-service](#task-021)

**Labels:** `priority: high`, `épico: history`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Implementar consumer que escuta eventos de consulta e persiste no banco do history-service.

## ✅ Critérios de Aceitação
- [ ] Queue separada: `appointment.history.queue` (mesmo exchange, binding próprio)
- [ ] DLQ: `appointment.history.dlq`
- [ ] Classe AppointmentHistoryListener com `@RabbitListener`
- [ ] Deserializa evento e delega para SaveAppointmentHistoryUseCase
- [ ] Persiste no banco local do history-service

## 🔧 Dependências Técnicas
- [TASK-004](#task-004) concluída
- [TASK-020](#task-020) concluída

---

### [TASK-022: Criar persistência JPA do history-service](#task-022)

**Labels:** `priority: high`, `épico: history`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Implementar adapter de saída com JPA para o history-service.

## ✅ Critérios de Aceitação
- [ ] AppointmentHistoryJpaEntity com anotações JPA
- [ ] AppointmentHistoryRepository (Spring Data JPA)
- [ ] AppointmentHistoryJpaGateway implementando AppointmentHistoryGateway
- [ ] Queries otimizadas para as consultas GraphQL (findByPatientId, upcoming, etc.)
- [ ] Tabela `appointment_history` no banco postgres-history

## 🔧 Dependências Técnicas
- [TASK-020](#task-020) concluída

---

### [TASK-023: Configurar Spring for GraphQL](#task-023)

**Labels:** `priority: high`, `épico: history`, `pontos: 5`, `type: feature`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Configurar Spring for GraphQL no history-service com schema-first approach.

## ✅ Critérios de Aceitação
- [ ] Dependência `spring-boot-starter-graphql`
- [ ] Schema GraphQL em `src/main/resources/graphql/schema.graphqls`:
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
- [ ] GraphQL endpoint acessível em `/graphql`
- [ ] GraphiQL UI habilitada em `/graphiql` (para testes)
- [ ] Configuração de segurança (JWT) no GraphQL endpoint

## 🔧 Dependências Técnicas
- [TASK-022](#task-022) concluída
- [TASK-005](#task-005) concluída (JWT para proteger endpoint)

---

### [TASK-024: Criar resolvers GraphQL](#task-024)

**Labels:** `priority: high`, `épico: history`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Implementar Query resolvers para as consultas GraphQL, delegando para use cases.

## ✅ Critérios de Aceitação
- [ ] AppointmentHistoryQueryResolver com `@QueryMapping`
- [ ] FindAppointmentsByPatientUseCase — lista histórico do paciente
- [ ] FindAppointmentsByDoctorUseCase — lista histórico por médico
- [ ] FindUpcomingAppointmentsUseCase — lista consultas futuras
- [ ] FindAppointmentHistoryByIdUseCase — busca por ID
- [ ] Validação de acesso por role (paciente só vê seus dados)
- [ ] Cada resolver delega para use case no application layer

## 🔧 Dependências Técnicas
- [TASK-023](#task-023) concluída

---

### [TASK-025: Testes do history-service](#task-025)

**Labels:** `priority: high`, `épico: history`, `pontos: 5`, `type: test`  
**Milestone:** Sprint 4 - History

## 📋 Descrição
Testes unitários e de integração para o history-service, incluindo testes de GraphQL.

## ✅ Critérios de Aceitação
- [ ] Testes unitários dos use cases com mocks
- [ ] Testes dos resolvers GraphQL com GraphQlTester (Spring)
- [ ] Testes do listener RabbitMQ
- [ ] Testes de integração com H2 + GraphQL endpoint
- [ ] Testes de autorização (role correta/incorreta no GraphQL)
- [ ] Cenários de sucesso e erro cobertos
- [ ] Cobertura ≥ 80% no módulo

## 🔧 Dependências Técnicas
- [TASK-024](#task-024) concluída

---

## 📋 ÉPICO 5: Integração e Comunicação entre Serviços

### [TASK-026: Integrar scheduling-service → RabbitMQ → notification-service](#task-026)

**Labels:** `priority: high`, `épico: integração`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 5 - Integração

## 📋 Descrição
Teste de integração end-to-end: criar consulta no scheduling → evento publicado → notification-service consome e "envia" notificação.

## ✅ Critérios de Aceitação
- [ ] Criar consulta via API REST do scheduling-service
- [ ] Verificar que mensagem foi publicada na exchange
- [ ] Notification-service consome e loga notificação
- [ ] Fluxo funcional via Docker Compose
- [ ] Log de confirmação visível no notification-service

## 🔧 Dependências Técnicas
- [TASK-014](#task-014) concluída (publisher)
- [TASK-017](#task-017) concluída (consumer)

---

### [TASK-027: Integrar scheduling-service → RabbitMQ → history-service](#task-027)

**Labels:** `priority: high`, `épico: integração`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 5 - Integração

## 📋 Descrição
Teste de integração end-to-end: criar consulta no scheduling → evento publicado → history-service consome e persiste → consultável via GraphQL.

## ✅ Critérios de Aceitação
- [ ] Criar consulta via API REST do scheduling-service
- [ ] History-service consome evento e persiste
- [ ] Consulta via GraphQL retorna o registro
- [ ] Fluxo funcional via Docker Compose
- [ ] Dado persiste corretamente no postgres-history

## 🔧 Dependências Técnicas
- [TASK-014](#task-014) concluída (publisher)
- [TASK-021](#task-021) concluída (consumer history)
- [TASK-024](#task-024) concluída (GraphQL resolvers)

---

### [TASK-028: Testar fluxo de DLQ end-to-end](#task-028)

**Labels:** `priority: high`, `épico: integração`, `pontos: 2`, `type: test`  
**Milestone:** Sprint 5 - Integração

## 📋 Descrição
Validar que mensagens com falha no processamento vão para a DLQ conforme esperado.

## ✅ Critérios de Aceitação
- [ ] Simular falha no notification-service (ex: exception no listener)
- [ ] Verificar que após 3 retries, mensagem vai para DLQ
- [ ] Mensagem na DLQ contém dados originais + headers de erro
- [ ] Verificar via RabbitMQ Management UI (localhost:15672)
- [ ] Documentar o fluxo de DLQ

## 🔧 Dependências Técnicas
- [TASK-018](#task-018) concluída (DLQ configurada)
- [TASK-026](#task-026) concluída (integração funcional)

---

## 📋 ÉPICO 6: Segurança e Controle de Acesso

### [TASK-029: Implementar controle de acesso por role nos endpoints](#task-029)

**Labels:** `priority: high`, `épico: segurança`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 5 - Integração

## 📋 Descrição
Garantir que cada endpoint respeita as regras de acesso definidas no PDF.

## ✅ Critérios de Aceitação
- [ ] Médicos: podem visualizar e editar o histórico de consultas
- [ ] Enfermeiros: podem registrar consultas e acessar o histórico
- [ ] Pacientes: podem visualizar apenas as suas consultas
- [ ] Endpoints de criação/edição: apenas DOCTOR e NURSE
- [ ] Endpoints de visualização: todos autenticados (com filtro por role)
- [ ] GraphQL: validação de acesso por role no resolver
- [ ] Testes de autorização para cada cenário
- [ ] Retorno 403 quando role não tem permissão

## 🔧 Dependências Técnicas
- [TASK-013](#task-013) concluída (controllers)
- [TASK-024](#task-024) concluída (resolvers GraphQL)

---

### [TASK-030: Testes de segurança (autenticação e autorização)](#task-030)

**Labels:** `priority: high`, `épico: segurança`, `pontos: 3`, `type: test`  
**Milestone:** Sprint 5 - Integração

## 📋 Descrição
Testes dedicados para validar segurança: token inválido, expirado, role incorreta, acesso a dados de outro usuário.

## ✅ Critérios de Aceitação
- [ ] Teste: request sem token → 401
- [ ] Teste: token inválido → 401
- [ ] Teste: token expirado → 401
- [ ] Teste: PATIENT tentando criar consulta → 403
- [ ] Teste: PATIENT tentando ver consulta de outro paciente → 403
- [ ] Teste: DOCTOR criando consulta → 201 (sucesso)
- [ ] Teste: NURSE registrando consulta → 201 (sucesso)
- [ ] Testes no scheduling-service e history-service

## 🔧 Dependências Técnicas
- [TASK-029](#task-029) concluída

---

## 📋 ÉPICO 7: Documentação, Qualidade e Entregáveis

### [TASK-031: Configurar JaCoCo e SonarCloud](#task-031)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: infra`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Configurar JaCoCo em cada módulo e SonarCloud para análise estática do projeto multi-module.

## ✅ Critérios de Aceitação
- [ ] JaCoCo configurado em cada módulo (prepare-agent, report, check ≥ 80%)
- [ ] sonar-project.properties configurado para multi-module
- [ ] SonarCloud com Quality Gate (0 bugs, 0 vulnerabilidades, cobertura ≥ 80%)
- [ ] Exclusões: config/**, dto/**, *Application.java
- [ ] Badge no README

## 🔧 Dependências Técnicas
- Todos os testes escritos (TASK-015, 019, 025, 030)

---

### [TASK-032: Configurar CI/CD (GitHub Actions)](#task-032)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: infra`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Pipeline GitHub Actions para build multi-module, testes, cobertura e SonarCloud.

## ✅ Critérios de Aceitação
- [ ] Workflow `ci-cd.yml`: build + test + sonar em push/PR para develop e main
- [ ] Workflow `branch-naming.yml`: validação de nomes de branch
- [ ] Workflow `auto-pr-to-main.yml`: sync develop → main
- [ ] Build falha se testes falharem ou cobertura < 80%
- [ ] Cache de Maven e SonarCloud configurados
- [ ] Badge de status no README

## 🔧 Dependências Técnicas
- [TASK-031](#task-031) concluída

---

### [TASK-033: Criar README.md completo](#task-033)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Documentação principal com arquitetura de microsserviços, tecnologias, endpoints, instruções de execução e diagramas.

## ✅ Critérios de Aceitação
- [ ] Descrição do projeto e objetivo
- [ ] Diagrama de arquitetura (microsserviços + RabbitMQ + bancos)
- [ ] Tecnologias utilizadas
- [ ] Tabela com endpoints REST do scheduling-service
- [ ] Documentação do schema GraphQL do history-service
- [ ] Instruções de execução (Docker Compose)
- [ ] Instruções para rodar testes
- [ ] Badges (CI/CD, cobertura)
- [ ] Link do repositório

## 🔧 Dependências Técnicas
- Todas as tasks de implementação concluídas

---

### [TASK-034: Criar documentação da API (docs/API.md)](#task-034)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Documentação detalhada dos endpoints REST e queries GraphQL com exemplos.

## ✅ Critérios de Aceitação
- [ ] Documentação de cada endpoint REST (método, path, headers, body, response)
- [ ] Documentação das queries GraphQL (schema, exemplos de query)
- [ ] Exemplos de request/response (sucesso e erro)
- [ ] Documentação de autenticação (como obter token, como usar)
- [ ] Tabela de códigos HTTP
- [ ] Exemplos de headers (Authorization: Bearer ...)

## 🔧 Dependências Técnicas
- Todos os endpoints implementados

---

### [TASK-035: Criar Collection Postman](#task-035)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Collection Postman com todos os endpoints REST e queries GraphQL para teste.

## ✅ Critérios de Aceitação
- [ ] Requests para Auth (register, login)
- [ ] Requests para Appointments (CRUD completo)
- [ ] Requests para GraphQL (queries do history-service)
- [ ] Variáveis de ambiente (URLs dos 3 serviços, token)
- [ ] Script de pré-request para auto-login e setar token
- [ ] Cenários de sucesso e erro
- [ ] Organização por pastas (Auth, Scheduling, History)
- [ ] `docs/api-collection/README.md` com instruções

## 🔧 Dependências Técnicas
- Todos os endpoints implementados

---

### [TASK-036: Criar relatório de entrega (Tech-Challenge-Fase3-Relatorio.md)](#task-036)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 3`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Relatório formal de entrega seguindo o padrão da Fase 2, incluindo arquitetura, decisões técnicas, endpoints, instruções.

## ✅ Critérios de Aceitação
- [ ] Cabeçalho institucional (FIAP, nomes, RM)
- [ ] Descrição do projeto
- [ ] Arquitetura (diagrama de microsserviços)
- [ ] Tecnologias utilizadas
- [ ] Endpoints da API (REST + GraphQL)
- [ ] Regras de negócio
- [ ] Instruções de execução
- [ ] Testes e qualidade
- [ ] Decisões técnicas
- [ ] Versão PDF gerada

## 🔧 Dependências Técnicas
- Todas as tasks anteriores concluídas

---

### [TASK-037: Criar script runner (run.sh)](#task-037)

**Labels:** `priority: medium`, `épico: qualidade`, `pontos: 2`, `type: infra`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Script interativo para facilitar execução do projeto, seguindo padrão da Fase 2.

## ✅ Critérios de Aceitação
- [ ] Menu interativo com opções:
  - [ ] Docker Compose up (build + start todos os serviços)
  - [ ] Docker Compose stop
  - [ ] Rodar testes (mvn clean verify)
  - [ ] Rodar collection (Newman)
  - [ ] Limpar bancos (docker-compose down -v)
  - [ ] Kill portas (8081, 8082, 8083)
- [ ] Aceita argumentos diretos (./run.sh docker, ./run.sh tests, etc.)
- [ ] Validação de pré-requisitos (Docker, Maven, Java)

## 🔧 Dependências Técnicas
- [TASK-003](#task-003) concluída

---

### [TASK-038: Preparar entrega final](#task-038)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 2`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Validar que todos os entregáveis estão prontos e o projeto sobe corretamente.

## ✅ Critérios de Aceitação
- [ ] Docker Compose sobe todos os serviços com um comando
- [ ] Todos os endpoints funcionam (REST + GraphQL)
- [ ] Autenticação JWT funcional
- [ ] Comunicação assíncrona funcional (RabbitMQ)
- [ ] DLQ funcional
- [ ] Collection Postman importável e funcional
- [ ] Cobertura ≥ 80% em todos os módulos
- [ ] README completo
- [ ] Relatório de entrega pronto (.md + .pdf)
- [ ] Repositório público no GitHub

## 🔧 Dependências Técnicas
- Todas as tasks anteriores concluídas

---

### [TASK-039: Gravar vídeo de apresentação](#task-039)

**Labels:** `priority: high`, `épico: qualidade`, `pontos: 2`, `type: docs`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Vídeo demonstrando as funcionalidades do sistema rodando via Docker Compose.

## ✅ Critérios de Aceitação
- [ ] Docker Compose up demonstrado
- [ ] Registro de usuários (médico, enfermeiro, paciente)
- [ ] Login e obtenção de token JWT
- [ ] Criação de consulta (como médico/enfermeiro)
- [ ] Verificação de notificação no log do notification-service
- [ ] Consulta via GraphQL no history-service
- [ ] Demonstração de controle de acesso (paciente não cria consulta)
- [ ] RabbitMQ Management UI (filas, mensagens)
- [ ] Duração ~5-10 minutos

## 🔧 Dependências Técnicas
- [TASK-038](#task-038) concluída

---

## 📋 ÉPICO 8: Extras (Não Obrigatórios)

> ⚠️ A task abaixo **NÃO é obrigatória** conforme o PDF da Fase 3.
> É boa prática que agrega qualidade ao projeto.

### [TASK-040: Deploy no Render.com (NÃO OBRIGATÓRIO)](#task-040)

**Labels:** `priority: medium`, `épico: extras`, `pontos: 5`, `type: infra`  
**Milestone:** Sprint 6 - Finalização

## 📋 Descrição
Deploy dos microsserviços no Render.com ou similar para disponibilizar URL pública.

## ✅ Critérios de Aceitação
- [ ] Scheduling-service deployado
- [ ] Notification-service deployado
- [ ] History-service deployado
- [ ] RabbitMQ (CloudAMQP free tier ou similar)
- [ ] PostgreSQL (Render free tier)
- [ ] URLs públicas documentadas no README
- [ ] Deploy automático via main branch

## 🔧 Dependências Técnicas
- [TASK-038](#task-038) concluída

---

### [TASK-041: Implementar scheduler de lembretes](#task-041)

**Labels:** `priority: high`, `épico: notification`, `pontos: 3`, `type: feature`  
**Milestone:** Sprint 3 - Notification

## 📋 Descrição
Além de notificar na criação/edição, implementar um scheduler que envia lembretes automáticos X horas antes da consulta.

## ✅ Critérios de Aceitação
- [ ] @Scheduled no notification-service ou scheduling-service
- [ ] Consulta consultas com dateTime nas próximas 24h
- [ ] Publica evento de lembrete no RabbitMQ
- [ ] Notification-service processa e "envia" lembrete
- [ ] Evita enviar lembrete duplicado (flag `reminderSent`)

## 🔧 Dependências Técnicas
- [TASK-026](#task-026) concluída

---

---

## 📊 RESUMO DO BACKLOG

**Total de Tasks:** 42  
**Obrigatórias:** 41 | **Não obrigatórias:** 1  
**Estimativa Total:** ~138 pontos

### Por Prioridade:
- **Alta:** 41 tasks (obrigatórias)
- **Média:** 1 task (deploy)

### Por Épico:
| Épico | Tasks | Pontos |
|-------|-------|--------|
| 1. Setup e Infraestrutura | 8 | 31 |
| 2. Scheduling Service | 8 | 34 |
| 3. Notification Service | 5 | 15 |
| 4. History Service (GraphQL) | 6 | 22 |
| 5. Integração entre Serviços | 3 | 8 |
| 6. Segurança | 2 | 6 |
| 7. Documentação e Qualidade | 9 | 24 |
| 8. Extras (Não Obrigatórios) | 1 | 5 |

---

## 🎯 ORDEM SUGERIDA DE EXECUÇÃO

| Sprint | Descrição | Tasks |
|--------|-----------|-------|
| Sprint 1 | Fundação (infra + setup) | [001](#task-001) → [007](#task-007), [042](#task-042) |
| Sprint 2 | Scheduling Service (core) | [008](#task-008) → [015](#task-015) |
| Sprint 3 | Notification Service | [016](#task-016) → [019](#task-019) |
| Sprint 4 | History Service (GraphQL) | [020](#task-020) → [025](#task-025) |
| Sprint 5 | Integração + Segurança | [026](#task-026) → [030](#task-030) |
| Sprint 6 | Finalização | [031](#task-031) → [041](#task-041) |

---

## 📝 NOTAS

- As estimativas são em pontos de história (Story Points)
- 1 ponto ≈ 1-2 horas de trabalho
- Tasks podem ser quebradas em subtasks menores se necessário
- Recomenda-se seguir a ordem sugerida para evitar dependências bloqueantes
- Clean Architecture segue padrão da Fase 2 (domain → application → infra)
- Cada serviço tem seu próprio Dockerfile e pode ser buildado independentemente
- O docker-compose.yml na raiz orquestra tudo
- RabbitMQ Management UI disponível em http://localhost:15672 (guest/guest)
- O PDF marca o serviço de histórico como "opcional", mas implementaremos por decisão do grupo
- DLQ implementada conforme Observação 7 do briefing
