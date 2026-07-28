# M1 Architecture Profile — Coolstore Cart Service

## 1. Purpose & domain

The Coolstore cart service provides shopping cart functionality for an e-commerce application (`CartServiceApplication.java:11`). It allows customers to create and manage shopping carts, add/remove items, calculate pricing with promotions and shipping costs, and process checkout operations (`ShoppingCartServiceTest.java:39`). The service manages cart state in-memory using `HashMap<String, ShoppingCart>` (`ShoppingCartServiceImpl.java:42`) and integrates with an external catalog service to fetch product information (`CatalogService.java:10`).

The core domain concepts are:
- **ShoppingCart**: A customer's cart containing items, pricing calculations, and totals (`ShoppingCart.java:7`)
- **ShoppingCartItem**: Individual items in a cart with product reference, quantity, and pricing (`ShoppingCartItem.java:5`)
- **Product**: Product information retrieved from external catalog (id, name, description, price) (`Product.java:5`)
- **Promotion**: Price reductions applied to specific products or cart-wide (e.g., free shipping threshold) (`Promotion.java:3`)
- **Pricing Logic**: Cart totals, item-level promotions, shipping calculations with tiered rates (`ShoppingCartServiceImpl.java:66`)

The application exposes REST endpoints for cart operations (`CartEndpoint.java:24`) and uses environment-driven configuration for the catalog service endpoint (`application.properties:6`).

## 2. Components & relationships

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ CartEndpoint│────▶│ShoppingCartService│────▶│  PromoService   │
└─────────────┘     │   (Interface)    │     │                 │
        │           └─────────────────┘     └─────────────────┘
        │                    │                       │
        ▼                    ▼                       ▼
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ JerseyConfig│     │ShoppingCartService│     │ShippingService  │
└─────────────┘     │     Impl         │     │                 │
                    └─────────────────┘     └─────────────────┘
                              │                       │
                              ▼                       ▼
                    ┌─────────────────┐     ┌─────────────────┐
                    │  CatalogService │     │   (Models)      │
                    │  (Feign Client) │     │ShoppingCart, etc│
                    └─────────────────┘     └─────────────────┘
