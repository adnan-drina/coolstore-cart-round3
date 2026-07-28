# S03: Core services modernization (REDESIGN)

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts the core pricing services to Quarkus-native CDI. These stateless services are inherently thread-safe and provide an ideal introduction to REDESIGN patterns. They demonstrate constructor injection, @ApplicationScoped lifecycle, and thread-safe design without the complexity of mutable state.

Position: **Third story** (depends on S02). Unlocks the cart aggregate modernization by providing stable, CDI-compliant service interfaces that ShoppingCartServiceImpl can depend on.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — Promotion pricing rules
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.beans.factory.annotation.Autowired; // WILL CHANGE
  import org.springframework.stereotype.Component; // WILL CHANGE
  
  @Component
  public class PromoService {
      private final Set<Promotion> promotions = new HashSet<>(); // immutable after init
      
      @Autowired // WILL BE REMOVED - constructor injection
      public PromoService() {
          // initialize promotions
      }
      
      public double calculateDiscount(ShoppingCartItem item) {
          // business logic
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — Shipping cost calculation
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.beans.factory.annotation.Autowired; // WILL CHANGE
  import org.springframework.stereotype.Component; // WILL CHANGE
  
  @Component
  public class ShippingService {
      
      @Autowired // WILL BE REMOVED - no dependencies
      public ShippingService() {
      }
      
      public double calculateShipping(ShoppingCart cart) {
          // business logic
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **ShoppingCartServiceImpl** — remains with Spring `@Autowired` field injection until S04
- **CatalogService** — remains Spring `@Component` until S05
- **CartEndpoint** — remains Spring `@RestController` until S05
- **ShoppingCart model** — converted in S04 as part of aggregate
- **Test files** — unchanged until S06 validation

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `PromoService` — **REDESIGN**
  - Target: CDI `@ApplicationScoped` bean with constructor injection
  - Concurrency: Immutable promotion set with thread-safe reads; writes occur only during initialization
  - Resource policy: Promotion rules loaded at startup; no runtime modification expected
  
- `ShippingService` — **REDESIGN**
  - Target: CDI `@ApplicationScoped` bean with constructor injection  
  - Concurrency: Stateless service with no mutable state; inherently thread-safe
  - Resource policy: No caching or external resource usage

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `springboot-di-to-quarkus-00003` (infer) — Apply Quarkus Spring DI conversion guidance:
  - Remove `@Component` → Add `@ApplicationScoped`
  - Remove `@Autowired` field injection → Add constructor injection
  - `import org.springframework.stereotype.Component` → `import jakarta.enterprise.context.ApplicationScoped`
  - `import org.springframework.beans.factory.annotation.Autowired` → `import jakarta.inject.Inject`

## Contracts owned by this story

- **Findings**: None specific to these services (springboot-di-to-quarkus-00003 handled in S01)
- **Preserve**: None specific to these services
- **Behavioral pins**: 
  - PromoService.calculateDiscount() returns identical values for same inputs
  - ShippingService.calculateShipping() returns identical values for same cart state
  - No behavioral changes to pricing logic
- **Forbidden**: None specific to this story

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- PromoService and ShippingService compile as CDI @ApplicationScoped beans
- Constructor injection works correctly (no @Autowired needed)
- Service methods return identical values to legacy implementation
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- deploy story only: factory pipeline green, deployed, acceptance path
  serving

**Thread-Safe Design**: These stateless services demonstrate the REDESIGN pattern — @ApplicationScoped lifecycle with constructor injection and inherent thread-safety through immutability.
