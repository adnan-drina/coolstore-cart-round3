# S02: Core model entities and catalog integration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story harvests the core data-model classes faithfully (package rename
only) and pins their behavior with characterization tests. The god-node
classes (Product, ShoppingCart, ShoppingCartItem) are referenced across all
service operations and must have their behavior pinned before dependent
services convert.

Position: S02 depends on S01 (platform foundation) and precedes S03 (service
interfaces). Conversion order from dependency analysis: Product (god-node),
Promotion, CartServiceApplication, ShoppingCartItem (god-node), ShoppingCart
(god-node).

## In scope

The exact legacy classes/files this story modernizes, quoting the real
staged source (verified against /projects/legacy and migration/staging).
These are PLAIN POJOs — an in-memory cart service (`needsDatabase: false`),
NO JPA. The only transform is the package rename com.redhat.coolstore →
com.demo (there are no javax imports to rename):

- `src/main/java/com/redhat/coolstore/model/Product.java` — product catalog value object
  ```java
  import java.io.Serializable;
  public class Product implements Serializable {
      private static final long serialVersionUID = -7304814269819778382L;
      private String itemId;
      private String name;
      private String desc;
      private double price;
      // no-arg + full constructors, getters, toString()
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — cart line item (god-node)
  ```java
  import java.io.Serializable;
  public class ShoppingCartItem implements Serializable {
      private static final long serialVersionUID = 6964558044240061049L;
      private double price;
      private int quantity;
      private double promoSavings;
      private Product product;   // plain object reference, NOT a JPA relationship
      // constructors, accessors, toString()
  }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — cart with pricing fields (god-node)
  ```java
  import java.io.Serializable;
  import java.util.ArrayList;
  import java.util.List;
  public class ShoppingCart implements Serializable {
      private static final long serialVersionUID = -1108043957592113528L;
      private double cartItemTotal, cartItemPromoSavings, shippingTotal, shippingPromoSavings, cartTotal;
      private String cartId;
      private List<ShoppingCartItem> shoppingCartItemList = new ArrayList<ShoppingCartItem>();  // plain in-memory list
      // constructors, getters/setters, toString()
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — promotional discount rule
  ```java
  public class Promotion {          // bare POJO, NOT Serializable
      private String itemId;
      private double percentOff;
      // constructors, getters, toString()
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
- Any persistence/JPA/datasource wiring — this is an in-memory service; models are POJOs

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
  - Role: value object capturing cart state including item list and pricing fields
  - Preserved behavior: maintains cart totals, holds the item list, exposes pricing fields
- **Promotion** — HARVEST
  - Role: value object capturing promotional discount rules
  - Preserved behavior: stores promotion metadata for cart item discount application

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **Faithful harvest [package rename]**: the models carry no javax imports and
  no annotations. Copy each from `migration/staging` with the package rename
  com.redhat.coolstore → com.demo via `harvest-from-staging.sh`. No JPA, no
  new fields, no `implements` changes.

## Contracts owned by this story

- **Findings**: javax-to-jakarta-import-00001 (0 incidents in the models — no javax imports present)
- **Preserve**: None — entity classes have no environment configuration
- **Behavioral pins** (characterization test contracts from architecture-profile §4):
  - Product creation: itemId, name, desc, price fields preserved exactly
  - ShoppingCartItem: price/quantity/promoSavings state + the `product` reference preserved
  - ShoppingCart totals: cartItemTotal, cartItemPromoSavings, shippingTotal, shippingPromoSavings, cartTotal fields maintained; item list initialized non-null
  - Cart initialization contract: new carts return zero totals (cartItemPromoSavings 0.0, cartItemTotal 0.0, shippingPromoSavings 0.0, cartTotal 0.0)
  - Models remain plain POJOs — no persistence behavior
  - No changes to service or endpoint classes

  **Evidence-based update (S01 completion)**: Platform foundation completed successfully. The models require only the package rename; there are no javax imports to convert. Platform modernization enables S02 harvest without dependency issues.

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- The 4 models harvested to com.demo as faithful POJOs (fidelity sensor green)
- No JPA annotations, no persistence dependency introduced
- Characterization tests added for god-node behavior (ShoppingCart, Product, ShoppingCartItem)
- Product → ShoppingCartItem → ShoppingCart dependency order maintained
- No changes to service or endpoint classes
