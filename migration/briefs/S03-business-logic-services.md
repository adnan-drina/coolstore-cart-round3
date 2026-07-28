# S03: Core business logic services

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts the core business logic services that provide promotion and shipping calculations for the cart service. These services implement the deterministic pricing behavior that ShoppingCartService depends on, establishing the calculation patterns needed before cart orchestration conversion.

This story follows S02 because it depends on the converted service interfaces (ShoppingCartService) and precedes S04 where ShoppingCartServiceImpl will be converted to use these business logic services. Architecture-profile §7 emphasizes thread-safe promotion calculations and deterministic shipping rates.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — promotion calculation service
  ```java
  // Spring @Component annotation and field injection to CDI conversion
  @Component
  public class PromoService {
      @Autowired
      private CatalogService catalogService;
      
      public double getProductPromoSavings(String productId, int quantity) {
          // promotion calculation logic
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — shipping calculation service
  ```java
  // Spring @Component annotation and field injection to CDI conversion  
  @Component
  public class ShippingService {
      @Autowired
      private PromoService promoService;
      
      public double calculateShipping(ShoppingCart cart) {
          // shipping calculation logic with tiered rates
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- ShoppingCartServiceImpl remains in Spring DI with field injection until S04
- CartEndpoint remains in Spring Web @RestController until S04
- Data models remain as converted in S01-S02
- Service interfaces remain as converted in S02

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `PromoService` — REDESIGN
  - Role: Promotion calculation service
  - Target: CDI managed bean with constructor injection; thread-safe promotion calculations, no mutable state

- `ShippingService` — REDESIGN  
  - Role: Shipping calculation service
  - Target: CDI managed bean with constructor injection; deterministic shipping rate calculations

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **springboot-di-to-quarkus-00003 [infer]**: Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
  - Target: native CDI constructor injection (NOT the spring-di extension)
- **javax-to-jakarta-import-00001 [recipe]**: Convert all `javax.*` imports to `jakarta.*`

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-di-to-quarkus-00003

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - None - environment-driven configuration handled in S02 (CatalogService)

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - PromoService product promotion savings calculations preserved (e.g., 10% discount logic)
  - ShippingService tiered shipping rates preserved (e.g., free shipping ≥ $75, otherwise $5.99, $9.99, etc.)
  - Dependency injection between PromoService and ShippingService preserved
  - ShoppingCartServiceTest.java pricing behavior must continue to pass
  - Contract GAPS: Thread safety verification needed for concurrent access scenarios

- **Forbidden**: the fabrication tripwires relevant here.
  - None specific to business logic services

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - PromoService converted to CDI with constructor injection
  - ShippingService converted to CDI with constructor injection
  - Promotion and shipping calculation logic preserved exactly
  - Existing pricing tests in ShoppingCartServiceTest continue to pass
  - No Spring @Component or @Autowired annotations remain on converted services
  - Thread safety verified through concurrent access scenarios
- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - Not applicable for S03 - business logic services only, not yet exposed via REST