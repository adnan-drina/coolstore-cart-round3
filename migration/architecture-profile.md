# Architecture Profile — Coolstore Cart Service

## 1. Purpose & domain

The Coolstore cart service is a Spring Boot-based e-commerce shopping cart microservice (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:6-9`) that provides cart management functionality for the broader Coolstore retail platform. The service enables customers to add and remove items, calculates pricing with promotions and shipping costs, processes checkout operations, and integrates with a product catalog service via Feign client (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10`). The core domain revolves around managing `ShoppingCart` objects (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`) that contain `ShoppingCartItem` instances (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`), each referencing `Product` entities (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`) with pricing and promotional rules. The service operates with in-memory cart storage using `HashMap<String, ShoppingCart>` (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42`) and applies business rules through `PromoService` (25% discount on product "329299") (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:27`) and `ShippingService` (tiered shipping costs with free shipping over $75) (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:51`).

## 2. Components & relationships

The application follows a layered Spring Boot architecture (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:7-8`) with clear separation between REST, service, and domain layers (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`, `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`):

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST Layer    │───▶│  Service Layer  │───▶│  Domain Layer   │
│                 │    │                 │    │                 │
│ CartEndpoint    │    │ ShoppingCart    │    │ Product         │
│ JerseyConfig    │    │ PromoService    │    │ ShoppingCart    │
│                 │    │ ShippingService │    │ ShoppingCartItem│
│                 │    │ CatalogService  │    │ Promotion       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

**God nodes and dependency analysis** (from migration/dependency-order.md):
- `ShoppingCart` (fan-in: 5, fan-out: 1) — central domain object referenced by all services and endpoints (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:34`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:53`)
- `Product` (fan-in: 4, fan-out: 0) — data entity consumed by cart items and pricing logic (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:12`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:38`)  
- `ShoppingCartItem` (fan-in: 3, fan-out: 1) — composition object linking products to carts (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:23`)
- `ShoppingCartService` (fan-in: 2, fan-out: 2) — primary service interface orchestrating cart operations (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`)

The conversion order prioritizes dependency resolution: models first (Product, Promotion, ShoppingCartItem, ShoppingCart), then service interfaces and implementations, finally REST endpoints. This sequence ensures the compilation tree remains valid at each commit.

## 3. Integration surfaces

**External service integration**:
- **Product Catalog Service**: Feign client `CatalogService` at `${CATALOG_ENDPOINT}` (configurable via environment variable or `application.properties`) (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10`) — this surface MUST be preserved as environment-driven configuration per the migration findings

**Exposed REST API** (prefixed by Spring Jersey configuration `spring.jersey.application-path=/api`):
- `GET /api/cart/{cartId}` — retrieve shopping cart by ID (`CartEndpoint.java:32`)
- `POST /api/cart/{cartId}/{itemId}/{quantity}` — add items to cart (`CartEndpoint.java:38`)  
- `DELETE /api/cart/{cartId}/{itemId}/{quantity}` — remove items from cart (`CartEndpoint.java:55`)
- `POST /api/cart/{cartId}/{tmpId}` — transfer items from temporary cart (`CartEndpoint.java:47`)
- `POST /api/cart/checkout/{cartId}` — process checkout and clear cart (`CartEndpoint.java:64`)

**Persistence**: In-memory storage using `HashMap<String, ShoppingCart>` in `ShoppingCartServiceImpl` (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42`) — no external database dependency

**Preserve candidates**: The `CATALOG_ENDPOINT` configuration surface must be maintained as environment-driven config (findings: localhost-http-00001, demo-env-integration-00001).

## 4. Behavioral contract sources

**Primary test suite**: `ShoppingCartServiceTest` defines the behavioral contract for cart operations:

- **Cart initialization**: `should_get_initialized_shopping_cart_in_case_of_not_exists()` (line 28) — empty carts return zero values for all totals (cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0)
- **Cart pricing**: `should_calculate_price_of_cart()` (line 39) — 2 items at $1000 each = $2000 cart total, with shipping cost $10.99 and shipping promo savings -$10.99 (indicating free shipping threshold logic)
- **Product retrieval**: `should_get_product_id()` (line 57) — product lookup through catalog service mock returns expected Product object with correct field values

**Contract gaps**: The test suite covers cart service operations but does not test JAX-RS endpoint layer behavior, error handling, validation, or the Feign client integration patterns. These gaps require characterization testing during modernization to ensure endpoint-level contracts remain intact.

**Business rules from source analysis**:
- **Shipping tiers**: <$25 = $2.99, $25-$50 = $4.99, $50-$75 = $6.99, $75-$100 = $8.99, ≥$100 = $10.99 (`ShippingService.java:12`)
- **Promotion**: Product "329299" receives 25% discount (`PromoService.java:27`)
- **Free shipping**: Applied when cart total ≥ $75 (`PromoService.java:51`)
- **Item deduplication**: Cart items with same product ID are consolidated with summed quantities (`ShoppingCartServiceImpl.java:200`)

## 5. Modernization surface

