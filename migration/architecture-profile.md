# Architecture Profile — Coolstore Cart Service (Spring Boot → Quarkus)

## 1. Purpose & domain

The Coolstore Cart Service is a shopping cart management microservice that provides cart lifecycle operations, pricing calculations, and shipping logic for an e-commerce platform. The application serves online shoppers by maintaining their cart state, calculating item totals with promotions, determining shipping costs based on cart value, and providing checkout functionality (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-70).

The core domain encompasses:
- **Cart management**: Create, retrieve, update, and delete cart items with session-scoped persistence (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42, src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:28)
- **Pricing engine**: Calculate item totals, apply promotional discounts (25% off item "329299"), and compute cart-level pricing (src/main/java/com/redhat/coolstore/service/PromoService.java:27, src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:52)
- **Shipping logic**: Tiered shipping costs based on cart total ($2.99 for <$25, $4.99 for $25-$49.99, $6.99 for $50-$74.99, $8.99 for $75-$99.99, $10.99 for $100+), with free shipping promotion for carts ≥$75 (src/main/java/com/redhat/coolstore/service/ShippingService.java:12-23, src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:52)
- **Product catalog integration**: Retrieve product information via Feign client from catalog service endpoint (src/main/java/com/redhat/coolstore/service/CatalogService.java:10)
- **Checkout workflow**: Clear cart items after successful order placement (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:142)

The service operates as an in-memory cart cache backed by HashMap storage (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42), replacing the original Infinispan Hot Rod distributed cache for migration demonstration purposes.

## 2. Components & relationships

