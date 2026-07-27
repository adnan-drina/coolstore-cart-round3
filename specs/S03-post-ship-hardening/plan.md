# S03 Post-ship Hardening Plan

## Mapping Strategy
This hardening story closes five defect classes identified in the post-ship semantic review. All changes preserve behavioral contracts - the WHAT stays the same, only the HOW becomes correct.

## Class: Infer (Design Decisions)
These tasks require judgment about API contracts, threading models, and cache policies - no mechanical transformation can derive the correct behavior.

### T-001: Thread-safe cart storage with ConcurrentHashMap
**Finding scope**: None (hardening story - post-review defects)
**File**: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

**Target shape**: 
- `carts` field becomes `ConcurrentHashMap<String, ShoppingCart>`
- All cart mutations use `carts.compute(cartId, ...)` for atomic operations
- Remove any `carts.put()` calls outside of compute operations
- No synchronized blocks - leverage ConcurrentHashMap's thread-safety

**Behavioral contract**: Single cart's pricing cannot be interleaved with other operations on same cart ID.

**Verification**: Concurrent test exercises parallel add/price on one cart without corrupted totals.

### T-002: Cache policy with 60-second refresh guard  
**Finding scope**: None (hardening story)
**File**: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

**Target shape**:
- Add `long lastCatalogRefresh = 0` field
- Add `static final long CATALOG_REFRESH_MS = 60_000` constant
- In `getProduct()`: check timestamp before fetching catalog
- Unknown itemId + <60s since last fetch → return cached result (even if missing)
- Unknown itemId + >=60s since last fetch → fetch new catalog  
- Never call `productMap.clear()` - only populate missing entries with `putAll()`

**Behavioral contract**: Unknown product ID never wipes existing cache; catalog fetches limited to once per 60s maximum.

**Verification**: Cache test proves unknown-id probes no longer clear the cache.

### T-003: GET idempotency - 404 on missing cart
**Finding scope**: None (hardening story - S02-authored API defect)
**File**: `src/main/java/com/demo/rest/CartEndpoint.java`
**Service dependency**: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

**Target shape**:
- `ShoppingCartService.getShoppingCart(String)` changes to return `Optional<ShoppingCart>` or null for missing cart
- `CartEndpoint.getCart()` returns 404 if cart not found, 200 with cart if exists
- Remove `computeIfAbsent()` pattern - creation only via add/set operations

**Behavioral contract**: GET never creates state - missing resource returns 404.

**Test changes**: Update create-on-GET assertions to 404 expectations citing this brief.

### T-004: Acceptance check - change to GET with proper DTO
**Finding scope**: None (hardening story - S02-authored API defect)
**File**: `src/main/java/com/demo/rest/CartEndpoint.java`

**Target shape**:
- Change `@POST` to `@GET` on `/acceptance-check`
- Return proper Map/DTO instead of raw JSON string: `Response.ok(Map.of("status", "ok")).build()`

**Behavioral contract**: Read-only health check uses GET method.

**Verification**: GET `/api/cart/acceptance-check` returns 200 OK.

### T-005: Validation and error mapping with ExceptionMapper
**Finding scope**: None (hardening story)
**Files**: 
- `src/main/java/com/demo/rest/CartEndpoint.java` (validation)
- New: `src/main/java/com/demo/rest/ServiceExceptionMapper.java`

**Target shape**:
- Add `@Min(1)` validation on `quantity` path parameter in CartEndpoint
- Add custom `ExceptionMapper<ProcessingException>` that returns 503 with problem details
- Add validation constraint violations → 400 with problem details
- Map catalog failures to 503 (never 500 stack traces)

**Behavioral contract**: Invalid input returns 400, catalog failures return 503, no raw stack traces.

**Verification**: 400/503 paths tested with proper problem details.

### T-006: Dedupe-before-pricing consistency
**Finding scope**: None (hardening story)
**File**: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

**Target shape**:
- Move `dedupeCartItems()` call BEFORE `priceShoppingCart()` in:
  - `addItem()`: `cart.setShoppingCartItemList(dedupeCartItems(cart)); priceShoppingCart(cart);`
  - `set()`: same ordering
- Ensure `promoSavings` calculated on deduped item quantities

**Behavioral contract**: Item-level `promoSavings` consistent with displayed quantities.

**Verification**: Add characterization test for dedupe semantics - known contract gap from profile.

## Test Task

### T-007: Characterize and pin existing behavior
**Finding scope**: None (behavioral contract preservation)
**Files**: All test files under `src/test/java/com/demo/`

**Target shape**:
- Run all existing tests - all must pass except create-on-GET assertions
- Update create-on-GET test assertions to 404 expectations (cite brief)
- Add concurrency test for cart operations
- Add cache behavior test  
- Add 400/503 error path tests
- Add dedupe characterization test

**Behavioral contract**: All pinned values (2000.0, -10.99, tier table, PromoService composition) remain unchanged.

**Verification**: Complete test suite passes including new hardening tests.

## Summary
- **T-001, T-002**: Thread safety and cache improvements (ShoppingCartServiceImpl)
- **T-003, T-004**: API idempotency fixes (CartEndpoint + service)
- **T-005**: Validation and error mapping (CartEndpoint + ExceptionMapper)
- **T-006**: Dedupe timing fix (ShoppingCartServiceImpl)
- **T-007**: Test suite expansion and pinning

All tasks preserve existing behavioral contracts while closing the identified defect classes.