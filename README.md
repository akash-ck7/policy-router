# Policy-Driven Message Routing System

## Overview
A monolithic Spring Boot application that routes messages to delivery channels (Email, SMS)
based on configurable policies and runtime conditions.

## Architecture
Monolithic Spring Boot app with internal event-driven async processing.
- HTTP layer handles requests synchronously
- Routing + delivery happens async via Redis queue + @Scheduled worker
- PostgreSQL for persistence, Flyway for migrations

## Design Patterns
| Pattern | Where |
|---------|-------|
| Strategy | MessageChannel interface + EmailChannel + SmsChannel |
| Chain of Responsibility | RulesEngine evaluates rules by priority |
| State Machine | MessageStatus transitions PENDING → SENT/FAILED |
| Repository | Spring Data JPA repositories |
| Factory | ChannelRegistry resolves channel by name |
| Facade | MessageService hides complexity from controllers |
| Producer-Consumer | MessageProducer → Redis → MessageWorker |
| Template Method | Base retry logic per rule |

## Tech Stack
- Java 21
- Spring Boot 3.2.5
- PostgreSQL 16
- Redis 7
- Flyway 9.22.3
- Lombok + MapStruct

## Setup

### Prerequisites
- Java 21
- Docker

### Run
```bash
docker-compose up -d
./mvnw spring-boot:run
```

### Test
```bash
./mvnw test
```

## API Endpoints

### Submit a message
```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ALERT",
    "priority": "CRITICAL",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "payload": {"title": "Server down", "body": "DB unreachable"}
  }'
```

### Get message status
```bash
curl http://localhost:8080/api/messages/{id}/status
```

### List all messages
```bash
curl http://localhost:8080/api/messages
```

### List routing rules
```bash
curl http://localhost:8080/api/rules
```

### List DLQ
```bash
curl http://localhost:8080/api/dlq
```

## Message Lifecycle
```
PENDING → ROUTED → DISPATCHED → SENT
                 ↘ FAILED → RETRYING → DISPATCHED (retry)
                                     ↘ DEAD (DLQ)
```

## Routing Rules (seeded on startup)
| Rule | Condition | Channels | Retry |
|------|-----------|----------|-------|
| Critical alerts | type=ALERT + priority=CRITICAL | SMS + Email | 5x |
| Promotions | type=PROMOTION | Email only | 2x |
| OTP | type=OTP | SMS → Email fallback | 3x |
| Notifications | type=NOTIFICATION | Email | 2x |

## Database Schema
- `messages` — core message table with status
- `routing_rules` — policy definitions
- `delivery_logs` — per channel delivery attempts
- `dead_letter_queue` — exhausted messages
- `user_preferences` — per user channel opt-in
```

---
