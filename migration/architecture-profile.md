# Architecture profile — Coolstore cart service

## Purpose & domain

The Coolstore cart service manages shopping cart operations for an e-commerce application, providing cart lifecycle management, dynamic pricing with promotions, and shipping cost calculations. The application serves customers by maintaining persistent cart state and computing totals in real-time as items are added, removed, or modified (ShoppingCartServiceTest:39-54).

Core domain concepts include:
- **Shopping Cart** - Container for cart items with pricing totals, promo savings, and shipping calculations
- **Product** - Retail items with identification, description, and pricing information  
- **Shopping Cart Item** - Individual cart entries linking products with quantities and calculated pricing
- **Promotion** - Discount rules applied to specific products or cart-level shipping thresholds

The service implements business rules for shipping tier calculations ($2.99 for <$25, $4.99 for <$50, $6.99 for <$75, $8.99 for <$100, $10.99 for ≥$100) and promotional discounts including a 25% discount on product ID "329299" and free shipping for carts ≥$75 (PromoService:27, 51-54).

## Components & relationships

The application follows a layered architecture with REST endpoints consuming services that operate on domain models (dependency-order.md:1-4, 19-29):

```
CartEndpoint → ShoppingCartService → ShoppingCartServiceImpl
                 ↓                      ↓
              PromoService           ShippingService
              (promotions)           (shipping tiers)
                 ↓                      ↓
              CatalogService ← (Feign client)
                 ↓
              Product (domain)
```

**Components:**
- **REST Layer** (`com.redhat.coolstore.rest.*`) - CartEndpoint exposes cart operations via JAX-RS (src/main/java/com/redhat/coolstore/rest/CartEndpoint:21-24)
- **Service Layer** (`com.redhat.coolstore.service.*`) - Business logic for cart management, pricing, promotions (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl:28-29)
- **Domain Layer** (`com.redhat.coolstore.model.*`) - Data structures and business entities (src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1-7)
- **Configuration** (`CartServiceApplication`, `JerseyConfig`) - Bootstrap and Jersey setup (src/main/java/com/redhat/coolstore/CartServiceApplication:7-9)

**God nodes** (highest fan-in): ShoppingCart (5), Product (4), ShoppingCartItem (3) - these central entities require careful characterization testing due to their widespread usage across components (dependency-order.md:8-14).

The service layer implements Inversion of Control with Spring DI (@Autowired dependencies), while maintaining in-memory cart state via HashMap storage for simplicity (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl:42, 49).

## Integration surfaces

**Exposed APIs:**
- JAX-RS REST endpoints under `/cart` path (src/main/java/com/redhat/coolstore/rest/CartEndpoint:23, 31-69):
  - `GET /cart/{cartId}` - Retrieve cart contents (CartEndpoint:34)
  - `POST /cart/{cartId}/{itemId}/{quantity}` - Add items to cart (CartEndpoint:44)  
  - `POST /cart/{cartId}/{tmpId}` - Replace cart contents (CartEndpoint:52)
  - `DELETE /cart/{cartId}/{itemId}/{quantity}` - Remove items from cart (CartEndpoint:61)
  - `POST /cart/checkout/{cartId}` - Clear cart after purchase (CartEndpoint:68)

**Consumed services:**
- CatalogService (Feign client) calls external product catalog at `${CATALOG_ENDPOINT}/api/products` (src/main/java/com/redhat/coolstore/service/CatalogService:10, 12)
- Configuration via `application.properties`: `CATALOG_ENDPOINT=http://localhost:8081` (src/main/resources/application.properties:6, localhost-http-00001 finding)

**Persistence:**
- In-memory cart storage using HashMap (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl:42)
- No database persistence - cart state lost on application restart

**Messaging/Health:**
- Spring Boot Actuator endpoints for health/metrics (spring-boot-starter-actuator dependency, springboot-actuator-to-quarkus-0100 finding)
- Migrated to Quarkus health endpoints via `quarkus-smallrye-health` (preserve: health endpoints)

All integration surfaces map to `preserve:` items in migration.yaml - env-driven config (demo-env-integration-00001), REST API contracts, and health endpoints must remain functional post-migration.

## Behavioral contract sources

**Primary test suite** - ShoppingCartServiceTest establishes behavioral expectations:

**Cart initialization** (ShoppingCartServiceTest:28-36):
- New carts return zero totals for all pricing fields
- Verifies cartId assignment and empty state preservation

**Pricing calculations** (ShoppingCartServiceTest:39-54):
- Cart with 2x $1000 items → cartItemTotal = $2000
- Shipping cost $10.99 applied for cart total ≥$100
- Shipping promo savings = -$10.99 (free shipping trigger)
- Final cartTotal = $2000 (item total + shipping - promo savings)

**Product retrieval** (ShoppingCartServiceTest:57-64):
- Product lookup via catalog service integration
- Mock catalog service returns predefined product list (ProductsObjectMother)

**Contract gaps:**
- No explicit error handling tests for invalid product IDs
- No concurrent access testing for shared cart state
- No boundary testing for promotional discount calculations
- No session management validation despite @SessionScope annotation

## Modernization surface

