# S01 Foundation Data Models - Specification

## Observed Legacy Behavior

The foundation data models (Product, ShoppingCart, ShoppingCartItem) form the core data structures of the Coolstore cart service. These HARVEST classes are referenced throughout the dependency graph and must be preserved exactly with only import conversion.

### Product Data Structure

**Legacy file**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java`

- **Fields**: itemId (String), name (String), desc (String), price (double)
- **Constructors**: Default constructor, full constructor with all fields
- **Methods**: Standard getters/setters, toString() for debugging
- **Serialization**: Implements Serializable with serialVersionUID
- **Dependencies**: Referenced by ShoppingCartItem (product field)

**Key behavior characteristics**:
- Simple POJO with no business logic
- No validation on fields
- Price stored as primitive double (not BigDecimal)
- toString() includes all fields for logging/debugging

### ShoppingCart Data Structure  

**Legacy file**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java`

- **Fields**: 
  - cartId (String)
  - shoppingCartItemList (List<ShoppingCartItem>)
  - cartItemTotal, cartItemPromoSavings, shippingTotal, shippingPromoSavings, cartTotal (double)
- **Constructors**: Default constructor, cartId-only constructor
- **Methods**: 
  - List management: add/remove shopping cart items
  - Reset functionality: clear all items
  - Getters/setters for all pricing fields
  - toString() for debugging

**Key behavior characteristics**:
- Maintains both item list AND pre-calculated totals
- Pricing fields are updated externally (by ShoppingCartServiceImpl)
- List management methods handle null safely
- Uses ArrayList as concrete implementation

### ShoppingCartItem Data Structure

**Legacy file**: `/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java`

- **Fields**: 
  - product (Product reference)
  - quantity (int)
  - price, promoSavings (double)
- **Constructors**: Default constructor
- **Methods**: Standard getters/setters, toString()
- **Serialization**: Implements Serializable with serialVersionUID

**Key behavior characteristics**:
- Simple container with no pricing calculation logic
- Price and promoSavings are updated externally
- No validation on quantity (can be negative/zero)
- toString() includes nested product information

## API Contract (Data Model Interfaces)

### Product
```java
// Legacy constructor signatures
Product()
Product(String itemId, String name, String desc, double price)

// Legacy method signatures
String getItemId()
void setItemId(String itemId)
String getName()
void setName(String name)  
String getDesc()
void setDesc(String desc)
double getPrice()
void setPrice(double price)
```

### ShoppingCart
```java
// Legacy constructor signatures
ShoppingCart()
ShoppingCart(String cartId)

// Legacy method signatures
String getCartId()
void setCartId(String cartId)
List<ShoppingCartItem> getShoppingCartItemList()
void setShoppingCartItemList(List<ShoppingCartItem>)
void resetShoppingCartItemList()
void addShoppingCartItem(ShoppingCartItem sci)
boolean removeShoppingCartItem(ShoppingCartItem sci)
double getCartItemTotal()
void setCartItemTotal(double cartItemTotal)
double getShippingTotal()
void setShippingTotal(double shippingTotal)
double getCartTotal()
void setCartTotal(double cartTotal)
double getCartItemPromoSavings()
void setCartItemPromoSavings(double cartItemPromoSavings)
double getShippingPromoSavings()
void setShippingPromoSavings(double shippingPromoSavings)
```

### ShoppingCartItem
```java
// Legacy constructor signatures  
ShoppingCartItem()

// Legacy method signatures
Product getProduct()
void setProduct(Product product)
int getQuantity()
void setQuantity(int quantity)
double getPrice()
void setPrice(double price)
double getPromoSavings()
void setPromoSavings(double promoSavings)
```

## Dependencies and Usage Patterns

### ShoppingCartServiceImpl Dependencies
The models are referenced extensively by `ShoppingCartServiceImpl.java`:

**ShoppingCart** (5 incoming references):
- Line 42: `HashMap<String, ShoppingCart>` - in-memory storage
- Line 62: cartId parameter for retrieval
- Line 66: cartItemList iteration for pricing
- Line 73: cart.total calculations 
- Line 91: cart reset operations

**Product** (4 incoming references):
- Line 47: `CatalogService.products()` returns List<Product>
- Line 55: productMap.get(id) lookup
- Line 67: product price extraction
- Line 80: product cache refresh

**ShoppingCartItem** (3 incoming references):
- Line 68: item.price = product.price * quantity
- Line 69: item.promoSavings = calculatePromoSavings(product, quantity)  
- Line 85: cartItemList iteration for totals

### God-Node Characterization Requirements

These classes are identified as god nodes with highest fan-in in dependency-order.md. Before conversion, their behavioral contracts must be characterized:

1. **Product**: Structure preservation (all 4 fields + constructors + serialization)
2. **ShoppingCart**: List management methods + pricing field consistency
3. **ShoppingCartItem**: Product reference integrity + field getters/setters

The ShoppingCartServiceTest assertions that depend on these models must continue to pass after conversion:
- Cart initialization: zero totals on new carts
- Cart pricing: price × quantity → cartItemTotal calculations
- Product lookup: catalog service integration preserves product matching

## Jakarta Import Requirements

**Legacy state**: The model classes currently use only java.* imports (Serializable, List, ArrayList). The javax-to-jakarta import conversion has been executed via recipe, but verification is needed that these specific files have no remaining javax imports.

**Expected state**: All imports converted to jakarta.* equivalents where applicable, though these POJOs have minimal imports to convert.

**Recipe status**: javax-to-jakarta-import-00001 was executed via recipe according to migration/recipe-log.md