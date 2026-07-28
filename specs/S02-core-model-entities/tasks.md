# S02 Core Model Entities Tasks

## Legacy Surface Waivers

**UI Surface**: `/api/cart/*` endpoints and `CartServiceApplication` are out of scope for S02 (core model entities only). REST endpoints and application bootstrap will be modernized in S05 and S04 respectively.

**Preserved Integrations**: 
- `CATALOG_ENDPOINT` environment variable preservation will be handled in S03 (service interfaces)
- `getMockProducts` forbidden patterns verified: no usage found in entity classes (S02 scope)

#### T-001: Harvest Product Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents - already recipe-executed)
|**Goal**: Copy Product.java from legacy to Quarkus project with JPA annotations
|**Acceptance**: src/main/java/com/redhat/coolstore/model/Product.java with jakarta.persistence.* imports; sensors green

#### T-002: Harvest Promotion Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents - already recipe-executed)
|**Goal**: Copy Promotion.java from legacy to Quarkus project with JPA annotations
|**Acceptance**: src/main/java/com/redhat/coolstore/model/Promotion.java with jakarta.persistence.* imports; sensors green

#### T-003: Harvest ShoppingCartItem Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents - already recipe-executed)
|**Goal**: Copy ShoppingCartItem.java from legacy to Quarkus project with JPA annotations and relationships
|**Acceptance**: src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java with jakarta.persistence.* imports and @OneToOne relationship; sensors green

#### T-004: Harvest ShoppingCart Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents - already recipe-executed)
|**Goal**: Copy ShoppingCart.java from legacy to Quarkus project with JPA annotations and relationships
|**Acceptance**: src/main/java/com/redhat/coolstore/model/ShoppingCart.java with jakarta.persistence.* imports and @OneToMany relationship; sensors green

#### T-005: Characterize Product Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-001 (N/A - characterization test requirement)
|**Goal**: Create comprehensive tests for Product entity to pin legacy behavior
|**Target design**: 
- src/test/java/com/redhat/coolstore/model/ProductTest.java
- Test constructors, property accessors, toString(), serialization
- @QuarkusTest with 100% coverage for Product entity
|**Acceptance**: ProductTest.java provides complete behavioral coverage; legacy behavior preserved; sensors green

#### T-006: Characterize ShoppingCartItem Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-002 (N/A - characterization test requirement)
|**Goal**: Create comprehensive tests for ShoppingCartItem to pin pricing calculations and relationships
|**Target design**: 
- src/test/java/com/redhat/coolstore/model/ShoppingCartItemTest.java
- Test property accessors, pricing (quantity × price - promoSavings), @OneToOne Product relationship
- @QuarkusTest with complete ShoppingCartItem behavior coverage
|**Acceptance**: ShoppingCartItemTest.java validates pricing contract and Product association; sensors green

#### T-007: Characterize ShoppingCart Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-003 (N/A - characterization test requirement)
|**Goal**: Create comprehensive tests for ShoppingCart to pin initialization and item management behavior
|**Target design**: 
- src/test/java/com/redhat/coolstore/model/ShoppingCartTest.java
- Test cart initialization (zero totals), item management (add/remove/reset), pricing fields, @OneToMany relationship
- @QuarkusTest with complete ShoppingCart lifecycle coverage
|**Acceptance**: ShoppingCartTest.java validates initialization contract and business methods; sensors green
