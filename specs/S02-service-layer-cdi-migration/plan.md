# S02 Plan: Service Layer CDI Migration and REST Endpoint Conversion

## Migration Strategy

**Rewrite Tasks** (mechanical transforms - OpenRewrite recipes):
- Jakarta EE import migration (javax → jakarta)
- Spring annotations to CDI annotations
- Spring Web to JAX-RS annotations
- Configuration file format conversion
- Feign client to Quarkus REST client

**Infer Tasks** (judgment required - OpenCode implementation):
- CDI bean design and constructor injection
- Session management strategy for stateless deployment
- REST client configuration and environment integration
- Service test characterization and validation

## Component Migrations

### 1. Service Layer Migration

#### ShoppingCartServiceImpl
**Legacy**: `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1-222`
```java
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired ShippingService ss;
    @Autowired CatalogService catalogServie;
    @Autowired PromoService ps;
    Map<String, ShoppingCart> carts;
    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new HashMap<>();
    }
}
```
**Target**: `@ApplicationScoped` CDI bean with constructor injection
```java
@ApplicationScoped
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShippingService shippingService;
    private final CatalogService catalogService;
    private final PromoService promoService;
    private Map<String, ShoppingCart> carts;
    private Map<String, Product> productMap = new HashMap<>();

    @Inject
    public ShoppingCartServiceImpl(
            ShippingService shippingService,
            CatalogService catalogService,
            PromoService promoService) {
        this.shippingService = shippingService;
        this.catalogService = catalogService;
        this.promoService = promoService;
    }

    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new HashMap<>();
    }
}
```
**Class**: infer

#### PromoService
**Legacy**: `src/main/java/com/redhat/coolstore/service/PromoService.java:1-77`
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
}
```
**Target**: CDI managed bean with constructor injection
```java
@ApplicationScoped
public class PromoService implements Serializable {
    private static final long serialVersionUID = 2088590587856645568L;
    private String name = null;
    private Set<Promotion> promotionSet = null;

    @Inject
    public PromoService() {
        promotionSet = new HashSet<Promotion>();
        promotionSet.add(new Promotion("329299", .25));
    }
}
```
**Class**: infer

#### ShippingService
**Legacy**: `src/main/java/com/redhat/coolstore/service/ShippingService.java:1-25`
```java
@Component
public class ShippingService {
    public void calculateShipping(ShoppingCart sc) {
        if (sc != null) {
            if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
                sc.setShippingTotal(2.99);
            } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
                sc.setShippingTotal(4.99);
            }
            // ... remaining tiers
        }
    }
}
```
**Target**: CDI managed bean with constructor injection
```java
@ApplicationScoped
public class ShippingService {
    
    @Inject
    public ShippingService() {
        // Default constructor for CDI
    }

    public void calculateShipping(ShoppingCart sc) {
        if (sc != null) {
            if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
                sc.setShippingTotal(2.99);
            } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
                sc.setShippingTotal(4.99);
            }
            // ... remaining tiers
        }
    }
}
```
**Class**: infer

### 2. External Service Migration

#### CatalogService
**Legacy**: `src/main/java/com/redhat/coolstore/service/CatalogService.java:1-92`
```java
@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```
**Target**: Quarkus REST client
```java
@RegisterRestClient(baseUri = "${catalog.endpoint}")
public interface CatalogService {
    
