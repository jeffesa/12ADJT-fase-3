# 📝 Resumo do Projeto - Tech Challenge Fase 3

## 🎯 Contexto

Este projeto é a continuação do Tech Challenge Fase 2 da Pós-Graduação em Arquitetura e Desenvolvimento Java (FIAP). A Fase 2 entregou um sistema de gestão de restaurantes com Clean Architecture. A Fase 3 muda o domínio para um **sistema hospitalar de agendamento de consultas**, com foco em **segurança, comunicação assíncrona e separação em microsserviços**.

**Repositório Fase 1:** https://github.com/jeffesa/12ADJT-fase-1
**Repositório Fase 2:** https://github.com/jeffesa/12ADJT-fase-2
**Repositório Fase 3:** https://github.com/jeffesa/12ADJT-fase-3

---

## 🏗️ Decisões Técnicas

### Por que manter Clean Architecture?

Apesar de o PDF da Fase 3 **não exigir explicitamente** Clean Architecture, optamos por manter o padrão da Fase 2 para:
- Demonstrar consistência e evolução ao longo das fases
- Facilitar a separação de responsabilidades em microsserviços
- Garantir testabilidade (domain e application sem framework)
- Mostrar domínio da arquitetura aos avaliadores

### Por que Monorepo com Multi-Module Maven?

Em vez de repositórios separados para cada serviço, usamos **um monorepo com módulos Maven**:
- `pom.xml` pai na raiz gerencia versões e dependências comuns
- Cada serviço é um módulo independente com seu próprio `pom.xml` e `Dockerfile`
- Um único `docker-compose.yml` orquestra todos os serviços
- Facilita a avaliação pelos professores (tudo em um lugar)
- Simplifica CI/CD (um pipeline, vários builds)

### Por que RabbitMQ (e não Kafka)?

- RabbitMQ é mais leve e simples para o escopo do projeto
- Suporte nativo a DLQ (Dead Letter Queue) out-of-the-box
- Spring AMQP bem maduro e documentado
- Ideal para comunicação ponto-a-ponto entre serviços
- Kafka seria overkill para 2-3 serviços com volume baixo

### Por que DLQ (Dead Letter Queue)?

- Mensagens que falharem no processamento são redirecionadas para uma fila DLQ
- Permite reprocessamento manual ou automático
- Evita perda de mensagens em caso de falhas
- Padrão robusto para comunicação assíncrona em produção

### Por que Spring Security com JWT?

O PDF exige autenticação com Spring Security e níveis de acesso:
- **JWT (JSON Web Token)** para stateless authentication entre microsserviços
- Cada serviço valida o token localmente (sem chamada ao serviço de auth)
- Roles: `ROLE_DOCTOR`, `ROLE_NURSE`, `ROLE_PATIENT`
- Login gera token; endpoints protegidos exigem Bearer token

### Por que GraphQL apenas no Serviço de Histórico?

O PDF associa GraphQL exclusivamente ao histórico:
- Spring for GraphQL (spring-boot-starter-graphql)
- Permite queries customizadas pelo cliente (filtrar por data, médico, status)
- Schema-first approach com arquivo `.graphqls`
- Ideal para leitura flexível de dados históricos
- Scheduling-service usa REST (operações de escrita)

### Por que manter cobertura de testes ≥ 80%?

Apesar de não ser obrigatório na Fase 3, mantemos o padrão da Fase 2:
- JaCoCo com check no `verify`
- SonarCloud para análise estática
- Demonstra qualidade e consistência entre fases

---

## 🔄 Diferenças em relação à Fase 2

| Aspecto | Fase 2 | Fase 3 |
|---------|--------|--------|
| Domínio | Restaurantes | Hospital (consultas) |
| Arquitetura | Monolito Clean Architecture | Microsserviços Clean Architecture |
| Serviços | 1 aplicação | 3 serviços (Agendamento, Notificação, Histórico) |
| Segurança | BCrypt apenas (sem auth real) | Spring Security + JWT + Roles |
| Comunicação | Síncrona (HTTP) | Assíncrona (RabbitMQ + DLQ) |
| API de consulta | REST apenas | REST + GraphQL |
| Banco de dados | 1 PostgreSQL | 1 PostgreSQL por serviço |
| Docker Compose | 2 containers (app + db) | 6+ containers (3 apps + db + rabbitmq) |
| Tipos de usuário | CUSTOMER, RESTAURANT_OWNER | DOCTOR, NURSE, PATIENT |

---

## 🛠️ Stack Tecnológica

| Tecnologia | Uso |
|-----------|-----|
| Java 17 | Linguagem principal |
| Spring Boot 3.2.x | Framework web |
| Spring Security | Autenticação + Autorização (JWT) |
| Spring Data JPA | Persistência |
| Spring for GraphQL | API GraphQL (serviço de histórico) |
| Spring AMQP | Integração RabbitMQ |
| PostgreSQL 15 | Banco de dados |
| RabbitMQ 3.x | Mensageria assíncrona |
| Docker + Compose | Containerização |
| Maven (multi-module) | Build |
| SpringDoc OpenAPI 2.3.0 | Documentação REST (Swagger) |
| JUnit 5 + Mockito | Testes |
| JaCoCo 0.8.11 | Cobertura ≥ 80% |
| SonarCloud | Qualidade de código |
| GitHub Actions | CI/CD |

---

## 📅 Planejamento

O projeto está organizado em **41 tasks** distribuídas em **8 épicos** e **6 sprints**.

Detalhes completos em: [BACKLOG.md](BACKLOG.md)

---

*Última atualização: Agosto/2026*
