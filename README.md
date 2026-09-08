# ERP Backend - Order Management API

[![CI](https://github.com/laahundskarl/erp-backend-java/actions/workflows/ci.yml/badge.svg)](https://github.com/laahundskarl/erp-backend-java/actions/workflows/ci.yml)

Backend REST service for a simplified ERP order-management module: a catalog of products
and services, orders, and order line items.

## Tech stack

- Java 21, Spring Boot 4.1, Maven (via the included Maven Wrapper — no local Maven install needed)
- Spring Web MVC, Spring Data JPA, PostgreSQL 16
- QueryDSL for dynamic, filterable, paginated list queries
- Bean Validation (Jakarta Validation), applied on both entities and request DTOs
- Flyway for versioned database schema migrations
- springdoc-openapi (Swagger UI) for interactive API documentation
- JUnit 5, Mockito and AssertJ for unit tests; Testcontainers for integration tests against a real PostgreSQL instance

## Prerequisites

- JDK 21 or newer. Check with `java -version`. If you have multiple JDKs installed, make sure
  `JAVA_HOME` points at one that is 17+ before running the wrapper (Spring Boot 4 requires it) —
  e.g. on Windows: `set JAVA_HOME=C:\Program Files\Java\jdk-21` (or the equivalent `$env:JAVA_HOME`/`export JAVA_HOME` for your shell).
- Docker, running locally. It's used two ways:
  - `compose.yaml` at the project root starts a local PostgreSQL 16 container. Spring Boot's
    Docker Compose support starts it **automatically** when you run the app (see below) — no
    manual `docker compose up` needed.
  - Integration tests use [Testcontainers](https://testcontainers.com) to spin up a throwaway
    PostgreSQL container per test run, so `./mvnw verify` also requires Docker to be running.

No local PostgreSQL install is required either way.

## Running the application

```bash
./mvnw spring-boot:run
```

This starts a local Postgres container (via `compose.yaml`), runs the Flyway migrations
against it, and starts the API on `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

To stop, `Ctrl+C` the process — the Postgres container it started is stopped automatically.

## Running the tests

```bash
./mvnw test      # unit tests only (fast, no Docker required)
./mvnw verify     # unit + integration tests (requires Docker; spins up a real Postgres via Testcontainers)
```

Unit tests (`*Test`) cover service-layer business rules with Mockito, with no Spring context
and no database. Integration tests (`*IT`) boot the full Spring context (mock web layer, no
real server socket) against a real PostgreSQL container and exercise the REST controllers
end-to-end with MockMvc, including validation, pagination, filters and error responses.

A [GitHub Actions workflow](.github/workflows/ci.yml) runs this same `./mvnw verify` on every
pull request.

## Domain model and business rules

| Entity | Purpose |
|---|---|
| `CatalogItem` | A sellable product or service (`type` distinguishes them). `active=false` keeps it visible but blocks it from being added to new orders. |
| `Order` | A customer order. `status` is `OPEN` or `CLOSED`; `discountPercentage` (0–100) applies only to `PRODUCT` items. |
| `OrderItem` | A line item on an order. `unitPrice` is copied from the catalog item's price when the item is added, so past orders are unaffected by later price changes. |

Enforced rules:

1. **Discount only while open.** `discountPercentage` can only be changed while the order is
   `OPEN`. Attempting to change it on a `CLOSED` order returns `409 Conflict`.
2. **No dangling references.** Deleting a `CatalogItem` that is referenced by at least one
   `OrderItem` returns `409 Conflict`.
3. **No inactive items on new order lines.** Adding an `OrderItem` for a `CatalogItem` with
   `active=false` returns `409 Conflict`.
4. **Discount never applies to services.** Order totals are always computed from the current
   items — never persisted — as `productsTotal × (1 − discount/100) + servicesTotal`.

**Deliberately out of scope:** adding/removing/editing items on a `CLOSED` order is *not*
blocked. Only the discount change is gated on order status; extending that restriction to line
items as well was left out to keep the rule set intentional rather than speculative.

## API overview

All request/response bodies are JSON. Every non-2xx response uses one error shape:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Order ... is CLOSED; its discount can only be changed while the order is OPEN",
  "path": "/api/orders/...",
  "fieldErrors": []
}
```

`fieldErrors` is populated (one entry per invalid field) only for `400` responses caused by
Bean Validation failures.

### Catalog items — `/api/catalog-items`

| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/catalog-items` | Create a product or service |
| `GET` | `/api/catalog-items/{id}` | Fetch one |
| `GET` | `/api/catalog-items?name=&type=&active=&page=&size=&sort=` | Paginated, filterable list |
| `PUT` | `/api/catalog-items/{id}` | Full update |
| `DELETE` | `/api/catalog-items/{id}` | Fails with `409` if referenced by an order item |

### Orders — `/api/orders`

| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/orders` | Create an order (always starts `OPEN`) |
| `GET` | `/api/orders/{id}` | Full detail: items and computed totals |
| `GET` | `/api/orders?status=&createdFrom=&createdTo=&page=&size=&sort=` | Paginated, filterable list (summary rows, no items) |
| `PUT` | `/api/orders/{id}` | Update `discountPercentage`/`notes`; `409` if changing the discount on a `CLOSED` order |
| `PATCH` | `/api/orders/{id}/status` | Open or close the order |
| `DELETE` | `/api/orders/{id}` | Deletes the order and its items |

### Order items — `/api/order-items`

| Method | Path | Notes |
|---|---|---|
| `POST` | `/api/order-items` | Add a line item; `409` if the catalog item is inactive |
| `GET` | `/api/order-items/{id}` | Fetch one |
| `GET` | `/api/order-items?orderId=&catalogItemId=&page=&size=&sort=` | Paginated, filterable list |
| `PUT` | `/api/order-items/{id}` | Update quantity |
| `DELETE` | `/api/order-items/{id}` | Remove the line item |

### Example: creating an order and adding a discounted product

```bash
# 1. Create a catalog product
curl -s -X POST http://localhost:8080/api/catalog-items \
  -H "Content-Type: application/json" \
  -d '{"name":"Office Chair","description":"Ergonomic","type":"PRODUCT","price":250.00,"active":true}'

# 2. Create an order
curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"discountPercentage":10,"notes":"First order"}'

# 3. Add the product to the order (use the ids returned above)
curl -s -X POST http://localhost:8080/api/order-items \
  -H "Content-Type: application/json" \
  -d '{"orderId":"<order-id>","catalogItemId":"<catalog-item-id>","quantity":2}'

# 4. Fetch the order — totals reflect the 10% discount on the product line
curl -s http://localhost:8080/api/orders/<order-id>
```

## Project structure

Packages are organized by feature rather than by technical layer:

```
com.leonardo.erp
├── catalogitem   # CatalogItem entity, repository (+ QueryDSL filtering), service, controller, DTOs
├── order         # Order entity, repository (+ QueryDSL filtering/projection), service, controller, DTOs
├── orderitem     # OrderItem entity, repository (+ QueryDSL filtering), service, controller, DTOs
├── common/exception  # ResourceNotFoundException, BusinessRuleException, ApiError, GlobalExceptionHandler (@RestControllerAdvice)
└── config        # QueryDSL JPAQueryFactory bean, OpenAPI metadata
```
