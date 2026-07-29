# S04 Service Implementations Specification

## Legacy Behavior and API Contract

This specification documents the observed behavior of service implementation classes in the legacy Spring Boot application, focusing on dependency injection patterns and business logic that must be preserved during migration to Quarkus CDI.

### Service Classes in Scope

#### 1. ShoppingCartServiceImpl (REDESIGN)

**Legacy Location**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28-222`

**Current Spring DI Pattern**:
```java
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    ShippingService ss;
    
    @Autowired
    CatalogService catalogServie;  // Note: typo in original
    
    @Autowired
    PromoService ps;
    
    Map<String, ShoppingCart> carts = new HashMap<>();
    
    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new HashMap<>();
    }
}
```

**Observed Business Operations**:

1. **Cart Initialization** (`getShoppingCart`, line 53-64):
   - Creates new cart if cartId doesn't exist
   - Auto-initializes cart with zero totals
   - Triggers pricing calculation on existing carts

2. **Cart Pricing Workflow** (`priceShoppingCart`, line 66-85):
   - Applies item-level promotions via `PromoService`
   - Calculates shipping costs via `ShippingService`
   - Applies shipping promotions (free shipping ≥ $75)
   - Computes final cart total

3. **Item Management Operations**:
   - `addItem` (line 151-176): Adds items with validation, dedupes cart items
   - `deleteItem` (line 119-139): Removes items or reduces quantities
   - `checkout` (line 142-148): Clears cart items after order

4. **Product Caching** (`getProduct`, line 108-116):
   - Fetches products from `CatalogService` and caches in `HashMap`
   - Cache never expires (documented TODO)

5. **Item Deduplication** (`dedupeCartItems`, line 200-221):
   - Aggregates quantities for same productId
   - Preserves product references and pricing

**Key Dependencies**:
- `ShippingService`: Tiered shipping calculation ($2.99-$10.99 thresholds)
- `CatalogService`: Product information retrieval
- `PromoService`: Promotional discount application

#### 2. PromoService (REDESIGN)

**Legacy Location**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:15-77`

**Current Spring DI Pattern**:
```java
@Component
public class PromoService implements Serializable {
    private Set<Promotion> promotionSet = new HashSet<>();
    
    public PromoService() {
        promotionSet = new HashSet<Promotion>();
        promotionSet.add(new Promotion("329299", .25));  // 25% off item 329299
    }
}
```

**Observed Business Operations**:

1. **Promotion Application** (`applyCartItemPromotions`, line 30-46):
   - Maps promotions by itemId
   - Applies percentage discounts to item prices
   - Calculates promotional savings per line item

2. **Shipping Promotion** (`applyShippingPromotions`, line 48-56):
   - Free shipping promotion for carts ≥ $75
   - Sets shipping total to $0 and records savings

3. **Promotion Management**:
   - Seeded with single promotion in constructor
   - Provides mutable access via getter/setter methods

#### 3. ShippingService (REDESIGN)

**Legacy Location**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:7-25`

**Current Spring DI Pattern**:
```java
@Component
public class ShippingService {
    public void calculateShipping(ShoppingCart sc) {
        if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
            sc.setShippingTotal(2.99);
        } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
            sc.setShippingTotal(4.99);
        } // ... additional tiers
    }
}
```

**Observed Business Operations**:

1. **Tiered Shipping Calculation**:
   - < $25: $2.99
   - $25-$49.99: $4.99
   - $50-$74.99: $6.99
   - $75-$99.99: $8.99
   - $100+: $10.99

2. **Stateless Operation**: No instance variables, pure function behavior

### Behavioral Contracts from Tests

**Source**: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:28,38,57`

1. **Cart Initialization**: New carts return zero for all monetary fields
2. **Pricing**: 2 items × $1000 = $2000 cartItemTotal, -$10.99 shippingPromoSavings (free shipping)
3. **Product Mock**: CatalogService returns Product("2222", "Bike", "Super bike", 200)

### Thread Safety Requirements

From `architecture-profile.md:103`, target contract requires:
- ShoppingCartServiceImpl: `ConcurrentHashMap<String, ShoppingCart>` with `compute()` operations
- PromoService: Thread-safe access to promotion data (static promotion set)
- ShippingService: Stateless, inherently thread-safe

### Error Handling Patterns

1. **Catalog Service Failures**: Logged as warnings, cart operation continues
2. **Invalid Products**: Logged warning, cart not modified
3. **Pricing Exceptions**: Item removed from cart, exception propagated

### API Surface Preservation

- All public methods maintain same signatures
- Return types unchanged (ShoppingCart, Product, void)
- Exception handling behavior preserved
- Business logic algorithms unchanged

## Evidence Links

- Legacy ShoppingCartServiceImpl: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`
- Legacy PromoService: `/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java`
- Legacy ShippingService: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java`
- Test specifications: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java`
- Architecture profile: `migration/architecture-profile.md:155-167`
