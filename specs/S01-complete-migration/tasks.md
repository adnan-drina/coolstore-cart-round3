# S01 Complete Migration Tasks

#### T-001: javax→jakarta import transformation
**Class**: rewrite
**Findings**: jakarta-jaxrs-to-quarkus-00010 (15 incidents)
**Goal**: Transform javax.ws.rs imports to jakarta.ws.rs across all JAX-RS resources
**Acceptance**: /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java javax imports → jakarta equivalents

#### T-002: Spring Boot to Quarkus parent POM conversion
**Class**: rewrite
**Findings**: springboot-parent-pom-to-quarkus-00000 (1 incident), javaee-pom-to-quarkus-00010 (1 incident)
**Goal**: Replace Spring Boot parent with Quarkus platform BOM
**Acceptance**: /projects/legacy/pom.xml parent section uses com.redhat.quarkus.platform BOM

#### T-003: Maven dependency conversion - Web/Jersey
**Class**: rewrite
**Findings**: springboot-web-to-quarkus-00000 (3 incidents), jakarta-jaxrs-to-quarkus-00010 (5 incidents), javaee-pom-to-quarkus-00020 (2 incidents)
**Goal**: Replace Spring Web/Jersey dependencies with Quarkus REST dependencies
**Acceptance**: /projects/legacy/pom.xml has quarkus-rest, quarkus-rest-client instead of spring-boot-starter-web/spring-boot-starter-jersey

#### T-004: Maven dependency conversion - Actuator/Metrics
**Class**: rewrite
**Findings**: springboot-actuator-to-quarkus-0100 (2 incidents), springboot-metrics-to-quarkus-0100 (3 incidents), springboot-metrics-to-quarkus-0200 (2 incidents), javaee-pom-to-quarkus-00030 (1 incident)
**Goal**: Replace Spring Boot Actuator/Micrometer with Quarkus SmallRye Health/Metrics
**Acceptance**: /projects/legacy/pom.xml has quarkus-smallrye-health, quarkus-smallrye-metrics

#### T-005: Maven dependency conversion - Feign to REST Client
**Class**: rewrite
**Findings**: spring-components-00001 (1 incident), spring-components-00002 (1 incident), javaee-pom-to-quarkus-00040 (1 incident)
**Goal**: Replace Spring Cloud OpenFeign with Quarkus REST client
**Acceptance**: /projects/legacy/pom.xml has quarkus-rest-client and removes spring-cloud-starter-openfeign

#### T-006: Maven plugin conversion
**Class**: rewrite
**Findings**: springboot-plugins-to-quarkus-0000 (1 incident), javaee-pom-to-quarkus-00050 (1 incident)
**Goal**: Replace Spring Boot Maven plugin with Quarkus Maven plugin
**Acceptance**: /projects/legacy/pom.xml build plugins section has quarkus-maven-plugin

#### T-007: Java EE modules removal handling
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (1 incident), javaee-pom-to-quarkus-00060 (1 incident)
**Goal**: Handle Java EE module dependencies and Spring Cloud dependencies removal
**Acceptance**: /projects/legacy/pom.xml removes spring-cloud-dependencies and unused Java EE modules

#### T-008: Bootstrap class removal
**Class**: rewrite
**Findings**: springboot-annotations-to-quarkus-00000 (1 incident), javaee-pom-to-quarkus-00080 (1 incident)
**Goal**: Remove CartServiceApplication Spring Boot bootstrap class
**Acceptance**: /projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java deleted

#### T-009: JerseyConfig removal
**Class**: rewrite
**Findings**: jakarta-jaxrs-to-quarkus-00010 (1 incident)
**Goal**: Remove JerseyConfig as Quarkus auto-discovers JAX-RS resources
**Acceptance**: /projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java deleted

#### T-010: Spring Boot properties conversion
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000 (5 incidents), localhost-http-00001 (1 incident), demo-env-integration-00001 (1 incident)
**Goal**: Convert Spring Boot application.properties to Quarkus configuration
**Acceptance**: /projects/legacy/src/main/resources/application.properties converted to Quarkus format

#### T-011: Model classes port to target package
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000 (4 incidents)
**Goal**: Port Product, Promotion, ShoppingCart, ShoppingCartItem to com.demo.model package
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java → /projects/modernized/src/main/java/com/demo/model/Product.java
- /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java → /projects/modernized/src/main/java/com/demo/model/Promotion.java
- /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java → /projects/modernized/src/main/java/com/demo/model/ShoppingCart.java
- /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java → /projects/modernized/src/main/java/com/demo/model/ShoppingCartItem.java
- Package: com.redhat.coolstore.model → com.demo.model
- HARVEST classes preserve exact field structure and behavior
**Acceptance**: All model classes compiled successfully with target package structure

#### T-012: Service interface port to target package
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000 (1 incident)
**Goal**: Port ShoppingCartService interface to com.demo.service package
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java → /projects/modernized/src/main/java/com/demo/service/ShoppingCartService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Interface methods preserved: addItem, removeItem, getShoppingCart, transferCart, checkout
**Acceptance**: ShoppingCartService interface compiled successfully

