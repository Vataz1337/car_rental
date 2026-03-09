# Car Rental Reservation System

A simple **Car Rental reservation API** built with **Spring Boot** and **PostgreSQL**.
The system allows renters to create reservations while ensuring that a car cannot be double-booked for overlapping periods.

The project demonstrates **REST API design, database modeling, concurrency handling, and integration testing**.

---

# Tech Stack

* **Java 21**
* **Spring Boot 3**

    * Spring Web
    * Spring Data JPA
    * Validation
* **PostgreSQL**
* **Liquibase** – database schema and seed data
* **Docker Compose** – local PostgreSQL setup
* **Testcontainers** – integration tests with real PostgreSQL
* **REST Assured** – API testing

---

# Architecture

The project follows a **standard layered architecture**:

```
Controller → Service → Repository
```

```
com.carrental
├── controller
├── service
├── repository
├── model
├── dto
├── enums
└── exception
```

* **Controller** – REST endpoints
* **Service** – business logic
* **Repository** – Spring Data JPA persistence layer
* **DTOs** – request/response models
* **Entities** – JPA domain objects

---

# Core Business Logic

Key rules implemented in the system:

* A reservation must be created **at least 1 hour in advance**
* Maximum rental period is **90 days**
* A car can only be reserved if it is **available for the entire requested time period**
* **Overlapping reservations are prevented**
* **Pessimistic database locking** is used to safely handle concurrent reservation requests

---

# API

### Create Reservation

```
POST /api/v1/reservations
```

Example request:

```json
{
  "renterName": "John Doe",
  "renterEmail": "john@example.com",
  "carType": "SEDAN",
  "startDate": "2025-07-01T10:00:00",
  "numberOfDays": 4
}
```

Successful response:

```
201 Created
```

Example response:

```json
{
  "reservationId": "uuid",
  "carModel": "Toyota Camry",
  "carType": "SEDAN",
  "startDate": "2025-07-01T10:00:00",
  "endDate": "2025-07-05T10:00:00"
}
```

---

# Running the Project

## Start database

```
docker compose up -d
```

## Run application

```
./mvnw spring-boot:run
```

The API will start on:

```
http://localhost:8080
```

Liquibase automatically applies the database schema and seed data on startup.

---

# Testing

Run tests with:

```
./mvnw test
```

Integration tests use **Testcontainers**, which automatically starts a real PostgreSQL container during the test run.

### Unit Tests (`ReservationServiceTest`)

* successful reservation creation with response mapping
* new renter creation when email not found
* renter reuse when email already exists
* rejection when start date is less than 1 hour from now
* rejection when rental period exceeds 90 days
* boundary test: exactly 90 days is accepted
* CarNotAvailableException when no cars of requested type are free

### Integration Tests (`ReservationControllerIntegrationTest`)

* successful reservation via API
* 409 Conflict when no cars available
* 400 Bad Request for past start date
* 400 Bad Request for null car type and null numberOfDays
* renter reuse on second reservation
* concurrent reservation handling with pessimistic locking

---

# Possible Improvements

Features that could be added in a production system:

* Authentication and authorization (Spring Security + JWT)
* Reservation cancellation endpoint
* Additional GET endpoints for querying reservations
* Database indexing on `reservation(car_id, start_date, end_date)` for performance
