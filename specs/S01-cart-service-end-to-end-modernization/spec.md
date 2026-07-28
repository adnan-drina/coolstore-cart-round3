# S01 Cart Service End-to-End Modernization — Specification

## Legacy Behavior Summary

The cart service is a Spring Boot application providing shopping cart lifecycle operations with integrated pricing calculations. It maintains persistent cart state across HTTP sessions, combines product prices with promotional discounts and shipping costs based on tiered thresholds.

## API Contract

**Base path:** `/cart` (CartEndpoint.java:23)

### REST Endpoints

1. **GET /cart/{cartId}** - Retrieve cart with calculated totals
   - Returns: ShoppingCart JSON with all pricing fields populated
   - Creates new cart if ID doesn't exist
   - Pricing is recalculated on every retrieval (CartEndpoint.java:34-35)

2. **POST /cart/{cartId}/{itemId}/{quantity}** - Add items to cart
   - Parameters: cartId (String), itemId (String), quantity (int)
   - Returns: Updated ShoppingCart with recalculated totals
   - Behavior: Validates product exists via CatalogService, creates ShoppingCartItem, applies deduplication logic
   - Exception handling: Removes item and rethrows if pricing fails (ShoppingCartServiceImpl.java:169-172)

3. **POST /cart/{cartId}/{tmpId}** - Transfer items between temp and persistent cart
   - Parameters: cartId (String), tmpId (String) 
   - Returns: Updated ShoppingCart with transferred items and recalculated totals
   - Used for cart session management during checkout flow

4. **DELETE /cart/{cartId}/{itemId}/{quantity}** - Remove items from cart
   - Parameters: cartId (String), itemId (String), quantity (int)
   - Returns: Updated ShoppingCart with recalculated totals
   - Behavior: Removes entire line item if quantity >= item quantity, otherwise decrements quantity

5. **POST /checkout/{cartId}** - Clear cart after purchase
   - Parameters: cartId (String)
   - Returns: Empty ShoppingCart with reset totals
   - Behavior: Removes all items but preserves cart ID

### Pricing Behavior (Pinned by ShoppingCartServiceTest)

**Cart Initialization Contract** (ShoppingCartServiceTest.java:28-36):
- New carts start with zero totals: cartItemPromoSavings=0.0, cartItemTotal=0.0, shippingPromoSavings=0.0, cartTotal=0.0
- Each cart gets unique ID and persists across service calls via HashMap (ShoppingCartServiceImpl.java:42)

**Pricing Calculation Contract** (ShoppingCartServiceTest.java:38-54):
- Cart with 2 items × $1000 = $2000 cartItemTotal
- Shipping calculation triggers for carts ≥$100: shippingTotal=10.99
- Free shipping promotion applies for carts ≥$75: shippingPromoSavings=-10.99, shippingTotal=0.0
- Final cartTotal = cartItemTotal + shippingTotal = $2000.00

**Product Lookup Contract** (ShoppingCartServiceTest.java:56-63):
- CatalogService mocked to return ProductsObjectMother.createVehicleProducts()
- getProduct("2222") returns Bike with price $200.00
- Products are cached in productMap HashMap (ShoppingCartServiceImpl.java:44)

### External Dependencies

**CatalogService Feign Client** (CatalogService.java:10):
- Interface: `@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")`
- Method: `List<Product> products()` via `@GetMapping("/api/products")`
- Environment variable: `${CATALOG_ENDPOINT:http://localhost:8081}` (application.properties:6)
- Preserved via migration.yaml:23 preserve list

### Domain Model

**ShoppingCart** (aggregate root):
- Fields: cartId, shoppingCartItemList, cartItemTotal, cartItemPromoSavings, shippingTotal, shippingPromoSavings, cartTotal
- Pricing orchestration: coordinates PromoService and ShippingService calculations (ShoppingCartServiceImpl.java:66-85)

**ShoppingCartItem** (entity):
- Fields: price, quantity, promoSavings, product (Product value object)
- Deduplication logic: combines quantities for same product ID (ShoppingCartServiceImpl.java:200-221)

**Product** (value object):
- Fields: itemId, name, desc, price
- Implements Serializable for HTTP session persistence

**Promotion**:
- Fields: itemId, percentOff
- Applied at item level by PromoService

### Configuration

- **CATALOG_ENDPOINT**: Environment-driven config preserved per migration.yaml:23
- **Port**: Default Spring Boot port (application.properties defines API path)
- **Session scope**: CartEndpoint uses WebApplicationContext.SCOPE_SESSION

### Behavioral Contract Gaps

**Missing Tests (add as characterization tests):**
1. Promotional discounts at item level (PromoService applies percentage discounts to specific product IDs)
2. Multi-quantity line items deduplication logic (ShoppingCartServiceImpl.java:200-221)  
3. Temp cart → persistent cart transfer operation (ShoppingCartServiceImpl.java:179-198)

### Legacy File References

- **CartEndpoint**: src/main/java/com/redhat/coolstore/rest/CartEndpoint.java
- **ShoppingCartServiceImpl**: src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java
- **CatalogService**: src/main/java/com/redhat/coolstore/service/CatalogService.java
- **ShoppingCart**: src/main/java/com/redhat/coolstore/model/ShoppingCart.java
- **ShoppingCartItem**: src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java
- **Product**: src/main/java/com/redhat/coolstore/model/Product.java
- **PromoService**: src/main/java/com/redhat/coolstore/service/PromoService.java
- **ShippingService**: src/main/java/com/redhat/coolstore/service/ShippingService.java
- **pom.xml**: Spring Boot platform dependencies
- **ShoppingCartServiceTest**: src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java

### Forbidden Fabrication Triggers

Per migration.yaml:24-31, the following patterns must not appear in migrated code:
- getMockProducts method
- "mock products" / "Mock products" / "mock Products" strings
- "Fallback to mock" text