#### T-013: ShoppingCartServiceImpl modernization - concurrency and DI
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize ShoppingCartServiceImpl with Quarkus-native DI and thread-safe patterns
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java → /projects/modernized/src/main/java/com/demo/service/ShoppingCartServiceImpl.java
- Package: com.redhat.coolstore.service → com.demo.service
- @Component → @ApplicationScoped
- @Autowired field injection → constructor injection
- Target contract (architecture-profile §7): HashMap→ConcurrentHashMap<String, ShoppingCart> with compute() operations
- normalizeBeforeDerive: maintain cart item deduplication before pricing
- Cache refresh guard: bounded refresh policy (no clear-on-miss)
**Acceptance**: ShoppingCartServiceImpl compiled with @ApplicationScoped, constructor injection, ConcurrentHashMap storage

#### T-014: PromoService modernization - thread-safe promotion application
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize PromoService with @ApplicationScoped and thread-safe promotion patterns
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java → /projects/modernized/src/main/java/com/demo/service/PromoService.java
- Package: com.redhat.coolstore.service → com.demo.service
- @Component → @ApplicationScoped
- @Autowired → constructor injection
- Target contract (architecture-profile §7): threadSafeState→ConcurrentHashMap for promotionSet storage
- Business rule preserved: 25% discount on product "329299"
**Acceptance**: PromoService compiled with @ApplicationScoped, constructor injection, thread-safe promotion application

#### T-015: ShippingService modernization - stateless application-scoped service
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize ShippingService as @ApplicationScoped stateless service
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java → /projects/modernized/src/main/java/com/demo/service/ShippingService.java
- Package: com.redhat.coolstore.service → com.demo.service
- @Component → @ApplicationScoped
- @Autowired → constructor injection (though stateless, maintains pattern)
- Target contract: maintain current shipping tier logic with thread-safe execution
- Shipping tiers preserved: <$25=$2.99, $25-$50=$4.99, $50-$75=$6.99, $75-$100=$8.99, ≥$100=$10.99
**Acceptance**: ShippingService compiled as @ApplicationScoped stateless service

#### T-016: CartEndpoint modernization - JAX-RS with target error handling
**Class**: infer
**Findings**: jakarta-jaxrs-to-quarkus-00010 (8 incidents), springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize REST endpoint with Quarkus JAX-RS patterns and target error handling
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java → /projects/modernized/src/main/java/com/demo/rest/CartEndpoint.java
- Package: com.redhat.coolstore.rest → com.demo.rest
- Session scope → Request scope (@RequestScoped)
- @Autowired → constructor injection
- Target contract (architecture-profile §7): GET idempotent→404 (never creates missing carts), validateInput→400
- JerseyConfig dependency removed (Quarkus auto-registration)
**Acceptance**: CartEndpoint compiled with Quarkus JAX-RS patterns, @RequestScoped, constructor injection, target error behavior

#### T-017: CatalogService modernization - Feign to REST client
**Class**: infer
**Findings**: spring-components-00001 (1 incident), spring-components-00002 (1 incident), springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize Feign client to MicroProfile REST client
**Target design**:
- /projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java → /projects/modernized/src/main/java/com/demo/service/CatalogService.java
- Package: com.redhat.coolstore.service → com.demo.service
- @FeignClient → @RegisterRestClient(configKey="catalog") 
- @Path and @GET methods for JAX-RS interface
- Environment configuration: ${CATALOG_ENDPOINT} preserved via quarkus.rest-client.catalog.url
- Target contract: maintain thread-safe HTTP client operations
**Acceptance**: CatalogService uses @RegisterRestClient with preserved environment configuration

#### T-018: Characterize legacy behavior through tests
**Class**: infer
**Findings**: (Test coverage requirement per brief)
**Goal**: Port legacy tests to characterize behavioral contracts for HARVEST classes and target contracts for REDESIGN classes
**Target design**:
- /projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java → /projects/modernized/src/test/java/com/demo/service/ShoppingCartServiceTest.java
- /projects/legacy/src/test/java/com/redhat/coolstore/rest/CartServiceBoundaryTest.java → /projects/modernized/src/test/java/com/demo/rest/CartServiceBoundaryTest.java
- /projects/legacy/src/test/java/com/redhat/coolstore/ProductsObjectMother.java → /projects/modernized/src/test/java/com/demo/ProductsObjectMother.java
- Package migration: com.redhat.coolstore → com.demo
- HARVEST class tests pin legacy behavior: Product, Promotion, ShoppingCart, ShoppingCartItem
- REDESIGN class tests pin target behavior: CartEndpoint GET→404 (not create-on-GET), validateInput→400
- Behavioral pins (from brief): cart initialization zeros, pricing calculations, 25% discount on "329299", shipping tiers
- Test framework: @QuarkusTest instead of @SpringBootTest
- Forbidden pattern coverage: Tests verify no `getMockProducts`, "mock products", "Mock products", "mock Products", "Fallback to mock" patterns
**Acceptance**: All legacy behavioral assertions pass, including target contract changes, forbidden patterns tested

#### T-019: REST API validation and acceptance path coverage
**Class**: infer
**Findings**: localhost-http-00001 (1 incident), demo-env-integration-00001 (1 incident)
**Goal**: Validate complete REST API including acceptance path and environment integration
**Target design**:
- Acceptance endpoint implementation: /projects/modernized/src/main/java/com/demo/rest/CartEndpoint.java add /acceptance-check GET method
- Application properties: /projects/modernized/src/main/resources/application.properties with CATALOG_ENDPOINT configuration
- Integration validation: test catalog service connectivity in ShoppingCartServiceImpl
- Target contract validation: implement 404/400/503 error handling in CartEndpoint
- UI Surface: REST API provides user-facing cart management functionality
**Acceptance**: All /api/cart/* endpoints functional, acceptance path green, environment configuration preserved, UI surface covered