```

**Architecture Statement**: This is a service-oriented design with clear separation between REST endpoints (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:1`), business logic (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1`), pricing services (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:16`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:8`), and data transfer objects (the model classes). The `CatalogService` is an external integration point implemented as a Feign client (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:5`). All services are Spring-managed components with dependency injection wiring the relationships (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-40`).

**God Nodes** (highest fan-in, from `dependency-order.md`):
- `ShoppingCart` (5 incoming): Central data structure referenced by all cart operations
- `Product` (4 incoming): Fundamental data type used throughout pricing logic  
- `ShoppingCartItem` (3 incoming): Container for cart items, referenced in pricing calculations
- `ShoppingCartService` (2 incoming): Core service interface referenced by endpoint and implementation

## 3. Integration surfaces

**Exposed APIs** (REST endpoints):
- `GET /cart/{cartId}` — Retrieve cart contents (`CartEndpoint.java:34`)
- `POST /cart/{cartId}/{itemId}/{quantity}` — Add item to cart (`CartEndpoint.java:41`)
- `POST /cart/{cartId}/{tmpId}` — Replace cart contents from temporary cart (`CartEndpoint.java:50`)
- `DELETE /cart/{cartId}/{itemId}/{quantity}` — Remove item from cart (`CartEndpoint.java:58`)
- `POST /cart/checkout/{cartId}` — Process checkout (clears cart) (`CartEndpoint.java:67`)

**Consumed Services**:
- Catalog service via Feign client (`CatalogService.java:10`) — URL configured via `${CATALOG_ENDPOINT}` (`application.properties:6`)

**Persistence**:
- In-memory cart storage using `HashMap<String, ShoppingCart>` (`ShoppingCartServiceImpl.java:42`)

**Configuration**:
- Environment-driven: `CATALOG_ENDPOINT=http://localhost:8081` (`application.properties:6`)
- JAX-RS application path: `/api` (JerseyConfig + application.properties configuration)

**Preserve-candidate surfaces**:
- Environment-driven catalog endpoint configuration — covered by `demo-env-integration-00001`
- Cart API contract preservation (paths, verbs, payload types) — UNCOVERED preserve candidate

## 4. Behavioral contract sources

**Primary test suite**: `ShoppingCartServiceTest` defines the core behavioral contracts:

**Cart initialization behavior** (`ShoppingCartServiceTest.java:28`):
- New carts start with zero totals (cartItemPromoSavings=0.0, cartItemTotal=0.0, shippingPromoSavings=0.0, cartTotal=0.0)

**Cart pricing behavior** (`ShoppingCartServiceTest.java:39`):
- Item pricing: price × quantity → cartItemTotal (`assertion: returns 2000.0, ShoppingCart::getCartItemTotal`)
- Shipping promotion: carts ≥ $75 get free shipping (`assertion: returns -10.99, ShoppingCart::getShippingPromoSavings`)
- Cart total: cartItemTotal + shippingTotal = cartTotal (`assertion: returns 2000.0, ShoppingCart::getCartTotal`)

**Product lookup behavior** (`ShoppingCartServiceTest.java:57`):
- Catalog service integration: `CatalogService.products()` returns product list, cached in `productMap`
- Product matching: `getProduct("2222")` finds product by itemId

**Contract gaps**:
- No explicit error handling contract (what happens when catalog service is unavailable)
- No validation contract (what happens with invalid quantities or non-existent products)
- No timeout/retry policy for catalog service calls

## 5. Modernization surface

**Component: REST Endpoint**
- **MUST change**: `@RestController` → JAX-RS annotations (`spring-web-to-quarkus-00000`, `jakarta-jaxrs-to-quarkus-00010`)
- **MUST change**: Field injection (`@Autowired`) → constructor injection (`springboot-di-to-quarkus-00003`)
- **MUST change**: Session scope → Quarkus-managed scoped bean (Jersey session handling removed)

**Component: Service Layer**
- **MUST change**: `@Service` → CDI managed bean (`springboot-di-to-quarkus-00003`)
- **MUST change**: `@Component` → CDI managed bean (`springboot-di-to-quarkus-00003`)
- **MUST change**: Field injection → constructor injection (`springboot-di-to-quarkus-00003`)

**Component: External Integration**
- **MUST change**: Feign client → REST Client (`springboot-di-to-quarkus-00000`)
- **MUST change**: Environment config → Quarkus REST client config (`demo-env-integration-00001`)

**Component: Configuration**
- **MUST change**: `@SpringBootApplication` → Quarkus bootstrap (removed entirely) (`springboot-annotations-to-quarkus-00000`)
- **MUST change**: Jersey config → Quarkus auto-discovery (`springboot-annotations-to-quarkus-00000`)

**Component: Platform**
- **MUST change**: Spring Boot → Quarkus platform (`javaee-pom-to-quarkus-*`)
- **MUST change**: Spring Actuator → Quarkus health (`springboot-actuator-to-quarkus-0100`)
- **MUST change**: javax → jakarta imports (`javax-to-jakarta-import-00001`)

**SHOULD change**: Metrics instrumentation (`springboot-metrics-to-quarkus-0200`)

## 6. Domain boundaries

**Single bounded context**: This service forms a coherent domain boundary where all components work together to provide cart functionality (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:24-69`). The cart domain is self-contained with:
- Clear data model (ShoppingCart, ShoppingCartItem, Product, Promotion) (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:7`, `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:5`, `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:5`, `/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:3`)
- Business rules (promotion application, shipping calculations) (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:30`, `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:10`)
- External integration (catalog service for product information) (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10`)
- Session management (per-cart state) (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:22`)

**Coupling characteristics**:
- Tight coupling between cart operations and pricing logic (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66` depends on PromoService, ShippingService)
- Loose coupling to catalog service via Feign client interface (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:5`)
- In-memory state management creates no external dependencies (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42`)

**Incremental modernization seams** (based on `dependency-order.md` conversion sequence):
1. **Data layer**: Models (Product, ShoppingCart, ShoppingCartItem, Promotion) — conversion order 1,2,4,6
2. **Service layer**: Pricing services (PromoService, ShippingService) — conversion order 8,9
3. **Integration layer**: CatalogService conversion — conversion order 5
4. **Orchestration**: ShoppingCartService implementation — conversion order 11
5. **Endpoint layer**: CartEndpoint last — conversion order 10

## 7. Class roles & target contract

### REDESIGN (modernized services, endpoints, configuration)

**CartEndpoint** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`)
- Role: REST resource exposing cart operations
- Target: JAX-RS resource with CDI constructor injection
- Target contract: 
  - Read operations return **404** on non-existent carts (never creates implicit carts)
  - Invalid inputs rejected with **400** (problem-detail format)
  - Downstream failures return **503** via JAX-RS ExceptionMapper
  - Concurrent access: thread-safe cart operations via synchronized HashMap
  - Cache policy: no product cache eviction on miss (bounded productMap with refresh guard)

**JerseyConfig** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`)
- Role: JAX-RS application configuration
- Target: removed — auto-discovery by Quarkus (`springboot-annotations-to-quarkus-00000`)

**CartServiceApplication** (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java`)
- Role: Spring Boot bootstrap and Feign client enablement
- Target: removed — Quarkus auto-configuration (`springboot-annotations-to-quarkus-00000`)

**ShoppingCartService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`)
- Role: Cart operations interface
- Target: CDI managed bean interface
- Target contract: Idempotent read operations, consistent pricing calculations, normalized-before-deriving cart totals

**ShoppingCartServiceImpl** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`)
- Role: Cart operations implementation with in-memory storage
- Target: CDI managed bean with constructor injection
- Target contract:
  - Concurrency: thread-safe singleton with ConcurrentHashMap and compute() operations
  - Resource policy: bounded productMap (no clear-on-miss), cart eviction strategy needed
  - Aggregate math: normalize-before-deriving (dedupe cart items before pricing calculations)
  - Error handling: catalog service failures surface as 503 via ExceptionMapper

**PromoService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java`)
- Role: Promotion calculation service
- Target: CDI managed bean with constructor injection
- Target contract: Thread-safe promotion calculations, no mutable state

**ShippingService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java`)
- Role: Shipping calculation service
- Target: CDI managed bean with constructor injection
- Target contract: Deterministic shipping rate calculations

**CatalogService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`)
- Role: External catalog integration via Feign client
- Target: REST Client with environment-driven URL configuration
- Target contract: Product catalog access with retry/timeout policies

### HARVEST (data/DTO/value objects)

**ShoppingCart** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`)
- Role: Cart data structure with pricing fields
- Target: preserve existing structure and behavior

**ShoppingCartItem** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`)
- Role: Cart item data structure
- Target: preserve existing structure and behavior

**Product** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`)
- Role: Product data structure
- Target: preserve existing structure and behavior

**Promotion** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java`)
- Role: Promotion data structure
- Target: preserve existing structure and behavior