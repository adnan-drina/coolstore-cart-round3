# S01 Domain Model Migration Tasks

## Migration Execution Plan

Following dependency-order.md conversion sequence with characterization tests positioned early per PLANNING.md:111-117.

|**UI Surface Waiver**: Legacy web surface is waived for S01 since domain models provide backend-only data contracts and are consumed exclusively by the service layer modernization covered in S02.

---

#### T-001: POM Conversion to Quarkus Platform
|**Class: rewrite**|
Convert pom.xml to Quarkus platform conventions per MAPPINGS.md scaffolding requirements.

||**Source**: `pom.xml` (current Spring Boot parent and dependencies)
||**Target**: `pom.xml` (Quarkus platform BOM with pinned plugins)
||**Finding Rule IDs**: javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020,javaee-pom-to-quarkus-00030,javaee-pom-to-quarkus-00040,javaee-pom-to-quarkus-00050,javaee-pom-to-quarkus-00060,javaee-pom-to-quarkus-00080,removed-javaee-modules-00020,spring-components-00001,spring-components-00002,springboot-metrics-to-quarkus-0100,springboot-metrics-to-quarkus-0200,springboot-parent-pom-to-quarkus-00000,springboot-plugins-to-quarkus-0000,springboot-properties-to-quarkus-00000,springboot-di-to-quarkus-00000,springboot-web-to-quarkus-00000

||**Changes**:
- Replace Spring Boot parent with Quarkus platform BOM: `com.redhat.quarkus.platform:quarkus-bom`
- Pin quarkus-maven-plugin version from platform BOM
- Add quarkus-rest, quarkus-smallrye-health dependencies (jakarta imports covered by recipe)
- Remove spring-web artifact references (REST deps arrive via quarkus dependencies)
- Add quarkus-junit-jupiter for testing
- Configure compiler plugin for Java 21
- Set up native profile for native builds
- Remove Spring-specific plugin configurations

||**Verification**: Project compiles with Quarkus platform dependencies, model classes can import jakarta packages

---

#### T-002: Package Migration for ShoppingCart
|**Class: rewrite**|
Harvest ShoppingCart from recipe-transformed staging and migrate package structure.

||**Source**: `migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCart.java` (Jakarta imports already applied)
||**Target**: `src/main/java/com/demo/model/ShoppingCart.java`
||**Package Transform**: `com.redhat.coolstore.model` → `com.demo.model`
||**Finding Rule IDs**: javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020

||**Changes**:
- Copy ShoppingCart.java from staging directory
- Update package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Update import statements for Jakarta packages (already recipe-transformed)
- Verify Serializable contract preserved

||**Verification**: Project compiles, ShoppingCart entity in com.demo.model package

---

#### T-003: Package Migration for Product  
|**Class: rewrite**|
Harvest Product entity and migrate to new package structure.

||**Source**: `migration/staging/src/main/java/com/redhat/coolstore/model/Product.java`
||**Target**: `src/main/java/com/demo/model/Product.java`
||**Package Transform**: `com.redhat.coolstore.model` → `com.demo.model`
||**Finding Rule IDs**: javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020

||**Changes**:
- Copy Product.java from staging directory
- Update package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve serialVersionUID: `-7304814269819778382L`
- Maintain constructor and getter/setter patterns

||**Verification**: Project compiles, Product entity in com.demo.model package

---

#### T-004: Package Migration for ShoppingCartItem
|**Class: rewrite** 
Harvest ShoppingCartItem entity and migrate to new package structure.

||**Source**: `migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`
||**Target**: `src/main/java/com/demo/model/ShoppingCartItem.java`
||**Package Transform**: `com.redhat.coolstore.model` → `com.demo.model`
||**Finding Rule IDs**: javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020

||**Changes**:
- Copy ShoppingCartItem.java from staging directory
- Update package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Verify ShoppingCart and Product references updated
- Preserve quantity and pricing fields

||**Verification**: Project compiles, ShoppingCartItem entity in com.demo.model package

---

#### T-005: Package Migration for Promotion
|**Class: rewrite**|
Harvest Promotion entity and migrate to new package structure.

||**Source**: `migration/staging/src/main/java/com/redhat/coolstore/model/Promotion.java`
||**Target**: `src/main/java/com/demo/model/Promotion.java`
||**Package Transform**: `com.redhat.coolstore.model` → `com.demo.model`
||**Finding Rule IDs**: javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020

||**Changes**:
- Copy Promotion.java from staging directory
- Update package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve promotion rule structure
- Update cross-references to migrated model classes

||**Verification**: Project compiles, Promotion entity in com.demo.model package

---

#### T-006: Preserved Integration Coverage
|**Class: infer**|
Document and preserve external integration contracts for downstream tasks.

||**CATALOG_ENDPOINT Integration**: 
- Preserved via application.properties migration (owned by S02)
- Domain models maintain Product data contract for catalog service
- Finding: demo-env-integration-00001 (preserved for service layer)

||**Test Integration**: 
- ProductsObjectMother.createVehicleProducts() pattern preserved
- Legacy test data structures maintained in characterization tests
- Finding: spring-components-00002 (test integration preserved)
- getMockProducts integration preserved for downstream testing

||**Decided Design**:
- **File Mapping**: Test classes use ObjectMother pattern for Product creation
- **API Contract**: Product constructor signature preserved: `Product(String itemId, String name, String desc, double price)`
- **Integration Pattern**: CatalogService.products() return type maintained as `List<Product>`
- **Test Architecture**: @MockBean patterns preserved for service testing
- **Package**: com.demo.model.* with Product data contract

