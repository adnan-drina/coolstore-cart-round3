# S01 §7 Target Shapes — REDESIGN Classes for Future Stories

This document establishes traceability to architecture-profile §7 (Class roles & target contract)
for all REDESIGN classes that are **out of scope** for S01 but will be implemented in future stories.

## S01 Scope Boundary

S01 converts only HARVEST foundation data models:
- `Product` → `com.demo.model.Product`
- `ShoppingCart` → `com.demo.model.ShoppingCart`
- `ShoppingCartItem` → `com.demo.model.ShoppingCartItem`

No REDESIGN classes are implemented in S01. The target shapes below are documented here so
future stories have an authoritative reference without re-reading the architecture profile.

## UI Surface Waiver

Foundation data models have no direct UI surface. Cart operations are exposed exclusively
through `CartEndpoint` (S04). All user-facing error handling, input validation, and status
codes are the responsibility of the endpoint layer, not the model layer.

## Preserve Contracts

Enforced by `migration.yaml` preserve list and preflight sensor grep:

| Contract | Source | Enforcement |
|---|---|---|
| `CATALOG_ENDPOINT` environment variable | migration.yaml §27-31 | Preflight sensor greps `src/main`, `pom.xml`, `k8s/` |

Any commit that removes `CATALOG_ENDPOINT` from configuration is a functional regression.

## Forbidden Guards

Enforced by `migration.yaml` forbidden list — sensors fail any commit introducing these into `src/main`:

| Pattern | Source |
|---|---|
| `getMockProducts` | migration.yaml §32 |
| `mock products` | migration.yaml §33 |
| `Mock products` | migration.yaml §34 |
| `mock Products` | migration.yaml §35 |
| `Fallback to mock` | migration.yaml §36 |

These are fabrication tripwires from run #2 T-027. The service layer must never fall back to
mock product data; catalog service failures must surface as 503 errors.

## REDESIGN Target Shapes by Story

### S02 — Service Interfaces and External Integration

#### CatalogService
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`
- **Role**: External catalog integration via Feign client
- **Target**: Quarkus REST Client with environment-driven URL configuration
- **Target contract**:
  - Product catalog access with retry/timeout policies
  - URL sourced from `CATALOG_ENDPOINT` environment variable (preserve contract)
  - No mock fallbacks on catalog unavailability (forbidden guard)

#### Promotion (model)
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java`
- **Role**: Promotion data structure
- **Target**: HARVEST — preserve existing structure and behavior
- **Package**: `com.redhat.coolstore.model` → `com.demo.model`

#### ShoppingCartService (interface)
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`
- **Role**: Cart operations interface
- **Target**: CDI managed bean interface
- **Target contract**: Idempotent read operations, consistent pricing calculations

### S03 — Core Business Logic Services

#### PromoService
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java`
- **Role**: Promotion calculation service
- **Target**: CDI managed bean with constructor injection
- **Target contract**:
  - Thread-safe promotion calculations
  - No mutable state

#### ShippingService
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java`
- **Role**: Shipping calculation service
- **Target**: CDI managed bean with constructor injection
- **Target contract**:
  - Deterministic shipping rate calculations

#### ShoppingCartServiceImpl
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`
- **Role**: Cart operations implementation with in-memory storage
- **Target**: CDI managed bean with constructor injection
- **Target contract**:
  - Concurrency: thread-safe singleton with `ConcurrentHashMap` and `compute()` operations
  - Resource policy: bounded productMap (no clear-on-miss), cart eviction strategy
  - Aggregate math: normalize-before-deriving (dedupe cart items before pricing)
  - Error handling: catalog service failures surface as 503 via ExceptionMapper

### S04 — Cart Operations and REST Endpoints

#### CartEndpoint
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`
- **Role**: REST resource exposing cart operations
- **Target**: JAX-RS resource with CDI constructor injection
- **Target contract**:
  - Read operations return **404** on non-existent carts (never creates implicit carts)
  - Invalid inputs rejected with **400** (problem-detail format)
  - Downstream failures return **503** via JAX-RS ExceptionMapper
  - Concurrent access: thread-safe cart operations
  - Cache policy: bounded productMap with refresh guard (no eviction on miss)

### S05 — Configuration and Bootstrap Cleanup

#### CartServiceApplication
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java`
- **Role**: Spring Boot bootstrap and Feign client enablement
- **Target**: Removed — Quarkus auto-configuration replaces Spring Boot bootstrap

#### JerseyConfig
- **Legacy**: `/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`
- **Role**: JAX-RS application configuration
- **Target**: Removed — Quarkus auto-discovery replaces explicit JAX-RS config

## Target Contract Flags

From `migration.yaml` targetContract (authoritative for all REDESIGN classes):

| Flag | Value | Applies to |
|---|---|---|
| `getIdempotent` | true | ShoppingCartService, CartEndpoint reads |
| `validateInput` | true | CartEndpoint |
| `mapErrors` | true | CartEndpoint, ShoppingCartServiceImpl |
| `threadSafeState` | true | ShoppingCartServiceImpl, PromoService |
| `cacheRefreshGuard` | true | ShoppingCartServiceImpl, CartEndpoint |
| `normalizeBeforeDerive` | true | ShoppingCartServiceImpl |

## Traceability Matrix

| Class | Story | Role | §7 Source |
|---|---|---|---|
| CatalogService | S02 | REDESIGN | architecture-profile §7 |
| Promotion (model) | S02 | HARVEST | architecture-profile §7 |
| ShoppingCartService | S02 | REDESIGN | architecture-profile §7 |
| PromoService | S03 | REDESIGN | architecture-profile §7 |
| ShippingService | S03 | REDESIGN | architecture-profile §7 |
| ShoppingCartServiceImpl | S03 | REDESIGN | architecture-profile §7 |
| CartEndpoint | S04 | REDESIGN | architecture-profile §7 |
| CartServiceApplication | S05 | REDESIGN (removed) | architecture-profile §7 |
| JerseyConfig | S05 | REDESIGN (removed) | architecture-profile §7 |
