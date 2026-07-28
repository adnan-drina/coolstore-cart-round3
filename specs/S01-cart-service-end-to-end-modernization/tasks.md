# S01 Cart Service End-to-End Modernization — Tasks

#### T-001: Convert Spring Boot platform to Quarkus platform BOM
**Class**: rewrite
**Findings**: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080
**Goal**: Replace Spring Boot dependencies with Quarkus platform dependencies and build configuration
**Target design**: 
- Replace spring-boot-starter-parent with com.redhat.quarkus.platform BOM
- Add quarkus-maven-plugin with proper groupId configuration
- Update Maven plugins to Quarkus equivalents (compiler, surefire, failsafe)
- Add native build profile and quarkus-junit dependency
**Target files**: pom.xml
**Acceptance**: pom.xml converts successfully with Quarkus platform BOM

#### T-002: Replace JAX-RS dependencies with quarkus-rest
**Class**: rewrite  
**Findings**: jakarta-jaxrs-to-quarkus-00010
**Goal**: Replace Jakarta EE JAX-RS dependency with Quarkus REST dependency
**Target design**: 
- Replace jakarta.jakartaee-web-api with quarkus-rest dependency
- Remove conflicting JAX-RS implementations
**Target files**: pom.xml
**Acceptance**: JAX-RS dependencies resolved through quarkus-rest

#### T-003: Replace Spring Boot Actuator with SmallRye Health
**Class**: rewrite
**Findings**: springboot-actuator-to-quarkus-0100
**Goal**: Convert Spring Boot Actuator health endpoints to Quarkus SmallRye Health
**Target design**: 
- Replace spring-boot-starter-actuator with quarkus-smallrye-health
- Convert /actuator/health endpoint to /q/health
**Target files**: pom.xml, src/main/resources/application.properties
**Acceptance**: Health endpoints accessible via /q/health

#### T-004: Replace Micrometer with SmallRye Metrics
**Class**: rewrite
**Findings**: springboot-metrics-to-quarkus-0100
**Goal**: Replace Micrometer metrics with MicroProfile Metrics
**Target design**: 
- Replace micrometer dependency with quarkus-smallrye-metrics
- Update metrics configuration for Quarkus format
**Target files**: pom.xml
**Acceptance**: Metrics available through Quarkus metrics endpoint

#### T-005: Convert application.properties from Spring Boot to Quarkus format
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000
**Goal**: Update application.properties keys for Quarkus compatibility
**Target design**: 
- Remove Spring Boot properties dependencies
- Maintain existing property keys in plain pass-through format
- Keep CATALOG_ENDPOINT configuration structure
**Target files**: pom.xml, src/main/resources/application.properties
**Acceptance**: Application properties work with Quarkus configuration

#### T-006: Remove Spring Boot application bootstrap and Java EE modules
**Class**: rewrite
**Findings**: springboot-annotations-to-quarkus-00000, removed-javaee-modules-00020
**Goal**: Delete Spring Boot application class and handle Java EE module dependencies
**Target design**: 
- Delete CartServiceApplication.java and @SpringBootApplication
- Remove @EnableFeignClients and Spring bootstrap model
- Handle javax.annotation.PostConstruct through Quarkus provided dependencies
**Target files**: src/main/java/com/demo/model/CartServiceApplication.java, pom.xml
**Acceptance**: Application boots without Spring Boot main class

#### T-007: Replace Spring parent POM with Quarkus BOM
**Class**: rewrite
**Findings**: springboot-parent-pom-to-quarkus-00000
**Goal**: Convert Spring Boot parent POM reference to Quarkus platform BOM
**Target design**: 
- Update parent POM reference from spring-boot-starter-parent to com.redhat.quarkus.platform
- Maintain proper BOM import structure
**Target files**: pom.xml
**Acceptance**: Maven build resolves Quarkus platform dependencies

#### T-008: Replace Spring Boot Maven plugin with Quarkus plugin
**Class**: rewrite
**Findings**: springboot-plugins-to-quarkus-0000
**Goal**: Convert spring-boot-maven-plugin to quarkus-maven-plugin
**Target design**: 
- Replace org.springframework.boot:spring-boot-maven-plugin with quarkus-maven-plugin
- Configure proper Quarkus plugin goals and configurations
**Target files**: pom.xml
**Acceptance**: Quarkus Maven plugin configured for build lifecycle

