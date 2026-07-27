# S01: Domain model migration

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story converts the domain model layer to be Quarkus-compatible. It is the first story in the modernization roadmap because the god nodes (ShoppingCart with fan-in 5, Product with fan-in 3) must be converted first per dependency-order.md to enable downstream service layer migration without compilation issues.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — Central domain entity with pricing totals and cart operations
  ```java
  package com.redhat.coolstore.model;

  import java.io.Serializable;
  import java.util.ArrayList;
  import java.util.List;

  public class ShoppingCart implements Serializable {
      private static final long serialVersionUID = -1108043957592113528L;
      private double cartItemTotal;
      private double cartItemPromoSavings;
      private double shippingTotal;
      private double shippingPromoSavings;
      private double cartTotal;
      private String cartId;
      private List<ShoppingCartItem> shoppingCartItemList = new ArrayList<ShoppingCartItem>();
      
      // Constructor, getters, setters, and business methods
  }
  ```

- `src/main/java/com/redhat/coolstore/model/Product.java` — Product data model used by cart items and catalog service
- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — Individual cart items with quantity and pricing
- `src/main/java/com/redhat/coolstore/model/Promotion.java` — Promotion rules for discounts

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Service layer classes (ShoppingCartServiceImpl, PromoService, ShippingService) — owned by S02
- REST endpoint (CartEndpoint) — owned by S03
- Configuration and application bootstrap — owned by S03

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

Domain models are POJOs with no mandatory changes per architecture-profile.md:110. However, javax.* imports in ShoppingCartServiceImpl were already converted to jakarta.* via the recipe execution (recipe-log.md:7).

## Contracts owned by this story

- **Findings**: springboot-di-to-quarkus-00003 (partial instances on ShoppingCartItem, Product, Promotion classes)
- **Preserve**: No preserve items directly in scope — cart operations preserved via business logic in services
- **Behavioral pins**: The legacy assertion values that must hold
  after this story (quote numbers/strings and their test source), and
  the contract GAPS this story closes with characterization tests.
  - **KEY ASSERTION** (ShoppingCartServiceTest:49-53): Cart with 2x $1000 items = $2000 cart item total; shipping promotion applies -$10.99 discount; final cart total = $2000 (item total + shipping total $0 after promotion)
  - **INTEGRATION ASSERTION** (CartServiceBoundaryTest:38-45): Same pricing logic validated at boundary level
  - **Contract Gap**: Shipping tier calculations need characterization test coverage
- **Forbidden**: No fabrication tripwires in domain models

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- Characterization tests validate ShoppingCart pricing behavior with 2x $1000 items yielding $2000 cart total and proper shipping promotion application
- Shipping tier calculations covered: $0-25=$2.99, $25-50=$4.99, $50-75=$6.99, $75-100=$8.99, $100+=$10.99
- All domain model classes maintain serialization compatibility for existing REST API consumers
- No new code duplication; POJO patterns consolidated where appropriate
