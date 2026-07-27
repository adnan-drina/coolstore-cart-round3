# S02: Service layer CDI migration and REST endpoint conversion

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story completes the modernization by converting the service layer from Spring DI to Quarkus CDI AND converting the REST endpoint from Spring Web to native JAX-RS. It follows S01 per dependency-order.md, creating the first deployable milestone where the application serves its complete API surface. The story addresses the @Scope(SCOPE_SESSION) challenge on CartEndpoint (line 22) which requires special handling for stateless Quarkus deployment.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — Main business service with Spring annotations
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

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — Promotion calculation service
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

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — Shipping calculation service
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

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — Feign client for external catalog
  ```java
  @FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
  interface CatalogService {
      @GetMapping("/api/products")
      List<Product> products();
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REST endpoint with Spring session scope
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

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot application bootstrap
  ```java
  @SpringBootApplication
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```

- `src/main/resources/application.properties` — Configuration file
  ```
  spring.application.name=coolstore-cart-legacy
  spring.jersey.application-path=/api
  CATALOG_ENDPOINT=http://localhost:8081
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Domain models (ShoppingCart, Product, ShoppingCartItem, Promotion) — owned by S01, already converted
- JerseyConfig — owned by S01, already converted

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- ShoppingCartServiceImpl: @Service → @ApplicationScoped CDI bean with constructor injection
- PromoService, ShippingService: @Component → CDI managed beans with constructor injection  
- CartEndpoint: @RestController → JAX-RS resource class with jakarta.* imports
- CartEndpoint: @Scope(SCOPE_SESSION) → stateless approach or appropriate Quarkus session management
- CartEndpoint: @Autowired → constructor injection per springboot-di-to-quarkus-00003
- CartEndpoint: javax.ws.rs.* → jakarta.ws.rs.* imports (already handled by recipe)
- CartServiceApplication: @SpringBootApplication → remove, convert to Quarkus main class or remove entirely
- CatalogService: Feign client → Quarkus REST client with environment-driven config
- CATALOG_ENDPOINT: ${CATALOG_ENDPOINT} → ${CATALOG_ENDPOINT:default} for cloud readiness
- Health endpoints: quarkus-smallrye-health provides /q/health

## Contracts owned by this story

- **Findings**: springboot-di-to-quarkus-00003 (all remaining service instances), springboot-di-to-quarkus-00003 (CartEndpoint), jakarta-jaxrs-to-quarkus-00010, springboot-actuator-to-quarkus-0100, springboot-annotations-to-quarkus-00000, demo-env-integration-00001, localhost-http-00001
- **Preserve**: CATALOG_ENDPOINT configuration mechanism preserved per migration.yaml:23
- **Behavioral pins**: Complete REST API contract preserved:
  - **GET /cart/{cartId}**: Returns shopping cart with current pricing
  - **POST /cart/{cartId}/{itemId}/{quantity}**: Adds items to cart
  - **POST /cart/{cartId}/{tmpId}**: Sets cart contents from temporary cart
  - **DELETE /cart/{cartId}/{itemId}/{quantity}**: Removes items from cart
  - **POST /checkout/{cartId}**: Processes checkout and clears cart
  - **Content-Type**: application/json maintained
  - **JAX-RS Path**: /api/cart (from spring.jersey.application-path=/api preserved)
  - **Shipping tiers**: $0-25=$2.99, $25-50=$4.99, $50-75=$6.99, $75-100=$8.99, $100+=$10.99
  - **Promotion rules**: 25% off item "329299"; free shipping on carts ≥ $75
- **Session management challenge**: @Scope(SCOPE_SESSION) on CartEndpoint:22 requires architectural decision for stateless Quarkus deployment
- **Forbidden**: getMockProducts, "Fallback to mock" prohibited per migration.yaml:27-28

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- **DEPLOYABLE MILESTONE**: Complete REST API serves at /api/cart/* with all endpoints functional
- All @Service → @ApplicationScoped, @Component → CDI beans with constructor injection
- CartEndpoint: @RestController → JAX-RS resource with jakarta.* imports
- CartEndpoint: @Autowired → constructor injection implemented
- CartEndpoint: @Scope(SCOPE_SESSION) appropriately handled: either removed (stateless approach) or converted to Quarkus session management equivalent
- CartServiceApplication: @SpringBootApplication removed; Quarkus bootstrap configured
- CATALOG_ENDPOINT preserved as environment-driven config with default value support
- quarkus-smallrye-health provides /q/health endpoint
- Feign client converted to Quarkus REST client while maintaining same API contract
- In-memory HashMap<String, ShoppingCart> preserved for now (cloud readiness follow-up)
- All endpoints return proper JSON responses with correct HTTP status codes
- All existing tests pass with converted service layer and endpoint
- Integration tests validate complete cart workflow: add items, pricing calculations, checkout
- Business logic validation: shipping calculations, promotions, cart operations tested
- Factory pipeline green: Maven build passes, no SonarQube violations, image builds successfully
