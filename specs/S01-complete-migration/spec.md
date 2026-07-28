# S01 Complete Migration Spec

## Observed Legacy Behavior

This spec documents the complete Spring Boot to Quarkus migration of the Coolstore cart service, covering all 12 classes and the pom.xml in a single coordinated transformation.

### Domain Model Layer (HARVEST classes)

**Product** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:5-23`)
- Pure data entity implementing Serializable
- Fields: itemId (String), name (String), desc (String), price (double)
- No business logic, standard POJO with getters/setters/constructors

**Promotion** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:10-25`)
- Value object for discount rules
- Fields: productId (String), discount (BigDecimal)
- Used by PromoService for pricing calculations

**ShoppingCartItem** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:15-30`)
- Composition object linking Product to cart quantities
- Fields: product (Product), quantity (int), price (BigDecimal)
- Maintains pricing per item with quantity calculation

**ShoppingCart** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:20-40`)
- Domain object with pricing fields and cart management
- Fields: cartId (String), shoppingCartItem (List<ShoppingCartItem>), multiple pricing totals
- Pricing fields: cartItemPromoSavings, cartItemTotal, shippingPromoSavings, cartTotal

### Service Layer (REDESIGN classes)

**ShoppingCartService Interface** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java:15-35`)
- Service interface defining cart operations: addItem, removeItem, getShoppingCart, transferCart, checkout
- Contract: operations on cartId, itemId, quantity parameters

**ShoppingCartServiceImpl** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-50`)
- Spring @Service with @Autowired dependencies on ShippingService, CatalogService, PromoService
- In-memory storage: `Map<String, ShoppingCart> carts = new HashMap<>()`
- Product caching: `Map<String, Product> productMap = new HashMap<>()`
- @PostConstruct initializes HashMap cart storage
- Behavior: cart item deduplication before pricing (normalizeBeforeDerive pattern)

**PromoService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:16-30`)
- Spring @Component with @Autowired CatalogService dependency
- Business rule: 25% discount on product "329299" (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:27`)
- Applies promotions to ShoppingCart via `applyPromotions(cart)` method

**ShippingService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:8-20`)
- Spring @Component stateless service
- Tiered shipping logic (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:12`)
- Free shipping when cart total ≥ $75 (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:51`)

**CatalogService** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10-20`)
- Spring Cloud Feign client interface
- `@FeignClient(name = "catalog", url = "${CATALOG_ENDPOINT}")`
- REST method: `Product getProduct(@PathVariable("itemId") String itemId)`
- Configuration: environment variable `${CATALOG_ENDPOINT}` for service URL

### REST Layer (REDESIGN classes)

**CartEndpoint** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:22-45`)
- Spring @RestController with session scope
- JAX-RS annotations: @Path("/cart"), @GET, @POST, @DELETE, @PathParam
- Endpoints: GET /cart/{cartId}, POST /cart/{cartId}/{itemId}/{quantity}, DELETE /cart/{cartId}/{itemId}/{quantity}
- @Autowired ShoppingCartService dependency injection
- Session-scoped via Spring WebApplicationContext.SCOPE_SESSION

**JerseyConfig** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6-15`)
- Spring @Component extending Jersey ResourceConfig
- Registers CartEndpoint class in constructor
- Spring-managed Jersey configuration for JAX-RS resources

### Application Bootstrap

