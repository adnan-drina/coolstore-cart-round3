# Architecture profile — Coolstore Cart Service (M1)

## 1. Purpose & domain

The Coolstore cart service provides shopping cart management capabilities for an e-commerce application. It serves customers and web/mobile clients who need to add, remove, and checkout items from shopping carts. The core domain centers around **shopping cart lifecycle management** with integrated **pricing, promotion, and shipping calculation**.

The application manages cart state in-memory (`HashMap<String, ShoppingCart>` per ShoppingCartServiceImpl.java:42), with **catalog service integration** via Feign client to fetch product information (src/main/java/com/redhat/coolstore/service/CatalogService.java:10). Pricing logic applies **cart-level and item-level promotions** with **tiered shipping costs** based on cart total thresholds (ShoppingCartServiceTest.java:51-53).

**Key domain concepts:**
- **ShoppingCart** - cart entity holding items, pricing totals, and promotion savings
- **Product** - catalog product with id, name, description, price  
- **ShoppingCartItem** - line item linking product, quantity, and pricing
- **Promotion** - discount rules applied at item-level or cart-level (e.g., free shipping >$75)

## 2. Components & relationships

The application follows a **layered architecture** with REST → Service → Integration boundaries:

```
┌─────────────────────────────────────────────────┐
│ CartEndpoint (REST layer)                       │
│ @RestController @Path("/cart")                  │
│ - HTTP endpoints for cart operations            │
└─────────────────┬───────────────────────────────┘
                  │ injects
                  ▼
┌─────────────────────────────────────────────────┐
│ ShoppingCartService (business logic interface) │
│ - Cart lifecycle + pricing orchestration       │
└─────────────────┬───────────────────────────────┘
                  │ implements
                  ▼
┌─────────────────────────────────────────────────┐
│ ShoppingCartServiceImpl                         │
│ @Service - core cart state management          │
│ - In-memory cart storage                       │
│ - Coordinates PromoService/ShippingService     │
│ - Calls CatalogService for product data        │
└────────┬─────────────────────────────┬──────────┘
         │ uses                         │ uses
         ▼                               ▼
┌──────────────┐                  ┌──────────────┐
│ PromoService │                  │ ShippingService │
│ @Component   │                  │ @Component    │
│ - Item promo │                  │ - Tiered      │
│ - Shipping   │                  │   shipping    │
│   promos     │                  │   calculation │
└──────────────┘                  └──────────────┘
         │                               │
         ▼                               ▼
┌──────────────┐                  ┌──────────────┐
│ Promotion    │                  │ ShoppingCart │
│ model        │                  │ model        │
└──────────────┘                  └──────────────┘
```

**God nodes** (dependency-order.md:8-14):
- **ShoppingCart** (fan-in: 5) - central domain entity, referenced by all services (src/main/java/com/redhat/coolstore/model/ShoppingCart.java)
- **Product** (fan-in: 4) - shared DTO from catalog integration (src/main/java/com/redhat/coolstore/model/Product.java)
- **ShoppingCartItem** (fan-in: 3) - cart line item domain object (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java)

**Conversion risk**: ShoppingCartServiceImpl holds **mutable shared state** (carts HashMap, productMap) requiring thread-safe target implementation.

## 3. Integration surfaces

**External service consumption:**
- **Catalog Service** - Feign client (`@FeignClient(name="catalogService", url="${CATALOG_ENDPOINT}")`) calling `/api/products` endpoint (CatalogService.java:10-13, application.properties:6)
  - Configuration: `CATALOG_ENDPOINT=http://localhost:8081` (application.properties:6) 
  - Preserve: `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT:http://localhost:8081}` target config

**External service exposure:**
- **REST API** - JAX-RS endpoints for cart operations (CartEndpoint.java:24):
  - `GET /cart/{cartId}` - retrieve cart state
  - `POST /cart/{cartId}/{itemId}/{quantity}` - add items  
  - `POST /cart/{cartId}/{tmpId}` - set cart from temp cart
  - `DELETE /cart/{cartId}/{itemId}/{quantity}` - remove items
  - `POST /cart/checkout/{cartId}` - checkout/clear cart
  - Media type: `application/json` (CartEndpoint.java:33,40,49,57,66)
- **Jersey configuration** - JAX-RS application setup (JerseyConfig.java:7-9)

