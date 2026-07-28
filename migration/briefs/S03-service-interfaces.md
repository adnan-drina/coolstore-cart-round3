# S03: Service interfaces and catalog client

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story establishes service interface contracts and configures the catalog service integration with environment-driven configuration. The interfaces define the behavioral contracts that implementations must preserve, and the catalog service URL must be preserved as ${CATALOG_ENDPOINT} per the migration.yaml preserve contract.

Position: S03 depends on S02 (core entities) and precedes S04 (implementations). Dependency order: Product → ShoppingCartItem → ShoppingCart → ShoppingCartService → CatalogService.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — service interface defining cart operations
  ```java
  import javax.enterprise.context.ApplicationScoped;  // → jakarta.enterprise.context.ApplicationScoped
  
  @ApplicationScoped
  public interface ShoppingCartService {
      ShoppingCart createShoppingCart(String cartId);
      ShoppingCart getShoppingCart(String cartId);
      ShoppingCart addItem(String cartId, String itemId, int quantity);
      ShoppingCart removeItem(String cartId, String itemId, int quantity);
      ShoppingCart removeAllItems(String cartId);
      ShoppingCart checkout(String cartId);
  }
  ```

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — Feign client for catalog service integration
  ```java
  import org.springframework.cloud.openfeign.FeignClient;  // → quarkus-rest-client
  import org.springframework.web.bind.annotation.GetMapping;  // → jakarta.ws.rs.GET
  
  @FeignClient(name = "catalog-service", url = "${CATALOG_ENDPOINT}")
  public interface CatalogService {
      @GetMapping("/api/products")
      List<Product> getProducts();
  }
  ```

- `src/main/resources/application.properties` — environment-driven configuration
  ```properties
  catalog.service.url=${CATALOG_ENDPOINT:http://localhost:8081}
  # This environment variable MUST be preserved per migration.yaml preserve: [CATALOG_ENDPOINT]
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service implementations (S04 handles ShoppingCartServiceImpl, PromoService, ShippingService)
- REST endpoints (S05 handles CartEndpoint)
- JerseyConfig and CartServiceApplication (S05 handles bootstrap)
- Test files remain as-is (behavior preserved)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- **ShoppingCartService** — HARVEST (service interface)
  - Role: service interface defining cart operations contract
  - Preserved behavior: maintains all business operation signatures and contracts
- **CatalogService** — REDESIGN (REST client)
  - Role: REST client for catalog service integration
  - Target: Convert FeignClient to quarkus-rest-client with environment-driven URL
  - Environment contract: URL driven by ${CATALOG_ENDPOINT} variable, default fallback preserved

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **javax-to-jakarta-import-00001 [recipe]**: javax.* → jakarta.* imports
  - ShoppingCartService.java: javax.enterprise.context → jakarta.enterprise.context
  
- **localhost-http-00001**: Local HTTP Calls → cloud-readiness: env-driven config
  - application.properties: hardcoded localhost → ${CATALOG_ENDPOINT} substitution
  
- **demo-env-integration-00001**: Environment-driven external configuration preserved
  - Maintain ${CATALOG_ENDPOINT} environment variable configuration
  - Target: `quarkus.rest-client.catalog-service.url` key in application.properties

## Contracts owned by this story

- **Findings**: localhost-http-00001, demo-env-integration-00001
- **Preserve**: CATALOG_ENDPOINT environment variable configuration (from migration.yaml)
  - Environment variable name: CATALOG_ENDPOINT
  - Default fallback: http://localhost:8081 (preserved)
  - Configuration location: application.properties
- **Behavioral pins** (interface contract preservation):
  - ShoppingCartService interface: all method signatures preserved exactly
  - Cart lifecycle operations: create, get, addItem, removeItem, removeAllItems, checkout
  - Catalog service integration: getProducts() returns List<Product> with same Product entity structure
- **Forbidden**: None applicable to interfaces

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- ShoppingCartService interface compiles with jakarta.* imports
- CatalogService converted to quarkus-rest-client with @RegisterRestClient annotation
- application.properties maintains ${CATALOG_ENDPOINT} environment configuration
- Interface methods preserved exactly - no behavioral changes
- Implementation classes (S04) can compile against these interfaces
- Environment-driven configuration test passes (catalog service URL configurable)
