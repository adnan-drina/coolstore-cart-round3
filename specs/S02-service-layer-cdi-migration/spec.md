# S02 Specification: Service Layer CDI Migration and REST Endpoint Conversion

## Story Overview

This specification covers the migration of the shopping cart service layer from Spring DI to Quarkus CDI and conversion of the REST endpoint from Spring Web to native JAX-RS. The story completes the modernization by creating the first deployable milestone where the application serves its complete API surface.

**Legacy Sources:**
- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1-222`
- `src/main/java/com/redhat/coolstore/service/PromoService.java:1-77`
- `src/main/java/com/redhat/coolstore/service/ShippingService.java:1-25`
- `src/main/java/com/redhat/coolstore/service/CatalogService.java:1-92`
- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:1-123`
- `src/main/java/com/redhat/coolstore/CartServiceApplication.java:1-133`
- `src/main/resources/application.properties:1-141`

## Core Business Behavior (Preserved via S01 Test Contracts)

### Shopping Cart Service Behavior

**Primary Service Contract** (`ShoppingCartServiceImpl.java:1-222`):
```java
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    @Autowired
    ShippingService ss;

    @Autowired
    CatalogService catalogServie;

    @Autowired
    PromoService ps;

    Map<String, ShoppingCart> carts;
    Map<String, Product> productMap = new HashMap<>();

    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new HashMap<>();
    }
    
    // Business methods...
}
```

**Behavioral Contract (Pinned by S01 Tests)**:
- **Cart Initialization**: `getShoppingCart(String cartId)` returns new cart with zero totals if not exists
- **Add Items**: `addItem(String cartId, String itemId, int quantity)` integrates with catalog service, applies pricing
- **Pricing Engine**: `priceShoppingCart(ShoppingCart cart)` applies shipping tiers + promotions
- **Checkout**: `checkout(String cartId)` clears cart after processing
- **In-Memory Storage**: `HashMap<String, ShoppingCart>` (cloud readiness follow-up)

### Pricing Engine Behavior

**Shipping Service Contract** (`ShippingService.java:10-23`):
```java
@Component
public class ShippingService {
    public void calculateShipping(ShoppingCart sc) {
        if (sc != null) {
            if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
                sc.setShippingTotal(2.99);
            } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
                sc.setShippingTotal(4.99);
            } else if (sc.getCartItemTotal() >= 50 && sc.getCartItemTotal() < 75) {
                sc.setShippingTotal(6.99);
            } else if (sc.getCartItemTotal() >= 75 && sc.getCartItemTotal() < 100) {
                sc.setShippingTotal(8.99);
            } else if (sc.getCartItemTotal() >= 100 && sc.getCartItemTotal() < 10000) {
                sc.setShippingTotal(10.99);
            }
        }
    }
}
```

**Shipping Tier Arithmetic (Pinned by ShippingTierTest)**:
- $0-25: $2.99
- $25-50: $4.99
- $50-75: $6.99
- $75-100: $8.99
- $100+: $10.99

**Promotion Service Contract** (`PromoService.java:1-77`):
```java
@Component
public class PromoService implements Serializable {
    private static final long serialVersionUID = 2088590587856645568L;
    private String name = null;
    private Set<Promotion> promotionSet = null;

    public PromoService() {
        promotionSet = new HashSet<Promotion>();
        promotionSet.add(new Promotion("329299", .25));
    }
    
    // Promotion business methods...
}
```

**Promotion Composition Semantics (CRITICAL - Carried from S01)**:
- **ZEROES**: `PromoService` ZEROES `shippingTotal` and keeps `shippingPromoSavings` informational
- **Cart Total Formula**: `cartTotal = itemTotal + shippingTotal` (where shippingTotal = 0 after promotion)
- **Test Assertion**: 2x $1000 items = $2000 cart item total, shipping promotion -$10.99, final cart total = $2000
- **DO NOT USE**: Additive composition (charge shipping then refund via savings)

### External Integration Behavior

**Catalog Service Contract** (`CatalogService.java:1-92`):
```java
@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```

**Configuration Contract** (`application.properties:1-141`):
```
spring.application.name=coolstore-cart-legacy
spring.jersey.application-path=/api
CATALOG_ENDPOINT=http://localhost:8081
```

### REST Endpoint Contract

**CartEndpoint API** (`CartEndpoint.java:1-123`):
```java
@RestController
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
@Path("/cart")
public class CartEndpoint implements Serializable {
    private static final long serialVersionUID = -7227732980791688773L;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GET
    @Path("/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart getCart(@PathParam("cartId") String cartId) {
        return shoppingCartService.getShoppingCart(cartId);
    }

    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart add(@PathParam("cartId") String cartId,
                            @PathParam("itemId") String itemId,
                            @PathParam("quantity") int quantity) throws Exception {
        return shoppingCartService.addItem(cartId, itemId, quantity);
    }

    // Additional endpoints: set, delete, checkout
}
```

