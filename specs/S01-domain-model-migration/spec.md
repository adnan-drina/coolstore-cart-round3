# S01 Domain Model Migration Specification

## Scope

This specification documents the domain model layer migration for the Coolstore cart service, covering the core POJO entities that form the business domain. The domain models are migrated as the foundation layer to enable downstream service and endpoint modernization.

## Legacy Code Evidence

### Core Domain Entities

**ShoppingCart** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1-127`):
- Central domain entity implementing `Serializable`
- Contains cart state: totals, savings, cartId, and item list
- Business operations: `addShoppingCartItem()`, `removeShoppingCartItem()`, `resetShoppingCartItemList()`
- Pricing fields: `cartItemTotal`, `cartItemPromoSavings`, `shippingTotal`, `shippingPromoSavings`, `cartTotal`

**Product** (`/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:1-54`):
- Serializable product data model
- Fields: `itemId`, `name`, `desc`, `price`
- Used by ShoppingCartItem and CatalogService integration

**ShoppingCartItem** (referenced in dependency-order.md:3):
- Individual cart item with quantity and pricing
- Links Product to ShoppingCart through shoppingCartItemList

**Promotion** (referenced in dependency-order.md:4):
- Promotion rules for discount calculations
- Referenced in pricing services

## API Contract

### Domain Object Serialization

**ShoppingCart JSON Contract**:
```json
{
  "cartId": "string",
  "cartItemTotal": "double",
  "cartItemPromoSavings": "double", 
  "shippingTotal": "double",
  "shippingPromoSavings": "double",
  "cartTotal": "double",
  "shoppingCartItemList": [
    {
      "product": {
        "itemId": "string",
        "name": "string", 
        "desc": "string",
        "price": "double"
      },
      "quantity": "integer",
      "price": "double"
    }
  ]
}
```

### Behavioral Contracts from Legacy Tests

**Primary Test Contract** (`ShoppingCartServiceTest.java:27-64`):

1. **Cart Initialization** (`should_get_initialized_shopping_cart_in_case_of_not_exists:28-36`):
   - Empty cart returns zero totals for all pricing fields
   - Assertions: `cartItemPromoSavings = 0.0`, `cartItemTotal = 0.0`, `shippingPromoSavings = 0.0`, `cartTotal = 0.0`

2. **Pricing Calculation** (`should_calculate_price_of_cart:39-54`) - **KEY ASSERTION**:
   - Cart with 2x $1000 items = $2000 cart item total
   - Shipping promotion applies -$10.99 discount when cart total ≥ $75
   - Final cart total = $2000 (item total $2000 + shipping total $0 after promotion)
   - Product data: `Product("1111", "Car", "Super car", 1000)`

**Boundary Test Contract** (`CartServiceBoundaryTest.java:34-46`):
- Integration-level validation of same pricing logic
- 2x $1000 items = $2000 cart total
- Shipping promotion -$10.99 applies above $75 cart threshold

### Shipping Tier Calculations (Untested Contract Gap)

**ShippingService** (`ShippingService.java:10-23`):
- $0-25 = $2.99 shipping
- $25-50 = $4.99 shipping  
- $50-75 = $6.99 shipping
- $75-100 = $8.99 shipping
- $100+ = $10.99 shipping

## Legacy Architecture Context

### Dependency Relationships (from dependency-order.md)

**God Nodes** (highest fan-in):
1. `ShoppingCart` (fan-in: 5) — Central domain entity referenced by all pricing operations
2. `Product` (fan-in: 3) — Shared product data model consumed by cart items and catalog service
3. `ShoppingCartItem` (fan-in: 2) — Links products to cart
4. `Promotion` (fan-in: 1) — Promotion rule entity

### Package Migration

**Source Package**: `com.redhat.coolstore.model.*`
**Target Package**: `com.demo.model.*`
**Rationale**: Project root is `com.demo` per architecture-profile.md:82

## Out-of-Scope Boundaries

**Not Migrated in S01** (owned by S02/S03):
- Service layer: `ShoppingCartServiceImpl`, `PromoService`, `ShippingService`
- REST endpoint: `CartEndpoint` 
- Application bootstrap: `CartServiceApplication`, `JerseyConfig`
- External integration: `CatalogService` Feign client

**UI Surface Waiver**: Legacy UI surface is waived for S01 since domain models provide backend-only data contracts and are consumed exclusively by the service layer modernization covered in S02.

## Preservation Requirements

### Serialization Compatibility
- Domain objects must maintain JSON serialization compatibility for existing REST API consumers
- Field names and types unchanged to preserve external contract
- `serialVersionUID` preserved for Java serialization

### Business Logic Preservation
- Cart operations (`add`, `remove`, `reset`) maintain exact behavioral contract
- Pricing calculation logic preserved through service layer (migrated in S02)
- No fabrication tripwires in domain models

### Test Coverage Requirements
- **Characterization tests**: Pin legacy assertion values from ShoppingCartServiceTest
- **Integration validation**: Boundary test coverage for cart operations
- **Shipping tier tests**: Coverage for untested shipping calculation logic
