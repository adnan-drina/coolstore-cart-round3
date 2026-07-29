# S03 Service Interfaces and Catalog Client Tasks

#### T-030: Convert ShoppingCartService interface imports
**Class**: rewrite
**Findings**: javax-to-jakarta-import-00001 [recipe]
**Goal**: Update ShoppingCartService interface from javax.* to jakarta.* imports while preserving all method signatures
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- src/main/java/com/redhat/coolstore/service/ShoppingCartService.java → src/main/java/com/demo/service/ShoppingCartService.java
- Preserve exact method signatures: getShoppingCart(String cartId), getProduct(String itemId), deleteItem(String cartId, String itemId, int quantity), checkout(String cartId), addItem(String cartId, String itemId, int quantity), set(String cartId, String tmpId), priceShoppingCart(ShoppingCart sc)
**Acceptance**: src/main/java/com/demo/service/ShoppingCartService.java with jakarta.* imports; interface compiles

#### T-031: Migrate application.properties with environment-driven config
**Class**: rewrite
**Findings**: localhost-http-00001 (1 incident), demo-env-integration-00001 (1 incident)
**Goal**: Convert Spring Boot properties to Quarkus configuration for REST client while preserving CATALOG_ENDPOINT
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- src/main/resources/application.properties → src/main/resources/application.properties
- Add quarkus.rest-client.catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}
- Preserve CATALOG_ENDPOINT environment variable per migration.yaml preserve contract
**Acceptance**: src/main/resources/application.properties with Quarkus REST client configuration and CATALOG_ENDPOINT variable

#### T-032: Update Maven dependencies for quarkus-rest-client
**Class**: rewrite
**Findings**: javax-to-jakarta-import-00001 [recipe]
**Goal**: Add quarkus-rest-client dependency to support CatalogService REST client conversion
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- pom.xml → pom.xml
- Add quarkus-rest-client dependency to support REST client functionality
**Acceptance**: pom.xml with quarkus-rest-client dependency; Maven build succeeds

#### T-033: Convert CatalogService from FeignClient to quarkus-rest-client
**Class**: infer
**Findings**: localhost-http-00001 (2 incidents), demo-env-integration-00001 (1 incident)
**Goal**: Convert Spring Cloud OpenFeign client to Quarkus REST Client with environment-driven URL configuration
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- src/main/java/com/redhat/coolstore/service/CatalogService.java → src/main/java/com/demo/service/CatalogService.java
- Convert @FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}") to @RegisterRestClient(configKey = "catalog-service")
- Convert @GetMapping("/api/products") to @GET @Path("/api/products")
- Preserve products() method signature returning List<Product>
**Acceptance**: src/main/java/com/demo/service/CatalogService.java with @RegisterRestClient and JAX-RS annotations; CATALOG_ENDPOINT environment variable preserved

#### T-034: Create interface contract tests
**Class**: infer
**Findings**: (interface preservation testing)
**Goal**: Develop tests to verify interface preservation and environment-driven configuration
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- src/test/java/com/demo/service/ServiceInterfacesTest.java
- Test ShoppingCartService interface methods (7 signatures preserved)
- Test CatalogService REST client configuration and method signature
- Test CATALOG_ENDPOINT environment variable substitution and default fallback
**Acceptance**: ServiceInterfacesTest.java with interface contract validation; all tests pass

#### T-035: Address lint preserve verification
**Class**: infer
**Findings**: (lint verification task)
**Goal**: Ensure all migration.yaml preserve items are referenced in the plan
**Target design** (infer tasks — REQUIRED, cite MAPPINGS.md):
- Verify CATALOG_ENDPOINT environment variable preservation per migration.yaml preserve contract
- Document that getMockProducts is a forbidden item (not preserve), handled by forbidden tripwire sensors
**Acceptance**: All actual preserve items (CATALOG_ENDPOINT) properly mapped; forbidden items handled by sensors

**UI Surface Coverage Waiver**: Service interfaces are internal contracts accessed through REST endpoints (CartEndpoint in S05). The user-facing API surface is preserved through the REST endpoints that will be implemented in S05, ensuring backward compatibility for all client code.
