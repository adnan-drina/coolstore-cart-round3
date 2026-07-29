# S04 Service Implementations Migration Plan

## Summary

This plan converts three service implementation classes from Spring dependency injection to Quarkus CDI with constructor injection, implementing thread-safe state management for cart storage and preserving all business logic contracts.

## Migration Mapping

### Class: ShoppingCartServiceImpl (REDESIGN)

**Legacy Pattern**: Spring @Service with @Autowired field injection
**Target Pattern**: CDI @ApplicationScoped with constructor injection
**Target Contract**: `architecture-profile.md:155-159`

**Findings**: `springboot-di-to-quarkus-00003` (lines 28,33,36,39 in ShoppingCartServiceImpl.java)

**Rewrite Actions** (mechanical):
- Replace `@Service` with `@ApplicationScoped`
- Replace `@Autowired` fields with constructor parameters
- Convert `Map<String, ShoppingCart> carts` to `ConcurrentHashMap<String, ShoppingCart>`
- Initialize `carts` in constructor (remove `@PostConstruct`)
- Preserve all business logic methods unchanged

**Infer Actions** (judgment):
- Implement thread-safe cart access using `ConcurrentHashMap.compute()` operations
- Fix typo in `catalogServie` → `catalogService`
- Maintain same public API signatures
- Ensure backward compatibility for cart lifecycle operations

### Class: PromoService (REDESIGN)

**Legacy Pattern**: Spring @Component with no dependencies
**Target Pattern**: CDI @ApplicationScoped with constructor injection
**Target Contract**: `architecture-profile.md:161-163`

**Findings**: `springboot-di-to-quarkus-00003` (line 15 in PromoService.java)

**Rewrite Actions** (mechanical):
- Replace `@Component` with `@ApplicationScoped`
- Add empty constructor (no dependencies to inject)

**Infer Actions** (judgment):
- Ensure thread-safe access to static promotion data
- Make promotion set immutable after construction
- Preserve seeded promotion data (25% off item "329299")

### Class: ShippingService (REDESIGN)

**Legacy Pattern**: Spring @Component with no dependencies
**Target Pattern**: CDI @ApplicationScoped with constructor injection
**Target Contract**: `architecture-profile.md:165-167`

**Findings**: `springboot-di-to-quarkus-00003` (line 7 in ShippingService.java)

**Rewrite Actions** (mechanical):
- Replace `@Component` with `@ApplicationScoped`
- Add empty constructor (no dependencies to inject)

**Infer Actions** (judgment):
- Preserve exact shipping tier calculation logic
- Ensure stateless operation (no mutable state)
- Maintain deterministic shipping cost computation

## Thread Safety Strategy

### ShoppingCartServiceImpl
- **Current**: Regular `HashMap<String, ShoppingCart>` with potential race conditions
- **Target**: `ConcurrentHashMap<String, ShoppingCart>` with `compute()` operations
- **Implementation**: Replace direct map access with atomic compute operations for thread-safe updates

### PromoService
- **Current**: `Set<Promotion>` potentially shared across threads
- **Target**: Thread-safe access to static promotion data
- **Implementation**: Immutable promotion set after construction, accessed via thread-safe collections

### ShippingService
- **Current**: Already stateless, inherently thread-safe
- **Target**: Maintain stateless design
- **Implementation**: No changes needed beyond DI annotation

## Business Logic Preservation

All existing business operations must produce identical results:

1. **Cart Pricing**: Same promotion application order (item promos → shipping calc → shipping promos → final total)
2. **Shipping Tiers**: Exact threshold values ($2.99, $4.99, $6.99, $8.99, $10.99)
3. **Free Shipping**: Cart total ≥ $75 gets $0 shipping
4. **Item Deduplication**: Same algorithm for aggregating quantities by productId
5. **Product Caching**: Same catalog service interaction and caching behavior

## Dependencies and Compilation Order

Per `migration/dependency-order.md:8-26`, service classes convert in this dependency-respecting order:

1. PromoService (no dependencies) — rewrite task
2. ShippingService (no dependencies) — rewrite task  
3. ShoppingCartServiceImpl (depends on PromoService, ShippingService, CatalogService) — infer task

## Test Strategy

**God Node Coverage**: ShoppingCart (fan-in: 5) requires characterization tests before ShoppingCartServiceImpl conversion

**Test Tasks**:
1. Characterize ShoppingCart behavior with legacy tests
2. Port ShoppingCartServiceTest with CDI injection
3. Add concurrency tests for multi-user cart access
4. Add error handling tests for catalog service failures
5. Add input validation tests for negative quantities/invalid items

## Success Criteria

- All services use `@ApplicationScoped` with constructor injection (zero @Autowired)
- ShoppingCartServiceImpl uses `ConcurrentHashMap` with atomic operations
- All existing tests pass with CDI injection
- Business logic behavior identical to legacy (same pricing, same shipping tiers)
- Thread-safe cart operations for concurrent users
- No breaking changes to service API contracts
