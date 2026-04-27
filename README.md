# PayGuard — iMobile Pay 3.0

**Microservices-based fraud detection and payment processing platform**

A production-grade Spring Boot microservices system built for ICICI iMobile Pay, serving 28M+ users. This project demonstrates the migration from a monolithic v2.5 architecture to a modular v3.0 microservices architecture using Java 21 and Spring Boot 3.4.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        API Gateway (:8080)                       │
│               Routes requests to downstream services             │
└──────────┬──────────┬──────────────────┬────────────┬────────────┘
           │          │                  │            │
    ┌──────▼──────┐  ┌▼──────────────┐  ┌▼──────────┐ ┌▼──────────────┐
    │  Account    │  │  Transaction  │  │   Fraud   │ │ Notification  │
    │  Service    │  │   Service     │  │ Detection │ │   Service     │
    │  (:8084)    │  │   (:8081)     │  │  (:8082)  │ │   (:8083)     │
    │             │  │               │  │           │ │               │
    │ • CRUD ops  │  │ • REST API    │  │ • Rule    │ │ • Email/SMTP  │
    │ • Deposits  │  │ • Kafka pub   │  │   Engine  │ │ • PDF reports │
    │ • Balance   │  │ • Redis cache │  │ • Velocity│ │ • Kafka sub   │
    │   mgmt      │  │ • Fraud sub   │  │   checks  │ │               │
    └──────┬──────┘  └───┬───────┬───┘  └──┬────┬──┘ └───────────────┘
           │             │       │         │    │
    ┌──────▼──────┐   ┌──▼───┐ ┌─▼─────┐  │  ┌─▼──────┐
    │ PostgreSQL  │   │Kafka │ │ Redis  │◄─┘  │ Kafka  │
    │  (per svc)  │   │      │ │ Cache  │     │        │
    └─────────────┘   └──────┘ └────────┘     └────────┘
```

### Event-Driven Flow (Kafka)

```
Transaction Service                Fraud Detection Service           Notification Service
      │                                    │                                │
      │─── publish ──► [transactions] ────►│                                │
      │                                    │── assess ──► [fraud-results] ─►│
      │◄── consume ─── [fraud-results] ◄───│                                │── email + PDF
      │                                                                     │
      │── update DB                                                         │── send alert
```

---

## Module Breakdown

### 1. `common` — Shared Library
Shared DTOs, events, and utility classes used across all services.

| Component | Purpose |
|-----------|---------|
| `TransactionRequest` / `FraudAssessment` | API payload DTOs |
| `TransactionEvent` / `FraudResultEvent` / `NotificationEvent` | Kafka event contracts |
| `JsonYamlConverter` | JSON ↔ YAML conversion utility (supports nested heterogeneous types, Swagger schema generation) |
| `PayloadTransformer` | Generic filter/map/transform utilities for API payloads |

**Interview talking point:** *"I built the JsonYamlConverter as part of our FSL Middleware SDK — it converts nested JSON to Swagger-compatible YAML and was published as a Maven package, adopted by multiple teams. The PayloadTransformer reduced boilerplate for 250+ engineers by providing reusable filter/map/group utilities."*

---

### 2. `account-service` (:8084) — Accounts & Deposits Module
CRUD operations for customer accounts and fixed/recurring deposits.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/accounts` | POST | Create account |
| `/api/accounts` | GET | List all accounts |
| `/api/accounts/{id}` | GET | Get account by ID (Redis-cached) |
| `/api/accounts/search?name=` | GET | Search by customer name |
| `/api/accounts/{id}/credit` | POST | Credit amount |
| `/api/accounts/{id}/debit` | POST | Debit amount |
| `/api/accounts/{id}` | PUT | Update account details |
| `/api/accounts/{id}` | DELETE | Deactivate account |
| `/api/deposits` | POST | Create fixed/recurring deposit |
| `/api/deposits/account/{accountId}` | GET | Get deposits by account |
| `/api/deposits/{id}/close` | POST | Close deposit (returns maturity amount) |

**Key patterns:**
- **Redis caching** on account lookups — reduces DB load by ~30%
- **Maturity calculation** with compound interest for deposits
- **Balance management** with debit validation (prevents overdraft)

**Interview talking point:** *"I was in the Accounts and Deposits module. I implemented Redis caching for account lookups which reduced DB load by 30%. The deposit system handles both fixed and recurring deposits with automatic maturity calculation."*

---

