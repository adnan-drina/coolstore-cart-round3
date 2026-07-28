# S02 Core Model Entities - Quarkus Migration Plan

## Overview
This plan migrates the core model entities (Product, ShoppingCartItem, ShoppingCart, Promotion) from legacy Spring Boot structure to Quarkus platform. These HARVEST classes are preserved with minimal changes per architecture profile §7.

## Class Roles & Migration Strategy

All classes are classified as **HARVEST** (data/DTO/value-object) per architecture profile §7:
- **Product** — Pure DTO for catalog product information
- **ShoppingCartItem** — Value object for cart line items (GOD-NODE)
- **ShoppingCart** — Entity for cart state management (GOD-NODE)
- **Promotion** — Value object for promotional discount rules

## Conversion Order

Per `migration/dependency-order.md`:
1. Product (god-node: characterization tests first)
2. Promotion
3. ShoppingCartItem (god-node: characterization tests first)
4. ShoppingCart (god-node: characterization tests first)

## Jakarta Import Status

**REcipe ALREADY EXECUTED**: `javax-to-jakarta-import-00001` completed in S01 platform foundation.

**Legacy State**: Entity classes contain NO javax.* imports — only:
- `java.io.Serializable`
- `java.util.ArrayList` (ShoppingCart)
- `java.util.List` (ShoppingCart)

**No mechanical changes required** for javax-to-jakarta conversion.

## Tasks

### Phase 1: Entity Class Harvesting (Rewrite Tasks)

#### T-001: Harvest Product Entity
**Class**: rewrite  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`  
**Target**: `src/main/java/com/redhat/coolstore/model/Product.java`  
**Finding Rule IDs**: javax-to-jakarta-import-00001  
**Changes**: 
- Copy legacy Product.java to Quarkus project structure
- Preserve all constructors, methods, and serialVersionUID
- Add Jakarta JPA annotations: `@Entity`, `@Table(name = "PRODUCT")`, `@Id`
- Add Jakarta persistence imports: `jakarta.persistence.Entity`, `jakarta.persistence.Id`, `jakarta.persistence.Table`
- Preserve all existing property accessors and business logic

**Target Shape** (from brief):
```java
@Entity
@Table(name = "PRODUCT")
public class Product {
    @Id
    private String itemId;
    private String name;
    private String desc;
    private double price;
    // Existing constructors, getters, setters preserved
}
```

#### T-002: Harvest Promotion Entity
**Class**: rewrite  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java`  
**Target**: `src/main/java/com/redhat/coolstore/model/Promotion.java`  
**Finding Rule IDs**: javax-to-jakarta-import-00001  
**Changes**:
- Copy legacy Promotion.java to Quarkus project structure
- Preserve all constructors, methods, and property accessors
- Add Jakarta JPA annotations: `@Entity`
- Add Jakarta persistence import: `jakarta.persistence.Entity`
- Maintain existing promotion logic structure

**Target Shape** (from brief):
```java
@Entity
public class Promotion {
    private String itemId;
    private double percentOff;
    // Existing constructors, getters, setters preserved
}
```

#### T-003: Harvest ShoppingCartItem Entity
**Class**: rewrite  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`  
**Target**: `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`  
**Finding Rule IDs**: javax-to-jakarta-import-00001  
**Changes**:
- Copy legacy ShoppingCartItem.java to Quarkus project structure
- Preserve all constructors, methods, and property accessors
- Add Jakarta JPA annotations: `@Entity`, `@OneToOne`
- Add Jakarta persistence imports: `jakarta.persistence.Entity`, `jakarta.persistence.OneToOne`
- Add relationship mapping: `@OneToOne private Product product;`
- Maintain existing pricing calculation logic

**Target Shape** (from brief):
```java
@Entity
public class ShoppingCartItem {
    @Id
    private String productId;
    private int quantity;
    private double price;
    private double promoSavings;
    @OneToOne
    private Product product;
    // Existing getters, setters preserved
}
```

#### T-004: Harvest ShoppingCart Entity
**Class**: rewrite  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`  
**Target**: `src/main/java/com/redhat/coolstore/model/ShoppingCart.java`  
**Finding Rule IDs**: javax-to-jakarta-import-00001  
**Changes**:
- Copy legacy ShoppingCart.java to Quarkus project structure
- Preserve all constructors, methods, and property accessors
- Add Jakarta JPA annotations: `@Entity`, `@OneToMany`
- Add Jakarta persistence imports: `jakarta.persistence.Entity`, `jakarta.persistence.OneToMany`
- Add relationship mapping: `@OneToMany private List<ShoppingCartItem> shoppingCartItemList;`
- Maintain existing cart management methods (add, remove, reset)
- Preserve all pricing field calculations

