# S03: Service Interfaces Tasks

#### T-001: Migrate ShoppingCartService interface to Jakarta imports
**Class**: rewrite
**Findings**: javax-to-jakarta-import-00001 (1)
**Goal**: Convert javax.* imports to jakarta.* in ShoppingCartService interface
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`
**Target**: `src/main/java/com/demo/service/ShoppingCartService.java`
**Package migration**: com.redhat.coolstore → com.demo (per migration.yaml targetPackage)
**Acceptance**: ShoppingCartService.java with jakarta.* imports in com.demo package; mvn clean test green

#### T-003: Migrate application.properties for environment-driven configuration
**Class**: rewrite
**Findings**: localhost-http-00001 (2), demo-env-integration-00001 (1)
**Goal**: Convert Spring Boot properties to Quarkus format while preserving CATALOG_ENDPOINT environment variable
**Source**: `/projects/legacy/src/main/resources/application.properties`
**Target**: `src/main/resources/application.properties`
**Package migration**: N/A - configuration file
**Acceptance**: application.properties with quarkus.rest-client.catalog-service.url; ${CATALOG_ENDPOINT} preserved; sensors green

#### T-002: Convert CatalogService from FeignClient to quarkus-rest-client
**Class**: infer
**Findings**: springboot-web-to-quarkus-00000 (1), localhost-http-00001 (1)
**Goal**: Convert CatalogService from Spring Cloud FeignClient to Quarkus REST client
**Source**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java`
**Target**: `src/main/java/com/demo/service/CatalogService.java`
**Package migration**: com.redhat.coolstore → com.demo (per migration.yaml targetPackage)
**Target design**:
- Convert @FeignClient to @RegisterRestClient + @Path annotations
- Change @GetMapping("/api/products") to @GET on getProducts() method
- Maintain List<Product> return type and method contract
- Preserve environment-driven URL via quarkus.rest-client configuration
**Acceptance**: CatalogService.java with quarkus-rest-client annotations in com.demo package; REST client contract preserved; mvn clean test green

#### T-004: Port characterization tests for service interfaces
**Class**: infer
**Findings**: springboot-web-to-quarkus-00000 (1)
**Goal**: Port ShoppingCartServiceTest to Quarkus and verify interface contracts
**Source**: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java`
**Target**: `src/test/java/com/demo/service/ShoppingCartServiceTest.java`
**Package migration**: com.redhat.coolstore → com.demo (per migration.yaml targetPackage)
**Target design**:
- Convert @RunWith(SpringRunner.class) to @QuarkusTest
- Maintain all existing test assertions for interface method contracts
- Mock CatalogService integration using @InjectMock or Mockito.mock()
- Test environment-driven configuration via ${CATALOG_ENDPOINT} substitution
- Verify all ShoppingCartService interface methods preserve legacy behavior
- DO NOT use getMockProducts or mock fallbacks (forbidden per migration.yaml)
**UI Surface**: Legacy UI surface is REST endpoints - service interfaces do not expose UI directly
**Acceptance**: ShoppingCartServiceTest.java with @QuarkusTest in com.demo package; all interface contracts verified; environment config test passes; ≥80% coverage; no forbidden mocks
