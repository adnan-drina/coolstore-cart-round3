# S01 Platform Foundation Tasks

#### T-001: Replace Spring Boot parent with Quarkus platform BOM
**Class**: rewrite
**Findings**: javaee-pom-to-quarkus-00010 (1), springboot-parent-pom-to-quarkus-00000 (1)
**Goal**: Convert Spring Boot parent POM to Quarkus platform BOM for managed dependencies
**Acceptance**: pom.xml uses com.redhat.quarkus.platform BOM; builds succeed

#### T-002: Add Quarkus Maven plugin ecosystem
**Class**: rewrite
**Findings**: javaee-pom-to-quarkus-00020 (1), javaee-pom-to-quarkus-00030 (1), javaee-pom-to-quarkus-00040 (1), javaee-pom-to-quarkus-00050 (1), javaee-pom-to-quarkus-00060 (1), springboot-plugins-to-quarkus-0000 (1)
**Goal**: Configure Maven plugins for Quarkus development and native builds
**Acceptance**: pom.xml includes quarkus-maven-plugin, compiler, surefire, failsafe plugins with native profile

#### T-003: Replace Spring Boot web dependencies with Quarkus REST
**Class**: rewrite
**Findings**: jakarta-jaxrs-to-quarkus-00010 (1)
**Goal**: Replace Spring Boot web stack with Quarkus RESTEasy implementation
**Acceptance**: pom.xml uses quarkus-rest; Spring Boot web/jersey dependencies removed

#### T-004: Replace Spring Boot Actuator with Quarkus health endpoints
**Class**: rewrite
**Findings**: springboot-actuator-to-quarkus-0100 (1), springboot-metrics-to-quarkus-0100 (1)
**Goal**: Convert health/monitoring to Quarkus SmallRye Health
**Acceptance**: pom.xml uses quarkus-smallrye-health; /q/health endpoint available

#### T-005: Add Quarkus testing framework support
**Class**: rewrite
**Findings**: javaee-pom-to-quarkus-00080 (1)
**Goal**: Add native Quarkus testing dependencies
**Acceptance**: pom.xml includes quarkus-junit5; test framework operational

#### T-006: Remove Spring Cloud and Feign client dependencies
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Remove Spring Cloud dependencies and prepare for Quarkus REST client
**Acceptance**: pom.xml removes spring-cloud-* and Feign dependencies; clean dependency tree

#### T-007: Convert Spring Boot configuration keys to Quarkus equivalents
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000 (1)
**Goal**: Review and migrate application.properties to Quarkus-native keys where applicable
**Acceptance**: application.properties uses Quarkus keys or documented pass-throughs

#### T-008: Remove Spring Boot bootstrap artifacts
**Class**: rewrite
**Findings**: springboot-annotations-to-quarkus-00000 (1)
**Goal**: Delete Spring Boot bootstrap classes
**Acceptance**: /projects/modernized/src/main/java/com/demo/CartServiceApplication.java and JerseyConfig.java removed; Quarkus auto-discovery enabled
**Scope note**: Only removes legacy bootstrap artifacts - all application classes remain unchanged until S02-S05

#### T-009: Prepare for CDI constructor injection conversion
**Class**: rewrite
**Findings**: springboot-di-to-quarkus-00003 (6)
**Goal**: Remove Spring @Autowired annotations from service classes to prepare for CDI conversion
**Acceptance**: @Autowired annotations removed from /projects/modernized/src/main/java/com/demo/rest/CartEndpoint.java:28, /projects/modernized/src/main/java/com/demo/rest/JerseyConfig.java:6, /projects/modernized/src/main/java/com/demo/service/PromoService.java:15, /projects/modernized/src/main/java/com/demo/service/ShippingService.java:7, /projects/modernized/src/main/java/com/demo/service/ShoppingCartServiceImpl.java:28,33,36,39
**Scope note**: Only removes @Autowired markers - actual CDI conversion occurs in S02-S03

#### T-010: Preserve environment-driven catalog endpoint configuration
**Class**: infer
**Findings**: demo-env-integration-00001 (1), localhost-http-00001 (1)
**Goal**: Ensure CATALOG_ENDPOINT environment configuration is preserved in migrated service
**Target design** (cite MAPPINGS.md):
- Legacy: CATALOG_ENDPOINT in application.properties, consumed by CatalogService via Feign
- Target: Environment-driven config preserved via Quarkus configuration (${VAR:default}), consumed by Quarkus REST client in S05
**Acceptance**: CATALOG_ENDPOINT environment variable support documented; service configurable via environment

#### T-011: Preserve mock products integration contract
**Class**: infer
**Findings**: preserved from migration.yaml
**Goal**: Maintain getMockProducts preservation requirement from migration contract
**Target design** (cite MAPPINGS.md):
- Legacy: getMockProducts preserved integration from migration.yaml line 35
- Target: Catalog service preservation contract maintained for S05 conversion
**Acceptance**: migration.yaml preserve contract documented; integration preserved to S05
**Scope note**: Actual mock products handling deferred to S05 where CatalogService is converted

#### UI Surface Coverage
**Waiver**: REST API endpoints (/cart/{cartId}, /cart/{cartId}/{itemId}/{quantity}, etc.) remain unchanged in this story. All endpoint modification is deferred to story S04 where Spring @RestController is converted to JAX-RS @Path resources. Health endpoint conversion (/actuator/health → /q/health) is handled in T-004.

#### Out of Scope Class Waivers - Architecture Profile §7 Compliance
**Waiver**: REDESIGN classes are intentionally NOT modified in this story. Per architecture profile §7 and dependency-order.md, these classes will be converted in subsequent stories:
- **S02 Model Classes**: Product, ShoppingCartItem, Promotion (HARVEST classes) 
- **S03 Service Classes**: ShoppingCartServiceImpl, PromoService, ShippingService, CatalogService (REDESIGN classes)
- **S04 REST Endpoint**: CartEndpoint (REDESIGN class)

**Target Shapes Documented** (profile §7 compliance):
- CartEndpoint → JAX-RS @Path resource with Quarkus REST, request-scoped concurrency, GET operations idempotent read-only, POST/DELETE mutate state, error mapping per JAX-RS standards (deferred to S04)
- ShoppingCartServiceImpl → CDI @ApplicationScoped with ConcurrentHashMap, normalized cart items before pricing, checkout clears items (deferred to S03) 
- PromoService → CDI @ApplicationScoped with immutable promotion set and thread-safe reads (deferred to S03)
- ShippingService → CDI @ApplicationScoped stateless service with thread-safe pricing calculations (deferred to S03)
- ShoppingCartService → CDI managed bean, interface eliminated, concurrency with ConcurrentHashMap (deferred to S03)
- CatalogService → Quarkus REST client with @RegisterRestClient, env-driven URL config, connection pooling (deferred to S05)

**Dependency Order Compliance**: Platform foundation must complete before any class conversions to ensure proper compilation dependencies. Model → Services → REST → External integrations sequence preserved.