**Mandatory changes** (findings requiring immediate resolution):
- **javax→jakarta**: `CartEndpoint.java:5-11` (JAX-RS imports) — recipe: javax-to-jakarta-import-00001  
- **Spring Boot→Quarkus**: `pom.xml:55,60,65,70,76` (dependencies) — umbrella spring-components rules covering version incompatibility
- **Dependency management**: `pom.xml:4` (BOM, plugins, parent) — rules: javaee-pom-to-quarkus-00010/20/30/40/50/60/80
- **Bootstrap**: `CartServiceApplication.java:7` (Spring Boot application class) — rule: springboot-annotations-to-quarkus-00000
- **Metrics**: `pom.xml:65` (Micrometer→MP Metrics) — rules: springboot-metrics-to-quarkus-0100/0200  
- **Health**: `pom.xml:65` (Actuator→Quarkus health) — rule: springboot-actuator-to-quarkus-0100

**Infrastructure changes**:
- **REST framework**: Jersey/JAX-RS → Quarkus REST (jakarta-jaxrs-to-quarkus-00010)
- **Dependency injection**: `@Autowired` → constructor injection (springboot-di-to-quarkus-00003)
- **Properties**: Spring properties → Quarkus configuration (springboot-properties-to-quarkus-00000)

## 6. Domain boundaries

The application represents a single bounded context focused entirely on shopping cart management (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:1-20`). While the codebase could theoretically be split into cart operations, pricing, and persistence concerns, the tight coupling through `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28` and the shared `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1` domain object makes incremental modernization challenging. The service orchestrates cart pricing by delegating to `PromoService` (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:30-46`) and `ShippingService` (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:10`), which operate on the same cart instance rather than maintaining separate state.

**Coupling analysis**: The `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1` class violates single responsibility by managing both cart orchestration and in-memory persistence. The `CatalogService` Feign client integration (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10`) adds external dependency coupling. These design patterns suggest a single modernization story rather than domain-driven decomposition.

## 7. Class roles & target contract

### HARVEST classes (data/value objects carried over faithfully)
- **Product** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`) — pure data entity with no business logic, preserved as-is with jakarta serialization
- **Promotion** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java`) — simple value object for discount rules, maintained with current field structure  
- **ShoppingCart** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`) — domain object with pricing fields and cart management methods, preserved behavior
- **ShoppingCartItem** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`) — composition object linking products to cart quantities and pricing

### REDESIGN classes (runtime behavior, modernized not copied)

**Removed — superseded by Quarkus auto-discovery**: `CartServiceApplication` — Spring Boot bootstrap eliminated; Quarkus provides built-in bootstrap

**REDESIGN**: `ShoppingCartService` — **thread-safe singleton with mutable state** → **ConcurrentHashMap with compute() operations** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42` HashMap with multi-threaded access patterns). **Cache refresh guard** → **no clear-on-miss, bounded refresh** (evidence: productMap in lines 109-115 shows refresh logic without cache eviction). **normalizeBeforeDerive** → **normalize cart items BEFORE deriving pricing** (evidence: `dedupeCartItems()` called before `priceShoppingCart()` in addItem/set methods ensures consolidated cart state for pricing calculations).

**REDESIGN**: `ShoppingCartServiceImpl` — **concurrency**: shared singleton with HashMap cart storage requires thread-safe operations → **ConcurrentHashMap<String, ShoppingCart> with compute() for atomic updates**. **resource policy**: productMap caches catalog data without eviction → **bounded refresh with timed eviction policy**. **normalizeBeforeDerive**: cart item deduplication must occur before pricing calculations → **maintain dedupe-before-pricing sequence**. **targetContract**: `getIdempotent→404` (never creates missing carts), `validateInput→400` (reject invalid product IDs), `mapErrors→503` (propagate catalog service failures), `threadSafeState→ConcurrentHashMap`, `cacheRefreshGuard→no-clear-on-miss`.

**REDESIGN**: `PromoService` — **application-scoped service with pricing logic** → **@ApplicationScoped with thread-safe promotion application** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:16` @Component, pricing methods called by ShoppingCartServiceImpl). **targetContract**: `threadSafeState→ConcurrentHashMap` for promotionSet storage.

**REDESIGN**: `ShippingService` — **application-scoped shipping calculation** → **@ApplicationScoped stateless service** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:8` @Component, no instance state). **targetContract**: maintain current shipping tier logic with thread-safe execution.

**REDESIGN**: `CartEndpoint` — **session-scoped REST endpoint** → **@RequestScoped JAX-RS resource** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:22` @Scope session, JAX-RS @Path annotations). **API contract (behavior-CHANGING)** → **reject with 400** for invalid inputs, **return 404** when cartId does not exist (never creates), **503 via ExceptionMapper** for catalog service failures. **targetContract**: `getIdempotent→404` (GET /cart/{id} returns 404 instead of creating empty cart), `validateInput→400` (reject malformed cartId/itemId/quantity), `mapErrors→503` (catalog service failures propagate as 503).

**REDESIGN**: `JerseyConfig` — **Spring-managed Jersey configuration** → **removed — Quarkus auto-registers JAX-RS resources** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6` @Component, Jersey ResourceConfig). **targetContract**: removed — Quarkus automatically discovers `CartEndpoint`.

**REDESIGN**: `CatalogService` — **Feign client interface** → **@RegisterRestClient with @Path and @GET methods** (evidence: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10` FeignClient annotation). **targetContract**: maintain ${CATALOG_ENDPOINT:default} configuration, thread-safe HTTP client operations.