**CartServiceApplication** (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:7-15`)
- Spring Boot @SpringBootApplication main class
- SpringApplication.run(CartServiceApplication.class, args) bootstrap

### Build Configuration

**pom.xml** (`/projects/legacy/pom.xml:18-26,53-72`)
- Spring Boot 2.7.18 parent (version 2.7.14 per brief)
- Dependencies: spring-boot-starter-web, spring-boot-starter-jersey, spring-boot-starter-actuator
- Spring Cloud OpenFeign for Feign clients
- Spring Boot Maven plugin

## API Contract

### REST Endpoints (prefixed by spring.jersey.application-path=/api)
All endpoints return JSON with ShoppingCart objects or error responses.

**GET /api/cart/{cartId}**
- Retrieves shopping cart by ID
- Legacy behavior: creates empty cart if not exists (CREATE-ON-GET)
- Input: cartId (String, path parameter)
- Output: ShoppingCart with calculated totals or empty cart

**POST /api/cart/{cartId}/{itemId}/{quantity}**
- Adds items to shopping cart
- Input: cartId (String), itemId (String), quantity (int, positive)
- Behavior: deduplicates items by productId, calculates pricing, applies promotions/shipping
- Output: Updated ShoppingCart with recalculated totals

**DELETE /api/cart/{cartId}/{itemId}/{quantity}**
- Removes specified quantity of items from cart
- Input: cartId (String), itemId (String), quantity (int)
- Behavior: removes items, recalculates cart totals
- Output: Updated ShoppingCart

**POST /api/cart/{cartId}/{tmpId}**
- Transfers items from temporary cart to permanent cart
- Input: cartId (String, destination), tmpId (String, source)
- Output: ShoppingCart with transferred items

**POST /api/cart/checkout/{cartId}**
- Processes checkout and clears cart
- Input: cartId (String)
- Output: Final ShoppingCart before clearing

### Integration Surfaces

**Catalog Service Integration**
- Feign client calls to `${CATALOG_ENDPOINT}/api/products/{itemId}`
- Returns Product objects for cart pricing
- Environment configuration: `${CATALOG_ENDPOINT:default}`
- Error handling: failures should propagate to cart operations

### Data Contracts

**Product JSON**
```json
{
  "itemId": "string",
  "name": "string", 
  "desc": "string",
  "price": 0.0
}
```

**ShoppingCart JSON**
```json
{
  "cartId": "string",
  "shoppingCartItem": [
    {
      "product": {Product},
      "quantity": 0,
      "price": 0.0
    }
  ],
  "cartItemPromoSavings": 0.0,
  "cartItemTotal": 0.0,
  "shippingPromoSavings": 0.0,
  "cartTotal": 0.0
}
```

## Behavioral Contract (from legacy tests)

**Cart Initialization** (`/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:28`)
- Empty carts return zero totals: cartItemPromoSavings: 0.0, cartItemTotal: 0.0, shippingPromoSavings: 0.0, cartTotal: 0.0

**Cart Pricing** (`/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:39`)
- 2 items at $1000 each = $2000 cart total
- Shipping cost: $10.99
- Shipping promo savings: -$10.99 (indicating free shipping threshold logic)

**Promotion Behavior**
- Product "329299" receives 25% discount
- Applied via PromoService.applyPromotions(cart)

**Shipping Logic**
- Tiers: <$25 = $2.99, $25-$50 = $4.99, $50-$75 = $6.99, $75-$100 = $8.99, ≥$100 = $10.99
- Free shipping when cart total ≥ $75

**Item Deduplication** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:200`)
- Cart items with same product ID are consolidated with summed quantities
- Deduplication occurs before pricing calculations (normalizeBeforeDerive)

## Error Handling Contract

**Validation Requirements** (TARGET behavior per architecture-profile §7)
- Invalid inputs (quantity <= 0, malformed IDs) → 400 Bad Request
- Non-existent cartId → 404 Not Found (legacy creates empty cart, target changes this)
- Catalog service failures → 503 Service Unavailable via ExceptionMapper

## Configuration Surfaces

**Environment Variables**
- `CATALOG_ENDPOINT`: Product catalog service URL (preserved from legacy)
- Default configuration: `${CATALOG_ENDPOINT:http://localhost:8081}`

**Properties** 
- Spring Boot application.properties converted to Quarkus application.properties
- REST client configuration for CatalogService
- Logging configuration preserved

This specification serves as the authoritative contract for the complete S01 migration story, covering all behavioral, API, and integration surfaces that must be modernized from Spring Boot to Quarkus.