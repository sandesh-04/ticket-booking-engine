# Event Ticket Booking Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36.svg)](https://maven.apache.org/)

A Spring Boot backend that books seats for an event where availability isn't a
single number — it's split across multiple ticket blocks (Early Bird, Regular,
VIP, etc.), each with its own remaining seat count and its own sale-end date.
Built to correctly handle the two failure modes that actually matter in a
booking system: **partial bookings** and **overselling under concurrent demand**.

## 📋 Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Architecture](#architecture)
- [Key Design Decisions](#key-design-decisions)
- [Tech Stack](#tech-stack)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [Author](#author)

## 🎯 Overview

Most CRUD-style booking demos treat "seats available" as one mutable number on
an event. Real ticketing systems don't work that way — inventory is almost
always released in tiers (early-bird pricing, phased releases, VIP holds),
each with its own count and its own cutoff. This project models that directly:
an event has many `TicketBlock`s, and a single booking request can legitimately
need to draw from more than one of them.

## 🔍 Problem Statement

Booking seats across multiple blocks raises three problems that a naive
"subtract from a number" implementation gets wrong:

1. **Allocation order** — if a block's sale window is about to close, its
   unsold seats should be used before a later block is touched, so inventory
   isn't left stranded in a block nobody can buy from much longer.
2. **All-or-nothing fulfillment** — if a request for 65 seats can only be
   partially filled (say, 40 available), the booking must fail as a whole.
   No one should walk away with 40 of the 65 seats they asked for.
3. **Concurrency safety** — if two requests for the same event's last few
   seats arrive at the same instant, the system must guarantee only as many
   seats are sold as actually exist — never more.

**Solution**: seats are drawn from the soonest-closing usable block first,
partially draining blocks as needed, within a single locked transaction. If
the full request can't be satisfied, every block touched during the attempt
is restored to its exact original count before the failure is returned.

## 🏗️ Architecture

A single, layered Spring Boot service — no unnecessary distributed-systems
complexity for a problem that doesn't need it:

```
┌─────────────────────────────────────────────┐
│                  Client                       │
│        (Swagger UI / curl / Postman)          │
└───────────────────┬───────────────────────────┘
                    │  HTTP (JSON)
┌───────────────────▼───────────────────────────┐
│                Controller Layer                │
│   TicketBlockController · BookingController    │
│       (request/response only, no logic)        │
└───────────────────┬───────────────────────────┘
                    │
┌───────────────────▼───────────────────────────┐
│                 Service Layer                  │
│   TicketBlockService · BookingService (+ Impl) │
│   — all business logic lives here, including   │
│     the soonest-closing-first allocation loop   │
└───────────────────┬───────────────────────────┘
                    │
┌───────────────────▼───────────────────────────┐
│               Repository Layer                 │
│          TicketBlockRepository (JPA)           │
│   custom @Query + @Lock(PESSIMISTIC_WRITE)      │
└───────────────────┬───────────────────────────┘
                    │
┌───────────────────▼───────────────────────────┐
│                MySQL Database                  │
│                 ticket_block table              │
└─────────────────────────────────────────────┘
```

DTOs (records) define every request/response shape at the controller boundary;
the `TicketBlock` JPA entity never crosses that boundary directly. A single
`@RestControllerAdvice` centralizes error handling for the whole app.

## 🎨 Key Design Decisions

**Pessimistic locking over optimistic-only.** `findUsableBlocksForUpdate`
takes a `PESSIMISTIC_WRITE` lock (`SELECT ... FOR UPDATE`) on every usable
block for an event. Under genuinely contested access — many people trying to
book the last few seats of a popular event — rows are *expected* to be
contended, not exceptionally contended. A lock that makes the second request
wait its turn is a better fit here than one that lets both proceed and then
discovers a conflict after the fact.

**Manual restore instead of exception-driven rollback.** "Not enough seats"
is an expected business outcome, not a runtime error, so the service returns
a structured `BookingResponse.failure(...)` rather than throwing. Because no
exception is thrown, the `@Transactional` method still commits normally when
it returns — so every block touched during a failed attempt is explicitly
reverted to its original seat count before the method returns. Skipping that
step would silently persist a partial draw even on a "failed" booking.

**DTOs are the only thing exposed at the API boundary.** The JPA entity
(`TicketBlock`) is never returned directly from a controller — every response
is mapped into a purpose-built record, so the database shape and the API
contract can evolve independently of each other.

## 🛠️ Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 (Web, Data JPA, Validation) |
| Database | MySQL 8 |
| ORM | Hibernate (via Spring Data JPA) |
| Build | Maven |
| API Docs | springdoc-openapi (Swagger UI) |

## 🔌 API Reference

Interactive docs available at `/swagger-ui/index.html` once the app is running.

**Ticket Blocks**
```
POST   /ticket-blocks           Create a ticket block for an event
GET    /ticket-blocks/{eventId} List all blocks for an event
```

**Bookings**
```
POST   /bookings                Book N seats for an event
```

**Example — booking across two blocks**

Request:
```json
POST /bookings
{ "eventId": 1, "seatsRequested": 65 }
```

Response (`200 OK`):
```json
{
  "success": true,
  "message": "Booking successful",
  "breakdown": [
    { "blockId": 1, "seatsTaken": 30 },
    { "blockId": 2, "seatsTaken": 35 }
  ]
}
```

Response on insufficient seats (`422 Unprocessable Entity`):
```json
{
  "success": false,
  "message": "Not enough seats available. No seats were booked; all block availability remains unchanged.",
  "breakdown": []
}
```

**Error responses**
```
400 Bad Request          validation failure (e.g. non-positive seat count)
404 Not Found             unknown eventId
500 Internal Server Error unexpected failure
```

## 🏁 Getting Started

### Prerequisites
- Java 21
- Maven
- A running MySQL 8 instance

### Setup

```bash
git clone <your-repo-url>
cd ticket-booking-engine
```

Update `src/main/resources/application.properties` with your MySQL
credentials (the schema is created automatically on first run):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticket_booking_db?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Run it:

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. API docs at `/swagger-ui/index.html`.

## 🧪 Testing

- **Unit tests** for the booking allocation logic, with the repository mocked
- **Repository tests** (`@DataJpaTest`) covering expiry filtering and ordering
- **Full HTTP integration tests** for the success and failure paths
- **A concurrency test** that fires many simultaneous booking requests at a
  single near-empty block and asserts the final seat count is exact —
  proving the pessimistic lock prevents overselling, not just assuming it

Run with:
```bash
./mvnw test
```

## 🗺️ Roadmap

Deliberately out of scope for the current version, listed honestly rather
than left unstated:

- Idempotency keys on `/bookings`, so a retried request can't double-book
- A persisted audit record of every booking attempt (success or failure)
- Authentication/authorization in front of the API
- Dockerized deployment

## 👤 Author

**[Sandesh Singh]** — [GitHub](https://github.com/sandesh-04) · [LinkedIn](https://linkedin.com/in/sandesh04)

---

*Built as a backend-engineering exercise focused on correctness under
concurrency and partial-failure handling — the two things most CRUD-style
booking demos skip.*
