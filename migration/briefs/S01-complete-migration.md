# S01: Complete Spring Boot to Quarkus migration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

What this story achieves and why it is next: its place in the roadmap,
what it unblocks, which stories it depends on (cite
dependency-order.md / architecture-profile.md).

This single story modernizes the entire Coolstore cart service from Spring Boot to Quarkus. It is positioned first because:

1. **Dependency graph requirements**: The dependency analysis shows god nodes (ShoppingCart with fan-in 5, Product with fan-in 4, ShoppingCartItem with fan-in 3) that must be characterized and converted before any dependent services
2. **Tight coupling**: The single bounded context (architecture-profile §6) has all services tightly coupled through shared domain objects, making incremental modernization compilation-impossible
3. **API surface coherence**: The entire /api/cart/* REST surface must modernize together to maintain API contracts
4. **No dependencies**: This story owns all components and has no external story dependencies

This story unblocks the complete migration by modernizing all 12 classes and the pom.xml in one coordinated transformation that maintains buildability at each commit.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — pure data entity for product information
  ```java
  public class Product {
      private String itemId;
      private BigDecimal price;
      private String name;
      private String description;
      // getters, setters, constructors
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — value object for discount rules
  ```java
  public class Promotion {
      private String productId;
      private BigDecimal discount;
      // getters, setters, constructors
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — composition object linking products to cart quantities
  ```java
  public class ShoppingCartItem {
      private Product product;
      private int quantity;
      private BigDecimal price;
      // getters, setters, constructors
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — domain object with pricing fields and cart management
  ```java
  public class ShoppingCart {
      private String cartId;
      private List<ShoppingCartItem> shoppingCartItem;
      private BigDecimal cartItemPromoSavings;
      private BigDecimal cartItemTotal;
      private BigDecimal shippingPromoSavings;
      private BigDecimal cartTotal;
      // pricing and cart management methods
  }
  ```

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — Feign client interface for product catalog integration
  ```java
  @FeignClient(name = "catalog", url = "${CATALOG_ENDPOINT}")
  public interface CatalogService {
      @RequestMapping(method = RequestMethod.GET, value = "/api/products/{itemId}")
      Product getProduct(@PathVariable("itemId") String itemId);
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — service interface for cart operations
  ```java
  public interface ShoppingCartService {
      ShoppingCart addItem(String cartId, String itemId, int quantity);
      ShoppingCart removeItem(String cartId, String itemId, int quantity);
      ShoppingCart getShoppingCart(String cartId);
      ShoppingCart transferCart(String tmpCartId, String cartId);
      ShoppingCart checkout(String cartId);
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — service implementation with HashMap storage
  ```java
  @Component
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      
      @Autowired
      private CatalogService catalogService;
      
      @Autowired
      private PromoService promoService;
      
      @Autowired
      private ShippingService shippingService;
      
      private HashMap<String, ShoppingCart> carts = new HashMap<>();
      
      @Override
      public ShoppingCart addItem(String cartId, String itemId, int quantity) {
          // implementation with HashMap storage and pricing logic
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — application-scoped pricing service
  ```java
  @Component
  public class PromoService {
      
      @Autowired
      private CatalogService catalogService;
      
      public void applyPromotions(ShoppingCart cart) {
          // 25% discount on product "329299"
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — application-scoped shipping calculation
  ```java
  @Component
  public class ShippingService {
      
      public BigDecimal calculateShipping(BigDecimal cartTotal) {
          // tiered shipping logic
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — session-scoped JAX-RS endpoint
  ```java
  @Path("/api/cart")
  @RequestScope
  public class CartEndpoint {
      
      @Autowired
      private ShoppingCartService shoppingCartService;
      
      @GET
      @Path("/{cartId}")
      public ShoppingCart getShoppingCart(@PathParam("cartId") String cartId) {
          return shoppingCartService.getShoppingCart(cartId);
      }
      
      @POST
      @Path("/{cartId}/{itemId}/{quantity}")
      public ShoppingCart addItemToCart(@PathParam("cartId") String cartId, 
                                       @PathParam("itemId") String itemId,
                                       @PathParam("quantity") int quantity) {
          return shoppingCartService.addItem(cartId, itemId, quantity);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — Spring-managed Jersey configuration
  ```java
  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(CartEndpoint.class);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot bootstrap
  ```java
  @SpringBootApplication
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```

- `pom.xml` — Spring Boot dependencies and build configuration
  ```xml
  <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.7.14</version>
  </parent>
  
  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-jersey</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-actuator</artifactId>
      </dependency>
  </dependencies>
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

This is a single-story migration, so there is no neighboring code. The entire cart service is modernized together, eliminating the need for temporary compatibility seams.

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `Product` — HARVEST
  - Pure data entity with no business logic, preserved as-is with jakarta serialization
  
- `Promotion` — HARVEST  
  - Simple value object for discount rules, maintained with current field structure
  
- `ShoppingCart` — HARVEST
  - Domain object with pricing fields and cart management methods, preserved behavior
  
- `ShoppingCartItem` — HARVEST
  - Composition object linking products to cart quantities and pricing
  
- `ShoppingCartService` — REDESIGN
  - Target: thread-safe singleton with mutable state → ConcurrentHashMap with compute() operations
  - Cache refresh guard → no clear-on-miss, bounded refresh
  - normalizeBeforeDerive → normalize cart items BEFORE deriving pricing
  
- `ShoppingCartServiceImpl` — REDESIGN
  - Target: concurrency: shared singleton with HashMap cart storage → ConcurrentHashMap<String, ShoppingCart> with compute() for atomic updates
  - Resource policy: productMap caches catalog data without eviction → bounded refresh with timed eviction policy
  - normalizeBeforeDerive: cart item deduplication must occur before pricing calculations → maintain dedupe-before-pricing sequence
  - targetContract: getIdempotent→404, validateInput→400, mapErrors→503, threadSafeState→ConcurrentHashMap, cacheRefreshGuard→no-clear-on-miss
  
- `PromoService` — REDESIGN
  - Target: @ApplicationScoped with thread-safe promotion application
  - targetContract: threadSafeState→ConcurrentHashMap for promotionSet storage
  
- `ShippingService` — REDESIGN
  - Target: @ApplicationScoped stateless service
  - targetContract: maintain current shipping tier logic with thread-safe execution
  
- `CartEndpoint` — REDESIGN
  - Target: @RequestScoped JAX-RS resource
  - targetContract: getIdempotent→404 (never creates missing carts), validateInput→400 (reject invalid inputs), mapErrors→503 (catalog service failures)
  
- `JerseyConfig` — REDESIGN
  - Target: removed — Quarkus auto-registers JAX-RS resources
  
- `CatalogService` — REDESIGN
  - Target: @RegisterRestClient with @Path and @GET methods
  - targetContract: maintain ${CATALOG_ENDPOINT:default} configuration, thread-safe HTTP client operations

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

Based on the MAPPINGS catalog and findings inventory:

1. **javax→jakarta import transformation**:
   - `javax.ws.rs.*` → `jakarta.ws.rs.*`
   - `javax.annotation.*` → `jakarta.annotation.*`

2. **Spring Boot → Quarkus dependency conversion**:
   - Spring Boot parent → Quarkus platform BOM (`com.redhat.quarkus.platform`)
   - Spring Web → quarkus-rest
   - Spring Jersey → quarkus-rest
   - Spring Actuator → quarkus-smallrye-health
   - Micrometer → quarkus-smallrye-metrics

3. **Dependency injection conversion**:
   - `@Autowired` constructor injection → native CDI constructor injection
   - `@Component` → `@ApplicationScoped` for services

4. **REST endpoint conversion**:
   - `@Path`, `@GET`, `@POST`, `@DELETE`, `@PathParam` → Jakarta EE 9+ equivalents
   - Session scope → Request scope for REST resources

5. **Bootstrap conversion**:
   - `@SpringBootApplication` + main class → Quarkus auto-bootstrap (remove completely)

6. **Configuration conversion**:
   - Spring properties → Quarkus configuration properties
   - Feign client → @RegisterRestClient with configuration

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-di-to-quarkus-00003: Constructor injection conversion
  - spring-components-00001, spring-components-00002: Version incompatibility resolution
  - jakarta-jaxrs-to-quarkus-00010: JAX-RS dependency replacement
  - javaee-pom-to-quarkus-* (00010, 00020, 00030, 00040, 00050, 00060, 00080): Maven/BOM/Plugin conversion
  - removed-javaee-modules-00020: Java EE module removal handling
  - springboot-actuator-to-quarkus-0100: Health endpoint conversion
  - springboot-annotations-to-quarkus-00000: Bootstrap model conversion
  - springboot-di-to-quarkus-00000: DI artifact replacement
  - springboot-metrics-to-quarkus-0100, 0200: Metrics conversion
  - springboot-parent-pom-to-quarkus-00000: Parent POM conversion
  - springboot-plugins-to-quarkus-0000: Maven plugin conversion
  - springboot-properties-to-quarkus-00000: Properties conversion
  - springboot-web-to-quarkus-00000: Web artifact conversion
  - localhost-http-00001: Environment-driven configuration
  - demo-env-integration-00001: Preserve external configuration

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - CATALOG_ENDPOINT: Environment variable `${CATALOG_ENDPOINT}` for product catalog service URL, maintained as `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT:http://localhost:8081}`

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - Cart initialization: empty carts return zero totals (cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0) from ShoppingCartServiceTest.should_get_initialized_shopping_cart_in_case_of_not_exists():28
  - Cart pricing: 2 items at $1000 each = $2000 cart total, with shipping cost $10.99 from ShoppingCartServiceTest.should_calculate_price_of_cart():39
  - Promotion: Product "329299" receives 25% discount from PromoService.java:27
  - Free shipping: Applied when cart total ≥ $75 from PromoService.java:51
  - Shipping tiers: <$25 = $2.99, $25-$50 = $4.99, $50-$75 = $6.99, $75-$100 = $8.99, ≥$100 = $10.99 from ShippingService.java:12
  - GAPS: Endpoint-level contracts (404 on missing cart, 400 on invalid input, 503 on catalog failures) need characterization testing

- **Forbidden**: the fabrication tripwires relevant here.
  - No mock product generation (`getMockProducts`, "mock products", "Mock products", "mock Products", "Fallback to mock")

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - All 47 findings resolved (0 remaining in re-analysis)
  - ShoppingCartServiceTest passes with all assertions
  - All /api/cart/* endpoints functional (GET, POST, DELETE, POST checkout, POST transfer)
  - CATALOG_ENDPOINT environment configuration preserved
  - No forbidden mock product patterns introduced
- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - Maven build successful with quarkus:dev and production profile
  - SonarQube quality gate green (≥80% coverage, 0 new violations)
  - Application deployed and serving /api/cart/* endpoints
  - Acceptance path /api/cart/acceptance-check functional
