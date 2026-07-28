# S05: Catalog service and REST endpoint (REDESIGN)

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story modernizes the integration boundary — the external catalog service and the REST API surface. This is the deployable endpoint where the application's external contract is finalized. Key aspects include cloud-ready configuration, Quarkus REST client integration, and the explicit behavior-changing redesign from session-scoped to request-scoped REST resources.

Position: **Fifth story** (depends on S04). This is the deployable milestone that proves the modernized application can serve its API contract in production.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — External catalog integration
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.beans.factory.annotation.Autowired; // WILL CHANGE
  import org.springframework.stereotype.Component; // WILL CHANGE
  import org.springframework.cloud.openfeign.FeignClient; // WILL CHANGE
  
  @Component
  @FeignClient(name = "catalog", url = "${CATALOG_ENDPOINT}") // WILL CHANGE
  public class CatalogService {
      
      @Autowired // WILL BE REMOVED
      public CatalogService() {
      }
      
      @RequestMapping(method = RequestMethod.GET, value = "/api/products/{id}") // WILL CHANGE
      public Product getProduct(@PathVariable("id") String id) {
          // Feign client call
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REST API facade
  ```java
  package com.redhat.coolstore.rest;
  
  import org.springframework.beans.factory.annotation.Autowired; // WILL CHANGE
  import org.springframework.web.bind.annotation.*; // WILL CHANGE
  import org.springframework.session.SessionScoped; // WILL BE REMOVED
  
  @RestController
  @SessionScoped // WILL BE REMOVED - behavior change!
  public class CartEndpoint {
      
      @Autowired
      private ShoppingCartServiceImpl shoppingCartService; // WILL CHANGE
      
      @GetMapping("/cart/{cartId}")
      public ShoppingCart getCart(@PathVariable String cartId) {
          return shoppingCartService.getShoppingCart(cartId);
      }
      
      @PostMapping("/cart/{cartId}/{itemId}/{quantity}")
      public ShoppingCart addItem(@PathVariable String cartId, @PathVariable String itemId, @PathVariable int quantity) {
          return shoppingCartService.addToCart(cartId, itemId, quantity);
      }
      
      // 5 more endpoints: POST /cart/{cartId}/tmpId, DELETE /cart/{cartId}/{itemId}/{quantity}, POST /cart/checkout/{cartId}
  }
  ```

- `src/main/resources/application.properties` — Configuration
  ```properties
  CATALOG_ENDPOINT=http://localhost:8082/api/catalog # WILL BECOME CLOUD-READY
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **ShoppingCartService interface** — already removed in S04
- **Test files** — unchanged until S06 validation
- **PromoService, ShippingService** — already modernized in S03

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `CatalogService` — **REDESIGN**
  - Target: Quarkus REST client with `@RegisterRestClient` and env-driven URL config
  - Concurrency: Stateless client with connection pooling via Quarkus REST
  - Resource policy: Timeout and retry policies per Quarkus REST client defaults; circuit breaker recommended
  
- `CartEndpoint` — **REDESIGN (behavior-CHANGING)**
  - Target: JAX-RS `@Path` resource with Quarkus REST (replaces Spring `@RestController`)
  - Concurrency: **Request-scoped resource** (replaced with request-scoped; Quarkus doesn't support session scope by default)
  - API contract (behavior-CHANGING): 
    - GET operations remain idempotent read-only
    - POST/DELETE operations mutate state (legacy behavior preserved)
    - No input validation changes planned
    - Error mapping: Map exceptions to appropriate HTTP status codes per JAX-RS standards
  - **Note: Explicit departure from Spring session scope; request-scoped implementation chosen for Quarkus compatibility**

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `springboot-di-to-quarkus-00003` (infer) — Apply Quarkus Spring DI conversion guidance:
  - Remove `@Component`/@RestController → Add appropriate JAX-RS annotations
  - Remove `@Autowired` field injection → Add constructor injection
  - Feign → Quarkus REST client with `@RegisterRestClient`

- `springboot-web-to-quarkus-00000` (infer) — Replace the Spring Web artifact:
  - `@RestController` → JAX-RS `@Path` resource
  - `@GetMapping/@PostMapping/@DeleteMapping` → JAX-RS `@GET/@POST/@DELETE`
  - `@PathVariable` → JAX-RS `@PathParam`
  - `@SessionScoped` → request scope (behavior change)

- `localhost-http-00001` (infer) — Cloud-readiness:
  - `CATALOG_ENDPOINT=http://localhost:8082` → `CATALOG_ENDPOINT=${CATALOG_ENDPOINT:http://localhost:8082/api/catalog}`
  - Environment variable override capability preserved

- `demo-env-integration-00001` (infer) — Environment-driven config preserved:
  - Surface IS the preserve contract: environment-driven config maintained
  - `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT}`

- `springboot-metrics-to-quarkus-0200` (infer) — Replace Micrometer with MP Metrics:
  - Add `@Counted`, `@Timed`, `@Gauge` annotations where appropriate

## Contracts owned by this story

- **Findings**: springboot-web-to-quarkus-00000, localhost-http-00001, demo-env-integration-00001, springboot-metrics-to-quarkus-0200
- **Preserve**: CATALOG_ENDPOINT environment-driven configuration preserved per migration.yaml
- **Behavioral pins**: 
  - GET `/cart/{cartId}` returns identical cart structure
  - POST `/cart/{cartId}/{itemId}/{quantity}` has identical cart mutation behavior
  - POST `/cart/{cartId}/tmpId` transfers cart contents identically
  - DELETE `/cart/{cartId}/{itemId}/{quantity}` removes items identically
  - POST `/cart/checkout/{cartId}` clears cart identically
  - **BEHAVIOR CHANGE**: Session scope → request scope (documented departure from legacy)
- **Forbidden**: getMockProducts, mock products variants (catalog service must call real endpoint)

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All 6 REST endpoints functional with identical behavior (except session scope change)
- CATALOG_ENDPOINT configurable via environment variable with defaults
- Quarkus REST client replaces Feign with proper error handling
- MP Metrics annotations added where beneficial
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- **DEPLOY STORY**: factory pipeline green, deployed, acceptance path serving

**DEPLOYABLE MILESTONE**: This story produces a fully functional REST API that can replace the legacy service. The session→request scope change is the only intentional behavioral modification and is documented.