**Mandatory changes** (by MTA rule):
- **javax→jakarta migration** (javax-to-jakarta-import-00001) - CartEndpoint, ShoppingCartServiceImpl import rewrites required
- **Spring→Quarkus BOM** (javaee-pom-to-quarkus-00010-60) - Platform dependency conversion mandatory
- **JAX-RS implementation** (jakarta-jaxrs-to-quarkus-00010) - Jersey→Quarkus REST migration
- **Spring Boot removal** (springboot-annotations-to-quarkus-00000) - Delete @SpringBootApplication main class
- **Health endpoint migration** (springboot-actuator-to-quarkus-0100) - Actuator→SmallRye Health

**Should changes:**
- **DI conversion** (springboot-di-to-quarkus-00003) - @Autowired→constructor injection recommended
- **Metrics conversion** (springboot-metrics-to-quarkus-0200) - Micrometer→MP Metrics annotations
- **Native REST** (springboot-web-to-quarkus-00000) - Direct JAX-RS resources vs spring-web extension

**Component-specific modernization:**
- **CartEndpoint** - JAX-RS annotations preserved, session scope maintained, DI converted to CDI
- **ShoppingCartServiceImpl** - @Service removed, constructor injection, HashMap concurrency reviewed
- **PromoService/ShippingService** - @Component removal, stateless service pattern
- **CatalogService** - Feign client replacement with REST client or direct HTTP calls

## Domain boundaries

**Single bounded context** - The application represents a cohesive cart management domain with tight coupling between components. Despite the modular package structure, the service forms one logical bounded context because (dependency-order.md:1-4, 19-29):

1. **Shared state** - All services operate on the same ShoppingCart/ShoppingCartItem domain objects
2. **Transaction boundary** - Cart pricing operations span multiple services in single logical operations  
3. **No clear separation** - No obvious subdomain boundaries within the cart functionality

**Coupling analysis:**
- Strong coupling between ShoppingCartServiceImpl and PromoService/ShippingService via method calls (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl:70-84)
- CatalogService decoupled via interface, but synchronous dependency for product lookups (src/main/java/com/redhat/coolstore/service/CatalogService:10-14)
- Domain models serve as data transfer objects across all layers (src/main/java/com/redhat/coolstore/model/*.java)

**Modernization approach** - Process as single migration story rather than domain decomposition.

## Class roles & target contract

### HARVEST classes (data/pure utilities)
- **Product** - Preserved exactly with serialization support and existing constructors
- **ShoppingCart** - Preserved with all pricing fields and cart operations, normalizeBeforeDerive pattern for pricing
- **ShoppingCartItem** - Preserved with quantity, pricing, and product linkage
- **Promotion** - Preserved with itemId and percentOff fields, seed data initialization

### REDESIGN classes (runtime behavior)
- **CartEndpoint** - Convert to Quarkus JAX-RS resource with `@Path`, `@GET`, `@POST`, `@DELETE` annotations preserved. Target: session-scoped CDI bean with constructor injection. Remove `@RestController`, `@Scope` annotations. GET endpoints remain idempotent, POST endpoints validate inputs. (src/main/java/com/redhat/coolstore/rest/CartEndpoint:21-24, 28-29)

- **ShoppingCartService** - Interface preserved, implementation modernized to CDI service with constructor injection. Remove `@Service` annotation. (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl:28-29)

- **ShoppingCartServiceImpl** - Target: CDI ApplicationScoped bean with constructor injection for ShippingService, CatalogService, PromoService. Remove @Service, @PostConstruct, @Autowired. Replace HashMap with ConcurrentHashMap for thread-safe singleton. Implement normalizeBeforeDerive: dedupe cart items before pricing calculations (ShoppingCartServiceImpl:200-221, 66-85). 

- **PromoService** - Target: CDI ApplicationScoped bean, remove @Component annotation, maintain promotion seed data and calculation logic unchanged (src/main/java/com/redhat/coolstore/service/PromoService:15, 27).

- **ShippingService** - Target: CDI ApplicationScoped bean, remove @Component annotation, maintain shipping tier calculations unchanged (src/main/java/com/redhat/coolstore/service/ShippingService:7).

- **CatalogService** - Target: REST client interface using Quarkus REST Client, maintain `@GetMapping("/api/products")` equivalent via `@GET` annotation with path preservation. Remove Feign annotations, use `${CATALOG_ENDPOINT:default}` configuration pattern (src/main/java/com/redhat/coolstore/service/CatalogService:10-11).

- **CartServiceApplication** - removed - what subsumes it: Quarkus bootstrap replaces Spring Boot main class, auto-discovers JAX-RS resources and CDI beans (src/main/java/com/redhat/coolstore/CartServiceApplication:7).

- **JerseyConfig** - removed - what subsumes it: Quarkus auto-discovers JAX-RS resources without explicit registration, CDI beans auto-discovered (src/main/java/com/redhat/coolstore/rest/JerseyConfig:6).

**Concurrency considerations:** ShoppingCartServiceImpl becomes shared singleton with mutable cart state. Target uses ConcurrentHashMap with compute() operations for thread-safe updates.

**Resource policy:** Product catalog results cached in memory without expiration - target maintains caching behavior but consider bounded cache implementation.

**Contract decisions:**
- **GET idempotency** - Cart retrieval operations remain read-only, no state mutation
- **Input validation** - Add validation for cartId, itemId, quantity parameters (positive integers)
- **Error mapping** - 503 Service Unavailable for catalog service failures, 404 Not Found for invalid cartId, 400 Bad Request for invalid quantities
- **Cache refresh guard** - Product catalog cache never expires (legacy behavior) - target maintains unbounded in-memory cache
- **Error handling** - Runtime exceptions propagate as HTTP 500, no custom ExceptionMapper implementation needed