**API Surface (Preserved)**:
- **GET** `/cart/{cartId}` → Returns shopping cart with current pricing
- **POST** `/cart/{cartId}/{itemId}/{quantity}` → Adds items to cart
- **POST** `/cart/{cartId}/{tmpId}` → Sets cart contents from temporary cart
- **DELETE** `/cart/{cartId}/{itemId}/{quantity}` → Removes items from cart
- **POST** `/checkout/{cartId}` → Processes checkout and clears cart
- **Content-Type**: `application/json`
- **JAX-RS Path**: `/cart`

**Session Management Challenge** (`CartEndpoint.java:22`):
- `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)` requires architectural decision for stateless Quarkus deployment
- Current approach: Remove session scope, make stateless
- Alternative: Quarkus session management equivalent (TBD by implementation)

## Test Contracts (From S01 - Must Be Satisfied)

### ShoppingCartPricingTest Contract

**Key Assertion** (`ShoppingCartPricingTest.java:49-53`):
```java
assertThat(testCart)
    .returns(0.0, ShoppingCart::getCartItemPromoSavings)
    .returns(2000.0, ShoppingCart::getCartItemTotal)
    .returns(-10.99, ShoppingCart::getShippingPromoSavings)
    .returns(2000.0, ShoppingCart::getCartTotal);
```

**Test Scenario**: 2x $1000 items → $2000 cart total with free shipping promotion

**Helper Methods** (`ShoppingCartPricingTest.java:154-178`):
- `applyShippingCalculation()` → Applies shipping tiers then free shipping promotion
- **MUST MATCH**: Migrated service arithmetic exactly

### ShippingTierTest Contract

**Tier Boundaries** (`ShippingTierTest.java:15-143`):
- All tier transitions are deterministic
- Just below each boundary stays in lower tier
- Exact boundary values: 0, 25, 50, 75, 100
- Expected rates: 2.99, 4.99, 6.99, 8.99, 10.99

### Expectation Helper Pattern (Carried from S01)

**Requirement**: Port test-local expectation models to migrated services
- `ShoppingCartPricingTest.TestObjects` → Apply against migrated services
- `ShippingTierTest.expectedShipping()` → ShippingService equivalent
- Do not duplicate helpers into src/main

## Configuration & Bootstrap Behavior

**Application Bootstrap** (`CartServiceApplication.java:1-133`):
```java
@SpringBootApplication
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
```

**Configuration Requirements**:
- **Environment-driven config**: `${CATALOG_ENDPOINT:default}` for cloud readiness
- **Jersey path mapping**: `/api` preserved for backwards compatibility
- **Health endpoints**: `quarkus-smallrye-health` provides `/q/health`
- **Root path**: DEFAULT (quarkus.http.root-path not modified)

## Migration Constraints

**Session Scope Decision**:
- Quarkus deployment is stateless
- Remove `@Scope(SCOPE_SESSION)` from CartEndpoint
- Service methods must be reentrant

**Forbidden Behaviors**:
- `getMockProducts()` → No mocking of external services
- "Fallback to mock" → Per migration.yaml:27-28
- Breaking JSON serialization contracts

**Cloud Readiness**:
- In-memory `HashMap<String, ShoppingCart>` preserved for now
- Configuration defaults must be cloud-ready
- External service integration via environment variables

## Deliverables

**Service Layer**:
- `ShoppingCartServiceImpl` → `@ApplicationScoped` CDI bean with constructor injection
- `PromoService`, `ShippingService` → CDI managed beans with constructor injection
- `CatalogService` → Quarkus REST client with environment-driven config

**REST Endpoint**:
- `CartEndpoint` → JAX-RS resource class with `jakarta.*` imports
- `@Autowired` → constructor injection per `springboot-di-to-quarkus-00003`
- `@RestController` → JAX-RS annotations

**Configuration**:
- `CartServiceApplication` → Quarkus bootstrap (remove @SpringBootApplication)
- `application.properties` → Quarkus `application.properties` keys
- CATALOG_ENDPOINT → `${CATALOG_ENDPOINT:default}`

**Ship Surface (Carried from S01)**:
- `/api/cart/acceptance-check` → Served by real endpoint
- `/` → Returns 200 with minimal index page
- Root path stays DEFAULT

**POM Debt (Carried from S01)**:
- Resolve `javaee-pom-to-quarkus-00030/00050/00060` on destination pom
- Compiler/failsafe/native-profile conventions complete