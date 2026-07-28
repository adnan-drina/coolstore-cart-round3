# S01 Foundation Data Models Tasks

#### T-001: Convert Product model to com.demo package
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Harvest Product.java with package restructuring
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java → src/main/java/com/demo/model/Product.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve: All fields (itemId, name, desc, price), constructors, getters/setters, toString(), Serializable
**Acceptance**: src/main/java/com/demo/model/Product.java; sensors green

#### T-002: Convert ShoppingCart model to com.demo package
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Harvest ShoppingCart.java with package restructuring
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java → src/main/java/com/demo/model/ShoppingCart.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve: All fields (cartId, shoppingCartItemList, pricing totals), constructors, methods
**Acceptance**: src/main/java/com/demo/model/ShoppingCart.java; sensors green

#### T-003: Convert ShoppingCartItem model to com.demo package
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Harvest ShoppingCartItem.java with package restructuring
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java → src/main/java/com/demo/model/ShoppingCartItem.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve: All fields (product, quantity, price, promoSavings), constructors, getters/setters, Serializable
**Acceptance**: src/main/java/com/demo/model/ShoppingCartItem.java; sensors green

#### T-004: Verify god-node behavior preservation in converted models
**Class**: infer
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Characterize behavioral contracts and ensure ShoppingCartServiceTest assertions continue to pass
**Target design**:
- src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java
- src/main/java/com/demo/model/Product.java
- src/main/java/com/demo/model/ShoppingCart.java  
- src/main/java/com/demo/model/ShoppingCartItem.java
- CATALOG_ENDPOINT environment configuration from application.properties
- Run ShoppingCartServiceTest to verify cart initialization behavior
- Verify Product structure preservation (id, name, description, price)
- Verify ShoppingCartItem pricing calculations (price × quantity → cartItemTotal)
- Confirm serialization compatibility maintained
**Acceptance**: All model-related test assertions pass; no regressions in cart pricing logic

#### T-005: Document §7 target shapes for future REDESIGN class implementation
**Class**: infer
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Document §7 target shapes for REDESIGN classes handled in future stories
**Target design**:
- S01 brief scope: HARVEST foundation data models only (Product, ShoppingCart, ShoppingCartItem)
- REDESIGN classes OUT OF SCOPE for S01 but §7 target shapes documented for future implementation:
  - CartEndpoint (S04): 404 on missing carts, 400 for invalid input, 503 on failures, thread-safe operations, cache refresh guard
  - ShoppingCartService (S03): idempotent reads, normalized pricing calculations
  - ShoppingCartServiceImpl (S03): thread-safe singleton with ConcurrentHashMap, 503 errors, compute operations, normalized derivations
  - PromoService (S02): thread-safe promotion calculations
  - ShippingService (S02): deterministic shipping rate calculations
  - CatalogService (S02): REST Client with retry/timeout policies
  - CartServiceApplication, JerseyConfig: removed during S04 endpoint conversion
- UI surface waiver: Foundation data models have no direct UI surface - cart operations exposed via CartEndpoint in S04
- Preserve contract: CATALOG_ENDPOINT environment configuration per migration.yaml §27-31
- Forbidden guard: getMockProducts patterns listed in migration.yaml §32-39 are fabrication tripwires for service layer
**Acceptance**: §7 traceability established; UI surface waived; preserve/forbidden contracts documented
