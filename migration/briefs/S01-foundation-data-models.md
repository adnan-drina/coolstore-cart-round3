# S01: Foundation data models with god-node characterization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts the core data structures that form the god nodes of the Coolstore cart service. These classes (Product, ShoppingCart, ShoppingCartItem) have the highest fan-in in the dependency graph (5, 4, and 3 incoming references respectively) and must be characterized before conversion to establish behavior baselines.

This story is first in the modernization sequence because these HARVEST classes serve as the foundation data layer that all other components depend upon. Converting them first ensures the tree compiles at every commit as specified in dependency-order.md.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — product data structure with id, name, description, price fields
  ```java
  // Original imports requiring javax→jakarta conversion
  package com.redhat.coolstore.model;
  
  public class Product {
      private String productId;
      private String name;
      private String description;
      private double price;
      // constructor, getters, setters
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — central cart data structure with pricing calculations
  ```java
  // Contains pricing fields and calculations that ShoppingCartServiceImpl depends on
  public class ShoppingCart {
      private String cartId;
      private List<ShoppingCartItem> cartItem;
      private double cartItemTotal;
      private double cartItemPromoSavings;
      private double shippingTotal;
      private double shippingPromoSavings;
      private double cartTotal;
      // pricing calculation methods
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — cart item container with product reference and quantity
  ```java
  public class ShoppingCartItem {
      private Product product;
      private int quantity;
      private double price;
      // getters, setters, pricing logic
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service layer classes (ShoppingCartService, ShoppingCartServiceImpl) remain in Spring DI until S02-S04
- REST endpoints (CartEndpoint) remain in Spring Web until S04
- External integration (CatalogService) remains as Feign client until S02

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `Product` — HARVEST
  - Role: Product data structure
  - Target: preserve existing structure and behavior; only javax→jakarta import conversion

- `ShoppingCart` — HARVEST  
  - Role: Cart data structure with pricing fields
  - Target: preserve existing structure and behavior; only javax→jakarta import conversion

- `ShoppingCartItem` — HARVEST
  - Role: Cart item data structure  
  - Target: preserve existing structure and behavior; only javax→jakarta import conversion

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **javax-to-jakarta-import-00001 [recipe]**: Convert all `javax.*` imports to `jakarta.*` 
  - Recipe executed automatically during migration
- **removed-javaee-modules-00020 [rewrite]**: JEE modules removed from JDK → provided by Quarkus platform dependencies

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - javax-to-jakarta-import-00001
  - removed-javaee-modules-00020

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - None - environment-driven configuration is handled in S02 (CatalogService)

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - ShoppingCartServiceTest.java assertions for cart totals must continue to pass
  - Product structure (id, name, description, price) preserved exactly
  - ShoppingCartItem pricing calculations (price × quantity → cartItemTotal) preserved

- **Forbidden**: the fabrication tripwires relevant here.
  - None specific to data models

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - All model classes convert with javax→jakarta imports only
  - Existing tests in ShoppingCartServiceTest continue to pass
  - Characterized god-node behavior preserved in converted classes
  - No new findings for Product, ShoppingCart, ShoppingCartItem classes
- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - Not applicable for S01 - foundation data models only