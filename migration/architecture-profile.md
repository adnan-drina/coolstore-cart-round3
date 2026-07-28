# Architecture Profile — CoolStore Cart Service (Legacy)

## 1. Purpose & domain

The CoolStore Cart Service is an e-commerce shopping cart management system that provides cart state manipulation and pricing services for an online retail platform (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7-13). The application serves web consumers and frontend clients that need to manage shopping carts across HTTP sessions.

The core domain consists of **Products** (catalog items with pricing), **Shopping Carts** (customer's selected items with quantities), and **Shopping Cart Item** (individual product selections with computed pricing). The service applies promotional pricing to both individual items (percentage discounts) and shipping costs (free shipping above cart value thresholds) through PromoService and ShippingService components (src/main/java/com/redhat/coolstore/service/PromoService.java:30-46, src/main/java/com/redhat/coolstore/service/ShippingService.java:10-24). All pricing is computed dynamically based on current product data and active promotion rules.

Key domain invariants: cart totals must remain consistent across add/remove operations, duplicate items are consolidated by product ID, and shipping costs are recalculated after every cart modification (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66-84).

## 2. Components & relationships

The application follows a layered architecture with clear separation of concerns (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-41):

```
┌─────────────────┐
│   CartEndpoint  │ ← HTTP/REST facade (session-scoped)
│  (@RestController)
└────────┬────────┘
         │ uses
         ▼
┌─────────────────────────┐
│  ShoppingCartService    │ ← Business logic facade
│  (interface + impl)     │
└────────┬───────┬────────┘
         │       │
         ▼       ▼
┌────────────────┐ ┌─────────────────┐
│ PromoService   │ │ ShippingService │ ← Pricing rules
│ (@Component)   │ │ (@Component)    │
└────────────────┘ └─────────────────┘
         │
         ▼
┌─────────────────────────────┐
│     Model Objects           │ ← Data structures
│  Product, ShoppingCart,     │
│  ShoppingCartItem, Promotion│
└─────────────────────────────┘
```

**God nodes** (dependency-order.md:8-14):
- `ShoppingCart` (fan-in: 5) — central aggregate holding cart state, pricing totals, and item collection
- `Product` (fan-in: 4) — referenced by all pricing and cart operations
- `ShoppingCartItem` (fan-in: 3) — carries product reference, quantity, and computed pricing

**Conversion order** (dependency-order.md:16-29): Model classes must be migrated first (Product → Promotion → ShoppingCartItem → ShoppingCart) before services, then REST endpoints. This ordering reflects compilation dependencies and ensures each layer compiles before dependent layers.

## 3. Integration surfaces

**Exposed APIs** (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:31-69):
- `GET /cart/{cartId}` — retrieve shopping cart state
- `POST /cart/{cartId}/{itemId}/{quantity}` — add items to cart
- `POST /cart/{cartId}/{tmpId}` — transfer cart contents between carts
- `DELETE /cart/{cartId}/{itemId}/{quantity}` — remove items from cart
- `POST /cart/checkout/{cartId}` — checkout cart (resets cart contents)

**Consumed services** (src/main/java/com/redhat/coolstore/service/CatalogService.java:10):
- External catalog service at `${CATALOG_ENDPOINT}` (application.properties:6) — provides product catalog via Feign client REST call to `/api/products` (CatalogService.java:12)

**Persistence surface** (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42):
- In-memory storage using `HashMap<String, ShoppingCart>` (ShoppingCartServiceImpl.java:42) — cart state is ephemeral and lost on application restart

**Configuration** (src/main/resources/application.properties:6):
- Environment-driven config via `CATALOG_ENDPOINT` property (application.properties:6) — supports override via environment variable or system property

**UNCOVERED preserve candidate**: No explicit migration.yaml preserve contract exists for the catalog endpoint configuration; this should be added to ensure the env-driven URL is maintained during migration.

## 4. Behavioral contract sources

The behavioral contract is defined by the test suite under `ShoppingCartServiceTest`:

**Core pricing behavior** (ShoppingCartServiceTest.java:39-54):
- Cart totals are calculated as sum of item prices × quantities (asserts `cartItemTotal` = 2000.0 for 2×1000-priced items)
- Free shipping promotion applies for carts ≥ $75 (asserts `shippingPromoSavings` = -10.99, `shippingTotal` = 0)
- Total equals cart items plus shipping (asserts `cartTotal` = 2000.0)

**Initialization behavior** (ShoppingCartServiceTest.java:28-36):
- Non-existent carts return initialized cart with zero totals (0.0 for all totals)
- Empty carts maintain zero state until items are added

**Product lookup contract** (ShoppingCartServiceTest.java:57-63):
- `getProduct(id)` returns product matching exact fields from catalog
- Product data is cached across calls and refreshed only when cache miss occurs

**Contract gaps**: No tests validate session-scoped REST behavior, concurrent access to shared cart map, or error handling for invalid catalog responses. The service has no defined contract for catalog service failures or malformed product data.

## 5. Modernization surface

**MUST change** (findings-inventory.md):
- `springboot-di-to-quarkus-00003` — Convert Spring `@Autowired` to CDI constructor injection for all services (CartEndpoint.java:28, ShoppingCartServiceImpl.java:28,33,36,39, PromoService.java:15, ShippingService.java:7)
- `jakarta-jaxrs-to-quarkus-00010` — Replace JAX-RS dependency with Quarkus REST (CartEndpoint.java:5-11)
- `springboot-web-to-quarkus-00000` — Convert Spring `@RestController` to native JAX-RS resource (CartEndpoint.java:21)
- `springboot-actuator-to-quarkus-0100` — Replace Spring Actuator with Quarkus health (`/q/health`)
- `javax-to-jakarta-import-00001` — Update all imports from `javax.*` to `jakarta.*` (CartEndpoint.java:5-11, ShoppingCartServiceImpl.java:11)

**SHOULD change**:
- `localhost-http-00001` — Make `CATALOG_ENDPOINT` configuration cloud-ready with default value (application.properties:6, ShoppingCartServiceTest.java:18)
- `springboot-metrics-to-quarkus-0200` — Replace Micrometer metrics with MP Metrics annotations

**POTENTIAL changes**:
- `demo-env-integration-00001` — External configuration surface needs preservation contract verification
- Platform contract rules: in-memory cart storage must be evaluated for cloud deployment, session-scoped REST resource needs Quarkus equivalent, and catalog service failure handling needs resilience patterns

## 6. Domain boundaries

The application represents a **single bounded context** with cohesive domain logic (migration/dependency-order.md:29). While the code is well-structured with clear separations, all components serve the unified purpose of shopping cart management (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66-84):

- The **cart domain** encompasses the model classes (Product, ShoppingCart, ShoppingCartItem, Promotion) and their relationships
- The **pricing domain** comprises PromoService and ShippingService with their business rules (src/main/java/com/redhat/coolstore/service/PromoService.java:30-46)
- The **API domain** consists of CartEndpoint providing REST operations (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:31-69)

Coupling analysis (migration/dependency-order.md:16-29): The ShoppingCart aggregate is the central hub, referenced by all other components. The explicit dependency chain (Model → Services → REST) indicates good architectural boundaries despite the monolith structure. No circular dependencies exist between components.

**Candidate seams for incremental modernization**: The clear separation suggests parallel migration tracks could migrate (1) model classes as HARVEST, (2) pricing services as REDESIGN, and (3) REST endpoint as REDESIGN, though the tight integration argues for single-pass migration to preserve domain invariants.

## 7. Class roles & target contract

### HARVEST
- **Product** — Data transfer object carrying catalog item information; preserved as-is with serialization compatibility (src/main/java/com/redhat/coolstore/model/Product.java:5-54)
- **ShoppingCartItem** — Value object representing line items; preserved with enhanced constructor and validation (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:5-58)
- **Promotion** — Configuration object holding promotion rules; preserved as immutable record-like structure (src/main/java/com/redhat/coolstore/model/Promotion.java:3-41)

### REDESIGN

**ShoppingCartService** — removed — Interface eliminated; service becomes concrete CDI managed bean (src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:6-20)
- Target: Native CDI constructor-injected service (removed interface abstraction)
- Concurrency: Shared singleton with thread-safe HashMap operations and synchronized pricing
- Resource policy: In-memory cart cache with no explicit eviction; needs bounded refresh policy for production

**ShoppingCartServiceImpl** — REDESIGN — Core shopping cart business logic (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-41)
- Target: CDI `@ApplicationScoped` bean with constructor injection of dependencies
- Concurrency: Mutable state in shared HashMap requires thread-safety (ConcurrentHashMap, synchronized pricing operations)
- Resource/cache policy: Product map cache refresh on miss; needs explicit cache eviction or size bounds
- Aggregate/derived math: Normalize cart items BEFORE pricing derivations to ensure totals consistency
- API contract: All methods remain idempotent; `checkout()` clears cart items but maintains cart record for session continuity

**PromoService** — REDESIGN — Promotion pricing rules engine (src/main/java/com/redhat/coolstore/service/PromoService.java:16)
- Target: CDI `@ApplicationScoped` bean with constructor injection
- Concurrency: Immutable promotion set with thread-safe reads; writes occur only during initialization
- Resource policy: Promotion rules loaded at startup; no runtime modification expected

**ShippingService** — REDESIGN — Shipping cost calculation (src/main/java/com/redhat/coolstore/service/ShippingService.java:8)
- Target: CDI `@ApplicationScoped` bean with constructor injection  
- Concurrency: Stateless service with no mutable state; inherently thread-safe
- Resource policy: No caching or external resource usage

**CatalogService** — REDESIGN — External catalog integration (src/main/java/com/redhat/coolstore/service/CatalogService.java:10)
- Target: Quarkus REST client with `@RegisterRestClient` and env-driven URL config
- Concurrency: Stateless client with connection pooling via Quarkus REST
- Resource policy: Timeout and retry policies per Quarkus REST client defaults; circuit breaker recommended

**CartEndpoint** — REDESIGN — REST API facade (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21)
- Target: JAX-RS `@Path` resource with Quarkus REST (replaces Spring `@RestController`)
- Concurrency: Session-scoped resource (replaced with request-scoped; Quarkus doesn't support session scope by default)
- API contract (behavior-CHANGING): 
  - GET operations remain idempotent read-only
  - POST/DELETE operations mutate state (legacy behavior preserved)
  - No input validation changes planned
  - Error mapping: Map exceptions to appropriate HTTP status codes per JAX-RS standards
- Note: Explicit departure from Spring session scope; request-scoped implementation chosen for Quarkus compatibility

**JerseyConfig** — removed — Jersey configuration subsumed by Quarkus auto-discovery (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:7)
- Target: Removed entirely; Quarkus auto-discovers JAX-RS resources without explicit registration

**CartServiceApplication** — removed — Spring Boot bootstrap subsumed by Quarkus CDI (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7)
- Target: Removed entirely; Quarkus provides native CDI bootstrap and discovery
