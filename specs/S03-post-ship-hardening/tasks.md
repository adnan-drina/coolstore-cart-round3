# S03 Post-ship Hardening Tasks

## T-001: Thread-safe cart storage with ConcurrentHashMap
**Class**: infer  
**Findings**: - (hardening story - post-review defects)

### Target Shape
Convert cart storage from thread-unsafe HashMap to ConcurrentHashMap with atomic operations:
- Change `private Map<String, ShoppingCart> carts;` to `private ConcurrentHashMap<String, ShoppingCart> carts;`
- Replace all `carts.put(cartId, cart)` calls with `carts.compute(cartId, (id, existing) -> cart)` for atomic updates
- Remove `carts.put()` in `getShoppingCart()` - only return existing cart or 404
- No synchronized blocks - leverage ConcurrentHashMap's internal thread-safety

### Files
- `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

### Verification
- Concurrent test exercises parallel add/price operations on same cart without corrupted totals
- All existing cart pricing tests continue to pass

---

## T-002: Cache policy with 60-second refresh guard
**Class**: infer  
**Findings**: - (hardening story)

### Target Shape  
Implement cache refresh guard to prevent excessive catalog fetches:
- Add `private long lastCatalogRefresh = 0;` field
- Add `private static final long CATALOG_REFRESH_MS = 60_000;` constant  
- In `getProduct()`: check `System.currentTimeMillis() - lastCatalogRefresh < CATALOG_REFRESH_MS` before fetching
- Unknown itemId + recent refresh (<60s) → return cached result (even if item missing)
- Unknown itemId + stale refresh (>=60s) → fetch new catalog, update timestamp
- Remove `productMap.clear()` - only populate with `putAll()`
- Store timestamp after successful catalog fetch

### Files
- `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

### Verification
- Cache test proves unknown-id probes no longer clear the cache
- Multiple rapid calls for missing items don't trigger multiple catalog fetches
- After 60s, missing items trigger catalog refresh

---

## T-003: GET idempotency - 404 on missing cart  
**Class**: infer  
**Findings**: - (hardening story - S02-authored API defect)

### Target Shape
Make GET operations truly idempotent - they never create state:
- Modify `ShoppingCartService.getShoppingCart(String)` to return `Optional<ShoppingCart>`
- CartEndpoint.getCart() returns 404 if Optional.empty(), 200 with cart if present
- Remove `computeIfAbsent()` pattern from service
- Cart creation only via mutating operations (add/set/checkout/delete)

### Files  
- `src/main/java/com/demo/service/ShoppingCartServiceImpl.java` (interface + implementation)
- `src/main/java/com/demo/service/ShoppingCartService.java` (interface)
- `src/main/java/com/demo/rest/CartEndpoint.java`

### Verification
- GET on missing cart ID returns 404 Not Found
- GET on existing cart returns 200 with cart data
- Cart creation via POST operations unchanged

---

## T-004: Acceptance check - change to GET with proper DTO
**Class**: infer  
**Findings**: - (hardening story - S02-authored API defect)

### Target Shape
Fix semantic mismatch for read-only health check:
- Change `@POST @Path("/acceptance-check")` to `@GET @Path("/acceptance-check")` 
- Replace hand-built JSON string with proper DTO: `Response.ok(Map.of("status", "ok")).build()`
- Maintains same `{"status": "ok"}` response body

### Files
- `src/main/java/com/demo/rest/CartEndpoint.java`

### Verification
- GET `/api/cart/acceptance-check` returns 200 OK
- Response body is proper JSON map, not raw string
- All existing acceptance tests pass

---

## T-005: Validation and error mapping with ExceptionMapper
**Class**: infer  
**Findings**: - (hardening story)

### Target Shape
Add proper input validation and error responses:
- Add `@Min(1)` validation on `quantity` path parameters in CartEndpoint methods
- Create `ServiceExceptionMapper` class implementing `ExceptionMapper<ProcessingException>`
- Map catalog service failures to 503 Service Unavailable with problem details
- Map validation constraint violations to 400 Bad Request with problem details
- Never return raw 500 stack traces

### Files
- `src/main/java/com/demo/rest/CartEndpoint.java`
- New: `src/main/java/com/demo/rest/ServiceExceptionMapper.java`

### Verification
- Negative/zero quantity returns 400 with validation error message
- Catalog failures return 503 with service unavailable message  
- Problem details format follows RFC 7807

---

## T-006: Dedupe-before-pricing consistency
**Class**: infer  
**Findings**: - (hardening story)

### Target Shape
Fix timing issue where dedupe runs after pricing:
- In `addItem()`: change to `cart.setShoppingCartItemList(dedupeCartItems(cart)); priceShoppingCart(cart);`
- In `set()`: same ordering - dedupe before pricing
- Ensure `promoSavings` calculations use deduped quantities consistently

### Files
- `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

### Verification
- Add characterization test for dedupe semantics (known contract gap)
- Promo calculations match displayed item quantities
- All pricing tests continue to pass

---

## T-007: Characterize and pin existing behavior
**Class**: infer  
**Findings**: - (behavioral contract preservation)

### Target Shape
Expand test coverage and pin behavioral contracts:
- Run complete existing test suite - all tests must pass
- Update create-on-GET assertions to 404 expectations (cite S03-post-ship-hardening brief)
- Add concurrency test for cart operations on same cart ID
- Add cache behavior test for missing product IDs  
- Add 400/503 error path tests
- Add dedupe characterization test
- Pin all existing assertion values (2000.0, -10.99, tier table, PromoService composition)

### Files
- All test files under `src/test/java/com/demo/`
- New test files: `src/test/java/com/demo/service/ConcurrencyTest.java`
- New test files: `src/test/java/com/demo/service/CacheTest.java`  
- New test files: `src/test/java/com/demo/rest/ErrorHandlingTest.java`
- New test files: `src/test/java/com/demo/service/DedupeTest.java`

### Verification  
- Complete test suite passes including new hardening tests
- All pinned behavioral values remain unchanged
- New concurrency/cache/validation tests exercise hardened behavior
- Factory pipeline green (build + SonarQube gate)
- Application deployed and accessible via `/q/health`
- `GET /api/cart/acceptance-check` returns 200 OK  
- Root route `/` returns 200 OK

---

## Behavioral Contract Preservation

**Scope**: UI Surface Waiver and Preserved Integrations
- **UI Surface**: No legacy UI surface exists - application is API-only with REST endpoints under /api/cart
- **CATALOG_ENDPOINT**: Preserved env-driven configuration, remains unchanged per brief
- **getMockProducts**: Preserved for test compatibility, remains unchanged per brief
- **All behavioral contracts**: Pinned test values (2000.0, -10.99, tier table, PromoService composition) remain unchanged