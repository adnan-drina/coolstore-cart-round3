# S02 Core Model Entities Tasks

## Legacy Surface Waivers

**UI Surface**: `/api/cart/*` endpoints and `CartServiceApplication` are out of scope for S02 (core model entities only). REST endpoints and application bootstrap will be modernized in S05 and S04 respectively.

**Preserved Integrations**:
- `CATALOG_ENDPOINT` environment variable preservation will be handled in S03 (service interfaces)
- `getMockProducts` forbidden patterns verified: no usage found in entity classes (S02 scope)

## Harvest (completed — faithful POJO, package rename only)

The four model classes were harvested faithfully from `migration/staging` via
`harvest-from-staging.sh` (package rename com.redhat.coolstore → com.demo only;
NO JPA — they are plain POJOs of an in-memory service, `needsDatabase: false`).
Product/ShoppingCartItem/ShoppingCart `implements Serializable`; Promotion is a
bare POJO. Present and fidelity-green under `src/main/java/com/demo/model/`:
Product.java, Promotion.java, ShoppingCartItem.java, ShoppingCart.java.

#### T-005: Characterize Product Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-001 (N/A — characterization test requirement)
|**Goal**: Create unit tests for Product to pin legacy behavior
|**Target design**:
- src/test/java/com/demo/model/ProductTest.java
- Plain JUnit 5 unit tests (POJO — no Quarkus runtime or persistence needed)
- Test the no-arg and full constructors, property accessors, toString(), and Serializable round-trip
|**Acceptance**: ProductTest.java provides complete behavioral coverage of the Product POJO; legacy behavior preserved; sensors green

#### T-006: Characterize ShoppingCartItem Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-002 (N/A — characterization test requirement)
|**Goal**: Create unit tests for ShoppingCartItem to pin its state and Product association
|**Target design**:
- src/test/java/com/demo/model/ShoppingCartItemTest.java
- Plain JUnit 5 unit tests (POJO)
- Test property accessors (price, quantity, promoSavings) and the plain `Product product` object reference (getProduct/setProduct); Serializable round-trip
|**Acceptance**: ShoppingCartItemTest.java validates the line-item state and Product association; sensors green

#### T-007: Characterize ShoppingCart Entity Behavior
|**Class**: infer
|**Findings**: architecture-profile-god-node-003 (N/A — characterization test requirement)
|**Goal**: Create unit tests for ShoppingCart to pin initialization and item-list behavior
|**Target design**:
- src/test/java/com/demo/model/ShoppingCartTest.java
- Plain JUnit 5 unit tests (POJO)
- Test cart initialization (zero totals), the pricing fields, and the plain in-memory `List<ShoppingCartItem>` management (get/set, the list is initialized non-null); Serializable round-trip
|**Acceptance**: ShoppingCartTest.java validates the initialization contract and item-list behavior; sensors green
