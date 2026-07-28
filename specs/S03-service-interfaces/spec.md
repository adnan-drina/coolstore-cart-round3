# S03: Service Interfaces Specification

## Observed Legacy Behavior and API Contract

### ShoppingCartService Interface
**Legacy File**: `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:6-20`

```java
public interface ShoppingCartService {
    ShoppingCart getShoppingCart(String cartId);
    Product getProduct(String itemId);
    ShoppingCart deleteItem(String cartId, String itemId, int quantity);
    ShoppingCart checkout(String cartId);
    ShoppingCart addItem(String cartId, String itemId, int quantity);
    ShoppingCart set(String cartId, String tmpId);
    void priceShoppingCart(ShoppingCart sc);
}
```

**Behavioral Contract**:
- Cart lifecycle operations: retrieve, modify, checkout cart by cartId
- Product catalog integration via getProduct() method
- Item management: add, delete items with quantity handling
- Temporary cart replacement via set() method
- Pricing calculation via priceShoppingCart() method
- All operations return ShoppingCart instances except priceShoppingCart() which is void
- getProduct() provides catalog integration for product lookups

**Method Signatures Preserved**:
- `ShoppingCart getShoppingCart(String cartId)` - retrieve existing cart
- `Product getProduct(String itemId)` - lookup product by ID
- `ShoppingCart deleteItem(String cartId, String itemId, int quantity)` - remove items
- `ShoppingCart checkout(String cartId)` - process checkout
- `ShoppingCart addItem(String cartId, String itemId, int quantity)` - add items
- `ShoppingCart set(String cartId, String tmpId)` - replace cart contents
- `void priceShoppingCart(ShoppingCart sc)` - calculate cart pricing

### CatalogService Interface  
**Legacy File**: `src/main/java/com/redhat/coolstore/service/CatalogService.java:1-14`

```java
package com.redhat.coolstore.service;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.redhat.coolstore.model.Product;

@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```

**Behavioral Contract**:
- REST client for external catalog service integration
- Retrieves product list via HTTP GET to `/api/products`
- Feign client configured with environment-driven URL substitution
- Returns List<Product> for catalog integration
- Method name: `products()` (not `getProducts()`)

**Configuration Contract**:
**Legacy File**: `src/main/resources/application.properties:4-6`

```properties
# Catalog products endpoint used by the Feign CatalogService.
# Override with env CATALOG_ENDPOINT or -DCATALOG_ENDPOINT=...
CATALOG_ENDPOINT=http://localhost:8081
```

**Environment-Driven Configuration**:
- Environment variable `CATALOG_ENDPOINT` controls catalog service URL
- Default fallback: `http://localhost:8081`  
- Preserved per migration.yaml `preserve: [CATALOG_ENDPOINT]` contract

### Integration Points

**ShoppingCartService Dependencies**:
- Depends on Product entity from `com.redhat.coolstore.model.Product`
- Depends on ShoppingCart entity from `com.redhat.coolstore.model.ShoppingCart`
- Coordinates with CatalogService via getProduct() method

**CatalogService Integration**:
- External REST service integration via Feign client
- Spring Cloud OpenFeign framework dependency
- Environment variable substitution for deployment flexibility

### Evidence References
- ShoppingCartService: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:6-20`
- CatalogService: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:1-14`
- Configuration: `/projects/legacy/src/main/resources/application.properties:4-6`
- Architecture Profile: `migration/architecture-profile.md:74-75`
