# S04: Service implementations and domain logic

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts service implementations to Quarkus CDI with constructor injection and implements thread-safe state management for the cart service. The core business logic must be preserved while modernizing the dependency injection pattern from Spring @Autowired to CDI constructor injection.

Position: S04 depends on S03 (service interfaces) and precedes S05 (REST endpoints). Dependency order: ShoppingCartService → ShoppingCartServiceImpl, PromoService, ShippingService.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — main cart service (REDESIGN)
  ```java
  import org.springframework.beans.factory.annotation.Autowired;  // → CDI constructor injection
  
  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      @Autowired
      private PromoService promoService;
      
      @Autowired  
      private ShippingService shippingService;
      
      @Autowired
      private CatalogService catalogService;
      
      private Map<String, ShoppingCart> carts = new HashMap<>();  // → ConcurrentHashMap
      // Thread-safe state management required per architecture-profile §7 target contract
  }
  ```

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — promotion calculation
  ```java
  import org.springframework.beans.factory.annotation.Autowired;  // → CDI constructor injection
  
  @Component
  public class PromoService {
      private static final Map<String, Promotion> PROMOS = Map.of(
          "329299", new Promotion("329299", 25.0)
      );
      // Static promotion data → thread-safe access pattern
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — shipping cost calculation
  ```java
  import org.springframework.beans.factory.annotation.Autowired;  // → CDI constructor injection
  
  @Component  
  public class ShippingService {
      // Stateless service → no mutable state
      public double calculateShipping(double cartTotal) {
          if (cartTotal >= 100) return 10.99;
          if (cartTotal >= 75) return 8.99;
          if (cartTotal >= 50) return 6.99;
          if (cartTotal >= 25) return 4.99;
          return 2.99;
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- REST endpoints (S05 handles CartEndpoint, JerseyConfig)
- Model entities (S02 already converted to jakarta imports)
- Service interfaces (S03 already converted)
- Spring Boot configuration (S01 already modernized)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- **ShoppingCartServiceImpl** — REDESIGN (service implementation)
  - Role: service implementation converted to CDI @ApplicationScoped with constructor injection
  - Target runtime contract:
    - **Concurrency**: `ConcurrentHashMap<String, ShoppingCart>` with `compute()` operations for thread-safe cart access
    - **Resource policy**: no cache clear-on-miss; bounded refresh when catalog data updates
    - **Aggregate math**: `normalizeBeforeDerive` — dedupe cart items before pricing calculations
    - **API contract**: all mutations validated; catalog failures surface as service-level exceptions for ExceptionMapper handling

- **PromoService** — REDESIGN (service component)
  - Role: promotion calculation component converted to CDI with thread-safe promotion data access
  - Target runtime contract:
    - **Concurrency**: static promotion data accessed via thread-safe collections (ConcurrentHashMap for lookup)
    - **Cache policy**: promotion set loaded once at construction, immutable thereafter

- **ShippingService** — REDESIGN (service component)
  - Role: shipping calculation component converted to CDI with deterministic shipping calculation logic
  - Target runtime contract:
    - **Concurrency**: stateless service with no mutable state
    - **Business logic**: tiered shipping calculation preserved exactly as legacy (2.99/4.99/6.99/8.99/10.99 thresholds with free shipping ≥75)

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **springboot-di-to-quarkus-00003**: Replace Spring DI with Quarkus CDI
  - ShoppingCartServiceImpl: @Service → @ApplicationScoped, constructor injection for promoService, shippingService, catalogService
  - PromoService: @Component → @ApplicationScoped, constructor injection (no dependencies)
  - ShippingService: @Component → @ApplicationScoped, constructor injection (no dependencies)
  - Target: native CDI constructor injection (NOT the spring-di extension)

## Contracts owned by this story

- **Findings**: springboot-di-to-quarkus-00003 (ShoppingCartServiceImpl, PromoService, ShippingService)
- **Preserve**: None - service implementations have no environment configuration
- **Behavioral pins** (from ShoppingCartServiceTest):
  - Cart initialization contract (test line 28): New carts return zero totals for all monetary fields
  - Pricing calculation contract (test line 38): Cart with 2 items at $1000 each returns cartItemTotal: 2000.0, cartTotal: 2000.0, shippingPromoSavings: -10.99
  - Product retrieval contract (test line 57): Catalog service mock returns specific Product with itemId "2222", name "Bike", desc "Super bike", price 200
  - Shipping tiers preserved: <$25: $2.99, $25-$49.99: $4.99, $50-$74.99: $6.99, $75-$99.99: $8.99, $100+: $10.99 (free shipping ≥75: $0.00)
- **Contract gaps** (characterization tests needed):
  - Error handling when catalog service unavailable
  - Input validation for negative quantities/invalid item IDs
  - Concurrent access patterns for multi-user scenarios
- **Forbidden**: None applicable

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All services converted to @ApplicationScoped with constructor injection (no @Autowired)
- ShoppingCartServiceImpl uses ConcurrentHashMap with compute() operations
- PromoService loads promotions once, immutable thereafter
- ShippingService shipping calculation preserved exactly
- All existing tests pass with CDI injection
- Business logic behavior preserved: cart totals, promotions, shipping tiers
- Characterization tests added for contract gaps (catalog errors, input validation, concurrency)
- Service layer compiles and functions independently of REST layer
