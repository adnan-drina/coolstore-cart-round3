# S02: Core domain models (HARVEST)

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story modernizes the core domain models with minimal changes — these are HARVEST classes that must preserve their data structures and contracts exactly. The models are god nodes (Product fan-in: 4, ShoppingCartItem fan-in: 3) and must be stable reference points for all subsequent service modernization.

Position: **Second story** (depends on S01). Unlocks service layer conversion by providing stable model contracts that services can safely depend on.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — Product DTO
  ```java
  package com.redhat.coolstore.model;
  
  import javax.xml.bind.annotation.XmlRootElement; // WILL CHANGE
  import javax.xml.bind.annotation.XmlElement; // WILL CHANGE
  
  @XmlRootElement
  public class Product {
      private String id;
      private String name;
      private String description;
      private double price;
      // getters/setters
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — Promotion configuration
  ```java
  package com.redhat.coolstore.model;
  
  public class Promotion {
      private String itemId;
      private int percentOff;
      // constructor, getters
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — Shopping cart line item
  ```java
  package com.redhat.coolstore.model;
  
  public class ShoppingCartItem {
      private Product product;
      private int quantity;
      private double price; // computed
      // constructor, getters
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **Services** (PromoService, ShippingService, etc.) — remain Spring `@Component` until S03
- **ShoppingCart** model — converted in S04 as part of aggregate modernization
- **CartEndpoint** — remains Spring `@RestController` until S05
- **Test files** — remain unchanged until S06 validation

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `Product` — **HARVEST**
  - Target: Data transfer object carrying catalog item information; preserved as-is with serialization compatibility
  
- `Promotion` — **HARVEST**
  - Target: Configuration object holding promotion rules; preserved as immutable record-like structure
  
- `ShoppingCartItem` — **HARVEST**
  - Target: Value object representing line items; preserved with enhanced constructor and validation

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `javax-to-jakarta-import-00001` (recipe) — Replace `javax.*` imports with `jakarta.*`:
  - `javax.xml.bind.annotation.XmlRootElement` → `jakarta.xml.bind.annotation.XmlRootElement`
  - `javax.xml.bind.annotation.XmlElement` → `jakarta.xml.bind.annotation.XmlElement`

## Contracts owned by this story

- **Findings**: None (javax-to-jakarta-import-00001 is recipe-executed and handled automatically)
- **Preserve**: None — models are pure data structures
- **Behavioral pins**: 
  - Product serialization contract must remain exactly the same
  - ShoppingCartItem price computation logic unchanged
  - Promotion configuration structure unchanged
- **Forbidden**: None specific to this story

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All model classes compile successfully with jakarta imports
- Product, Promotion, ShoppingCartItem have identical JSON/XML serialization
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- deploy story only: factory pipeline green, deployed, acceptance path
  serving

**HARVEST Fidelity Guarantee**: These classes maintain 100% behavioral compatibility — no business logic changes, only import updates. Characterization tests in S04-S05 will verify legacy values are preserved.
