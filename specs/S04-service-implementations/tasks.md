# S04 Service Implementations Tasks

## Task List (dependency-ordered, rewrite tasks first)

#### T-001: Convert PromoService to CDI @ApplicationScoped  
**Class: rewrite**  
**Findings**: `springboot-di-to-quarkus-00003`  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:15`  
**Target**: `src/main/java/com/demo/service/PromoService.java`

Harvest transformed file from staging directory. Replace Spring @Component with Quarkus @ApplicationScoped. Add CDI constructor (no dependencies). Preserve business logic unchanged.

**Actions**:
1. Harvest from `migration/staging/src/main/java/com/redhat/coolstore/service/PromoService.java`
2. Replace `@Component` with `@ApplicationScoped`
3. Add public constructor with no parameters (CDI requirement)
4. Ensure thread-safe access to promotionSet
5. Preserve seeded promotion (item "329299" at 25% off)
6. Run tests to verify promotion calculation behavior

---

#### T-002: Convert ShippingService to CDI @ApplicationScoped
**Class: rewrite**  
**Findings**: `springboot-di-to-quarkus-00003`  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:7`  
**Target**: `src/main/java/com/demo/service/ShippingService.java`

Harvest transformed file from staging directory. Replace Spring @Component with Quarkus @ApplicationScoped. Add CDI constructor (no dependencies). Preserve exact shipping tier calculation.

**Actions**:
1. Harvest from `migration/staging/src/main/java/com/redhat/coolstore/service/ShippingService.java`
2. Replace `@Component` with `@ApplicationScoped`
3. Add public constructor with no parameters (CDI requirement)
4. Preserve exact shipping tiers: <$25: $2.99, $25-$49.99: $4.99, $50-$74.99: $6.99, $75-$99.99: $8.99, $100+: $10.99
5. Validate free shipping promotion ≥$75
6. Run tests to verify shipping calculation logic

---

#### T-003: Convert ShoppingCartServiceImpl to CDI with thread-safe state
**Class: infer**  
**Findings**: `springboot-di-to-quarkus-00003`  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-39`  
**Target Contract**: `migration/architecture-profile.md:155-159`

Convert from Spring @Service/@Autowired to CDI @ApplicationScoped with constructor injection. Implement thread-safe cart storage using ConcurrentHashMap. Fix catalog service field typo.

**Decided Design**:
- File mapping: `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`
- Annotations: `@ApplicationScoped` (replace `@Service`), CDI constructor injection
- Field signatures: Constructor(ShippingService, CatalogService, PromoService) with corrected field names
- State management: `ConcurrentHashMap<String, ShoppingCart>` replace `Map<String, ShoppingCart>`

**Actions**:
1. Harvest from `migration/staging/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`
2. Replace `@Service` with `@ApplicationScoped`
3. Convert @Autowired fields to constructor parameters:
   - ShippingService ss
   - CatalogService catalogServie (fix typo: catalogService)
   - PromoService ps
4. Replace `Map<String, ShoppingCart> carts` with `ConcurrentHashMap<String, ShoppingCart>`
5. Initialize carts in constructor (remove @PostConstruct)
6. Replace HashMap access with ConcurrentHashMap.compute() operations for thread safety
7. Preserve all business logic methods unchanged:
   - priceShoppingCart() workflow
   - addItem/deleteItem operations
   - product caching behavior
   - item deduplication algorithm
8. Run ShoppingCartServiceTest to verify behavioral preservation

---

#### T-004: Port ShoppingCartServiceTest to CDI injection
**Class: infer**  
**Findings**: N/A (test modernization)  
**Source**: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java`  
**Target**: `src/test/java/com/demo/service/ShoppingCartServiceTest.java`

Convert test from Spring @MockBean to Quarkus @InjectMock for CDI services. Verify all existing test assertions pass with CDI injection.

**Decided Design**:
- File mapping: `src/test/java/com/demo/service/ShoppingCartServiceTest.java`
- Test framework: Quarkus @InjectMock replace Spring @MockBean
- Service signatures: Same test methods with CDI injection patterns