**Infrastructure integration:**
- **Spring Boot Actuator** - health/metrics endpoints (pom.xml:65-66) → migrate to `quarkus-smallrye-health`
- **In-memory storage** - no external persistence, cart state lost on restart

**Environment-driven configuration** (demo-env-integration-00001, localhost-http-00001): `CATALOG_ENDPOINT` property must be preserved through migration.

## 4. Behavioral contract sources

**Core cart operations** (ShoppingCartServiceTest.java):
- **Cart initialization** (should_get_initialized_shopping_cart_in_case_of_not_exists:29-36) - new carts have zero totals: cartItemPromoSavings=0.0, cartItemTotal=0.0, shippingPromoSavings=0.0, cartTotal=0.0
- **Cart pricing** (should_calculate_price_of_cart:39-54) - cart with 2x $1000 items yields: cartItemTotal=2000.0, shippingPromoSavings=-10.99, cartTotal=2000.0 (free shipping applied at >$75 threshold)

**REST boundary contract** (CartServiceBoundaryTest.java:35-46):
- **Add item endpoint** - POST `/api/cart/1/1111/2` produces ShoppingCart with cartItemTotal=2000.0, cartItemPromoSavings=0.0, shippingPromoSavings=-10.99, cartTotal=2000.0, single shoppingCartItemList entry

**Promotion behavior** (PromoService.java:48-55): **Free shipping promotion** applies when `cartItemTotal >= 75.0`, setting `shippingTotal = 0` and `shippingPromoSavings = -shippingTotal`

**Test data contracts** (ProductsObjectMother.java:10-12):
- Product "1111": Car, Super car, $1000  
- Product "2222": Bike, Super bike, $200

**Contract gaps**: No negative test cases (invalid products, quantity validation), no concurrent access behavior specified, no error handling for catalog service failures.

## 5. Modernization surface