||**Design Decisions**:
- External catalog endpoint configuration preserved for S02 ownership
- Test object mothers continue functioning with migrated model classes
- Service-level integration patterns maintained for downstream modernization
- Legacy test method signatures maintained for backward compatibility

||**Verification**: Integration contracts preserved for service layer modernization

---

#### T-007: Characterization Tests for ShoppingCart Pricing Behavior
|**Class: infer**|
Create comprehensive characterization tests to pin legacy pricing assertions from ShoppingCartServiceTest.

||**Source Tests**: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:39-54`
||**Target**: `src/test/java/com/demo/model/ShoppingCartPricingTest.java`
||**Finding Rule IDs**: springboot-di-to-quarkus-00003

||**Coverage Requirements**:
- **KEY ASSERTION**: Cart with 2x $1000 items = $2000 cart item total, -$10.99 shipping promotion, $2000 cart total
- **Cart initialization**: Zero totals for new empty cart
- **Product integration**: Product data preservation (`Product("1111", "Car", "Super car", 1000)`)
- **Pricing calculation**: Service-level priceShoppingCart() integration

||**Design Decisions**:
- Test ShoppingCart in isolation with mocked service dependencies
- Pin exact assertion values from legacy test: `returns(2000.0, ShoppingCart::getCartItemTotal)`
- Use AssertJ fluent assertions matching legacy test style
- Create ObjectMother pattern for test data consistency

||**Verification**: All characterization assertions pass, 80% coverage threshold met

---

#### T-008: Shipping Tier Calculation Characterization Tests
|**Class: infer**|
Add characterization tests for untested shipping calculation logic (CartServiceBoundaryTest gap coverage).

||**Source Service**: `ShippingService.java:10-23` (architecture-profile.md:79)
||**Target**: `src/test/java/com/demo/model/ShippingTierTest.java`
||**Finding Rule IDs**: springboot-di-to-quarkus-00003

||**Shipping Tiers Coverage**:
- $0-25 cart total = $2.99 shipping
- $25-50 cart total = $4.99 shipping
- $50-75 cart total = $6.99 shipping  
- $75-100 cart total = $8.99 shipping
- $100+ cart total = $10.99 shipping

||**Design Decisions**:
- Direct unit tests on shipping calculation logic
- Boundary value testing for threshold transitions
- Integration with promotion logic for -$10.99 free shipping above $75
- Test shipping tier application before promotion discounts

||**Verification**: All shipping tier boundaries validated, edge cases covered

---

#### T-009: Product Model Characterization Tests
|**Class: infer**|
Create characterization tests for Product entity behavior and serialization.

||**Source Entity**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:1-54`
||**Target**: `src/test/java/com/demo/model/ProductTest.java`
||**Finding Rule IDs**: springboot-di-to-quarkus-00003

||**Coverage Requirements**:
- **Constructor validation**: All fields properly initialized
- **Serialization contract**: JSON serialization compatibility
- **Field accessors**: Getter/setter behavior
- **toString() format**: String representation consistency

||**Design Decisions**:
- Test Product as pure POJO (no Spring dependencies)
- Verify JSON field names match legacy API expectations
- Include serialVersionUID preservation test
- Cross-reference with ShoppingCartItem integration

||**Verification**: Product entity behavior matches legacy specification

---

#### T-010: Model Layer Integration Validation
|**Class: infer**|
Validate integration between migrated model classes and service layer contracts.

||**Integration Points**: ShoppingCart ↔ ShoppingCartItem ↔ Product relationships
||**Target**: `src/test/java/com/demo/model/ModelIntegrationTest.java`
||**Finding Rule IDs**: springboot-di-to-quarkus-00003

||**Integration Scenarios**:
- ShoppingCart with multiple ShoppingCartItems
- Product reference integrity in cart items
- List manipulation operations (add, remove, reset)
- Pricing calculation data flow from items to cart totals

||**Design Decisions**:
- Service-agnostic model testing (no Spring Boot context)
- Focus on model-to-model relationships and data integrity
- Verify behavioral contracts from legacy service expectations
- Cross-validate serialization compatibility across all model classes

||**Verification**: All model relationships function correctly, service integration ready

---

|**Summary**

|**Rewrite Tasks**: T-001 through T-005 (POM conversion, package migration, file harvesting)
|**Infer Tasks**: T-006 through T-010 (integration coverage, characterization tests)

|**Total Findings Covered**: 
- javaee-pom-to-quarkus-00010,javaee-pom-to-quarkus-00020,javaee-pom-to-quarkus-00030,javaee-pom-to-quarkus-00040,javaee-pom-to-quarkus-00050,javaee-pom-to-quarkus-00060,javaee-pom-to-quarkus-00080,removed-javaee-modules-00020,spring-components-00001,spring-components-00002,springboot-metrics-to-quarkus-0100,springboot-metrics-to-quarkus-0200,springboot-parent-pom-to-quarkus-00000,springboot-plugins-to-quarkus-0000,springboot-properties-to-quarkus-00000,springboot-di-to-quarkus-00000,springboot-web-to-quarkus-00000

|**Recipe-Executed (No Tasks)**: 
- javax-to-jakarta-import-00001 (already in migration/staging)

|**God Node Priority**: ShoppingCart (T-002, T-007) and Product (T-003, T-009) converted first with early characterization test coverage.