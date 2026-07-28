# S02: Core model entities and catalog integration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story stabilizes the core data model entities with Jakarta imports and characterization tests for god-node behavior. The god-node classes (Product, ShoppingCart, ShoppingCartItem) are referenced across all service operations and must have their behavior pinned before dependent services convert.

Position: S02 depends on S01 (platform foundation) and precedes S03 (service interfaces). Conversion order from dependency analysis: Product (god-node), Promotion, CartServiceApplication, ShoppingCartItem (god-node), ShoppingCart (god-node).

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — entity for product catalog information
  ```java
  import javax.persistence.Entity;  // → jakarta.persistence.Entity
  import javax.persistence.Id;
  import javax.persistence.Table;
  
  @Entity
  @Table(name = "PRODUCT")
  public class Product {
      @Id
      private String itemId;
      private String name;
      private String desc;
      private double price;
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — cart line items (god-node)
  ```java
  import javax.persistence.Entity;  // → jakarta.persistence.Entity
  import javax.persistence.OneToOne;
  
  @Entity
  public class ShoppingCartItem {
      @Id
      private String productId;
      private int quantity;
      private double price;
      private double promoSavings;
      @OneToOne
      private Product product;
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — cart entity with pricing fields (god-node)
  ```java
  import javax.persistence.Entity;  // → jakarta.persistence.Entity
  import javax.persistence.OneToMany;
  
  @Entity
  public class ShoppingCart {
      @Id
      private String cartId;
      @OneToMany
      private List<ShoppingCartItem> shoppingCartItemList;
      private double cartItemTotal;
      private double cartItemPromoSavings;
      private double shippingCosts;
      private double shippingPromoSavings;
      private double cartTotal;
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — promotional discount rules
  ```java
  import javax.persistence.Entity;  // → jakarta.persistence.Entity
  
  @Entity
  public class Promotion {
      private String itemId;
      private double percentOff;
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service implementations (S04 handles ShoppingCartServiceImpl, PromoService, ShippingService)
- REST endpoints (S05 handles CartEndpoint, JerseyConfig, CartServiceApplication)
- Catalog service integration points (S03 handles CatalogService)
- JPA table creation code (removed in S01 via BOM)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- **Product** — HARVEST (data/DTO/value-object/pure-utility)
  - Role: pure data transfer object representing catalog product information
  - Preserved behavior: maintains itemId, name, description, and price fields
- **ShoppingCartItem** — HARVEST (god-node)
  - Role: value object representing individual cart line items
  - Preserved behavior: maintains line item pricing state and quantity management
- **ShoppingCart** — HARVEST (god-node)
  - Role: entity capturing cart state including item list, pricing fields
  - Preserved behavior: maintains cart totals, applies promotional calculations, manages item lifecycle
- **Promotion** — HARVEST
  - Role: value object capturing promotional discount rules
  - Preserved behavior: stores promotion metadata for cart item discount application

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **javax-to-jakarta-import-00001 [recipe]**: The package 'javax' has been replaced by 'jakarta' → jakarta.* imports
  - Product.java: Entity, Id, Table imports
  - ShoppingCartItem.java: Entity, OneToOne imports  
  - ShoppingCart.java: Entity, OneToMany imports
  - Promotion.java: Entity import

## Contracts owned by this story

- **Findings**: javax-to-jakarta-import-00001
- **Preserve**: None - entity classes have no environment configuration
- **Behavioral pins** (characterization test contracts from architecture-profile §4):
  - Product creation: itemId, name, desc, price fields preserved exactly
  - ShoppingCartItem pricing: quantity × price - promoSavings calculation preserved
  - ShoppingCart totals: cartItemTotal, cartItemPromoSavings, shippingCosts, shippingPromoSavings, cartTotal fields maintained
  - Cart initialization contract (test line 28): New carts return zero totals (cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0)
  - Entity classes compile and basic JPA operations work
  - No changes to service or endpoint classes

  **Evidence-based update (S01 completion)**: Platform foundation completed successfully. All javax-to-jakarta recipe transforms confirmed complete in migration/recipe-log.md. Entity classes now compile with jakarta.* imports. Platform modernization enables S02 entity conversion without dependency issues.

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All javax.* imports replaced with jakarta.* imports in entity classes
- Existing tests continue to pass with jakarta imports
- Characterization tests added for god-node behavior (ShoppingCart, Product, ShoppingCartItem)
- Product → ShoppingCartItem → ShoppingCart dependency order maintained
- Entity classes compile and basic JPA operations work
- No changes to service or endpoint classes
