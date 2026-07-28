# S01 Foundation Data Models - Migration Plan

## Overview

This plan converts the three foundation data model classes (Product, ShoppingCart, ShoppingCartItem) with HARVEST role designation. These classes preserve existing structure and behavior with only mechanical javax→jakarta import conversion and package restructuring.

## Jakarta Import Conversion

**Finding**: `javax-to-jakarta-import-00001` [recipe]  
**Status**: Already executed via recipe in migration/recipe-log.md  
**Action**: Verification only - confirm converted imports in harvested files

## God-Node Characterization Tasks

**Finding**: `removed-javaee-modules-00020` [rewrite]  
**Issue**: JEE modules removed from JDK 11+ → provided by Quarkus platform dependencies  
**Action**: Class: rewrite - Convert package structure and verify Quarkus platform integration

## HARVEST Class Conversion Strategy

**Design decision**: All three model classes are HARVEST role (preserve existing structure and behavior):

1. **Product** → Harvest to `com.demo.model.Product`
   - Package: `com.redhat.coolstore.model` → `com.demo.model`
   - Preserve: All fields, constructors, methods, serialization
   - Convert: Only package declaration and import statements

2. **ShoppingCart** → Harvest to `com.demo.model.ShoppingCart`  
   - Package: `com.redhat.coolstore.model` → `com.demo.model`
   - Preserve: All fields (cartId, item list, pricing totals), constructors, methods
   - Convert: Only package declaration and import statements

3. **ShoppingCartItem** → Harvest to `com.demo.model.ShoppingCartItem`
   - Package: `com.redhat.coolstore.model` → `com.demo.model`  
   - Preserve: All fields (product, quantity, pricing), constructors, methods
   - Convert: Only package declaration and import statements

## Package Restructuring

**Source packages**: `com.redhat.coolstore.model.*`  
**Target package**: `com.demo.model.*`  
**Mapping rationale**: Modernized application uses `com.demo` package root per architecture profile

## Dependency Impact

**High fan-in classes requiring early conversion**:
- ShoppingCart: 5 incoming references (god node #1)
- Product: 4 incoming references (god node #2)  
- ShoppingCartItem: 3 incoming references (god node #3)

**Dependent classes** (converted in later stories):
- ShoppingCartServiceImpl - references all three models
- PromoService, ShippingService - use Product
- CartEndpoint - uses ShoppingCart and ShoppingCartItem

## Conversion Order

Per dependency-order.md conversion sequence:
1. Product (order 2)
2. ShoppingCart (order 1) 
3. ShoppingCartItem (order 4)

This ensures compilation at every commit as dependencies are converted before dependents.

## Verification Requirements

**Behavioral contracts from ShoppingCartServiceTest**:
- Cart initialization with zero totals preserved
- Product structure (id, name, description, price) unchanged
- ShoppingCartItem pricing calculations preserved
- Serialization compatibility maintained

**Jakarta import verification**:
- No remaining javax.* imports
- Jakarta imports properly configured
- Serializable interface compatibility

## Build Integration

**POM dependencies**: No additional dependencies required for model conversion  
**Quarkus integration**: Models work with Quarkus CDI without additional configuration  
**Test compatibility**: Existing test assertions continue to pass