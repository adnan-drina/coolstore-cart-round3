# S02: Service interfaces and external integration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts service interfaces and external integration layer, establishing the CDI patterns needed by downstream implementations. The service interface conversion enables dependency injection wiring, while CatalogService conversion establishes the REST client pattern for external product catalog access.

This story follows S01 because it depends on the converted data models (Product, ShoppingCartItem) and precedes S03-S04 where services will be implemented and consumed. Architecture-profile §7 designates CatalogService as having the external integration role that must be modernized before cart operations.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — core cart operations interface
  ```java
  // Spring @Service annotation to CDI conversion
  public interface ShoppingCartService {
      ShoppingCart createShoppingCart(String cartId);
      ShoppingCart addToCart(String cartId, String itemId, int quantity);
      ShoppingCart removeFromCart(String cartId, String itemId, int quantity);
      ShoppingCart getShoppingCart(String cartId);
      ShoppingCart checkout(String cartId);
  }
  ```

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — external catalog integration via Feign client
  ```java
  // Feign client to REST Client conversion
  @FeignClient(name = "catalog", url = "${CATALOG_ENDPOINT}")
  public interface CatalogService {
      @RequestLine("GET /products")
      List<Product> products();
      
      @RequestLine("GET /products/{id}")
      Product getProduct(@Param("id") String id);
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — promotion data structure with javax→jakarta imports
  ```java
  public class Promotion {
      private String productId;
      private double discount;
      private String promoCode;
      // getters, setters
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- ShoppingCartServiceImpl remains in Spring DI with field injection until S04
- PromoService and ShippingService remain as Spring @Component beans until S03
- CartEndpoint remains in Spring Web @RestController until S04
- javax→jakarta import conversion for Promotion only (not other model classes handled in S01)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `ShoppingCartService` — REDESIGN
  - Role: Cart operations interface
  - Target: CDI managed bean interface with idempotent read operations, consistent pricing calculations, normalized-before-deriving cart totals

- `CatalogService` — REDESIGN  
  - Role: External catalog integration via Feign client
  - Target: REST Client with environment-driven URL configuration; target contract: product catalog access with retry/timeout policies

- `Promotion` — HARVEST
  - Role: Promotion data structure
  - Target: preserve existing structure and behavior; only javax→jakarta import conversion

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **springboot-di-to-quarkus-00003 [infer]**: Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
  - Target: native CDI constructor injection (NOT the spring-di extension)
- **demo-env-integration-00001 [infer]**: Environment-driven external configuration must be preserved
  - Target: the surface IS the preserve contract: record under migration.yaml `preserve:`; target keeps env-driven config (`${VAR:default}` / `quarkus.rest-client.<key>.url`)
- **localhost-http-00001 [infer]**: Local HTTP Calls → cloud-readiness: hardcoded/localhost service URLs → env-driven config
- **javax-to-jakarta-import-00001 [recipe]**: Convert all `javax.*` imports to `jakarta.*`

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - demo-env-integration-00001
  - localhost-http-00001
  - springboot-di-to-quarkus-00003

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - **CATALOG_ENDPOINT**: Environment-driven external configuration must be preserved
    - Mechanism: `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT:default}`
    - Config source: `application.properties` with `${CATALOG_ENDPOINT:http://localhost:8081}`

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - CatalogService interface preserves exact method signatures (products(), getProduct(String id))
  - Environment-driven configuration maintains the same catalog endpoint behavior
  - Product list caching behavior preserved (bounded productMap)
  - Contract GAPS: No explicit timeout/retry policy established (deferred to S04 service implementation)

- **Forbidden**: the fabrication tripwires relevant here.
  - None specific to service interfaces

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - ShoppingCartService interface converted to CDI without implementation
  - CatalogService converted to REST Client with environment-driven config
  - Promotion model converted with javax→jakarta imports only
  - CATALOG_ENDPOINT preserve contract tested and verified
  - No Spring annotations remain on converted interfaces
- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - Not applicable for S02 - service interfaces and integration layer only