    @GET
    @Path("/api/products")
    List<Product> products();
}
```
**Configuration**:
```properties
# Quarkus application.properties
catalog.endpoint=${CATALOG_ENDPOINT:http://localhost:8081}
```
**Class**: infer

### 3. REST Endpoint Migration

#### CartEndpoint
**Legacy**: `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:1-123`
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
**Target**: JAX-RS resource class with constructor injection
```java
@Path("/api/cart")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CartEndpoint implements Serializable {
    private static final long serialVersionUID = -7227732980791688773L;

    private final ShoppingCartService shoppingCartService;

    @Inject
    public CartEndpoint(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GET
    @Path("/{cartId}")
    public ShoppingCart getCart(@PathParam("cartId") String cartId) {
        return shoppingCartService.getShoppingCart(cartId);
    }

    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    public ShoppingCart add(@PathParam("cartId") String cartId,
                            @PathParam("itemId") String itemId,
                            @PathParam("quantity") int quantity) throws Exception {
        return shoppingCartService.addItem(cartId, itemId, quantity);
    }

    // Additional endpoints: set, delete, checkout
}
```
**Class**: infer

### 4. Configuration Migration

#### Application Bootstrap
**Legacy**: `src/main/java/com/redhat/coolstore/CartServiceApplication.java:1-133`
```java
@SpringBootApplication
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
```
**Target**: Quarkus main class or remove entirely
```java
public class CartServiceApplication {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
```
**Alternative**: Remove entirely if Quarkus can bootstrap without explicit main
**Class**: infer

#### Configuration Properties
**Legacy**: `src/main/resources/application.properties:1-141`
```
spring.application.name=coolstore-cart-legacy
spring.jersey.application-path=/api
CATALOG_ENDPOINT=http://localhost:8081
```
**Target**: Quarkus application.properties
```
# Quarkus configuration
quarkus.application.name=coolstore-cart
catalog.endpoint=${CATALOG_ENDPOINT:http://localhost:8081}
quarkus.http.port=8080

# Health endpoints
quarkus.smallrye-health.enabled=true
```
**Class**: rewrite

## Preserved Behaviors

### Shipping Tier Arithmetic (Pinned by S01 Tests)
Must preserve exact tier boundaries and rates from `ShippingTierTest.java:15-143`:
- $0-25: $2.99
- $25-50: $4.99
- $50-75: $6.99
- $75-100: $8.99
- $100+: $10.99

### Promotion Composition Semantics (Carried from S01)
- PromoService ZEROES shippingTotal
- shippingPromoSavings kept informational
- cartTotal = itemTotal + shippingTotal (where shippingTotal = 0 after promotion)
- 2x $1000 items test: cart item total = $2000, shipping promo savings = -$10.99, final cart total = $2000

### API Surface Preservation
- GET `/api/cart/{cartId}` → Returns shopping cart with current pricing
- POST `/api/cart/{cartId}/{itemId}/{quantity}` → Adds items to cart
- POST `/api/cart/{cartId}/{tmpId}` → Sets cart contents from temporary cart
- DELETE `/api/cart/{cartId}/{itemId}/{quantity}` → Removes items from cart
- POST `/api/cart/checkout/{cartId}` → Processes checkout and clears cart
- POST `/api/cart/acceptance-check` → Health/acceptance endpoint
- Content-Type: application/json maintained

## Session Management Strategy

**Problem**: `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)` on CartEndpoint:22
**Solution**: Stateless approach for Quarkus deployment
- Remove @Scope annotation
- Make CartEndpoint @ApplicationScoped
- Service methods must be reentrant
- No session state in endpoint

## Findings Resolution

### Spring Boot DI to Quarkus CDI (springboot-di-to-quarkus-00003)
- ShoppingCartServiceImpl: @Service → @ApplicationScoped
- PromoService, ShippingService: @Component → CDI managed beans
- CartEndpoint: @Autowired → constructor injection
- All services: field injection → constructor injection

### Jakarta EE (jakarta-jaxrs-to-quarkus-00010)
- CartEndpoint: javax.ws.rs.* → jakarta.ws.rs.*
- RestController → JAX-RS annotations (@Path, @GET, @POST, @DELETE)

### Spring Boot Actuator (springboot-actuator-to-quarkus-0100)
- Replace Spring Boot actuator with quarkus-smallrye-health
- Health endpoint: /q/health

### Spring Boot Annotations (springboot-annotations-to-quarkus-00000)
- @SpringBootApplication → Remove or convert to Quarkus bootstrap
- @RestController → JAX-RS resource
- @Service, @Component → CDI annotations

### Environment Integration (demo-env-integration-00001)
- CATALOG_ENDPOINT configuration mechanism preserved
- Default value support: ${CATALOG_ENDPOINT:http://localhost:8081}

### Localhost HTTP (localhost-http-00001)
- Environment-driven endpoint configuration
- Cloud-ready default values

## POM Debt Resolution (Carried from S01)

### Java EE to Quarkus BOM (javaee-pom-to-quarkus-00030/00050/00060)
- Resolve compiler/failsafe/native-profile conventions
- Complete BOM alignment for Quarkus 3.27, Java 21
- Platform dependency management

## Test Contracts (Must Be Satisfied)

### Service Test Characterization
- Port ShoppingCartPricingTest expectations to migrated services
- ShippingTierTest tier arithmetic must match exactly
- Apply TestObjects helper methods against real services
- Validate promotion composition semantics

### Integration Tests
- Complete cart workflow: add items, pricing calculations, checkout
- Business logic validation: shipping calculations, promotions
- External service integration: CatalogService REST client
- Error handling: external service failures

## Ship Surface Requirements (Carried from S01)

### Acceptance Path
- `/api/cart/acceptance-check` → Served by real endpoint
- Minimal health/acceptance check logic
- Returns appropriate HTTP status codes

### Root Path
- `/` → Returns 200 with minimal index page
- quarkus.http.root-path stays DEFAULT
- No root-path redirection or modification

## Implementation Constraints

**Cloud Readiness**:
- In-memory HashMap<String, ShoppingCart> preserved for now
- Environment-driven configuration with defaults
- No hardcoded localhost references

**Forbidden Patterns**:
- No getMockProducts() calls
- No "Fallback to mock" behavior
- No breaking JSON serialization contracts

**Performance Requirements**:
- Stateless service design
- Reentrant method calls
- Thread-safe in-memory storage