#### T-009: Remove Spring Web dependency artifact
**Class**: rewrite
**Findings**: springboot-web-to-quarkus-00000
**Goal**: Remove Spring Web artifact dependency that conflicts with native JAX-RS
**Target design**: 
- Remove spring-boot-starter-web dependency from pom.xml
- Ensure JAX-RS dependencies are provided through quarkus-rest instead
**Target files**: pom.xml
**Acceptance**: Spring Web dependency removed, no conflicts with quarkus-rest

#### T-010: Convert existing tests to QuarkusTest framework
**Class**: rewrite
**Findings**: springboot-plugins-to-quarkus-0000 (indirect)
**Goal**: Update ShoppingCartServiceTest to use Quarkus testing framework
**Target design**: 
- Replace @RunWith(SpringRunner.class) with @QuarkusTest
- Replace @SpringBootTest with @QuarkusTest annotation
- Update CATALOG_ENDPOINT property handling for Quarkus config
- Maintain all existing assertion values as behavioral pins
- Preserve mock CatalogService behavior
**Target files**: src/test/java/com/demo/service/ShoppingCartServiceTest.java
**Acceptance**: All tests pass with preserved behavioral assertions

#### T-011: Convert Product value object model
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (indirect)
**Goal**: Convert Product model with proper package migration and Serializable contract
**Target design**:
- src/main/java/com/redhat/coolstore/model/Product.java → src/main/java/com/demo/model/Product.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve Serializable interface and all fields
- Maintain JSON serialization compatibility
- Add characterization tests for serialization behavior
**Target files**: src/main/java/com/demo/model/Product.java
**Acceptance**: Product model compiles and serializes correctly

#### T-012: Convert Promotion model class
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (indirect)
**Goal**: Convert Promotion model with package migration
**Target design**:
- src/main/java/com/redhat/coolstore/model/Promotion.java → src/main/java/com/demo/model/Promotion.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve constructor behavior and discount calculation fields
- Remove any Spring annotations if present
**Target files**: src/main/java/com/demo/model/Promotion.java
**Acceptance**: Promotion model compiles and maintains discount behavior

#### T-013: Convert ShoppingCartItem entity model
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (indirect)
**Goal**: Convert ShoppingCartItem entity with package migration and Serializable contract
**Target design**:
- src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java → src/main/java/com/demo/model/ShoppingCartItem.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve Serializable interface and Product reference compatibility
- Maintain price, quantity, promoSavings fields
- Add characterization tests for entity behavior
**Target files**: src/main/java/com/demo/model/ShoppingCartItem.java
**Acceptance**: ShoppingCartItem compiles and maintains Product references

