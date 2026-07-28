# S04: Cart aggregate and service (REDESIGN)

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story modernizes the cart aggregate — the heart of the application. ShoppingCart is the central god-node (fan-in: 5) containing mutable state and complex business logic. This is the most critical REDESIGN as it implements thread-safe state management, synchronized pricing operations, and the "normalize-before-derive" pattern from architecture-profile §7.

Position: **Fourth story** (depends on S03). This unblocks the REST endpoint modernization by providing a stable, thread-safe service interface that the HTTP layer can safely call.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — Central aggregate
  ```java
  package com.redhat.coolstore.model;
  
  public class ShoppingCart {
      private String cartId;
      private List<ShoppingCartItem> cartItem;
      private double cartItemTotal;
      private double shippingTotal;
      private double cartTotal;
      private double shippingPromoSavings;
      // basic constructors, getters
  }
  ```

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — Business logic
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.beans.factory.annotation.Autowired; // WILL CHANGE
  import org.springframework.stereotype.Service; // WILL CHANGE
  
  @Service
  public class ShoppingCartServiceImpl implements ShoppingCartService {
      
      @Autowired
      private PromoService promoService; // WILL CHANGE
      
      @Autowired
      private ShippingService shippingService; // WILL CHANGE
      
      @Autowired
      private CatalogService catalogService; // WILL CHANGE
      
      @Autowired
      private ShoppingCartServiceImpl thisInstance; // WILL BE REMOVED
      
      private Map<String, ShoppingCart> carts = new HashMap<>(); // WILL BECOME ConcurrentHashMap
      
      public ShoppingCart addToCart(String cartId, String productId, int quantity) {
          // NORMALIZE BEFORE DERIVE pattern: normalize items first
          ShoppingCart cart = getShoppingCart(cartId);
          // add logic
          calculateCartTotals(cart); // then derive totals
          return cart;
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **ShoppingCartService interface** — will be removed entirely per §7 target
- **CatalogService** — remains Spring `@Component` until S05
- **CartEndpoint** — remains Spring `@RestController` until S05
- **Test files** — unchanged until S06 validation

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `ShoppingCart` — **REDESIGN**
  - Target: Enhanced constructors and validation per HARVEST-with-enhancement pattern
  - Concurrency: Immutable value object for read operations; mutable only within service context
  - Aggregate pattern: Contains cart items and derived totals with guaranteed consistency
  
- `ShoppingCartServiceImpl` — **REDESIGN**
  - Target: CDI `@ApplicationScoped` bean with constructor injection (interface eliminated)
  - Concurrency: Mutable state in shared HashMap requires thread-safety (ConcurrentHashMap, synchronized pricing operations)
  - Resource/cache policy: Product map cache refresh on miss; needs explicit cache eviction or size bounds
  - Aggregate/derived math: **NORMALIZE CART ITEMS BEFORE PRICING DERIVATIONS** to ensure totals consistency
  - API contract: All methods remain idempotent; `checkout()` clears cart items but maintains cart record for session continuity

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `springboot-di-to-quarkus-00003` (infer) — Apply Quarkus Spring DI conversion guidance:
  - Remove `@Service` → Add `@ApplicationScoped`
  - Remove `@Autowired` field injection → Add constructor injection
  - `import org.springframework.stereotype.Service` → `import jakarta.enterprise.context.ApplicationScoped`
  - `import org.springframework.beans.factory.annotation.Autowired` → `import jakarta.inject.Inject`
  - Remove self-injection (`thisInstance`) — not needed in proper CDI
  - `Map<String, ShoppingCart> carts = new HashMap<>()` → `Map<String, ShoppingCart> carts = new ConcurrentHashMap<>()`

**Thread-Safe Design Requirements:**
- All cart operations must be synchronized or use thread-safe collections
- Pricing calculations must be atomic
- Cart state must remain consistent under concurrent access

## Contracts owned by this story

- **Findings**: None specific to this story (springboot-di-to-quarkus-00003 handled in S01)
- **Preserve**: None specific to these classes
- **Behavioral pins**: 
  - Cart totals calculation must be identical: `cartItemTotal` = sum(item.price × quantity)
  - Free shipping promotion logic unchanged: ≥ $75 = free shipping ($10.99 savings)
  - Cart initialization behavior: non-existent carts return zero totals
  - Cart consistency: totals always reflect current cart items
- **Forbidden**: None specific to this story

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- ShoppingCart has enhanced constructors with validation
- ShoppingCartServiceImpl is CDI @ApplicationScoped with constructor injection
- ConcurrentHashMap prevents race conditions on cart storage
- All pricing calculations produce identical results to legacy
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- deploy story only: factory pipeline green, deployed, acceptance path
  serving

**Critical Path**: This is the heart of the application. The thread-safe design and normalize-before-derive pattern are essential for production readiness.
