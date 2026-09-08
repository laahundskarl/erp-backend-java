# ERP Backend — Design Notes (Level III)

Source requirements: a Java backend coding assessment specification, Level III tier.

## Goal

A REST backend for a simplified order-management module of an ERP: a catalog of
sellable items (products or services), orders, and order line items, with the
discount, deactivation and referential-integrity rules described below.

## Stack

- Java 21, Spring Boot 3.3, Maven (via Maven Wrapper — no local Maven install required)
- Spring Web, Spring Data JPA, PostgreSQL 16
- QueryDSL (dynamic filters on list endpoints)
- Bean Validation (Jakarta Validation) on request DTOs
- Flyway for versioned schema migrations
- springdoc-openapi (Swagger UI) for interactive API docs
- Lombok to cut entity/DTO boilerplate
- JUnit 5 + Mockito for unit tests, Testcontainers for integration tests against a real Postgres

## Domain model

| Entity | Fields |
|---|---|
| `CatalogItem` | `id (UUID)`, `name`, `description`, `type (PRODUCT\|SERVICE)`, `price`, `active` |
| `Order` | `id (UUID)`, `createdAt`, `status (OPEN\|CLOSED)`, `discountPercentage`, `notes`, `items[]` |
| `OrderItem` | `id (UUID)`, `order`, `catalogItem`, `quantity`, `unitPrice` (snapshotted from the catalog item's price at the time the item is added, so historical orders are stable even if the catalog price changes later) |

Order totals (`productsTotal`, `servicesTotal`, `discountAmount`, `grandTotal`) are computed
on read from the current items, never persisted — this rules out the total drifting from
the underlying items.

`grandTotal = productsTotal * (1 - discountPercentage / 100) + servicesTotal`,
where `productsTotal`/`servicesTotal` are the summed `unitPrice * quantity` of items whose
`catalogItem.type` is `PRODUCT` / `SERVICE` respectively. The discount never applies to
service items, per the requirement.

## Business rules (enforced in the service layer, not just validation)

1. `discountPercentage` can only be changed while `Order.status == OPEN`; attempting it on a
   `CLOSED` order is a 409 Conflict.
2. Deleting a `CatalogItem` that is referenced by any `OrderItem` is a 409 Conflict.
3. Adding an `OrderItem` for a `CatalogItem` with `active == false` is a 409 Conflict.

## API shape

Each entity gets a full top-level REST resource (Create/Read/Update/Delete/paginated List),
since the spec lists all three as independent, first-class registrations:

- `/api/catalog-items` — filters: `name`, `type`, `active`
- `/api/orders` — filters: `status`, date range; plus `PATCH /api/orders/{id}/status` to
  open/close an order explicitly
- `/api/order-items` — filters: `orderId`, `catalogItemId`

Filters are implemented with QueryDSL predicates; pagination uses Spring's standard
`Pageable` (`page`, `size`, `sort` query params), responses use Spring's `Page<T>` JSON shape.

## Error handling

A single `@RestControllerAdvice`:

- Bean Validation failures (`MethodArgumentNotValidException`, `ConstraintViolationException`) → `400 Bad Request`, with a list of field-level messages
- Not found (custom `ResourceNotFoundException`) → `404 Not Found`
- Business rule violations (custom `BusinessRuleException`) → `409 Conflict`
- Anything unexpected → `500 Internal Server Error` with a generic body

All error responses share one JSON shape: `timestamp`, `status`, `error`, `message`, `path`,
and optionally `fieldErrors[]`.

## Testing strategy

- Unit tests (Mockito, no Spring context) for service-layer business rules: discount gating,
  delete guard, inactive-item guard, total calculation.
- Integration tests (`@SpringBootTest` + Testcontainers `PostgreSQLContainer`) for
  repositories/QueryDSL filters and for controllers end-to-end (status codes, validation,
  pagination). Requires Docker to be running locally to execute `./mvnw test`.

## Out of scope (YAGNI, not requested by the spec)

- Authentication/authorization
- Customer/party entities
- Blocking item add/remove on a `CLOSED` order (the spec only requires blocking the discount
  change on closed orders — extending the block to items would be an unrequested rule). This
  is called out again in the README so it reads as a deliberate scope decision.