The architecture follows a layered REST controller → service → model pattern with Spring dependency injection managing component lifecycle (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-70, src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-222):

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CartEndpoint  │    │JerseyConfig     │    │CartServiceApp   │
│   (@RestController│───▶│  (@Component)   │    │@SpringBootApp   │
│    @Path("/cart")│    │ResourceConfig   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │ injects                │ registers             │ bootstrap
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Service Layer                                  │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ShoppingCartService│  │PromoService  │  │ShippingService   │  │
│  │Impl (@Service)   │◀─▶│(@Component)  │◀─│(@Component)      │  │
│  └──────────────────┘  └──────────────┘  └──────────────────┘  │
│           │                                                       │
│           │ calls                                                 │
│           ▼                                                       │
│  ┌──────────────────┐                                             │
│  │ CatalogService   │                                             │
│  │ (@FeignClient)   │◀────────────────────────────────────────────┘
│  │ url=${CATALOG_   │                                             
│  │  ENDPOINT}       │                                             
│  └──────────────────┘                                             
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Model Layer                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────┐ │
│  │ShoppingCart  │  │ShoppingCart  │  │   Product    │  │Promo │ │
│  │  (entity)    │  │Item (entity) │  │   (entity)   │  │(ent) │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**God nodes and architectural risk**: The dependency analysis identifies ShoppingCart (fan-in: 5), Product (fan-in: 4), and ShoppingCartItem (fan-in: 3) as god nodes with highest coupling (/projects/modernized/migration/dependency-order.md:10). These classes form the core data model and are referenced across all service operations, making them critical to stabilize with characterization tests before conversion (/projects/modernized/migration/dependency-order.md:16).

**Conversion dependency order**: The legacy dependency graph dictates compilation order (/projects/modernized/migration/dependency-order.md:18): Product → Promotion → CartServiceApplication → ShoppingCartItem → CatalogService → ShoppingCart → ShoppingCartService → PromoService → ShippingService → CartEndpoint → ShoppingCartServiceImpl → JerseyConfig. This order ensures the tree compiles at every commit during migration.

## 3. Integration surfaces

**Exposed REST API** (`/api/cart/*`):
- `GET /cart/{cartId}` — retrieve cart with pricing calculation (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:32)
- `POST /cart/{cartId}/{itemId}/{quantity}` — add items to cart (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:38)
- `POST /cart/{cartId}/{tmpId}` — replace cart contents from temporary cart (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:47)
- `DELETE /cart/{cartId}/{itemId}/{quantity}` — remove items from cart (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:55)
- `POST /cart/checkout/{cartId}` — checkout cart and clear items (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:64)

**Consumed external services**:
- **Catalog Service** (env-driven): Feign client at `${CATALOG_ENDPOINT}` retrieving product list via `/api/products` (src/main/java/com/redhat/coolstore/service/CatalogService.java:10, application.properties:6)

**Persistence surface**:
- **In-memory HashMap**: `Map<String, ShoppingCart> carts` storing cart state (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42)

**Preserve contract**: The migration.yaml `preserve: [CATALOG_ENDPOINT]` mandates maintaining the environment-driven catalog service URL configuration as `${CATALOG_ENDPOINT}` substitution.

## 4. Behavioral contract sources

**Primary behavioral contract**: `ShoppingCartServiceTest` (src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java) defines the expected behavior:

- **Cart initialization contract** (line 28): New carts return zero totals for all monetary fields (cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0)
- **Pricing calculation contract** (line 38): Cart with 2 items at $1000 each returns cartItemTotal: 2000.0, cartTotal: 2000.0, shippingPromoSavings: -10.99 (free shipping promotion applied)
- **Product retrieval contract** (line 57): Catalog service mock returns specific Product with itemId "2222", name "Bike", desc "Super bike", price 200

**Contract gaps**: No tests exist for:
- Error handling when catalog service is unavailable
- Input validation for negative quantities or invalid item IDs
- Concurrent access patterns for multi-user cart scenarios
- Session lifecycle management and cart expiration

## 5. Modernization surface

**Component-by-component modernization requirements**:

**CartEndpoint (REDESIGN)**:
- **MUST**: `jakarta-jaxrs-to-quarkus-00010` → convert JAX-RS to quarkus-rest dependency (findings-inventory.md:47)
- **MUST**: `springboot-di-to-quarkus-00003` → replace @Autowired with CDI constructor injection (findings-inventory.md:16)
- **SHOULD**: `springboot-web-to-quarkus-00000` → native JAX-RS instead of spring-web extension (findings-inventory.md:149)

**ShoppingCartServiceImpl (REDESIGN)**:
- **MUST**: `springboot-di-to-quarkus-00003` → constructor injection for dependencies (findings-inventory.md:20)
- **MUST**: `javax-to-jakarta-import-00001` → jakarta.* imports (findings-inventory.md:5)
- **EXAMINE**: In-memory HashMap state requires thread-safe implementation with targetContract `threadSafeState: true`

**PromoService (REDESIGN)**:
- **MUST**: `springboot-di-to-quarkus-00003` → CDI constructor injection (findings-inventory.md:18)
- **EXAMINE**: Static promotion data requires thread-safe access pattern

**ShippingService (REDESIGN)**:
- **MUST**: `springboot-di-to-quarkus-00003` → CDI constructor injection (findings-inventory.md:19)

**JerseyConfig (REDESIGN)**:
- **MUST**: `springboot-di-to-quarkus-00003` → CDI conversion (findings-inventory.md:17)
- **REMOVED**: Quarkus auto-discovers JAX-RS resources, eliminating Jersey configuration need

**CartServiceApplication (REDESIGN)**:
- **REMOVED**: `springboot-annotations-to-quarkus-00000` → delete @SpringBootApplication and main class (findings-inventory.md:107)
- **TARGET**: Quarkus bootstrap subsumes Spring Boot application startup

## 6. Domain boundaries

**Single bounded context**: The application represents a unified shopping cart domain with tightly coupled components that operate as one functional unit. While technically separable into cart operations, pricing services, and shipping calculation, the legacy design intentionally couples these concerns through shared data structures (ShoppingCart entity with pricing fields) and synchronized pricing operations (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66: `priceShoppingCart()` applies promotions then shipping then totals).

**Coupling justification**: The cart, promotion, and shipping services share the ShoppingCart data model and coordinate through the pricing workflow. PromoService.applyCartItemPromotions() precedes ShippingService.calculateShipping() within priceShoppingCart(), and both services modify the same ShoppingCart object state (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:70-84), necessitating their current integration level.

**Migration strategy**: Rather than attempting domain decomposition, the modernization maintains this bounded context integrity, focusing on platform migration (Spring→Quarkus) rather than architectural refactoring.

## 7. Class roles & target contract

### HARVEST classes (data/DTO/value-object/pure-utility)

**Product** (src/main/java/com/redhat/coolstore/model/Product.java) — pure data transfer object representing catalog product information with itemId, name, description, and price fields. Preserved as-is with potential Jakarta serialization annotations.

**ShoppingCart** (src/main/java/com/redhat/coolstore/model/ShoppingCart.java) — entity capturing cart state including item list, pricing fields, and cart identifier. Preserved behavior: maintains cart totals, applies promotional calculations, manages item lifecycle operations (add/remove/reset).

**ShoppingCartItem** (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java) — value object representing individual cart line items with quantity, price, promotional savings, and embedded Product reference. Preserved behavior: maintains line item pricing state and quantity management.

**Promotion** (src/main/java/com/redhat/coolstore/model/Promotion.java) — value object capturing promotional discount rules with itemId and percentage off fields. Preserved behavior: stores promotion metadata for cart item discount application.

**ShoppingCartService** (src/main/java/com/redhat/coolstore/service/ShoppingCartService.java) — service interface defining cart operations contract. Preserved interface contracts for all business operations.

### REDESIGN classes (service/endpoint/REST client/config)

**CartEndpoint** (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21) — JAX-RS REST controller converted to Quarkus JAX-RS with session-scoped cart access. **Target runtime contract**:
- **Concurrency**: stateless endpoint with thread-safe session-scoped service injection
- **Resource policy**: GET returns **404** on missing cartId (never creates cart for read operations)
- **Input validation**: reject with **400** (problem-detail) for negative quantities, null/empty itemId
- **Error mapping**: **503** via JAX-RS **ExceptionMapper** for catalog service failures (never raw 500)
- **API contract**: POST operations are idempotent for same cartId/itemId/quantity combinations

**JerseyConfig** (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6) — removed — Quarkus auto-discovers JAX-RS resources via @Path and @ApplicationScoped annotations.

**CartServiceApplication** (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7) — removed — Quarkus bootstrap and CDI container replaces Spring Boot application startup.

**ShoppingCartServiceImpl** (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28) — service implementation converted to CDI @ApplicationScoped with constructor injection. **Target runtime contract**:
- **Concurrency**: `ConcurrentHashMap<String, ShoppingCart>` with `compute()` operations for thread-safe cart access
- **Resource policy**: no cache clear-on-miss; bounded refresh when catalog data updates
- **Aggregate math**: `normalizeBeforeDerive` — dedupe cart items before pricing calculations (line 168: `cart.setShoppingCartItemList(dedupeCartItems(cart))`)
- **API contract**: all mutations validated; catalog failures surface as service-level exceptions for ExceptionMapper handling

**PromoService** (src/main/java/com/redhat/coolstore/service/PromoService.java:15) — component converted to CDI with thread-safe promotion data access. **Target runtime contract**:
- **Concurrency**: static promotion data accessed via thread-safe collections (ConcurrentHashMap for lookup)
- **Cache policy**: promotion set loaded once at construction, immutable thereafter

**ShippingService** (src/main/java/com/redhat/coolstore/service/ShippingService.java:7) — component converted to CDI with deterministic shipping calculation logic. **Target runtime contract**:
- **Concurrency**: stateless service with no mutable state
- **Business logic**: tiered shipping calculation preserved exactly as legacy (2.99/4.99/6.99/8.99/10.99 thresholds with free shipping ≥75)