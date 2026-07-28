# S05: REST endpoints and application bootstrap

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story completes the migration by converting REST endpoints to Quarkus JAX-RS, removing JerseyConfig (auto-discovery), and eliminating the Spring Boot application bootstrap. The final surface layer transformation implements the target contracts for GET idempotency, input validation, and error mapping as specified in the architecture profile.

Position: S05 depends on S04 (service implementations) and is the final story. This is the deploy milestone that proves the entire API contract works end-to-end.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — JAX-RS REST controller (REDESIGN)
  ```java
  import org.springframework.beans.factory.annotation.Autowired;  // → CDI constructor injection
  import javax.ws.rs.*;  // → jakarta.ws.rs
  
  @RestController
  @Path("/cart")
  public class CartEndpoint {
      @Autowired
      private ShoppingCartService cartService;  // → constructor injection
      
      @GetMapping("/{cartId}")
      public ShoppingCart getCart(@PathVariable String cartId) {
          return cartService.getShoppingCart(cartId);  // May create cart if missing (legacy behavior)
      }
      
      @PostMapping("/{cartId}/{itemId}/{quantity}")
      public ShoppingCart addItem(...) { ... }
      
      @PostMapping("/checkout/{cartId}")
      public ShoppingCart checkout(...) { ... }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — Jersey configuration (REMOVED)
  ```java
  import org.glassfish.jersey.server.ResourceConfig;  // → REMOVED - Quarkus auto-discovers
  
  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(RestClient.class);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot bootstrap (REMOVED)
  ```java
  import org.springframework.boot.SpringApplication;  // → REMOVED
  import org.springframework.boot.autoconfigure.SpringBootApplication;  // → REMOVED
  
  @SpringBootApplication
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);  // → REMOVED
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Model entities (S02 already complete)
- Service interfaces (S03 already complete)
- Service implementations (S04 already complete)
- Test files remain as-is (behavior contracts tested)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- **CartEndpoint** — REDESIGN (REST endpoint)
  - Role: JAX-RS REST controller converted to Quarkus JAX-RS with session-scoped cart access
  - Target runtime contract:
    - **Concurrency**: stateless endpoint with thread-safe session-scoped service injection
    - **Resource policy**: GET returns **404** on missing cartId (never creates cart for read operations) - **DELIBERATE DEPARTURE** from legacy
    - **Input validation**: reject with **400** (problem-detail) for negative quantities, null/empty itemId
    - **Error mapping**: **503** via JAX-RS **ExceptionMapper** for catalog service failures (never raw 500)
    - **API contract**: POST operations are idempotent for same cartId/itemId/quantity combinations

- **JerseyConfig** — REMOVED
  - Role: Jersey configuration eliminated
  - Target: Quarkus auto-discovers JAX-RS resources via @Path and @ApplicationScoped annotations

- **CartServiceApplication** — REMOVED
  - Role: Spring Boot application bootstrap eliminated  
  - Target: Quarkus bootstrap and CDI container replaces Spring Boot application startup

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **jakarta-jaxrs-to-quarkus-00010**: Replace jakarta JAX-RS dependency → `quarkus-rest` dependency
- **springboot-di-to-quarkus-00003**: Replace Spring DI with Quarkus CDI
  - CartEndpoint: @RestController → @Path, @Autowired → constructor injection
- **springboot-annotations-to-quarkus-00000**: Replace SpringBootApplication → delete main class and @SpringBootApplication

## Contracts owned by this story

- **Findings**: jakarta-jaxrs-to-quarkus-00010, springboot-di-to-quarkus-00003 (CartEndpoint, JerseyConfig), springboot-annotations-to-quarkus-00000
- **Preserve**: None - this is the final surface layer
- **Behavioral pins** (contract changes from architecture-profile §7):
  - **GET idempotency contract**: GET /cart/{cartId} returns 404 for missing cart (legacy would create empty cart)
  - **Input validation contract**: POST requests with negative quantities/empty itemId return 400 Bad Request
  - **Error mapping contract**: Catalog service failures return 503 Service Unavailable via ExceptionMapper (never 500 Internal Server Error)
  - **Spring Boot removal**: No @SpringBootApplication or main() method - Quarkus handles bootstrap
- **Contract gaps** (characterization tests added):
  - REST endpoint validation for malformed requests
  - Error mapping for service failures
  - GET operation idempotency (no cart creation on read)
- **Forbidden**: 
  - getMockProducts (tripwire from migration.yaml)
  - "mock products", "Mock products", "mock Products" (tripwire variants)
  - "Fallback to mock" (tripwire variant)

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- CartEndpoint converted to @Path with quarkus-rest dependency
- JerseyConfig completely removed (Quarkus auto-discovery works)
- CartServiceApplication and main() method removed
- GET /cart/{cartId} returns 404 for non-existent carts (DEPARTURE from legacy)
- Input validation rejects negative quantities with 400 status
- ExceptionMapper handles catalog service errors as 503
- All REST operations work through quarkus-rest (not spring-web)
- Integration tests pass with new error handling contracts
- Deploy milestone: factory pipeline green, deployed, `/q/health` serves, `/api/cart/*` endpoints functional
- Migration complete - all findings resolved

**Evidence-based update (S01 completion)**: Platform foundation provides quarkus-rest dependency. JAX-RS conversion from Spring @RestController can proceed. GET→404 idempotency contract implementation enabled by platform modernization completed in S01.
