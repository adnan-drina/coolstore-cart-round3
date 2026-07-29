# S03 Specification: Service Interfaces and Catalog Client

## Legacy Behavior Observed

### ShoppingCartService Interface Contract

**File**: `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`

The ShoppingCartService interface defines a plain service contract without any annotations (jakarta.* imports will be added during migration). The interface provides cart operation methods:

1. **`getShoppingCart(String cartId)`** - Retrieves a shopping cart by ID
2. **`getProduct(String itemId)`** - Retrieves a product from the catalog service by item ID
3. **`deleteItem(String cartId, String itemId, int quantity)`** - Removes items from a cart
4. **`checkout(String cartId)`** - Processes cart checkout and clears items
5. **`addItem(String cartId, String itemId, int quantity)`** - Adds items to a cart
6. **`set(String cartId, String tmpId)`** - Replaces cart contents with temporary cart
7. **`priceShoppingCart(ShoppingCart sc)`** - Calculates pricing for a cart

All method signatures are preserved exactly as defined. This interface is consumed by:
- `ShoppingCartServiceImpl` (S04): implements the service methods
- `CartEndpoint` (S05): uses the service for REST operations

### CatalogService Interface Contract

**File**: `src/main/java/com/redhat/coolstore/service/CatalogService.java`

The CatalogService is a Feign client interface that communicates with an external catalog service:

1. **`products()`** - Returns `List<Product>` from the catalog service endpoint `/api/products`

**Current Implementation**:
- Uses Spring Cloud OpenFeign `@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")`
- Maps Spring `@GetMapping("/api/products")` to the HTTP call
- Environment variable `${CATALOG_ENDPOINT}` drives the service URL with default fallback

**Consumed by**: `ShoppingCartServiceImpl` (S04) calls `products()` method for catalog integration

### Configuration Contract

**File**: `src/main/resources/application.properties`

```
# Catalog products endpoint used by the Feign CatalogService.
# Override with env CATALOG_ENDPOINT or -DCATALOG_ENDPOINT=...
CATALOG_ENDPOINT=http://localhost:8081
```

The catalog service URL is environment-driven with a `CATALOG_ENDPOINT` environment variable. The configuration supports:
- Environment variable substitution via `${CATALOG_ENDPOINT}`
- Default fallback value: `http://localhost:8081`
- **Preserve contract**: The `CATALOG_ENDPOINT` environment variable name must be maintained

## API Contract Summary

### Interface Contract (ShoppingCartService)

```
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

**Behavioral constraints**:
- All methods preserve their exact signatures
- Service implementation may use CDI injection (jakarta.* imports)
- No annotation requirements for the interface itself

### REST Client Contract (CatalogService)

```
@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
public interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}
```

**Behavioral constraints**:
- Method name `products()` preserved for calling compatibility
- Returns `List<Product>` object
- URL driven by environment variable configuration
- HTTP GET to `/api/products` endpoint

## Legacy Code Evidence

### ShoppingCartService Usage in Service Implementation

**File**: `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` (line references from brief)

```java
// ShoppingCartService interface methods called by implementation
@Inject
private CatalogService catalogService;

public ShoppingCart getShoppingCart(String cartId) {
    // Implementation depends on interface contract
}

public Product getProduct(String itemId) {
    // Implementation depends on catalogService.products() method
    List<Product> products = catalogService.products();
    // Filter products by itemId
}
```

### ShoppingCartService Usage in REST Endpoint

**File**: `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` (line references from brief)

```java
@Inject
private ShoppingCartService shoppingCartService;

// REST operations depend on all seven interface methods
public Response getCart(@PathParam("cartId") String cartId) {
    ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
    // Returns cart with pricing calculation
}
```

## Migration Constraints

### Preserve Requirements

1. **Environment Variable**: `CATALOG_ENDPOINT` must be preserved as-is
2. **Method Signatures**: All seven ShoppingCartService methods maintain exact contracts
3. **Method Name**: `catalogService.products()` method name preserved
4. **Return Types**: All method return types preserved exactly

### Forbidden Changes

1. Interface method renaming or signature changes
2. Removal of environment variable configuration
3. Breaking the method call chain (`CartEndpoint` → `ShoppingCartService` → `CatalogService`)
4. Changing the `products()` return type from `List<Product>`

## Testing Strategy

### Interface Testing

**Primary test source**: `src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java`

- Tests ShoppingCartService implementation behavior (S04 scope)
- CatalogService mock returns specific Product with itemId "2222", name "Bike", desc "Super bike", price 200
- Validates cart initialization contract (zero totals for new carts)
- Validates pricing calculation contract (cartItemTotal: 2000.0, cartTotal: 2000.0, shippingPromoSavings: -10.99 for 2 items at $1000 each)

### Configuration Testing

- Environment variable substitution test
- Default fallback value test
- Application.properties parsing validation

## Out of Scope for S03

- **Service Implementations**: `ShoppingCartServiceImpl`, `PromoService`, `ShippingService` (S04)
- **REST Endpoints**: `CartEndpoint`, `JerseyConfig`, `CartServiceApplication` (S05)
- **Test Implementation**: Legacy tests remain unchanged (behavior preserved)
- **Business Logic**: Cart operations, pricing, shipping (handled by S04)

## Dependencies

**Preceding**: S02 (core entities) - Product, ShoppingCartItem, ShoppingCart models
**Following**: S04 (implementations) - ShoppingCartServiceImpl, PromoService, ShippingService
**Conversion Order**: Product → ShoppingCartItem → ShoppingCart → ShoppingCartService → CatalogService
