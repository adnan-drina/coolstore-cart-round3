# S01 Complete Migration Plan

## Findings to Quarkus Mapping

This plan maps the MTA findings to the decided Quarkus target patterns per the MAPPINGS.md catalog and architecture-profile §7.

### Dependency Injection Patterns

**Findings**: springboot-di-to-quarkus-00000, springboot-di-to-quarkus-00003
- **Legacy**: `@Component` + `@Autowired` field injection
- **Target**: `@ApplicationScoped` + constructor injection (per MAPPINGS.md line 67-68)
- **Classes affected**: ShoppingCartServiceImpl, PromoService, ShippingService

### JAX-RS and REST Patterns

**Findings**: jakarta-jaxrs-to-quarkus-00010
- **Legacy**: Spring Jersey with `@Path`, `@GET`, `@POST`, `@DELETE`, `@PathParam`
- **Target**: `quarkus-rest` with Jakarta EE 9+ imports (`jakarta.ws.rs.*`)
- **Classes affected**: CartEndpoint, CatalogService (REST client)
- **Removed**: JerseyConfig (superseded by Quarkus auto-discovery)

### Bootstrap and Application Patterns

**Findings**: springboot-annotations-to-quarkus-00000
- **Legacy**: `@SpringBootApplication` main class with `SpringApplication.run`
- **Target**: Delete completely (Quarkus has no main class, MAPPINGS.md line 66)
- **Classes affected**: CartServiceApplication

### Dependency Management Patterns

**Findings**: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080
- **Legacy**: Spring Boot 2.7.18 parent, Spring Web/Jersey/Actuator dependencies
- **Target**: Quarkus platform BOM (`com.redhat.quarkus.platform`), `quarkus-rest`, `quarkus-smallrye-health`
- **Files affected**: pom.xml

### Web and REST Client Patterns

**Findings**: springboot-web-to-quarkus-00000
- **Legacy**: `spring-boot-starter-web`, `spring-boot-starter-jersey`, Spring Cloud OpenFeign
- **Target**: `quarkus-rest`, MicroProfile REST client (`@RegisterRestClient`)
- **Classes affected**: CartEndpoint, CatalogService

### Metrics and Health Patterns

**Findings**: springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200
- **Legacy**: Spring Boot Actuator with Micrometer
- **Target**: `quarkus-smallrye-health` (/q/health), `quarkus-smallrye-metrics`
- **Files affected**: pom.xml, configuration

### Configuration and Environment Patterns

**Findings**: springboot-properties-to-quarkus-00000, localhost-http-00001, demo-env-integration-00001
- **Legacy**: Spring Boot `application.properties`, environment variables
- **Target**: Quarkus `application.properties`, `@ConfigProperty`, preserved environment configuration
- **Preserved**: `CATALOG_ENDPOINT` environment variable configuration

### Maven Plugin Patterns

**Findings**: springboot-plugins-to-quarkus-0000
- **Legacy**: `spring-boot-maven-plugin`
- **Target**: `quarkus-maven-plugin` with Maven plugin conversion
- **Files affected**: pom.xml build plugins

### Thread Safety and Concurrency (Architecture Profile §7)

**Findings**: Implicit (not explicit MTA findings, but required by §7)
- **Legacy**: HashMap storage, no thread safety guarantees
- **Target**: `ConcurrentHashMap<String, ShoppingCart>` with `compute()` operations for atomic updates
- **Classes affected**: ShoppingCartServiceImpl, PromoService

### Error Handling Patterns (Architecture Profile §7)

**Findings**: Implicit (behavior-changing redesign requirement)
- **Legacy**: Create-on-GET behavior, no input validation
- **Target**: GET idempotent → 404 on missing cart, validateInput → 400 on invalid input
- **Classes affected**: CartEndpoint

### Feign to REST Client Patterns

**Findings**: spring-components-00001, spring-components-00002 (version incompatibility resolution)
- **Legacy**: Spring Cloud `@FeignClient` with OpenFeign
- **Target**: MicroProfile `@RegisterRestClient` with `quarkus-rest-client`
- **Classes affected**: CatalogService

## Class-by-Class Mapping

### HARVEST Classes (Faithful Port)

**Product** → **Product**
- Maintain exact field structure, constructors, getters/setters
- No business logic changes, pure data entity
- Target package: `com.demo.model`

**Promotion** → **Promotion**  
- Value object preserved as-is
- Maintain discount calculation logic
- Target package: `com.demo.model`

**ShoppingCart** → **ShoppingCart**
- Domain object with pricing fields preserved
- Maintain cart management methods
- Target package: `com.demo.model`

**ShoppingCartItem** → **ShoppingCartItem**
- Composition object linking products to quantities
- Maintain pricing calculations
- Target package: `com.demo.model`

### REDESIGN Classes (Runtime Modernization)

**ShoppingCartService** → **ShoppingCartService**
- Interface preserved, implementation modernized
- Target: thread-safe singleton with ConcurrentHashMap storage
- Target package: `com.demo.service`

**ShoppingCartServiceImpl** → **ShoppingCartServiceImpl**
- `@Component` → `@ApplicationScoped` + constructor injection
- HashMap → ConcurrentHashMap with compute() operations
- Cache refresh guard: no clear-on-miss, bounded refresh policy
- Target package: `com.demo.service`

**PromoService** → **PromoService**
- `@Component` → `@ApplicationScoped` + constructor injection
- Thread-safe promotion application
- Target package: `com.demo.service`

**ShippingService** → **ShippingService**
- `@Component` → `@ApplicationScoped` + constructor injection
- Stateless service maintained
- Target package: `com.demo.service`

**CartEndpoint** → **CartEndpoint**
- Spring @RestController → JAX-RS resource with `@Path`
- Session scope → Request scope (`@RequestScoped`)
- GET idempotent → 404 on missing cart (behavior change)
- ValidateInput → 400 on invalid parameters
- Target package: `com.demo.rest`

**JerseyConfig** → **REMOVED**
- Jersey ResourceConfig superseded by Quarkus auto-discovery
- CartEndpoint registered automatically by Quarkus

**CatalogService** → **CatalogService**
- `@FeignClient` → `@RegisterRestClient` + `@Path`
- Thread-safe HTTP client operations
- Environment configuration preserved: `${CATALOG_ENDPOINT:default}`
- Target package: `com.demo.service`

**CartServiceApplication** → **REMOVED**
- Spring Boot bootstrap eliminated
- Quarkus provides built-in bootstrap

## Build Configuration Mapping

### Parent and BOM
- Spring Boot parent → Quarkus platform BOM (`com.redhat.quarkus.platform`)
- Spring Cloud dependencies removed (OpenFeign → REST client)

### Dependencies
- `spring-boot-starter-web` → `quarkus-rest`
- `spring-boot-starter-jersey` → Removed (quarkus-rest serves JAX-RS)
- `spring-boot-starter-actuator` → `quarkus-smallrye-health`
- `spring-cloud-starter-openfeign` → `quarkus-rest-client`

### Plugins
- `spring-boot-maven-plugin` → `quarkus-maven-plugin`

### Configuration Properties
- Spring properties → Quarkus configuration properties
- REST client config: `quarkus.rest-client.catalog.url=${CATALOG_ENDPOINT}`

This plan ensures all mandatory findings are resolved while modernizing to Quarkus-native patterns per the architecture profile and MAPPINGS catalog.