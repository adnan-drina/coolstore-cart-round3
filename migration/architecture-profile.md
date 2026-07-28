# Architecture Profile — Legacy Cart Service

## Purpose & domain

The application is a shopping cart management service from the Coolstore microservices reference implementation (src/main/java/com/redhat/coolstore/CartServiceApplication.java:1). It provides cart lifecycle operations (create, add items, remove items, checkout) with integrated pricing calculations that combine product prices, promotional discounts, and shipping costs based on cart total thresholds.

For end users, the service maintains persistent shopping cart state identified by cart ID across HTTP sessions (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42). Each cart contains line items referencing products from an external catalog service, with calculated totals including item subtotals, promotional savings (both item-level and shipping-level), and shipping costs determined by tiered thresholds ($0-25: $2.99, $25-50: $4.99, $50-75: $6.99, $75-100: $8.99, $100+: $10.99) (src/main/java/com/redhat/coolstore/service/ShippingService.java:12-22).

The core domain concepts center on **ShoppingCart** as the aggregate root containing **ShoppingCartItem** entities that reference **Product** entities (src/main/java/com/redhat/coolstore/model/ShoppingCart.java:7, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:1, src/main/java/com/redhat/coolstore/model/Product.java:5). **Promotion** objects define discount rules applied at both item level (product-specific percentage discounts) and cart level (free shipping for carts over $75) (src/main/java/com/redhat/coolstore/service/PromoService.java:30-56).

This represents a trimmed version of the original Coolstore cart-service with distributed cache (Infinispan Hot Rod) and Drools-based pricing rules removed (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:19-27), leaving an in-memory implementation suitable for migration demonstration.

## Components & relationships

The application follows a layered architecture with REST endpoint → service layer → model hierarchy:

```
┌─────────────────┐
│ CartEndpoint    │◄── JAX-RS + Spring MVC hybrid
│ (@RestController│    (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-23)
│  + @Path)       │
└────────┬────────┘
         │ field injection
         ▼
┌─────────────────┐
│ ShoppingCart-   │◄── Service interface + implementation
│ ServiceImpl     │    (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:29)
│ (@Service)      │
└────────┬────────┘
         │ dependency
         ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ PromoService    │    │ ShippingService │    │ CatalogService  │
│ (@Component)    │    │ (@Component)    │    │ (@FeignClient)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────┬───────────┴───────────┬───────────┘
                     ▼                       ▼
             ┌─────────────────┐    ┌─────────────────┐
             │ ShoppingCart    │    │ Product         │
             │ (aggregate)     │    │ (value object)  │
             └────────┬────────┘    └─────────────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ ShoppingCartItem│
             │ (entity)        │
             └─────────────────┘
```

The **god nodes** with highest fan-in are ShoppingCart (5 incoming dependencies from CartEndpoint, ShoppingCartServiceImpl, and the pricing services) and Product (4 dependencies across the service layer), indicating they are central to the domain logic. ShoppingCartService acts as the primary application service coordinating between the endpoint and the domain models.

God nodes require careful handling during migration: ShoppingCartServiceImpl:66-85 implements the pricing orchestration that must remain functionally equivalent, and the Product model's serializable contract affects serialization across the REST boundary.

## Integration surfaces

**External service consumption:**
- CatalogService Feign client retrieves product catalog from `${CATALOG_ENDPOINT}` environment variable (src/main/java/com/redhat/coolstore/service/CatalogService.java:10, application.properties:6). This is an **UNCOVERED preserve candidate** — the migration.yaml preserve list includes CATALOG_ENDPOINT but doesn't specify the Feign→REST client migration strategy.

**Exposed APIs:**
- REST endpoint at `/api/cart/*` (application.properties:3, CartEndpoint.java:23)
  - `GET /cart/{cartId}` — retrieve cart with calculated totals
  - `POST /cart/{cartId}/{itemId}/{quantity}` — add items to cart  
  - `POST /cart/{cartId}/{tmpId}` — transfer items between temp cart and persistent cart
  - `DELETE /cart/{cartId}/{itemId}/{quantity}` — remove items from cart
  - `POST /checkout/{cartId}` — clear cart after purchase
- All endpoints produce `application/json` MediaType (CartEndpoint.java:33, 40, 49, 57, 66)

**Persistence:**
- In-memory HashMap<String, ShoppingCart> stores cart state (ShoppingCartServiceImpl.java:42) with no database integration. This is the primary modernization risk — production deployments would require external persistence.

**Configuration:**
- `CATALOG_ENDPOINT` environment variable defaults to `http://localhost:8081` (application.properties:6). The migration must preserve environment-driven configuration per demo-env-integration-00001 finding.

**Preserve contract coverage:**
- CATALOG_ENDPOINT: covered by migration.yaml:23 preserve list
- In-memory cart state: **UNCOVERED** — no persistence strategy defined for migration

## Behavioral contract sources

The application behavior is pinned by the legacy test suite, particularly ShoppingCartServiceTest (src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java):

**Cart initialization contract (ShoppingCartServiceTest:28-36):**
- New carts initialize with zero totals: cartItemPromoSavings=0.0, cartItemTotal=0.0, shippingPromoSavings=0.0, cartTotal=0.0
- Each cart gets unique ID and persists across service calls via HashMap

