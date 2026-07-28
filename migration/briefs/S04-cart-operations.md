# S04: Cart operations and REST endpoints

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts the core cart operations implementation and REST endpoints, delivering the first deployable milestone. ShoppingCartServiceImpl implements the thread-safe cart behavior with ConcurrentHashMap, while CartEndpoint exposes the API contract via JAX-RS. This story pins the target contract from architecture-profile §7 and makes the application operational.

This story follows S03 because it depends on the converted business logic services (PromoService, ShippingService) and service interfaces (ShoppingCartService). Architecture-profile §7 emphasizes behavior-changing redesign for these classes, including GET→404 changes and concurrent access safety.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — cart operations implementation with in-memory storage
  ```java
  // Spring @Service annotation, field injection, and HashMap storage
  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      @Autowired
      private CatalogService catalogService;
      
      @Autowired
      private PromoService promoService;
      
      @Autowired
      private ShippingService shippingService;
      
      // HashMap<String, ShoppingCart> cartMap = new HashMap<>();
      private Map<String, ShoppingCart> cartMap = new HashMap<>();
      
      public ShoppingCart createShoppingCart(String cartId) {
          // creates cart implicitly on first access
      }
      
      public ShoppingCart getShoppingCart(String cartId) {
          // returns existing cart or creates new one
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REST resource exposing cart operations
  ```java
  // Spring @RestController and @Autowired field injection
  @RestController
  @RequestMapping("/cart")
  public class CartEndpoint {
      @Autowired
      private ShoppingCartService shoppingCartService;
      
      @GetMapping("/{cartId}")
      public ShoppingCart getCart(@PathVariable String cartId) {
          // returns cart contents or creates new cart (BEHAVIOR CHANGE)
      }
      
      @PostMapping("/{cartId}/{itemId}/{quantity}")
      public ShoppingCart addToCart(@PathVariable String cartId, ...) {
          // add item logic
      }
      
      // other endpoints...
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- CartServiceApplication and JerseyConfig remain until S05
- Business logic services remain as converted in S03
- Data models remain as converted in S01-S02

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `ShoppingCartServiceImpl` — REDESIGN
  - Role: Cart operations implementation with in-memory storage
  - Target: CDI managed bean with constructor injection
  - Target contract: 
    - **Concurrency**: thread-safe singleton with ConcurrentHashMap and compute() operations
    - **Resource policy**: bounded productMap (no clear-on-miss), cart eviction strategy needed
    - **Aggregate math**: normalize-before-deriving (dedupe cart items before pricing calculations)
    - **Error handling**: catalog service failures surface as 503 via ExceptionMapper

- `CartEndpoint` — REDESIGN  
  - Role: REST resource exposing cart operations
  - Target: JAX-RS resource with CDI constructor injection
  - Target contract:
    - **Read operations return 404** on non-existent carts (BEHAVIOR CHANGE: legacy creates implicit carts)
    - **Invalid inputs rejected with 400** (problem-detail format)
    - **Downstream failures return 503** via JAX-RS ExceptionMapper
    - **Concurrent access**: thread-safe cart operations via synchronized HashMap
    - **Cache policy**: no product cache eviction on miss (bounded productMap with refresh guard)

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **springboot-web-to-quarkus-00000 [infer]**: Replace the Spring Web artifact with Quarkus 'spring-web' extension
  - Target: native JAX-RS resources (NOT the spring-web extension)
- **springboot-di-to-quarkus-00003 [infer]**: Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
  - Target: native CDI constructor injection (NOT the spring-di extension)
- **jakarta-jaxrs-to-quarkus-00010 [rewrite]**: Replace jakarta JAX-RS dependency
  - Target: `quarkus-rest` dependency
- **javax-to-jakarta-import-00001 [recipe]**: Convert all `javax.*` imports to `jakarta.*`

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-web-to-quarkus-00000
  - springboot-di-to-quarkus-00003
  - jakarta-jaxrs-to-quarkus-00010

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - **CATALOG_ENDPOINT**: Environment-driven external configuration preserved via REST Client config

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - **Cart pricing**: `cartItemTotal=2000.0`, `shippingPromoSavings=-10.99`, `cartTotal=2000.0` from ShoppingCartServiceTest.java:39
  - **BEHAVIOR CHANGES** (from architecture-profile §7):
    - GET `/cart/{cartId}` returns **404** for non-existent carts (legacy creates implicit carts)
    - Invalid quantities reject with **400** (legacy allows negative quantities)
    - Catalog service failures return **503** (legacy behavior unclear)
  - Thread-safe cart operations with ConcurrentHashMap
  - Product catalog access with bounded productMap

- **Forbidden**: the fabrication tripwires relevant here.
  - None of the forbidden items appear in cart operations

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - ShoppingCartServiceImpl converted to CDI with constructor injection and ConcurrentHashMap
  - CartEndpoint converted to JAX-RS with constructor injection
  - All REST endpoints operational: GET/POST/DELETE /cart/{cartId}*, POST /cart/checkout/{cartId}
  - Cart pricing behavior preserved exactly (assertions from ShoppingCartServiceTest.java)
  - GET endpoints return 404 for non-existent carts (BEHAVIOR CHANGE)
  - Concurrent access scenarios verified thread-safe
  - Integration tests pass for complete cart workflow
  - DEPLOY STORY: factory pipeline green, deployed, /api/cart endpoints serving

- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - **YES - S04 is the first deployable milestone**
  - Factory pipeline must build, test, and deploy successfully
  - Acceptance path `/api/cart` endpoints must be operational
  - Health endpoint `/q/health` must return UP status