#### T-014: Convert CatalogService Feign client to Quarkus REST client
**Class**: infer
**Findings**: demo-env-integration-00001, springboot-di-to-quarkus-00003
**Goal**: Convert Feign client interface to Quarkus REST client with environment-driven config
**Target design**:
- src/main/java/com/redhat/coolstore/service/CatalogService.java → src/main/java/com/demo/service/CatalogService.java
- Replace @FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}") with @RegisterRestClient
- Convert @GetMapping("/api/products") to JAX-RS @GET @Path annotations
- Implement environment-driven URL: ${CATALOG_ENDPOINT:http://localhost:8081}
- Preserve CATALOG_ENDPOINT from migration.yaml:23 preserve list
- Update quarkus.rest-client.<key>.url configuration format
**Target files**: src/main/java/com/demo/service/CatalogService.java
**Acceptance**: CatalogService calls external service with preserved environment config

#### T-015: Convert ShoppingCart aggregate root model
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (indirect)
**Goal**: Convert ShoppingCart aggregate with package migration and pricing preservation
**Target design**:
- src/main/java/com/redhat/coolstore/model/ShoppingCart.java → src/main/java/com/demo/model/ShoppingCart.java
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve all pricing fields and calculation methods
- Maintain HashMap<String, ShoppingCart> persistence contract
- Add characterization tests for pricing orchestration behavior
**Target files**: src/main/java/com/demo/model/ShoppingCart.java
**Acceptance**: ShoppingCart compiles and maintains pricing calculations

#### T-016: Convert ShoppingCartService interface
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003
**Goal**: Convert service interface with package migration
**Target design**:
- src/main/java/com/redhat/coolstore/service/ShoppingCartService.java → src/main/java/com/demo/service/ShoppingCartService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Remove Spring annotations if present
- Maintain method signatures for endpoint compatibility
**Target files**: src/main/java/com/demo/service/ShoppingCartService.java
**Acceptance**: Service interface compiles and maintains method signatures

#### T-017: Convert PromoService with constructor injection
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003
**Goal**: Convert PromoService from field injection to constructor injection
**Target design**:
- src/main/java/com/redhat/coolstore/service/PromoService.java → src/main/java/com/demo/service/PromoService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Convert @Autowired field injection → constructor injection
- Preserve percentage discount calculation logic
- Maintain @Component annotation compatibility
**Target files**: src/main/java/com/demo/service/PromoService.java
**Acceptance**: PromoService compiles with constructor injection and discounts work

#### T-018: Convert ShippingService with constructor injection
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003
**Goal**: Convert ShippingService from field injection to constructor injection
**Target design**:
- src/main/java/com/redhat/coolstore/service/ShippingService.java → src/main/java/com/demo/service/ShippingService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Convert @Autowired field injection → constructor injection
- Preserve tiered shipping thresholds ($2.99, $4.99, $6.99, $8.99, $10.99)
- Maintain @Component annotation compatibility
**Target files**: src/main/java/com/demo/service/ShippingService.java
**Acceptance**: ShippingService compiles with constructor injection and thresholds work

#### T-019: Convert CartEndpoint REST controller
**Class**: infer
**Findings**: jakarta-jaxrs-to-quarkus-00010, springboot-di-to-quarkus-00003, localhost-http-00001, springboot-metrics-to-quarkus-0200
**Goal**: Convert JAX-RS + Spring MVC hybrid endpoint to pure JAX-RS with constructor injection
**Target design**:
- src/main/java/com/redhat/coolstore/rest/CartEndpoint.java → src/main/java/com/demo/rest/CartEndpoint.java
- Package: com.redhat.coolstore.rest → com.demo.rest
- Convert @RestController + @Path hybrid → pure JAX-RS @Path
- Remove WebApplicationContext.SCOPE_SESSION annotation
- Convert @Autowired field injection → constructor injection
- Update MediaType.APPLICATION_JSON imports
- Preserve all 5 endpoint behaviors exactly (GET /cart/{cartId}, POST /cart/{cartId}/{itemId}/{quantity}, POST /cart/{cartId}/{tmpId}, DELETE /cart/{cartId}/{itemId}/{quantity}, POST /checkout/{cartId})
- Add MP Metrics annotations if micrometer metrics present
**Target files**: src/main/java/com/demo/rest/CartEndpoint.java
**Acceptance**: All cart endpoints serve correctly with preserved behavior

#### T-020: Convert ShoppingCartServiceImpl main service implementation
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003, springboot-metrics-to-quarkus-0200
**Goal**: Convert primary service with full constructor injection and pricing orchestration
**Target design**:
- src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java → src/main/java/com/demo/service/ShoppingCartServiceImpl.java
- Package: com.redhat.coolstore.service → com.demo.service
- Convert all @Autowired field injections → constructor injection
- Preserve pricing orchestration workflow (ShoppingCartServiceImpl.java:66-85)
- Maintain HashMap<String, ShoppingCart> persistence implementation
- Convert @PostConstruct to Quarkus lifecycle method
- Add MP Metrics annotations if present
- Preserve SLF4J logging configuration
**Target files**: src/main/java/com/demo/service/ShoppingCartServiceImpl.java
**Acceptance**: Main service compiles with constructor injection and pricing works

#### T-021: Convert JerseyConfig configuration class
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003
**Goal**: Convert Jersey configuration with package migration
**Target design**:
- src/main/java/com/redhat/coolstore/rest/JerseyConfig.java → src/main/java/com/demo/rest/JerseyConfig.java
- Package: com.redhat.coolstore.rest → com.demo.rest
- Convert @Autowired field injection → constructor injection or remove if unnecessary
- Update package scanning for new com.demo package structure
- Remove Spring-specific configurations
**Target files**: src/main/java/com/demo/rest/JerseyConfig.java
**Acceptance**: Jersey configuration compiles and scans correct packages

#### T-022: Address forbidden fabrication patterns
**Class**: infer
**Findings**: Forbidden patterns from migration.yaml:27-31
**Goal**: Remove any mock/fallback fabrication patterns that violate migration contract
**Target design**:
- Scan converted code for forbidden patterns: getMockProducts, "mock products", "Mock products", "mock Products", "Fallback to mock"
- Remove any mock/fallback implementations from converted code
- Ensure no fabrication patterns appear in src/main source
- Replace with proper error handling or business logic
**Target files**: All converted source files
**Acceptance**: No forbidden patterns remain in migrated code

#### T-023: Add characterization tests for behavioral contract gaps
**Class**: infer
**Findings**: Contract gaps from brief
**Goal**: Add missing tests for uncovered behaviors identified in legacy analysis
**Target design**:
- Add tests for promotional discounts at item level (PromoService percentage discounts)
- Add tests for multi-quantity line items deduplication logic (ShoppingCartServiceImpl.java:200-221)
- Add tests for temp cart → persistent cart transfer operation (ShoppingCartServiceImpl.java:179-198)
- Pin legacy assertion values as behavioral contracts
**Target files**: src/test/java/com/demo/service/ShoppingCartServiceCharacterizationTest.java
**Acceptance**: All behavioral gaps have test coverage

#### T-024: Address Spring Boot version compatibility issues
**Class**: infer
**Findings**: spring-components-00001, spring-components-00002
**Goal**: Resolve Jakarta EE 9+ compatibility through complete platform conversion
**Target design**:
- Ensure no remaining Spring Boot 2.x specific dependencies
- Verify Jakarta EE 9+ compatibility through Quarkus platform
- Map version incompatibility to umbrella conversion tasks
- Confirm no version conflicts remain
**Target files**: pom.xml, all converted source files
**Acceptance**: No version incompatibility findings remain

#### T-025: Convert localhost HTTP calls to cloud-ready configuration
**Class**: infer
**Findings**: localhost-http-00001
**Goal**: Replace hardcoded localhost URLs with environment-driven configuration
**Target design**:
- Replace hardcoded localhost:8081 in application.properties with env-driven config
- Maintain ${CATALOG_ENDPOINT:default} resolution pattern
- Ensure cloud-readiness for external service calls
- Preserve demo environment integration requirements
**Target files**: src/main/resources/application.properties
**Acceptance**: No hardcoded localhost URLs remain, env-driven config works

#### T-026: Add missing Spring DI replacement task
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000
**Goal**: Complete Spring DI replacement mapping to native CDI
**Target design**:
- Map Spring DI dependencies to native CDI equivalents across all service classes
- Verify constructor injection works with Quarkus CDI container
- Ensure @Component, @Service annotations are properly converted
**Target files**: All service classes, pom.xml
**Acceptance**: Spring DI completely replaced with native CDI

#### T-027: Final build verification and endpoint validation
**Class**: infer
**Findings**: Complete migration verification
**Goal**: Verify all endpoints serve correctly and build completes successfully
**Target design**:
- Ensure Maven build: mvn clean test passes
- Verify all 5 endpoints respond correctly:
  - GET /cart/{cartId}
  - POST /cart/{cartId}/{itemId}/{quantity}
  - POST /cart/{cartId}/{tmpId}
  - DELETE /cart/{cartId}/{itemId}/{quantity}
  - POST /checkout/{cartId}
- Confirm all migrated classes compile and tests pass
- Validate pricing calculations match legacy assertions
**Target files**: All migrated files, pom.xml
**Acceptance**: Complete build success and all endpoints functional

#### UI Surface Waiver
**Class**: infer
**Goal**: Document legacy user interface surface coverage
**Target design**:
- Legacy cart service exposes REST API interface, no web UI
- All user interactions happen through REST endpoints documented above
- No legacy HTML/jsp/web interface exists to migrate
- REST API contract preserved exactly across all 5 endpoints
**Acceptance**: REST API interface fully preserved and documented