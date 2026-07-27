# T-006: Preserved Integration Coverage

## Integration Contracts for Downstream Service Layer Modernization

This document captures the external integration contracts that must be preserved during the migration from the legacy Spring Boot application to the Quarkus-based modernized application.

---

## 1. CATALOG_ENDPOINT Integration

### Integration Point
- **Finding**: `demo-env-integration-00001`
- **Preserved**: Via application.properties migration (owned by S02)
- **Configuration**: `CATALOG_ENDPOINT=http://localhost:8081`

### Contract Preservation
- **Domain Models**: Product data contract maintained for catalog service integration
- **Data Structure**: Product class constructor and field accessors remain unchanged
- **API Endpoint**: `GET /api/products` contract preserved from legacy specification
- **Integration Pattern**: Service layer will continue to call catalog endpoint via Feign client

### Downstream Requirements
- Service layer modernization (S02) will implement the same Product data contracts
- External catalog service integration must remain functional
- Environment variable override capability: `-DCATALOG_ENDPOINT=...`

---

## 2. Test Integration Contracts

### ProductsObjectMother Pattern
- **Finding**: `spring-components-00002` (test integration preserved)
- **Source**: `/projects/legacy/src/test/java/com/redhat/coolstore/ProductsObjectMother.java`
- **Method**: `createVehicleProducts()`
- **Test Data**: Preserved for downstream testing compatibility

### API Contract: Product Constructor
- **Signature**: `Product(String itemId, String name, String desc, double price)`
- **Preserved Fields**:
  - `itemId`: String identifier for the product
  - `name`: String product name
  - `desc`: String product description
  - `price`: double product price
- **Serialization**: JSON field names match legacy API expectations
- **Package**: `com.demo.model.Product` (migrated from `com.redhat.coolstore.model`)

### Test Object Mothers
- **Legacy Pattern**: `ProductsObjectMother.createVehicleProducts()`
- **Preserved Method Signature**: Static factory returning `List<Product>`
- **Test Data Values**:
  ```java
  new Product("1111", "Car", "Super car", 1000)
  new Product("2222", "Bike", "Super bike", 200)
  ```

### getMockProducts Integration
- **Pattern**: `@MockBean CatalogService` for service testing
- **Mock Configuration**: `given(this.catalogService.products()).willReturn(ProductsObjectMother.createVehicleProducts())`
- **Preserved For**: Downstream service layer integration testing

---

## 3. Service Layer API Contracts

### CatalogService.products() Return Type
- **Return Type**: `List<Product>`
- **Method Signature**: `List<Product> products()`
- **Integration**: ShoppingCartService.getProduct() method contract
- **Mock Support**: Mockito `given()` pattern preserved

### ShoppingCartService Integration Points
- **Method**: `getProduct(String productId)`
- **Return Type**: `Product`
- **Integration**: Uses CatalogService to retrieve product data
- **Test Pattern**: MockBean with ProductsObjectMother data

---

## 4. Test Architecture Preservation

### Spring Boot Test Patterns
- **@MockBean**: Used for external service mocking (CatalogService)
- **@SpringBootTest**: Configuration preserved for integration testing
- **AssertJ**: Fluent assertion patterns maintained
- **Property Override**: `properties = "CATALOG_ENDPOINT=http://localhost"`

### Legacy Test Coverage Requirements
- **ShoppingCartServiceTest**: Key assertion values must be preserved
  - Cart with 2x $1000 items = $2000 cart item total
  - -$10.99 shipping promotion (above $75 threshold)
  - $2000 cart total after promotion application
- **CartServiceBoundaryTest**: REST endpoint contract testing preserved

---

## 5. Migration Impact Assessment

### What Changes
- **Package Structure**: `com.redhat.coolstore.model` → `com.demo.model`
- **Framework**: Spring Boot → Quarkus
- **Build System**: Spring Maven → Quarkus Platform BOM

### What Must Not Change
- **Product Data Contract**: Constructor signature and field structure
- **Test Method Signatures**: Legacy test assertion values
- **Service API Contracts**: Method signatures and return types
- **Integration Endpoints**: External service call patterns
- **Test Data Patterns**: ObjectMother patterns and mock configurations

---

## 6. Verification Requirements

### Downstream Service Layer (S02)
- All Product data structures remain compatible
- CatalogService interface can be implemented with migrated model classes
- Test mocking patterns continue to work without modification

### Characterized Behavior (T-007)
- Product creation with legacy test data values
- ShoppingCart integration with Product references
- Shipping calculation with $10.99 free shipping promotion above $75

### Test Compatibility
- ProductsObjectMother.createVehicleProducts() returns valid Product objects
- Mock configurations in legacy tests work with migrated model classes
- AssertJ assertion patterns remain functional

---

## 7. Ownership and Next Steps

### Current Status
- ✅ Product entity migrated to com.demo.model package
- ✅ Constructor signature preserved
- ✅ Serialization contracts maintained
- ✅ Test data patterns documented

### S02 Ownership
- **Service Layer Integration**: CatalogService.products() implementation
- **Application Properties**: CATALOG_ENDPOINT configuration migration
- **Feign Client**: External catalog service integration
- **Test Integration**: Mock configuration updates

### Preserved for Service Layer
- Product data contracts and serialization compatibility
- ObjectMother test data patterns
- Mock service integration patterns
- Assertion values for characterized behavior tests

---

**Migration Phase**: S01 - Domain Model Migration
**Task**: T-006 - Preserved Integration Coverage
**Status**: Complete - Integration contracts documented and preserved
**Next**: Service layer modernization (S02)