# CartFlow — Enterprise E-Commerce Backend

A production-style e-commerce backend built with Java 21 and Spring Boot, covering the full commerce lifecycle: catalog, cart, checkout, mock payments, coupons, reviews, notifications, and an admin dashboard — all secured with JWT authentication and role-based access control.

Built incrementally across 16 phases as a learning-first, architecture-first project. See [Engineering Decisions](#engineering-decisions) below for the reasoning behind key choices, not just what was built.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1, Spring MVC, Spring Data JPA (Hibernate) |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Database | MySQL 8, Flyway (20 versioned migrations) |
| Caching | Redis |
| Email | Spring Mail (SMTP) |
| Documentation | Swagger / OpenAPI |
| PDF Generation | OpenPDF |
| Containerization | Docker, Docker Compose |
| Build | Maven |

## Architecture

Layered architecture (Controller → Service → Repository → Database), organized as feature-oriented packages rather than layer-oriented ones — each business domain (`product`, `order`, `payment`, `coupon`, etc.) owns its full stack of controller/service/repository/entity/DTO.

```
Client
  ↓
Security Filter (JWT validation)
  ↓
Controller (validation, delegation only)
  ↓
Service (business logic, transactions)
  ↓
Repository (Spring Data JPA)
  ↓
MySQL
```

Every write path is server-authoritative: prices, totals, tax, and inventory are always calculated and verified server-side — the client never dictates a monetary value.

## Getting Started

### Option A — Docker Compose (recommended, zero manual setup)

```bash
git clone <repo-url>
cd cartflow
cp .env.example .env   # fill in real values
docker compose up --build
```

The app, MySQL, and Redis start together. Flyway runs all migrations automatically against a fresh database. API available at `http://localhost:8080`, docs at `http://localhost:8080/swagger-ui.html`.

### Option B — Local development (IDE)

Requires: Java 21, Maven, a running MySQL 8 instance, a running Redis instance.

1. Create the database: `CREATE DATABASE cartflow_dev;`
2. Set environment variables in your IDE run configuration:
    - `DB_PASSWORD`, `JWT_SECRET`, `MAILTRAP_USERNAME`, `MAILTRAP_PASSWORD`
3. Run `CartflowBackendApplication`. Flyway migrates on startup.

## Environment Variables

| Variable | Purpose |
|---|---|
| `DB_HOST` | MySQL host (defaults to `localhost`; Compose sets `mysql`) |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | HMAC-SHA256 signing key for access tokens (32+ chars) |
| `REDIS_HOST` | Redis host (defaults to `localhost`; Compose sets `redis`) |
| `MAILTRAP_USERNAME` / `MAILTRAP_PASSWORD` | SMTP credentials for transactional email |

No secret is ever hardcoded in source or committed — all externalized via environment variables from the first checkpoint of the project onward.

## API Documentation

Full interactive API documentation via Swagger UI at `/swagger-ui.html` once running — every endpoint, request/response schema, and authentication requirement is documented there rather than duplicated here.

Base path: `/api/v1`. Authentication: `Authorization: Bearer <token>` header, obtained via `POST /auth/login`.

## Core Features

- **Auth** — JWT access + rotating refresh tokens, BCrypt hashing, forgot/reset password via real email, account soft-delete
- **Catalog** — categories, brands, products with image upload, dynamic filtering (JPA Specifications), keyword search, Redis-cached reads
- **Cart & Wishlist** — server-computed totals, duplicate-item merging, ownership-scoped everywhere
- **Checkout** — two-pass inventory validation, atomic transaction spanning cart/inventory/address/coupon/order
- **Coupons** — percentage/fixed discounts, per-user usage limits, expiry and minimum-purchase enforcement
- **Orders** — full lifecycle (created → confirmed → packing → shipped → delivered), state-machine-enforced status transitions, PDF invoices, cancellation with inventory release and payment refund reconciliation
- **Payments** — mock gateway simulation (initiate/verify), retry-safe
- **Reviews** — verified-purchaser-only, live rating recalculation
- **Notifications** — real transactional email (order confirmation, payment status, shipping updates) plus in-app notification records
- **Admin** — dashboard, user management (block/activate), order fulfillment control, revenue/sales/product/customer reports

## Engineering Decisions

A few decisions worth calling out, since they reflect real trade-offs rather than defaults:

- **Cart/Wishlist totals are computed on read, not stored** — avoids a cached total silently drifting from actual line items. **Order totals, by contrast, are stored and frozen at checkout** — an order is a historical financial record and must never change after the fact, even if product prices change later.
- **Refresh tokens are opaque random strings, not JWTs**, with rotation on every use — reduces the blast radius of a leaked token and makes revocation trivial (a database delete, not a blocklist).
- **IDOR protection is structural, not incidental** — nearly every ownership-scoped query (`findByIdAndUser`, `findByIdAndCustomer`) makes it impossible to fetch another user's data by guessing an ID, rather than relying on a separate check that's easy to forget.
- **Authorization checks live on the Service layer, not the Controller** — so a future internal caller (a scheduled job, another service) can't accidentally bypass `@PreAuthorize` by skipping the HTTP layer.
- **N+1 queries are fixed via batch-fetch (`findByIdIn` + in-memory grouping)** rather than `@EntityGraph` or `JOIN FETCH`, for explicitness — a full product listing page resolves in ~5 queries regardless of page size, down from ~25.

## Project Structure

```
com.cartflow
├── config          # Security, CORS, OpenAPI, caching, static resources
├── security        # JWT filter, UserPrincipal, CustomUserDetailsService
├── common          # ApiResponse envelope
├── exception       # Custom exceptions + GlobalExceptionHandler
├── authentication  # register, login, refresh, logout, password reset
├── user / address / category / brand / product / inventory
├── cart / wishlist / coupon / order / payment / review / notification
└── admin           # dashboard, reports, order/user management
```

## Frontend

A React + TypeScript client consuming this API lives in a companion repository: [cartflow-frontend](https://github.com/ppatel-tech/cartflow-frontend) — covers the full customer journey (auth, catalog, cart, checkout, orders, reviews) and an admin panel (dashboard, order fulfillment, user management, reports).

## Database

20 versioned Flyway migrations under `src/main/resources/db/migration`, applied automatically on startup. Schema never modified via `ddl-auto` — Flyway is the single source of truth for structure (`ddl-auto: validate` only, per ADR-012).

## License

Educational / portfolio project.