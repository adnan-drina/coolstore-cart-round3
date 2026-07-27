# Test Integration Patterns - Legacy Compatibility Guide

## ProductsObjectMother Pattern Preservation

### Source Pattern
```java
package com.redhat.coolstore;

import com.redhat.coolstore.model.Product;
import java.util.Arrays;
import java.util.List;

public class ProductsObjectMother {
    public static List<Product> createVehicleProducts() {
        return Arrays.asList(
            new Product("1111", "Car", "Super car", 1000),
            new Product("2222", "Bike", "Super bike", 200));
    }
}
```

### Migrated Package Requirements
- **Legacy Package**: `com.redhat.coolstore.model.Product`
- **Migrated Package**: `com.demo.model.Product`
- **ObjectMother Location**: `src/test/java/com/demo/ProductsObjectMother.java`
- **Updated Import**: `import com.demo.model.Product;`

---

## Key Test Assertion Values

### ShoppingCartServiceTest.java (Lines 39-54)
**Legacy Test**: `should_calculate_price_of_cart()`

```java
final ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1");
ShoppingCartItem sci = new ShoppingCartItem();
sci.setProduct(new Product("1111", "Car", "Super car", 1000));
sci.setQuantity(2);
sci.setPrice(1000);
shoppingCart.addShoppingCartItem(sci);

shoppingCartService.priceShoppingCart(shoppingCart);

assertThat(shoppingCart)
    .returns(0.0, ShoppingCart::getCartItemPromoSavings)
    .returns(2000.0, ShoppingCart::getCartItemTotal)        // KEY VALUE
    .returns(-10.99, ShoppingCart::getShippingPromoSavings) // KEY VALUE
    .returns(2000.0, ShoppingCart::getCartTotal);           // KEY VALUE
```

### Critical Assertion Values
- **Product Data**: `new Product("1111", "Car", "Super car", 1000)`
- **Cart Item Total**: `2000.0` (2 × $1000)
- **Shipping Promotion**: `-10.99` (free shipping above $75)
- **Final Cart Total**: `2000.0`

### CartServiceBoundaryTest.java (Lines 34-46)
**REST Endpoint Test**: `should_add_item_to_shopping_cart()`

```java
final ShoppingCart shoppingCart = this.restTemplate.postForObject(
    "/api/cart/1/1111/2", "", ShoppingCart.class);

assertThat(shoppingCart)
    .returns(0.0, ShoppingCart::getCartItemPromoSavings)
    .returns(2000.0, ShoppingCart::getCartItemTotal)        // KEY VALUE
    .returns(-10.99, ShoppingCart::getShippingPromoSavings) // KEY VALUE
    .returns(2000.0, ShoppingCart::getCartTotal)            // KEY VALUE
    .extracting(ShoppingCart::getShoppingCartItemList)
    .asList()
    .hasSize(1);
```

---

## Mock Configuration Patterns

### CatalogService Mock Setup
```java
@MockBean
CatalogService catalogService;

@Autowired
ShoppingCartService shoppingCartService;

@Test
public void should_get_product_id() {
    given(this.catalogService.products()).willReturn(ProductsObjectMother.createVehicleProducts());
    
    final Product product = shoppingCartService.getProduct("2222");
    assertThat(product)
        .isEqualToIgnoringNullFields(new Product("2222", "Bike", "Super bike", 200));
}
```

### Hoverfly Mock Setup (Boundary Tests)
```java
@ClassRule
public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(dsl(
    service("catalog")
        .get("/api/products")
        .willReturn(success().body(json(ProductsObjectMother.createVehicleProducts())))
));

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "CATALOG_ENDPOINT=http://catalog")
```

---

## Catalog Service Integration

### Service Interface Contract
```java
public interface CatalogService {
    List<Product> products();
    Product getProduct(String productId);
}
```

### External Configuration
- **Property**: `CATALOG_ENDPOINT=http://localhost:8081`
- **Environment Override**: `-DCATALOG_ENDPOINT=http://catalog`
- **Feign Client**: Integration with external catalog service

### Product Data Flow
1. ShoppingCartService.getProduct("2222")
2. Calls catalogService.products()
3. Returns Product matching ID "2222"
4. Expected: `new Product("2222", "Bike", "Super bike", 200)`

---

## Package Migration Impact

### What Must Be Updated
- **Model Classes**: `com.redhat.coolstore.model.*` → `com.demo.model.*`
- **Test Imports**: Update ProductsObjectMother imports
- **Service Layer**: Update CatalogService references

### What Remains Unchanged
- **Product Constructor**: `Product(String itemId, String name, String desc, double price)`
- **Field Names**: itemId, name, desc, price
- **Serialization**: JSON field structure
- **Test Assertion Values**: 2000.0, -10.99, 2000.0
- **Mock Patterns**: given().willReturn() configuration
- **ObjectMother Method**: createVehicleProducts() return values

---

**Status**: Documentation complete for T-006 integration contract preservation