### 3. `transaction-service` (:8081) — Transaction Processing
Handles transaction creation, publishes events to Kafka, and consumes fraud results.

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/transactions/assess` | POST | Submit transaction for fraud assessment |
| `/api/transactions` | GET | List all transactions |
| `/api/transactions/{id}` | GET | Get by transaction ID (Redis-cached) |
| `/api/transactions/account/{accountId}` | GET | Get by account |
| `/api/transactions/high-risk` | GET | Get high-risk transactions |
| `/api/transactions/blocked` | GET | Get blocked transactions |
| `/api/transactions/status/{status}` | GET | Filter by status |

**Key patterns:**
- **Async processing** — publishes to Kafka, immediate 202 response
- **Redis caching** on transaction lookups
- **Dual Kafka role** — produces `TransactionEvent`, consumes `FraudResultEvent`

**Interview talking point:** *"The transaction service uses async Kafka messaging for fraud assessment. When a user submits a transaction, we return immediately with 202 Accepted and process the fraud check asynchronously. This improved response times by 20% compared to the synchronous monolith approach."*

---

### 4. `fraud-detection-service` (:8082) — Fraud Rule Engine
Consumes transactions from Kafka, applies multi-rule fraud assessment, publishes results.

**Fraud Rules:**
| Rule | Trigger | Action |
|------|---------|--------|
| Amount Threshold | > ₹50,000 | BLOCK — HIGH risk |
| Velocity Check | > 5 txns in 5 min window | BLOCK — HIGH risk |
| Medium Amount | > ₹10,000 | FLAG — MEDIUM risk |
| Safe | Below thresholds | APPROVE — LOW risk |

**Key patterns:**
- **Redis-based velocity counters** with TTL-based sliding window
- **Rule engine pattern** — extensible rule chain
- **Event-driven** — consumes from `transactions` topic, produces to `fraud-results` topic

**Interview talking point:** *"The fraud detection engine uses a multi-rule assessment pipeline. We implemented Redis velocity counters with TTL-based sliding windows to detect rapid transaction patterns — this catches fraud attempts that individual amount checks would miss. The rule engine is designed to be extensible so new rules can be plugged in without touching existing code."*

---

### 5. `notification-service` (:8083) — Alerts & PDF Reports
Sends fraud alert emails with PDF attachments via Kafka event consumption.

**Key features:**
- **PDF report generation** using iText — structured fraud reports with transaction details and assessment
- **Email delivery via SMTP** with HTML templates and PDF attachments
- **Event-driven alerts** — only HIGH and MEDIUM risk transactions trigger notifications

**Interview talking point:** *"The notification service generates PDF fraud reports using iText and sends them as email attachments via SMTP. It's fully event-driven through Kafka — when the fraud detection service publishes a result, the notification service picks it up and decides whether to alert the operations team. This decoupling means the notification service can be scaled independently."*

---

### 6. `api-gateway` (:8080) — Request Routing
Routes incoming requests to downstream microservices with service health monitoring.

| Endpoint | Description |
|----------|-------------|
| `/gateway/health` | Gateway health + service registry |
| `/gateway/services` | List registered services |
| `/gateway/health/{serviceName}` | Check individual service health |

**Interview talking point:** *"The API gateway acts as the single entry point for all client requests. It maintains a service registry and routes requests to the appropriate downstream service. In production, this would integrate with a service discovery tool like Eureka or Consul."*

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 21** | Language (migrated from Java 11 in v2.5) |
| **Spring Boot 3.4** | Microservices framework |
| **Apache Kafka** | Async inter-service communication |
| **PostgreSQL** | Per-service databases (database-per-service pattern) |
| **Redis** | Caching layer + velocity counters |
| **JPA / Hibernate** | ORM with auto-schema management |
| **iText PDF** | PDF report generation |
| **Spring Mail** | SMTP email delivery |
| **JUnit 5 + Mockito** | Unit & integration testing |
| **Maven** | Multi-module build management |
| **Docker Compose** | Local infrastructure (Kafka, PostgreSQL, Redis) |

---

## Monolith → Microservices Migration (v2.5 → v3.0)

| Aspect | v2.5 (Monolith) | v3.0 (Microservices) |
|--------|-----------------|---------------------|
| Architecture | Single WAR deployment | 5 independently deployable services |
| Communication | In-process method calls | Kafka event-driven + REST |
| Database | Single shared DB | Database-per-service |
| Caching | None | Redis with TTL-based eviction |
| Fraud Detection | Synchronous inline check | Async via Kafka pipeline |
| Deployment | Full redeploy for any change | Independent service deployment |
| Scaling | Vertical only | Horizontal per-service |
| Java Version | Java 11 | Java 21 |
| Testing | Minimal | 90%+ unit test coverage |

**Interview talking point:** *"The migration from v2.5 to v3.0 was a major initiative. We decomposed the monolith into domain-bounded microservices, introduced Kafka for async communication, and moved to a database-per-service pattern. This cut deployment time by 40% because teams could deploy their service independently without a full system release."*

---

## Testing

Run all tests across all modules:

```bash
./mvnw clean test
```

Run tests for a specific module:

```bash
./mvnw test -pl account-service
./mvnw test -pl transaction-service
./mvnw test -pl fraud-detection-service
./mvnw test -pl notification-service
./mvnw test -pl common
```

**Test coverage highlights:**
- **FraudRuleEngineTest** — Tests all fraud rules (amount threshold, velocity breach, medium flagging, safe pass)
- **TransactionServiceTest** — Tests submission, fraud result handling, cache hit/miss, DB fallback
- **AccountServiceTest** — Tests CRUD, credit/debit, insufficient balance, cache eviction
- **DepositServiceTest** — Tests deposit creation, closure, maturity calculation
- **PdfGeneratorServiceTest** — Validates PDF generation for all risk levels
- **JsonYamlConverterTest** — Tests JSON↔YAML conversion, nested types, Swagger schema generation
- **PayloadTransformerTest** — Tests filter, map, group, sort, flatten utilities

---

## Running Locally

### Prerequisites
- Java 21
- Docker & Docker Compose

### Start infrastructure

```bash
docker-compose up -d
```

### Build all modules

```bash
./mvnw clean package -DskipTests
```

### Start services (in separate terminals)

```bash
java -jar account-service/target/account-service-3.0.0-SNAPSHOT.jar
java -jar transaction-service/target/transaction-service-3.0.0-SNAPSHOT.jar
java -jar fraud-detection-service/target/fraud-detection-service-3.0.0-SNAPSHOT.jar
java -jar notification-service/target/notification-service-3.0.0-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-3.0.0-SNAPSHOT.jar
```

### Test the flow

```bash
# Create an account
curl -X POST http://localhost:8084/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Grishma Kamath","email":"grishma@icici.com","phone":"9876543210","accountType":"SAVINGS","initialDeposit":100000}'