**Target Shape** (from brief):
```java
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
    // Existing business methods preserved
}
```

### Phase 2: Characterization Tests (Infer Tasks)

#### T-005: Characterize Product Entity Behavior
**Class**: infer  
**Target**: `src/test/java/com/redhat/coolstore/model/ProductTest.java`  
**Finding Rule IDs**: architecture-profile-god-node-001  
**Changes**:
- Create comprehensive test class for Product entity
- Test default and parameterized constructors
- Test all property accessors (getter/setter pairs)
- Test toString() method output
- Verify serialization compatibility
- Assert legacy behavior is preserved (itemId, name, desc, price fields)

**Test Coverage**:
```java
@QuarkusTest
class ProductTest {
    @Test void testDefaultConstructor() { /* verify defaults */ }
    @Test void testParameterizedConstructor() { /* verify values */ }
    @Test void testPropertyAccessors() { /* verify getters/setters */ }
    @Test void testToString() { /* verify string representation */ }
}
```

#### T-006: Characterize ShoppingCartItem Entity Behavior
**Class**: infer  
**Target**: `src/test/java/com/redhat/coolstore/model/ShoppingCartItemTest.java`  
**Finding Rule IDs**: architecture-profile-god-node-002  
**Changes**:
- Create comprehensive test class for ShoppingCartItem entity
- Test property accessors and business logic
- Test pricing calculations: quantity × price - promoSavings
- Test Product association (@OneToOne relationship)
- Verify legacy pricing contract behavior

**Test Coverage**:
```java
@QuarkusTest
class ShoppingCartItemTest {
    @Test void testPropertyAccessors() { /* verify getters/setters */ }
    @Test void testPricingCalculation() { /* verify quantity × price - promoSavings */ }
    @Test void testProductAssociation() { /* verify @OneToOne relationship */ }
}
```

#### T-007: Characterize ShoppingCart Entity Behavior
**Class**: infer  
**Target**: `src/test/java/com/redhat/coolstore/model/ShoppingCartTest.java`  
**Finding Rule IDs**: architecture-profile-god-node-003  
**Changes**:
- Create comprehensive test class for ShoppingCart entity
- Test cart initialization contract (all totals = 0.0)
- Test item management methods (add, remove, reset)
- Test pricing field preservation (cartItemTotal, cartItemPromoSavings, shippingCosts, shippingPromoSavings, cartTotal)
- Test ShoppingCartItemList relationship (@OneToMany)
- Verify cart lifecycle behavior from test evidence (ShoppingCartServiceTest.java:28)

**Test Coverage**:
```java
@QuarkusTest
class ShoppingCartTest {
    @Test void testCartInitialization() { /* verify zero totals contract */ }
    @Test void testAddShoppingCartItem() { /* verify item addition */ }
    @Test void testRemoveShoppingCartItem() { /* verify item removal */ }
    @Test void testResetShoppingCartItemList() { /* verify cart clearing */ }
    @Test void testPricingFields() { /* verify all pricing fields maintained */ }
}
```

## Contract Compliance

### Behavioral Contracts (per brief)
- **Product creation**: itemId, name, desc, price fields preserved exactly
- **ShoppingCartItem pricing**: quantity × price - promoSavings calculation preserved
- **ShoppingCart totals**: cartItemTotal, cartItemPromoSavings, shippingCosts, shippingPromoSavings, cartTotal fields maintained
- **Cart initialization contract**: New carts return zero totals (cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0)

### Findings Coverage
- **javax-to-jakarta-import-00001**: Entity classes receive JPA annotations with jakarta.persistence.* imports (not javax.*)

### Architecture Profile Compliance
- All classes marked as HARVEST per §7
- God-node characterization tests for ShoppingCart, Product, ShoppingCartItem
- Dependency order maintained: Product → Promotion → ShoppingCartItem → ShoppingCart
- No changes to service or endpoint classes (out of scope)

## Quality Gates

- All entity classes compile with jakarta.persistence.* imports
- Characterization tests provide 100% coverage for HARVEST entities
- Tests verify behavioral contracts from legacy analysis
- Build remains green after each commit per dependency order
- No regression in existing service tests
