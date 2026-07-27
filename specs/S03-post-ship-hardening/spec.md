# S03 Post-ship Hardening Specification

## Legacy Behavior Observed

### ShoppingCartServiceImpl.java - Thread Safety and Cache Issues

**File**: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

#### Thread Safety Issue (Lines 39-40, 55)
```java
private Map<String, ShoppingCart> carts;
private final Map<String, Product> productMap = new HashMap<>();
```

**Problem**: Both maps are plain `HashMap` in an `@ApplicationScoped` singleton. This creates a **thread safety hazard** where:
- Concurrent cart mutations on the same cart ID can interleave
- Product cache operations can race during catalog fetches
- No synchronization means corrupted state under concurrent access

**Evidence**: The service has no synchronization mechanisms and is accessed via REST endpoints that can handle concurrent requests.

#### Cache Policy Issue (Lines 109-117)
```java
@Override
public Product getProduct(String itemId) {
    if (!productMap.containsKey(itemId)) {
        // Legacy contract: catalog failures propagate — never a
        // fabricated fallback (migration.yaml forbidden:)
        List<Product> products = catalogService.products();
        productMap.clear();
        productMap.putAll(products.stream().collect(Collectors.toMap(Product::getItemId, Function.identity())));
    }
    return productMap.get(itemId);
}
```

**Problem**: Every unknown itemId **wipes and refetches the entire catalog**:
- Performance degradation: O(n) catalog fetch for every new product ID
- Race condition: `clear()` can lose concurrent cache additions
- Unnecessary network load: Refetches full catalog even when one item is missing

**Evidence**: Lines 109-117 show the `clear()` and `putAll()` pattern for every cache miss.

#### Dedupe Timing Issue (Lines 169-170, 191-192)
```java
priceShoppingCart(cart);
cart.setShoppingCartItemList(dedupeCartItems(cart));
```

**Problem**: `dedupeCartItems()` runs AFTER pricing, but pricing uses the pre-dedupe item list. This causes inconsistent `promoSavings` values because:
- Pricing calculates totals on individual cart items
- Dedupe merges quantities AFTER pricing
- Final response shows promoSavings calculated on different item counts than displayed

**Evidence**: Lines 169-170 in `addItem()` and 191-192 in `set()` show dedupe after pricing.

### CartEndpoint.java - API Idempotency Issues

**File**: `src/main/java/com/demo/rest/CartEndpoint.java`

#### GET Creates Cart (Lines 28-33)
```java
@GET
@Path("/{cartId}")
@Produces(MediaType.APPLICATION_JSON)
public ShoppingCart getCart(@PathParam("cartId") String cartId) {
    return shoppingCartService.getShoppingCart(cartId);
}
```

**Problem**: **GET is not idempotent** - it creates a cart if missing:
- `ShoppingCartServiceImpl.getShoppingCart()` (line 60) uses `computeIfAbsent()`
- Missing cart ID returns a newly created cart (200 OK)
- Violates REST semantics: GET should not create resources

**Evidence**: The service method `getShoppingCart()` at line 60 in ShoppingCartServiceImpl uses `computeIfAbsent()`.

#### POST Acceptance Check (Lines 68-72)
```java
@POST
@Path("/acceptance-check")
public Response acceptanceCheck() {
    return Response.ok().entity("{\"status\": \"ok\"}").build();
}
```

**Problem**: **Semantic mismatch** - this is a read-only health check but uses POST:
- Platform acceptance curl is GET (as documented in brief)
- Returns raw JSON string instead of proper Map/DTO
- POST should be reserved for resource creation/modification

**Evidence**: Lines 68-72 show the POST method with hand-built JSON string.

### Missing Validation and Error Handling

**Current State**: No validation on path parameters, no error mapping:
- Negative or zero quantities accepted without 400 response
- Catalog failures return 500 stack traces instead of proper 503
- No `ExceptionMapper` for structured error responses

## Behavioral Contracts to Preserve

All existing pinned behavioral tests must remain green **unchanged** except assertions that verify create-on-GET behavior, which must change to 404 expectations citing this brief.

**Pinned Test Expectations**:
- Pricing: 2000.0, -10.99 values
- Tier table shipping calculations
- PromoService zeroes shippingTotal composition
- All quantity-based calculations
- JSON serialization compatibility

**Test Changes Required** (cite this spec):
- Tests asserting create-on-GET → 404 expectations
- `acceptance-check` endpoint test → GET method

## API Contract Changes

### GET /{cartId}
- **Old behavior**: Missing cart → creates new cart, returns 200 OK
- **New behavior**: Missing cart → 404 Not Found
- **Justification**: REST idempotency - GET should not modify state

### GET /acceptance-check  
- **Old behavior**: POST method, returns raw JSON string
- **New behavior**: GET method, returns `{"status": "ok"}` as proper Map/DTO
- **Justification**: Semantic correctness - read-only endpoint should use GET

## Error Response Specifications

### 400 Bad Request
- **Trigger**: `quantity <= 0` in add/delete operations
- **Body**: Problem Details JSON with validation error message
- **Example**: `{"type":"about:blank","title":"Validation Failed","status":400,"detail":"Quantity must be positive"}`

### 404 Not Found
- **Trigger**: GET on missing cart ID
- **Body**: Problem Details JSON with not found message
- **Example**: `{"type":"about:blank","title":"Cart Not Found","status":404,"detail":"Cart 'abc123' does not exist"}`

### 503 Service Unavailable
- **Trigger**: Catalog service failures in `getProduct()`
- **Body**: Problem Details JSON with service unavailable message
- **Example**: `{"type":"about:blank","title":"Service Unavailable","status":503,"detail":"Catalog service temporarily unavailable"}`

## Concurrency Requirements

### Cart Operation Concurrency
- Multiple concurrent requests for the same cart ID must not corrupt totals
- Single cart's pricing operations must be atomic
- Add/remove operations on same cart must sequence correctly

### Cache Concurrency
- Product catalog fetches must be thread-safe
- Concurrent cache misses should not cause multiple catalog fetches
- Cache refreshes must not lose existing cached data

## Cache Refresh Policy

### 60-Second Refresh Guard
- Track last catalog fetch timestamp
- Unknown item ID checks timestamp before fetching
- If refresh needed and < 60s since last fetch → return cached result (even if item missing)
- If refresh needed and >= 60s since last fetch → fetch new catalog
- Never `clear()` the cache - only populate missing entries

### Performance Requirements
- Unknown product ID lookup: O(1) cache hit or single O(n) catalog fetch
- No full cache clears on individual product misses
- Concurrent catalog requests: only one fetch, others wait for result