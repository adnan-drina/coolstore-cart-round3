# S03: Service Interfaces Migration Plan

## Migration Strategy

This story establishes service interface contracts and configures environment-driven catalog service integration. The plan follows the dependency order: Product → ShoppingCartItem → ShoppingCart → ShoppingCartService → CatalogService.

### Class Roles

**HARVEST Classes**:
- **ShoppingCartService** — Service interface defining cart operations contract, preserved as-is with jakarta.* imports

**REDESIGN Classes**:
- **CatalogService** — REST client converted from Spring Cloud FeignClient to quarkus-rest-client with environment-driven URL

## Task Breakdown

### T-001: Migrate ShoppingCartService interface to Jakarta imports
**Class**: rewrite  
**Finding Rules**: javax-to-jakarta-import-00001  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`  
**Target**: `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`

Mechanical transform:
- javax.enterprise.context.* imports → jakarta.enterprise.context.*
- javax.* imports → jakarta.* imports (if any)
- Preserve exact method signatures and contracts

### T-002: Convert CatalogService from FeignClient to quarkus-rest-client  
**Class**: infer  
**Finding Rules**: springboot-web-to-quarkus-00000, localhost-http-00001  
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`  
**Target**: `src/main/java/com/redhat/coolstore/service/CatalogService.java`

Design decision: Convert Spring Cloud FeignClient to Quarkus REST client with @RegisterRestClient annotation. Method name changed from `products()` to `getProducts()` for clarity.

**Decided target shape**:
```java
package com.redhat.coolstore.service;

import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.redhat.coolstore.model.Product;

@RegisterRestClient
@Path("/api/products")
public interface CatalogService {
    @GET
    List<Product> getProducts();
}
```

### T-003: Migrate application.properties for environment-driven configuration
**Class**: rewrite  
**Finding Rules**: localhost-http-00001, demo-env-integration-00001  
**Source**: `/projects/legacy/src/main/resources/application.properties`  
**Target**: `src/main/resources/application.properties`

Mechanical transform:
- Convert Spring Boot properties to Quarkus format
- Maintain ${CATALOG_ENDPOINT} environment variable substitution
- Add quarkus.rest-client.catalog-service.url configuration for REST client
- Preserve default fallback value: http://localhost:8081

## Dependencies and Order

**Conversion Order** (per migration/dependency-order.md):
1. **Product** (already migrated in S02)
2. **ShoppingCartItem** (already migrated in S02) 
3. **ShoppingCart** (already migrated in S02)
4. **ShoppingCartService** (T-001) - interface migration
5. **CatalogService** (T-002) - REST client conversion  
6. **application.properties** (T-003) - configuration migration

This order ensures compilation at every commit and follows the dependency graph from M1 analysis.

## Test Strategy

**Characterization Tests** (T-004):
- Port existing ShoppingCartServiceTest to Quarkus (@QuarkusTest)
- Verify ShoppingCartService interface methods contract preservation
- Mock CatalogService integration for interface testing
- Test environment-driven configuration with ${CATALOG_ENDPOINT} substitution

**Coverage Requirements**:
- All migrated interface methods tested
- Environment configuration verification test
- REST client contract validation
- Interface compatibility verification

## Quality Gates

**Build Verification**:
- `mvn -q clean test` passes at each commit
- Interface methods compile with jakarta.* imports
- CatalogService REST client properly configured
- Environment configuration test passes

**Code Quality**:
- Zero SonarQube violations on migrated files
- Interface contracts exactly preserved
- Environment variable configuration maintained

## Success Criteria

- [ ] ShoppingCartService compiles with jakarta.* imports
- [ ] CatalogService converted to quarkus-rest-client
- [ ] application.properties maintains ${CATALOG_ENDPOINT} configuration  
- [ ] All interface methods preserved exactly
- [ ] Environment-driven configuration test passes
- [ ] Sensors green at every commit
- [ ] Story milestone green
