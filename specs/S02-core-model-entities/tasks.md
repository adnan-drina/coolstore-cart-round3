# S02 Core Model Entities Tasks

## Legacy Surface Waivers

**UI Surface**: `/api/cart/*` endpoints and `CartServiceApplication` are out of scope for S02 (core model entities only). REST endpoints and application bootstrap will be modernized in S05 and S04 respectively.

**Preserved Integrations**:
- `CATALOG_ENDPOINT` environment variable preservation will be handled in S03 (service interfaces)
- `getMockProducts` forbidden patterns verified: no usage found in entity classes (S02 scope)

**Ground truth (verified against /projects/legacy and migration/staging)**: the four
model classes are plain POJOs. Product, ShoppingCartItem and ShoppingCart
`implements Serializable` (with a `serialVersionUID`); Promotion is a bare POJO.
NONE carry `@Entity`/`@Id`/`@Table`/`@OneToOne`/`@OneToMany` or any `javax`/`jakarta`
persistence import — this is an in-memory cart service (`needsDatabase: false`).
Harvest them FAITHFULLY: package rename `com.redhat.coolstore` → `com.demo` only.
No JPA annotations, no persistence dependency, no invented fields.

#### T-001: Harvest Product Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents — no javax imports in models)
|**Goal**: Harvest Product.java from migration/staging as a faithful POJO (package rename only) — run `.hermes/skills/migration-harness/scripts/harvest-from-staging.sh model/Product.java`. Do NOT add JPA annotations or a persistence import.
|**Acceptance**: src/main/java/com/demo/model/Product.java — plain POJO `implements Serializable`, fields itemId/name/desc/price, matches staging modulo the package rename; sensors green

#### T-002: Harvest Promotion Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents — no javax imports in models)
|**Goal**: Harvest Promotion.java from migration/staging as a faithful POJO (package rename only) — run `harvest-from-staging.sh model/Promotion.java`. Do NOT add `implements Serializable`, JPA annotations, or a persistence import.
|**Acceptance**: src/main/java/com/demo/model/Promotion.java — bare POJO, fields itemId/percentOff, matches staging modulo the package rename; sensors green

#### T-003: Harvest ShoppingCartItem Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents — no javax imports in models)
|**Goal**: Harvest ShoppingCartItem.java from migration/staging as a faithful POJO (package rename only) — run `harvest-from-staging.sh model/ShoppingCartItem.java`. The `Product product` field is a plain object reference, NOT a JPA relationship.
|**Acceptance**: src/main/java/com/demo/model/ShoppingCartItem.java — POJO `implements Serializable`, fields price/quantity/promoSavings/product, matches staging modulo the package rename; sensors green

#### T-004: Harvest ShoppingCart Entity
|**Class**: rewrite
|**Findings**: javax-to-jakarta-import-00001 (0 incidents — no javax imports in models)
|**Goal**: Harvest ShoppingCart.java from migration/staging as a faithful POJO (package rename only) — run `harvest-from-staging.sh model/ShoppingCart.java`. The `List<ShoppingCartItem>` field is a plain in-memory list, NOT a JPA relationship.
|**Acceptance**: src/main/java/com/demo/model/ShoppingCart.java — POJO `implements Serializable`, pricing fields + cartId + `List<ShoppingCartItem>`, matches staging modulo the package rename; sensors green

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