**Mandatory changes** (findings-inventory.md):
- **POM conversion** (javaee-pom-to-quarkus-00010/20/30/40/50/60/80): Spring Boot parent → Quarkus BOM, add Quarkus plugins, Spring Boot Actuator → quarkus-smallrye-health
- **JAX-RS migration** (jakarta-jaxrs-to-quarkus-00010): javax.* → jakarta.* imports (CartEndpoint.java:5-11, ShoppingCartServiceImpl.java:11)
- **Spring removal** (springboot-annotations-to-quarkus-00000): Remove `@SpringBootApplication` + main class bootstrap (CartServiceApplication.java:7)
- **CDI conversion** (springboot-di-to-quarkus-00003): `@Service`/@Component` → constructor injection CDI (CartEndpoint.java:28, ShoppingCartServiceImpl.java:28,33,36,39, PromoService.java:15, ShippingService.java:7)
- **JDK module cleanup** (removed-javaee-modules-00020): OpenJDK 11+ removed JavaEE modules → provided by Quarkus platform

**Optional conversions** (should):
- **Metrics replacement** (springboot-metrics-to-quarkus-0100/0200): Micrometer → quarkus-smallrye-metrics
- **REST client conversion** (springboot-web-to-quarkus-00000): Feign client → quarkus-rest-client

**Potential examination** (platform contract):
- **Thread safety** - concurrent HashMap access requires `@ApplicationScoped` + thread-safe operations
- **Memory management** - unbounded cart cache needs eviction policy 
- **Configuration surface** - CATALOG_ENDPOINT env var preserved in target contract

## 6. Domain boundaries

The application forms **a single bounded context** for cart management. All classes operate within the cart domain namespace (`com.redhat.coolstore.*`), with clear **transactional boundaries** at cart operations defined by cart lifecycle methods in ShoppingCartServiceImpl.java:53-64.

**Coupling analysis** (dependency-order.md:16-29):
- **Low coupling** - service layer has single responsibility (PromoService handles promotions, ShippingService handles shipping, CatalogService handles product retrieval) with minimal interdependencies  
- **God node coupling** - ShoppingCart domain entity centralizes pricing state in src/main/java/com/redhat/coolstore/model/ShoppingCart.java, requiring **cohesive modernization** of pricing logic in ShoppingCartServiceImpl

**Incremental modernization strategy**: All classes belong to the **cart domain story** - no natural seams for parallel modernization. Sequential conversion following dependency order (dependency-order.md:16-29) ensures compilation at each step.

## 7. Class roles

### HARVEST Classes

**Product** - HARVEST (src/main/java/com/redhat/coolstore/model/Product.java) - pure data transfer object containing product information (itemId, name, desc, price) with no business logic, constructors, and standard bean methods. This POJO serves as a DTO for catalog data transfer and will be carried over faithfully to the target without modification.

**ShoppingCart** - HARVEST (src/main/java/com/redhat/coolstore/model/ShoppingCart.java) - domain entity representing cart state with items list, pricing totals, and state management methods (addShoppingCartItem, removeShoppingCartItem, resetShoppingCartItemList). This entity captures the cart domain model and pricing state, preserved with its current structure and behavior patterns.

**ShoppingCartItem** - HARVEST (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java) - value object linking Product reference with quantity, price, and promo savings. This line item entity maintains product-to-cart relationship and per-item pricing state, harvested without functional changes.

**Promotion** - HARVEST (src/main/java/com/redhat/coolstore/model/Promotion.java) - immutable configuration object holding discount rules (itemId, percentOff). This promotion rule entity represents pricing configuration that applies to specific products and cart-level thresholds.

**ProductsObjectMother** - HARVEST (src/test/java/com/redhat/coolstore/ProductsObjectMother.java) - test utility class providing product test data via createVehicleProducts() method returning predetermined Product instances for testing scenarios.

### REDESIGN Classes

**CartEndpoint** - REDESIGN (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-24) - JAX-RS REST controller with @RestController and @Path annotations providing HTTP endpoints for cart operations. Modernized to Quarkus JAX-RS resource with @ApplicationScoped CDI scope and constructor injection pattern, maintaining session-scoped cart operations through cartId parameters, ensuring GET endpoint idempotency, input validation on quantity/itemId parameters, and comprehensive error mapping for catalog service failures.

**ShoppingCartServiceImpl** - REDESIGN (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-29) - Spring @Service implementation managing core cart business logic with @Autowired field injection of PromoService, ShippingService, CatalogService. Modernized to @ApplicationScoped CDI bean with constructor injection, implementing thread-safe cart state management using ConcurrentHashMap for both carts and productMap collections, implementing bounded cache with LRU eviction for productMap to prevent memory leaks, ensuring GET-idempotent cart operations, and providing graceful catalog service failure handling with retry/fallback mechanisms.

**PromoService** - REDESIGN (src/main/java/com/redhat/coolstore/service/PromoService.java:15-16) - Spring @Component managing promotion calculations with @Autowired field injection pattern. Modernized to @ApplicationScoped CDI bean with constructor injection, implementing immutable promotion rules loaded at initialization, thread-safe promotion application methods, cart-level promotions based on cartItemTotal thresholds (free shipping >$75 rule preserved), and item-level promotions applied through product promotion mapping.

**ShippingService** - REDESIGN (src/main/java/com/redhat/coolstore/service/ShippingService.java:7-8) - Spring @Component providing shipping calculation functionality with no injection dependencies. Modernized to @ApplicationScoped CDI bean implementing tiered shipping calculation logic preserving exact thresholds (2.99 for <$25, 4.99 for <$50, 6.99 for <$75, 8.99 for <$100, 10.99 for >=$100), ensuring thread-safe calculation operations with no state accumulation between requests.

**CatalogService** - REDESIGN (src/main/java/com/redhat/coolstore/service/CatalogService.java:5-10) - Spring Cloud @FeignClient interface defining REST client contract to catalog endpoint with @FeignClient annotation specifying service name and URL configuration from CATALOG_ENDPOINT property. Modernized to Quarkus REST client with @RegisterRestClient annotation, constructor injection pattern, product list retrieval functionality from CATALOG_ENDPOINT with environment variable configuration support, circuit breaker pattern for resilience, timeout handling for catalog service failures.

**JerseyConfig** - REDESIGN (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6-7) - Spring @Component extending Jersey ResourceConfig for JAX-RS application configuration, registering CartEndpoint class manually. Modernized by complete removal - Quarkus auto-discovers JAX-RS resources without manual ResourceConfig registration, eliminating the need for this configuration class.

**CartServiceApplication** - REDESIGN (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7) - Spring Boot application bootstrap class with @SpringBootApplication annotation and @EnableFeignClients for Feign client activation. Modernized by complete removal - Quarkus handles application bootstrap differently, @SpringBootApplication annotation deleted per springboot-annotations-to-quarkus-00000 rule, Feign clients converted to REST clients, main method removed as unnecessary.