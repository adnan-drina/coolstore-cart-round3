# S03 Plan: Service Interfaces and Catalog Client

## Migration Mapping Summary

This plan addresses the service interface contracts and catalog client integration for the Coolstore Cart Service. The plan converts FeignClient REST client to quarkus-rest-client while preserving all interface contracts and environment-driven configuration.

**Class Roles**:
- **ShoppingCartService** - HARVEST (service interface)
- **CatalogService** - REDESIGN (REST client)

## Migration Tasks

### ShoppingCartService Interface Migration

**Task**: T-030 - Convert ShoppingCartService interface imports

**Class**: rewrite

**Finding Rule**: javax-to-jakarta-import-00001 [recipe]

**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`

**Target**: `src/main/java/com/demo/service/ShoppingCartService.java`

**Action**: Update interface imports from javax.* to jakarta.* while preserving all method signatures

**Evidence**:
```
// Source (legacy)
import com.redhat.coolstore.model.Product;
import com.redhat.coolstore.model.ShoppingCart;

// Target (jakarta imports)
import com.demo.model.Product;
import com.demo.model.ShoppingCart;
```

**Rationale**: ShoppingCartService is HARVEST class - preserve exact interface contract with only import package changes

---

### CatalogService REST Client Migration

**Task**: T-031 - Convert CatalogService from FeignClient to quarkus-rest-client

**Class**: infer

**Finding Rules**: 
- localhost-http-00001 (Local HTTP Calls → cloud-readiness)
- demo-env-integration-00001 (Environment-driven configuration preserved)

**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`

**Target**: `src/main/java/com/demo/service/CatalogService.java`

**Action**: Convert Spring Cloud OpenFeign client to Quarkus REST Client with environment-driven URL

**Target Contract**:
```java
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.demo.model.Product;

@RegisterRestClient(configKey = "catalog-service")
@Path("/api/products")
public interface CatalogService {
    @GET
    List<Product> products();
}
```

**Configuration**: `application.properties`
```
# Catalog service configuration - environment-driven
quarkus.rest-client.catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}
```

**Rationale**: CatalogService is REDESIGN class requiring:
- Replace `@FeignClient` with `@RegisterRestClient`
- Convert Spring `@GetMapping("/api/products")` to JAX-RS `@GET @Path("/api/products")`
- Preserve `CATALOG_ENDPOINT` environment variable per preserve contract
- Maintain `products()` method name for calling compatibility (ShoppingCartServiceImpl dependency)

**Environment Contract**: 
- Variable: `CATALOG_ENDPOINT`
- Default: `http://localhost:8081`
- Location: `application.properties` with `quarkus.rest-client.catalog-service.url` key

**Dependencies**:
- Product model (S02) - already migrated
- ShoppingCartService interface (T-030) - must precede this task

---

### Configuration Migration

**Task**: T-032 - Migrate application.properties with environment-driven config

**Class**: rewrite

**Finding Rules**:
- localhost-http-00001 (Local HTTP Calls → env-driven config)
- demo-env-integration-00001 (Environment-driven configuration preserved)

**Source**: `/projects/legacy/src/main/resources/application.properties`

**Target**: `src/main/resources/application.properties`

**Action**: Convert Spring Boot properties to Quarkus configuration for REST client

**Source Configuration**:
```properties
spring.application.name=coolstore-cart-legacy
spring.jersey.application-path=/api

# Catalog products endpoint used by the Feign CatalogService.
# Override with env CATALOG_ENDPOINT or -DCATALOG_ENDPOINT=...
CATALOG_ENDPOINT=http://localhost:8081
```

**Target Configuration**:
```properties
# Quarkus application configuration

# JAX-RS application path (replaces spring.jersey.application-path)
quarkus.application.name=coolstore-cart

# REST client configuration for catalog service
quarkus.rest-client.catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}
```

**Rationale**: Preserve `CATALOG_ENDPOINT` environment variable while converting to Quarkus REST client configuration format

**Preserve Verification**: `CATALOG_ENDPOINT` environment variable name maintained exactly per migration.yaml preserve contract

---

### POM Dependencies Migration

**Task**: T-033 - Update Maven dependencies for quarkus-rest-client

**Class**: rewrite

**Finding Rules**: 
- javax-to-jakarta-import-00001 [recipe]

**Action**: Add quarkus-rest-client dependency to pom.xml

**Target**: `pom.xml`

**Dependency Addition**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client</artifactId>
</dependency>
```

**Rationale**: Support CatalogService REST client conversion to quarkus-rest-client

**Evidence**: Platform foundation (S01) provides quarkus-rest dependency, ensuring compatibility

---

### Interface Contract Testing

**Task**: T-034 - Create interface contract tests

**Class**: infer

**Action**: Develop tests to verify interface preservation and environment-driven configuration

**Test Scope**:
1. **ShoppingCartService Interface Test**: Verify all seven method signatures preserved
   - getShoppingCart(String cartId)
   - getProduct(String itemId)
   - deleteItem(String cartId, String itemId, int quantity)
   - checkout(String cartId)
   - addItem(String cartId, String itemId, int quantity)
   - set(String cartId, String tmpId)
   - priceShoppingCart(ShoppingCart sc)

2. **CatalogService REST Client Test**: Verify environment-driven configuration
   - Environment variable substitution test
   - Default fallback value test
   - REST client annotation validation
   - Method signature preservation (products() returns List<Product>)

3. **Configuration Test**: Verify application.properties parsing
   - CATALOG_ENDPOINT variable preservation
   - Quarkus REST client configuration format

**Test Location**: `src/test/java/com/demo/service/ServiceInterfacesTest.java`

**Rationale**: Ensure interface contracts maintained and configuration preserved through migration

---

## Migration Dependencies

**Dependency Order**: Product → ShoppingCartItem → ShoppingCart → ShoppingCartService → CatalogService

**Task Dependencies**:
1. T-030 (ShoppingCartService) - prerequisite for T-031 (CatalogService)
2. T-033 (POM dependencies) - prerequisite for T-031 (CatalogService)
3. T-031 (CatalogService) - prerequisite for T-032 (Configuration)
4. T-030, T-031, T-032 - prerequisites for T-034 (Interface Testing)

## Quality Gate Coverage

**Coverage Areas**:
- Interface method signature preservation (7 methods)
- Environment variable configuration preservation
- REST client conversion functionality
- Configuration format conversion
- Package import updates (javax.* → jakarta.*)

**Test Strategy**:
- Interface contracts validated before implementation migration
- Configuration tests ensure environment variable preservation
- REST client tests verify functionality post-conversion

## Story Completion Criteria

✅ **ShoppingCartService interface compiles** with jakarta.* imports  
✅ **CatalogService converted** to quarkus-rest-client with @RegisterRestClient  
✅ **application.properties maintains** ${CATALOG_ENDPOINT} environment configuration  
✅ **Interface methods preserved** exactly - no behavioral changes  
✅ **Implementation classes** (S04) can compile against these interfaces  
✅ **Environment-driven configuration** test passes (catalog service URL configurable)  