**Actions**:
1. Read legacy test at `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:28,38,57`
2. Replace Spring @MockBean with Quarkus @InjectMock for:
   - CatalogService
   - ShippingService  
   - PromoService
3. Update ShoppingCartServiceImpl instantiation to use @Inject
4. Preserve all behavioral assertions:
   - Cart initialization: zero totals for new carts
   - Pricing: 2 items × $1000 = $2000 cartItemTotal, -$10.99 shippingPromoSavings
   - Product mock: returns Product("2222", "Bike", "Super bike", 200)
5. Run tests to verify CDI injection works correctly
6. Verify shipping tiers and free shipping logic in tests

---

#### T-005: Create ConcurrencyTest for thread-safe validation
**Class: infer**  
**Findings**: N/A (contract gap from brief:129)  
**Source**: `migration/briefs/S04-service-implementations.md:125-128`  
**Target**: `src/test/java/com/demo/service/ConcurrencyTest.java`

Create new test class for concurrent access patterns to validate thread-safe cart operations with ConcurrentHashMap.

**Decided Design**:
- File mapping: `src/test/java/com/demo/service/ConcurrencyTest.java`
- Test annotations: @QuarkusTest for integration testing
- Service injection: @Inject ShoppingCartServiceImpl

**Actions**:
1. Create new test class `src/test/java/com/demo/service/ConcurrencyTest.java`
2. Test concurrent cart creation and access:
   - Multiple threads creating carts with same cartId
   - Concurrent addItem operations to same cart
   - Concurrent read operations during cart updates
3. Verify no race conditions in cart pricing calculations
4. Test atomic operations using ConcurrentHashMap.compute()
5. Validate cart state consistency across concurrent operations
6. Ensure business logic remains correct under concurrent load

---

#### T-006: Create ErrorHandlingTest for service failure scenarios
**Class: infer**  
**Findings**: N/A (contract gap from brief:126-127)  
**Source**: `migration/briefs/S04-service-implementations.md:125-127`  
**Target**: `src/test/java/com/demo/service/ErrorHandlingTest.java`

Create new test class for catalog service failures and input validation to ensure robust error handling behavior.

**Decided Design**:
- File mapping: `src/test/java/com/demo/service/ErrorHandlingTest.java`
- Test annotations: @QuarkusTest with @InjectMock CatalogService
- Exception handling: Verify service-level exception mapping for REST layer

**Actions**:
1. Create new test class `src/test/java/com/demo/service/ErrorHandlingTest.java`
2. Test catalog service unavailable scenarios:
   - Simulate catalog service throwing exceptions
   - Verify cart operations handle failures gracefully
   - Test product caching fallback behavior
3. Test input validation:
   - Add items with negative quantities (should reject or clamp)
   - Add items with invalid/missing item IDs
   - Test empty/null cart IDs
4. Verify logging behavior for error conditions
5. Ensure service-level exceptions surface correctly for REST layer handling
6. Validate that cart state remains consistent after errors

---

## UI Surface Waiver

**Rationale**: This story modernizes service layer implementations (ShoppingCartServiceImpl, PromoService, ShippingService) with no REST endpoints or web UI components. The user-facing cart operations are handled at the REST layer (S05), not in service implementations. Service implementations provide business logic and data management without direct user interaction surfaces.

**Evidence**: No user interface code exists in the service classes; all user interaction occurs through CartEndpoint (S05 story scope).

## Preserved Integration Points

**CATALOG_ENDPOINT**: Environment-driven catalog service URL configuration maintained per `migration.yaml` preserve requirement. Configuration lives in application properties, preserved through S01 platform modernization. No service implementation changes required for endpoint URL handling.

## Dependency Order Compliance

Tasks follow `migration/dependency-order.md:8-26`:
1. PromoService (no dependencies) - T-001 rewrite
2. ShippingService (no dependencies) - T-002 rewrite  
3. ShoppingCartServiceImpl (depends on PromoService, ShippingService) - T-003 infer
4. Test porting and characterization - T-004-006 infer