**Pricing calculation contract (ShoppingCartServiceTest:38-54):**
- Cart with 2 items × $1000 = $2000 cartItemTotal
- Shipping calculation triggers for carts ≥$100: shippingTotal=10.99 (ShoppingCartServiceTest:52)
- Free shipping promotion applies for carts ≥$75: shippingPromoSavings=-10.99, shippingTotal=0.0 (ShoppingCartServiceTest:51-52)
- Final cartTotal = cartItemTotal + shippingTotal = $2000.00

**Product lookup contract (ShoppingCartServiceTest:56-63):**
- CatalogService mocked to return ProductsObjectMother.createVehicleProducts()
- getProduct("2222") returns Bike with price $200.00 (ShoppingCartServiceTest:60-62)

**Contract gaps:**
- No tests for promotional discounts at item level (PromoService applies percentage discounts to specific product IDs)
- No tests for multi-quantity line items or deduplication logic (ShoppingCartServiceImpl:200-221)
- No tests for temp cart → persistent cart transfer (set operation)

The pricing thresholds and promotional rules are business-critical: shipping tiers ($2.99, $4.99, $6.99, $8.99, $10.99) and free shipping above $75 must remain functionally equivalent post-migration.

## Modernization surface

**MUST change (mandatory findings by rule id):**

- **javax→jakarta import migration** (javax-to-jakarta-import-00001): CartEndpoint.java:5-11 imports javax.ws.rs.* must become jakarta.* per recipe execution log:9
- **Spring Boot → Quarkus platform** (javaee-pom-to-quarkus-00010/00020/00030/00040/00050/00060/00080): pom.xml:4,17,55,60,65,70,76,82 require platform BOM, quarkus-maven-plugin, and Quarkus test dependencies
- **Spring DI → native CDI** (springboot-di-to-quarkus-00003): CartEndpoint.java:28, JerseyConfig.java:6, ShoppingCartServiceImpl.java:28,33,36,39 field injection must convert to constructor injection
- **JAX-RS → quarkus-rest** (jakarta-jaxrs-to-quarkus-00010): pom.xml:60 jakarta.jakartaee-web-api requires replacement with quarkus-rest
- **Actuator → SmallRye Health** (springboot-actuator-to-quarkus-0100): pom.xml:65 spring-boot-starter-actuator → quarkus-smallrye-health
- **Remove @SpringBootApplication** (springboot-annotations-to-quarkus-00000): CartServiceApplication.java:7 main class and bootstrap model deleted
- **Java EE modules** (removed-javaee-modules-00020): ShoppingCartServiceImpl.java:11 javax.annotation.PostConstruct provided by Quarkus BOM

**SHOULD change (optional improvements):**

- **Micrometer → SmallRye Metrics** (springboot-metrics-to-quarkus-0100/0200): pom.xml:65 and metrics call sites → MP Metrics annotations
- **Spring Web → native JAX-RS** (springboot-web-to-quarkus-00000): pom.xml:55 spring-boot-starter-web → native JAX-RS resources
- **Properties handling** (springboot-properties-to-quarkus-00000): pom.xml:55 spring-boot-properties → Quarkus application.properties keys

**Needs examination (potential integration points):**

- **Environment-driven config** (demo-env-integration-00001): CATALOG_ENDPOINT=${CATALOG_ENDPOINT:default} must preserve environment variable resolution
- **Localhost HTTP calls** (localhost-http-00001): application.properties:6 hardcoded localhost:8081 requires cloud-readiness assessment
- **Feign → REST client** (INFERRED from catalogServie field): CatalogService.java:10 @FeignClient requires Quarkus REST client migration strategy

## Domain boundaries

This application represents a **single bounded context** for shopping cart management. All components are tightly coupled around the ShoppingCart aggregate per dependency-order.md:1-4 (12 classes, 19 intra-project reference edges):

- **Cart management domain**: CartEndpoint, ShoppingCartService interface/impl, ShoppingCart, ShoppingCartItem
- **Pricing domain**: PromoService, ShippingService (coupled to ShoppingCart for calculation) (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66-85)
- **Catalog integration**: CatalogService (Feign client) provides Product lookup (src/main/java/com/redhat/coolstore/service/CatalogService.java:10)

The dependency graph shows tight integration: ShoppingCartServiceImpl depends on all three service components (PromoService, ShippingService, CatalogService) and coordinates the pricing workflow per dependency-order.md:16-29 conversion order. The god nodes (ShoppingCart fan-in=5, Product fan-in=4 per dependency-order.md:8-13) indicate central domain concepts rather than architectural boundaries.

For incremental modernization, **no natural service cuts exist** — all components must migrate together to maintain the pricing contract integrity (dependency-order.md:18-29 conversion order sequence). The cart service cannot function without product lookup, promotional calculations, or shipping tier determinations. A future enhancement could separate product catalog caching into its own component, but the current in-memory implementation makes such separation premature.

The primary modernization risk is the in-memory HashMap persistence (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42) — production deployments would require adding external persistence (database or cache) as a separate bounded context, but this is outside the current migration scope.