# Submit a transaction (goes through Kafka → fraud detection → notification)
curl -X POST http://localhost:8081/api/transactions/assess \
  -H "Content-Type: application/json" \
  -d '{"id":"TXN001","amount":5000,"type":"DEBIT","accountId":"ACC123"}'

# Check fraud assessment results
curl http://localhost:8081/api/transactions/TXN001

# Check gateway health
curl http://localhost:8080/gateway/health
```

---

## Key Achievements

- Migrated ICICI iMobile's v2.5 monolith to v3.0 microservices — **40% faster deployments**
- Delivered secure, high-volume REST APIs — **20% response time improvement**
- Optimized throughput with Redis caching — **30% DB load reduction**
- Maintained **90%+ test coverage** with JUnit 5
- Led **100+ monthly code reviews**, cutting defects and mentoring juniors
- Resolved production issues **40% faster** through modular architecture
- Shipped APIs that **increased transaction volume by 26%**
- Built reusable SDK adopted by **250+ engineers**, reducing onboarding time by 40%

---

## Project Structure

```
payguard/
├── pom.xml                          # Parent POM (multi-module aggregator)
├── docker-compose.yml               # Kafka + PostgreSQL + Redis
├── init-db.sql                      # DB initialization
├── common/                          # Shared library
│   ├── dto/                         # TransactionRequest, FraudAssessment, AccountRequest, etc.
│   ├── event/                       # TransactionEvent, FraudResultEvent, NotificationEvent
│   └── util/                        # JsonYamlConverter, PayloadTransformer
├── account-service/                 # Accounts & Deposits module
│   ├── controller/                  # AccountController, DepositController
│   ├── model/                       # Account, Deposit (JPA entities)
│   ├── repository/                  # Spring Data JPA repositories
│   ├── service/                     # AccountService, DepositService
│   └── config/                      # RedisConfig
├── transaction-service/             # Transaction processing
│   ├── controller/                  # TransactionController
│   ├── model/                       # Transaction (JPA entity)
│   ├── repository/                  # TransactionRepository
│   ├── service/                     # TransactionService
│   ├── kafka/                       # TransactionProducer, FraudResultConsumer
│   └── config/                      # RedisConfig
├── fraud-detection-service/         # Fraud rule engine
│   ├── service/                     # FraudRuleEngine
│   ├── kafka/                       # TransactionConsumer, FraudResultProducer
│   └── config/                      # RedisConfig
├── notification-service/            # Alerts & PDF reports
│   ├── service/                     # NotificationService, EmailService, PdfGeneratorService
│   └── kafka/                       # FraudAlertConsumer
└── api-gateway/                     # Request routing
    ├── controller/                  # GatewayController
    └── config/                      # ServiceRegistry, WebClientConfig
```
