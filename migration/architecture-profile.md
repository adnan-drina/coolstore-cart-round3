# Architecture Profile (M1 Analysis)

## Purpose & domain

The legacy application is an e-commerce shopping cart service that manages customer shopping carts, applies pricing rules, calculates shipping costs, and processes promotions. It provides cart management functionality for the Coolstore retail system, serving as the backend service for cart operations including item addition/removal, cart pricing, and checkout processing (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:7-8`).

The core domain centers around **ShoppingCart** management with four key business concepts: 
- **Cart Items**: Product entries with quantity and pricing that form the shopping experience
- **Pricing Engine**: Automated calculation of cart totals using product prices, promotional discounts, and shipping costs
- **Promotion Rules**: Business logic that applies percentage discounts to specific products and free shipping thresholds
- **Shipping Calculation**: Tiered shipping cost structure based on cart total thresholds

The application serves frontend e-commerce interfaces that need to display cart totals, apply promotional savings, and process customer checkout flows. It integrates with an external product catalog service to fetch current product information and pricing.

## Components & relationships

The application follows a layered architecture with clear separation between REST endpoints, business services, and domain models:

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   REST Endpoint     │    │   Business Logic    │    │   Domain Models     │
│                     │    │                     │    │                     │
│ CartEndpoint        │───▶│ ShoppingCartService │───▶│ ShoppingCart        │
│ (@RestController)   │    │ (ShoppingCartService│    │ ShoppingCartItem    │
│                     │    │  Impl + Interfaces) │    │ Product             │
└─────────────────────┘    │                     │    │ Promotion           │
                           │ PromoService         │    │                     │
                           │ ShippingService      │    └─────────────────────┘
                           │ CatalogService       │
                           │ (Feign Client)       │
                           └─────────────────────┘
```

**God Nodes** (highest fan-in from `dependency-order.md:9-15`):
- `ShoppingCart` (fan-in: 5) — Central domain entity referenced by all pricing and cart operations
- `Product` (fan-in: 3) — Shared product data model consumed by cart items and catalog service
- `ShoppingCartService` (fan-in: 1, fan-out: 2) — Primary business interface (`dependency-order.md:9-15`)

**Conversion Order Risk** (dependency-order.md:16-29): The model classes (ShoppingCart, Product) must be converted first as they are god nodes with highest fan-in, followed by the service layer, then endpoints (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1-127`).

## Integration surfaces

**External Service Consumption**:
- **Catalog Service**: REST API consumed via Feign client (`CatalogService:10`) 
  - Endpoint: `${CATALOG_ENDPOINT}/api/products` (application.properties:6)
  - Configuration: Environment-driven via `CATALOG_ENDPOINT` variable 
  - **Preserve candidate**: `demo-env-integration-00001` (application.properties:6)

**Exposed API** (CartEndpoint:21-69):
- **GET** `/cart/{cartId}` — Retrieve shopping cart with current pricing
- **POST** `/cart/{cartId}/{itemId}/{quantity}` — Add items to cart
- **POST** `/cart/{cartId}/{tmpId}` — Set cart contents from temporary cart
- **DELETE** `/cart/{cartId}/{itemId}/{quantity}` — Remove items from cart
- **POST** `/checkout/{cartId}` — Process checkout and clear cart
- **Content-Type**: `application/json`
- **JAX-RS Path**: `/cart` (CartEndpoint:23)

**Configuration**:
- **Catalog Endpoint**: `CATALOG_ENDPOINT=http://localhost:8081` (application.properties:6)
- **Jersey Path**: `spring.jersey.application-path=/api` (application.properties:2)
- **Application Name**: `spring.application.name=coolstore-cart-legacy` (application.properties:1)

**Persistence**: **IN-MEMORY ONLY** — Cart state stored in `HashMap<String, ShoppingCart>` (ShoppingCartServiceImpl:42) with no database persistence layer.

## Behavioral contract sources

**Primary Test Contract** (ShoppingCartServiceTest:27-64):
- `should_get_initialized_shopping_cart_in_case_of_not_exists()`: Validates cart initialization with zero totals
- `should_calculate_price_of_cart()`: **KEY ASSERTION** (ShoppingCartServiceTest:49-53)
  - Cart with 2x $1000 items = $2000 cart item total
  - Shipping promotion applies -$10.99 discount (ShoppingCartServiceTest:52)
  - Final cart total = $2000 (item total + shipping total $0 after promotion)
- `should_get_product_id()`: Validates product catalog integration

**Boundary Test Contract** (CartServiceBoundaryTest:34-46):
- `should_add_item_to_shopping_cart()`: **INTEGRATION ASSERTION** (CartServiceBoundaryTest:38-45)
  - Same pricing logic: 2x $1000 items = $2000 cart total
  - Shipping promotion -$10.99 applies above $75 cart threshold
  - **Contract Gap**: No explicit test for shipping tier calculations (ShippingService:10-23)

**Critical Behavior Not Tested**:
- Shipping tier calculations: $0-25=$2.99, $25-50=$4.99, $50-75=$6.99, $75-100=$8.99, $100+=$10.99
- Promotion application edge cases: empty carts, threshold boundaries
- Cart deduplication logic: multiple items of same product (ShoppingCartServiceImpl:200-221)

## Modernization surface

**Components and Required Changes**:

**REST Endpoint** (CartEndpoint:1-70):
- **MUST CHANGE**: `javax.ws.rs` → `jakarta.ws.rs` imports (CartEndpoint:5-11) — `javax-to-jakarta-import-00001`
- **MUST CHANGE**: `@RestController` → JAX-RS annotations (`@Path`, `@GET`, `@POST`, `@DELETE`) — `jakarta-jaxrs-to-quarkus-00010`
- **MUST CHANGE**: `@Autowired` → Constructor injection — `springboot-di-to-quarkus-00003`

**Business Services** (ShoppingCartServiceImpl:1-222):
- **MUST CHANGE**: `@Service` → CDI `@ApplicationScoped` or constructor injection — `springboot-di-to-quarkus-00003`
- **MUST CHANGE**: `javax.annotation.PostConstruct` → `@PostConstruct` equivalent — `javax-to-jakarta-import-00001`
- **SHOULD CHANGE**: In-memory `HashMap` → External state management for cloud readiness

**Pricing Services** (PromoService:1-77, ShippingService:1-25):
- **MUST CHANGE**: `@Component` → CDI managed beans — `springboot-di-to-quarkus-00003`
- **PRESERVE**: Promotion business logic (`applyCartItemPromotions`, `applyShippingPromotions`) — **CORE DOMAIN BEHAVIOR**

**Configuration** (application.properties:1-6):
- **MUST CHANGE**: Spring properties → Quarkus `application.properties` keys
- **MUST CHANGE**: `CATALOG_ENDPOINT=http://localhost:8081` → `${CATALOG_ENDPOINT:http://localhost:8081}` — `demo-env-integration-00001`
- **PRESERVE**: Jersey path mapping (`/api`) for backwards compatibility

**Domain Models** (ShoppingCart:1-127, Product:1-54, ShoppingCartItem:1-58, Promotion:1-41):
- **NO MANDATORY CHANGES**: POJOs remain compatible, serialization behavior preserved

## Domain boundaries

**Single Bounded Context** (effectively monolithic): All components operate within the e-commerce cart management domain with tight coupling around the `ShoppingCart` god entity.

**Coupling Analysis** (dependency relationships):
- **Tight Coupling**: `ShoppingCartServiceImpl` depends on all pricing services (`PromoService`, `ShippingService`, `CatalogService`) creating a single service responsibility (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:33-40`)
- **Shared State**: In-memory `HashMap<String, ShoppingCart>` (ShoppingCartServiceImpl:42) couples all cart operations to a single implementation
- **God Node Risk**: `ShoppingCart` entity contains all business state, preventing clear separation between pricing, inventory, and cart management (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1-127`)

**Candidate Seams for Incremental Modernization** (`dependency-order.md:16-29`):
1. **Cart Operations**: `getShoppingCart`, `addItem`, `deleteItem`, `set`, `checkout` (ShoppingCartService:7-19)
2. **Pricing Engine**: `priceShoppingCart`, `applyCartItemPromotions`, `applyShippingPromotions`, `calculateShipping` (ShoppingCartServiceImpl:66-85)
3. **External Integration**: `CatalogService` Feign client abstraction (CatalogService:10)

**Risk Assessment**: High coupling and single-service implementation make incremental migration challenging. The in-memory state and tightly-integrated pricing logic require careful preservation of business behavior during migration.

**Platform Contract Rules Impact**:
- **Environment-driven config**: Must preserve `${CATALOG_ENDPOINT}` configuration mechanism
- **In-memory state**: Migration requires decision on state management strategy (external cache, database, or session management)
- **UI surface**: JAX-RS endpoints must maintain `/cart` path structure for frontend compatibility