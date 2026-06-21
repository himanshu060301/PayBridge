# Payment Processing System

A production-inspired payment processing service built with Spring Boot that demonstrates scalable backend design patterns including Idempotency, Transactional Outbox Pattern, Kafka Event Publishing, Retry Mechanism, and Dockerized deployment.

## Features

* Payment initiation API
* Idempotency key support to prevent duplicate payments
* Transactional Outbox Pattern for reliable event publishing
* Kafka integration for asynchronous event-driven communication
* Payment retry mechanism
* Global exception handling
* PostgreSQL persistence
* Dockerized deployment
* RESTful API design
* Structured logging with SLF4J

---

## Tech Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Data JPA
* Spring Validation

### Database

* PostgreSQL

### Messaging

* Apache Kafka

### Build Tool

* Maven

### Containerization

* Docker

### Utilities

* Lombok
* Jackson

---

## Architecture

Client
↓
Payment API
↓
PostgreSQL
↓
Outbox Table
↓
Outbox Publisher Scheduler
↓
Kafka Topic
↓
Payment Consumer

The project implements the Transactional Outbox Pattern to ensure that database updates and event publishing remain consistent even during failures.

---

## Key Design Patterns

### Idempotency Pattern

Prevents duplicate payment creation when the same request is submitted multiple times.

Workflow:

1. Client sends Idempotency-Key header.
2. Request hash is generated.
3. Existing key is checked.
4. Cached response is returned if available.
5. Duplicate requests are prevented.

---

### Transactional Outbox Pattern

Instead of directly publishing events to Kafka:

1. Payment is saved.
2. Event is saved in Outbox table.
3. Scheduled publisher reads pending events.
4. Events are published to Kafka.
5. Event marked as processed.

Benefits:

* No event loss
* Reliable delivery
* Eventual consistency

---

### Retry Mechanism

Failed or pending payments can be retried.

Retry count is tracked per order and stored in the database.

---

## Database Tables

### payments

Stores payment transactions.

Important fields:

* payment_id
* order_id
* amount
* currency
* payment_method
* status
* retry_count
* gateway_reference

### idempotency_keys

Stores request metadata.

Important fields:

* idempotency_key
* request_hash
* payment_id
* status
* created_at

### outbox_events

Stores events waiting to be published.

Important fields:

* aggregate_id
* event_type
* payload
* processed

---

## API Endpoints

### Initiate Payment

POST /api/payments/initiate

Headers:

Idempotency-Key: unique-key
X-API-KEY : secret_key

Request:

```json
{
  "orderId": "ORD-1001",
  "orderDesc": "Phone Cover"
  "amount": 1000,
  "currency": "INR",
  "paymentMethod": "UPI"
}
```

Response:

```json
{
  "paymentId": "PAY-12345",
  "orderId": "ORD-1001",
  "amount": 1000,
  "currency": "INR",
  "status": "INITIATED"
}
```

---

## Running Locally

### Clone Repository

```bash
git clone <repository-url>
cd api
```

### Build Application

```bash
mvn clean package
```

### Run Application

```bash
java -jar target/*.jar
```

---

## Docker

Build Image

```bash
docker build -t payment-api .
```

Run Container

```bash
docker run -p 8083:8083 payment-api
```

---

## Kafka Configuration

Topic:

```text
payment-events
```

Consumer Group:

```text
payment-group
```

Events are published from the Outbox table and consumed asynchronously.

---

## Error Handling

Centralized exception handling is implemented using:

* @RestControllerAdvice
* Custom application exceptions
* Database exception handling
* Consistent error response format

Example:

```json
{
  "message": "Payment already completed for orderId: ORD-1001",
  "status": 400,
  "timestamp": "2026-05-30T10:15:30"
}
```

---

## Learning Outcomes

This project demonstrates:

* Spring Boot development
* REST API design
* Event-driven architecture
* Kafka integration
* Transaction management
* Idempotency implementation
* Transactional Outbox Pattern
* Docker containerization
* Production-ready backend design concepts

```
```
