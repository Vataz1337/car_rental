# Car Rental System

A simulated Car Rental reservation system built with Spring Boot, PostgreSQL, and Liquibase.

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.5** (Web, Data JPA, Validation)
- **PostgreSQL 16**
- **Liquibase** — schema migrations and seed data
- **Docker Compose** — local database setup
- **Testcontainers** — integration tests against a real PostgreSQL instance
- **REST Assured** — HTTP-level integration testing

## Architecture

Standard MVC layered architecture:

```
Controller → Service → Repository
```

```
com.carrental
├── controller       # REST endpoints
├── service          # Business logic
├── repository       # Spring Data JPA repositories
├── model            # JPA entities (Car, Renter, Reservation)
├── dto              # Request/Response records
├── enums            # CarType (SEDAN, SUV, VAN)
└── exception        # GlobalExceptionHandler, CarNotAvailableException
```

## Data Model

| Table         | Description                              |
|---------------|------------------------------------------|
| `car`         | Fleet inventory with type and model      |
| `renter`      | Person making the reservation            |
| `reservation` | Links car and renter with rental period  |

Renters are deduplicated by email — a returning renter is reused rather than inserted again.

## Running Locally

**Prerequisites:** Docker, Java 21, Maven

**1. Start the database**
```bash
docker compose up -d
```

**2. Run the application**
```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. Liquibase runs automatically on startup — schema and seed data are applied.

## API

### Create a Reservation

```
POST /api/v1/reservations
```

**Request body:**
```json
{
  "renterName": "John Doe",
  "renterEmail": "john@example.com",
  "carType": "SEDAN",
  "startDate": "2025-07-01T10:00:00",
  "endDate": "2025-07-05T10:00:00"
}
```

**Responses:**

| Status | Description                                      |
|--------|--------------------------------------------------|
| `201`  | Reservation created successfully                 |
| `400`  | Validation failed (past date, null fields, etc.) |
| `409`  | No car of requested type available for the period|
| `503`  | System busy due to concurrent requests           |

**Response body:**
```json
{
  "reservationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "carModel": "Toyota Camry",
  "carType": "SEDAN",
  "startDate": "2025-07-01T10:00:00",
  "endDate": "2025-07-05T10:00:00"
}
```

## Car Fleet (Seed Data)

| Type  | Models                              | Count |
|-------|-------------------------------------|-------|
| SEDAN | Toyota Camry, Honda Accord, VW Passat | 3   |
| SUV   | Ford Explorer, Toyota RAV4          | 2     |
| VAN   | Ford Transit, Mercedes Sprinter     | 2     |

## Business Rules

- Reservation must be made at least **1 hour in advance**
- Maximum rental period is **90 days**
- A car is only reserved if it is **available for the entire requested period** — no overlapping reservations
- Concurrent reservation attempts are handled safely via **pessimistic locking** at the database level

## Running Tests

```bash
./mvnw test
```

Tests use **Testcontainers** — a real PostgreSQL container is spun up automatically. No manual setup required.

### Test coverage

| Test | Description |
|------|-------------|
| `shouldCreateReservationSuccessfully` | Happy path — 201 returned with correct body |
| `shouldReturn409WhenNoCarAvailable` | All cars of type taken — 409 returned |
| `shouldReturn400WhenStartDateIsInPast` | Past start date — 400 with validation error |
| `shouldReturn400WhenCarTypeIsNull` | Missing car type — 400 with validation error |
| `shouldReuseExistingRenterOnSecondReservation` | Same email reuses existing renter row |

## Known Gaps

These are intentionally out of scope for the assessment but would be required in production:

- **No authentication** — Spring Security with JWT would gate all endpoints
- **No cancellation endpoint** — `DELETE /api/v1/reservations/{id}` with cancellation window rules
- **No GET endpoints** — fetching reservations by ID or filtering by type/date
- **Fleet size in DB** — currently seeded via Liquibase; a real system would manage fleet dynamically
- **Timezone handling** — `LocalDateTime` is timezone-naive; a multi-branch international system would use `ZonedDateTime` with a branch timezone field
- **Index on reservation** — `(car_id, start_date)` index would improve availability query performance at scale
