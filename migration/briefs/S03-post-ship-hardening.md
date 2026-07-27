# S03: Post-ship hardening — concurrency, API correctness, cache policy

## Goal & position

Close the defect classes the fidelity contract deliberately carried
from the legacy (and the two S02-authored defects), found by the
post-ship semantic code review (rhoai3 docs/DRYRUN-M-PROCESS.md,
"Code-quality review"). Depends on S02 (the service is deployed and
accepted); the pinned behavioral contracts stay green throughout —
hardening changes HOW the code is correct, never WHAT it computes.

## In scope

- `src/main/java/com/demo/service/ShoppingCartServiceImpl.java` — the
  shared-state and cache findings:
  ```java
  private Map<String, ShoppingCart> carts;                       // plain HashMap, @ApplicationScoped singleton
  private final Map<String, Product> productMap = new HashMap<>();
  // getProduct: ANY unknown itemId wipes and refetches the whole cache
  if (!productMap.containsKey(itemId)) {
      List<Product> products = catalogService.products();
      productMap.clear();
      productMap.putAll(...);
  }
  ```
- `src/main/java/com/demo/rest/CartEndpoint.java` — API correctness:
  ```java
  @GET @Path("/{cartId}")            // GET CREATES a cart (computeIfAbsent downstream)
  public ShoppingCart getCart(...)
  @POST @Path("/acceptance-check")   // POST-only; the platform acceptance curl is a GET
  public Response acceptanceCheck()
  ```
- `src/main/resources/application.properties` — no changes expected;
  config stays as shipped.
- New: a JAX-RS `ExceptionMapper` class for catalog/processing
  failures; validation on path params.

## Out of scope

Externalizing cart state (Redis/DB) — the in-memory decision is
documented in the architecture profile and stands for this demo;
S03 makes the in-memory implementation CORRECT, not distributed.
Replica-divergence remains a documented limitation. No model/pricing
logic changes of any kind.

## Decided target shapes

1. **Thread safety**: `carts` and `productMap` become
   `ConcurrentHashMap`; cart mutations go through
   `carts.compute(cartId, ...)` so a single cart's pricing is not
   interleaved; no `synchronized` blocks on the service.
2. **Cache policy**: an unknown itemId must NOT wipe the cache — cache
   the full catalog on first need, refresh only when the requested id
   is absent AND a refresh has not run within the last 60 seconds
   (simple timestamp guard, no new dependencies); an id still absent
   after refresh is simply unknown (no clear()).
3. **API idempotency**: `GET /{cartId}` returns the cart WITHOUT
   creating one (absent → 404); creation happens on the mutating
   verbs (add/set) exactly as before. `acceptance-check` becomes
   `@GET` (it is a read) returning the same `{"status":"ok"}` shape
   as a proper DTO/Map, not a hand-built string.
4. **Validation + error mapping**: `quantity <= 0` → 400 with a
   problem-detail body; catalog `ProcessingException` → 503 via a
   dedicated `ExceptionMapper` (never a raw 500 stack trace). No
   fabricated fallbacks — failure semantics stay honest.
5. **Dedupe consistency**: `dedupeCartItems` runs BEFORE final pricing
   (or re-prices after) so item-level `promoSavings` in responses are
   consistent; add the missing characterization test for dedupe
   semantics (the profile's known contract gap).

## Contracts owned by this story

- **Findings**: none (hardening story — see the review, not MTA).
- **Preserve**: CATALOG_ENDPOINT stays env-driven and untouched.
- **Behavioral pins**: ALL existing pinned tests stay green unchanged
  — 2000.0 / −10.99 / tier table / composition (PromoService zeroes
  shippingTotal). The 404-on-missing-cart change is a deliberate,
  documented API change: update ONLY the tests that asserted
  create-on-GET, citing this brief.
- **Forbidden**: unchanged tripwires; no mock/fallback data in
  src/main, ever.

## Done-criteria

- builds + sensors green every commit; milestone green at story end
- a concurrency test exercises parallel add/price on one cart (no
  corrupted totals); cache test proves unknown-id probes no longer
  clear the cache; 400/404/503 paths tested
- dedupe characterization test exists and passes
- deploy story: factory green, deployed, `GET /api/cart/acceptance-check`
  200, route `/` 200
