# S01: Cart domain modernization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Modernize the complete Coolstore cart service from Spring Boot to Quarkus, resolving all mandatory findings while preserving behavioral contracts. This single bounded context story unblocks the entire migration - it's the only natural division given the god node coupling around ShoppingCart. Dependency order is respected: models → integration → services → REST layer. Deploy milestone proves live API contract preservation.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — HARVEST DTO with product data (itemId, name, desc, price)
- `src/main/java/com/redhat/coolstore/model/Promotion.java` — HARVEST promotion rule entity (itemId, percentOff)
- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — HARVEST cart line item linking Product + quantity
- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — HARVEST domain entity with pricing totals and state methods
- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — REDESIGN Feign client interface
  ```java
  @FeignClient(name="catalogService", url="${CATALOG_ENDPOINT}")
  public interface CatalogService {
      @RequestMapping(method = RequestMethod.GET, value = "/api/products")
      List<Product> getProducts();
  }
  ```
- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — service interface (no changes)
- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — REDESIGN core service with cart state management
  ```java
  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      @Autowired private PromoService promoService;
      @Autowired private ShippingService shippingService;
      @Autowired private CatalogService catalogService;
      private HashMap<String, ShoppingCart> carts = new HashMap<>();
      private HashMap<String, Product> productMap = new HashMap<>();
  }
  ```
- `src/main/java/com/redhat/coolstore/service/PromoService.java` — REDESIGN promotion calculations
  ```java
  @Component
  public class PromoService {
      @Autowired private Promotion promotion;
  }
  ```
- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — REDESIGN shipping calculation (no dependencies)
- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REDESIGN JAX-RS controller
  ```java
  @RestController
  @Path("/cart")
  public class CartEndpoint {
      @Autowired private ShoppingCartService shoppingCartService;
      
      @GET @Path("/{cartId}")
      public ShoppingCart getShoppingCart(@PathParam("cartId") String cartId) { ... }
      
      @POST @Path("/{cartId}/{itemId}/{quantity}")
      public ShoppingCart addItemToCart(...) { ... }
  }
  ```
- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — REDESIGN (to be removed)
  ```java
  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(CartEndpoint.class);
      }
  }
  ```
- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — REDESIGN (to be removed)
  ```java
  @SpringBootApplication
  @EnableFeignClients
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```
- `src/main/resources/application.properties` — config with CATALOG_ENDPOINT
  ```properties
  CATALOG_ENDPOINT=http://localhost:8081
  ```
- `pom.xml` — Spring Boot dependencies to convert

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Test files remain in legacy Spring Boot test structure until story completion
- No external service extraction - in-place modernization only

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `Product` — HARVEST: pure DTO carried over faithfully
- `ShoppingCart` — HARVEST: domain entity with pricing state preserved
- `ShoppingCartItem` — HARVEST: value object with product relationship intact
- `Promotion` — HARVEST: immutable promotion rule configuration
- `CartEndpoint` — REDESIGN: @ApplicationScoped CDI resource with constructor injection, GET-idempotent operations, input validation (quantity/itemId), comprehensive error mapping for catalog failures
- `ShoppingCartServiceImpl` — REDESIGN: @ApplicationScoped CDI bean with constructor injection, ConcurrentHashMap for thread-safe state, bounded LRU cache for productMap, GET-idempotent cart operations, retry/fallback for catalog service failures
- `PromoService` — REDESIGN: @ApplicationScoped CDI bean with constructor injection, immutable promotion rules, thread-safe application methods, cart-level free shipping >$75 threshold preserved
- `ShippingService` — REDESIGN: @ApplicationScoped CDI bean with tiered shipping thresholds (2.99, 4.99, 6.99, 8.99, 10.99), thread-safe stateless calculations
- `CatalogService` — REDESIGN: @RegisterRestClient with constructor injection, circuit breaker resilience, timeout handling, CATALOG_ENDPOINT environment variable support
- `JerseyConfig` — REDESIGN: complete removal - Quarkus auto-discovers JAX-RS resources
- `CartServiceApplication` — REDESIGN: complete removal - Quarkus bootstrap replaces Spring Boot

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **POM conversion**: Spring Boot parent → Quarkus BOM (`com.redhat.quarkus.platform`), Quarkus plugins, Spring Boot Actuator → quarkus-smallrye-health
- **JAX-RS migration**: javax.* → jakarta.* imports, @RestController → @Path, JAX-RS resources auto-discovered
- **CDI conversion**: @Service/@Component/@Autowired → constructor injection @ApplicationScoped beans
- **JDK modules**: removed JavaEE modules → provided by Quarkus platform
- **Configuration**: CATALOG_ENDPOINT preserved as `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT:http://localhost:8081}`
- **REST client**: @FeignClient → @RegisterRestClient with circuit breaker
- **Bootstrap**: @SpringBootApplication removed, main method deleted

## Contracts owned by this story

- **Findings**: All mandatory rule ids from findings inventory (24 rules, 47 incidents):
  - `springboot-di-to-quarkus-00003`, `spring-components-00001`, `spring-components-00002`, `localhost-http-00001`, `demo-env-integration-00001`, `jakarta-jaxrs-to-quarkus-00010`, `javaee-pom-to-quarkus-*`, `removed-javaee-modules-00020`, `springboot-actuator-to-quarkus-0100`, `springboot-annotations-to-quarkus-00000`, `springboot-di-to-quarkus-00000`, `springboot-metrics-to-quarkus-0100`, `springboot-metrics-to-quarkus-0200`, `springboot-parent-pom-to-quarkus-00000`, `springboot-plugins-to-quarkus-0000`, `springboot-properties-to-quarkus-00000`, `springboot-web-to-quarkus-00000`
- **Preserve**: `CATALOG_ENDPOINT` environment-driven configuration preserved in target contract
- **Behavioral pins**: 
  - Cart totals: cartItemTotal=2000.0, shippingPromoSavings=-10.99, cartTotal=2000.0 for 2x $1000 items
  - Free shipping promotion applies when cartItemTotal >= 75.0 (ShoppingCartServiceTest.java:51-53)
  - Product test data: "1111" Car $1000, "2222" Bike $200 (ProductsObjectMother.java:10-12)
- **Forbidden**: No mock products fallbacks, getMockProducts methods, or similar fabrication

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All 24 mandatory findings resolved (zero incidents on re-analysis)
- Cart API endpoints serving: GET /cart/{cartId}, POST /cart/{cartId}/{itemId}/{quantity}, DELETE /cart/{cartId}/{itemId}/{quantity}, POST /cart/checkout/{cartId}
- Behavioral contracts verified: cart totals calculation, free shipping >$75 rule, product test data preserved
- deploy story only: factory pipeline green, deployed, acceptance path
  serving (/api/cart/acceptance-check)
