# S02 Core Model Entities Specification

## Observed Legacy Behavior

This specification documents the observed behavior and API contracts of the core model entities in the Coolstore Cart Service. These entity classes form the foundational data model for cart operations and are classified as HARVEST classes (data/DTO/value-object classes) that should be preserved with minimal changes.

## Entity Classes and Contracts

### Product Entity
**File**: `src/main/java/com/redhat/coolstore/model/Product.java`

**Role**: Pure data transfer object representing catalog product information

**API Contract**:
- **Constructors**: 
  - Default constructor `Product()`
  - Parameterized constructor `Product(String itemId, String name, String desc, double price)`
- **Properties**: 
  - `itemId` (String): Unique product identifier
  - `name` (String): Product display name
  - `desc` (String): Product description
  - `price` (double): Product price
- **Accessor Methods**: Standard getter/setter pairs for all properties
- **Serialization**: Implements `Serializable` with `serialVersionUID = -7304814269819778382L`

**Legacy Evidence**:
```java
public class Product implements Serializable {
    private String itemId;
    private String name;
    private String desc;
    private double price;
    // Constructors, getters, setters, toString()
}
```

### ShoppingCartItem Entity
**File**: `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`

**Role**: Value object representing individual cart line items (GOD-NODE)

**API Contract**:
- **Constructors**: 
  - Default constructor `ShoppingCartItem()`
- **Properties**: 
  - `price` (double): Unit price of the item
  - `quantity` (int): Number of items in cart
  - `promoSavings` (double): Promotional discount amount
  - `product` (Product): Associated product information
- **Accessor Methods**: Standard getter/setter pairs for all properties
- **Serialization**: Implements `Serializable` with `serialVersionUID = 6964558044240061049L`

**Legacy Evidence**:
```java
public class ShoppingCartItem implements Serializable {
    private double price;
    private int quantity;
    private double promoSavings;
    private Product product;
    // Getters, setters, toString()
}
```

### ShoppingCart Entity
**File**: `src/main/java/com/redhat/coolstore/model/ShoppingCart.java`

**Role**: Entity capturing cart state including item list, pricing fields (GOD-NODE)

**API Contract**:
- **Constructors**: 
  - Default constructor `ShoppingCart()`
  - Parameterized constructor `ShoppingCart(String cartId)`
- **Properties**: 
  - `cartId` (String): Unique cart identifier
  - `shoppingCartItemList` (List<ShoppingCartItem>): List of cart items
  - `cartItemTotal` (double): Subtotal before promotions and shipping
  - `cartItemPromoSavings` (double): Total promotional savings on items
  - `shippingTotal` (double): Shipping cost before promotions
  - `shippingPromoSavings` (double): Promotional savings on shipping
  - `cartTotal` (double): Final cart total after all calculations
- **Business Methods**:
  - `addShoppingCartItem(ShoppingCartItem sci)`: Adds item to cart
  - `removeShoppingCartItem(ShoppingCartItem sci)`: Removes item from cart, returns boolean
  - `resetShoppingCartItemList()`: Clears all items from cart
- **Serialization**: Implements `Serializable` with `serialVersionUID = -1108043957592113528L`

**Legacy Evidence**:
```java
public class ShoppingCart implements Serializable {
    private String cartId;
    private List<ShoppingCartItem> shoppingCartItemList = new ArrayList<ShoppingCartItem>();
    private double cartItemTotal;
    private double cartItemPromoSavings;
    private double shippingTotal;
    private double shippingPromoSavings;
    private double cartTotal;
    // Constructors, getters, setters, business methods, toString()
}
```

### Promotion Entity
**File**: `src/main/java/com/redhat/coolstore/model/Promotion.java`

**Role**: Value object capturing promotional discount rules

**API Contract**:
- **Constructors**: 
  - Default constructor `Promotion()`
  - Parameterized constructor `Promotion(String itemId, double percentOff)`
- **Properties**: 
  - `itemId` (String): Product identifier for promotion
  - `percentOff` (double): Discount percentage (0.0 to 100.0)
- **Accessor Methods**: Standard getter/setter pairs for all properties

**Legacy Evidence**:
```java
public class Promotion {
    private String itemId;
    private double percentOff;
    // Constructors, getters, setters, toString()
}
```

## Key Behavioral Contracts

### Cart Initialization Contract
From `ShoppingCartServiceTest.java:28`:
- New carts must initialize all pricing fields to 0.0
- cartItemPromoSavings: 0.0
- cartItemTotal: 0.0
- shippingPromoSavings: 0.0
- cartTotal: 0.0

### Item Pricing Contract
- ShoppingCartItem: quantity × price - promoSavings calculation preserved
- ShoppingCart: cartItemTotal, cartItemPromoSavings, shippingCosts, shippingPromoSavings, cartTotal fields maintained

### Dependency Relationships
- Product is referenced by ShoppingCartItem
- ShoppingCartItem is contained by ShoppingCart
- ShoppingCart references multiple ShoppingCartItem instances
- Promotion rules apply to specific Product items

## Migration Requirements

### Jakarta Import Conversion (Recipe-Executed)
Per `migration/recipe-log.md`, the following javax.* to jakarta.* conversions are ALREADY COMPLETE:
- Product.java: No JPA imports in legacy (plain POJO)
- ShoppingCartItem.java: No JPA imports in legacy (plain POJO)
- ShoppingCart.java: No JPA imports in legacy (plain POJO)
- Promotion.java: No JPA imports in legacy (plain POJO)

### Current Import State
All entity classes currently use only:
- `java.io.Serializable`
- `java.util.ArrayList` (ShoppingCart only)
- `java.util.List` (ShoppingCart only)

No javax.* imports exist in these entity classes, so no recipe conversion is required.

## Testing Requirements

### Characterization Tests Required
God-node entities (ShoppingCart, Product, ShoppingCartItem) require characterization tests to pin behavior before dependent service conversion:

1. **Product**: Test constructor, property access, toString() behavior
2. **ShoppingCartItem**: Test property access, pricing calculations, product association
3. **ShoppingCart**: Test cart initialization, item management, pricing field preservation

### Test Evidence Sources
- Primary: `src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java` lines 28, 38, 52, 57
- Secondary: Legacy behavior analysis of entity methods and constructors
