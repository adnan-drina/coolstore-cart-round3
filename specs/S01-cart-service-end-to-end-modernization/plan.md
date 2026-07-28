# S01 Cart Service End-to-End Modernization — Plan

## Migration Strategy

This story modernizes the complete cart service from Spring Boot to Quarkus following the dependency order analysis. All 12 classes are converted together to maintain pricing contract integrity (dependency-order.md:18-29).

## Platform Conversion (Umbrella Tasks)

### Platform Dependencies and Build Configuration
**Class: rewrite**  
**Findings:** javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000

Convert pom.xml Spring Boot platform to Quarkus platform:
- Replace spring-boot-starter-parent with com.redhat.quarkus.platform BOM
- Add quarkus-maven-plugin with proper groupId
- Replace Spring plugins with Quarkus equivalents
- Add quarkus-junit for testing

### Web and JAX-RS Dependencies
**Class: rewrite**  
**Findings:** jakarta-jaxrs-to-quarkus-00010, springboot-web-to-quarkus-00000

Replace JAX-RS and Spring Web dependencies:
- Replace jakarta.jakartaee-web-api with quarkus-rest
- Remove spring-boot-starter-web dependency

### Health and Metrics
**Class: rewrite**  
**Findings:** springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100

Replace Spring Boot Actuator with SmallRye Health:
- Remove spring-boot-starter-actuator
- Add quarkus-smallrye-health dependency
- Convert /actuator/health → /q/health endpoint

### Properties and Configuration
**Class: rewrite**  
**Findings:** springboot-properties-to-quarkus-00000

Convert application.properties:
- Remove Spring Boot properties dependencies
- Keep existing property keys (plain pass-through format)

## Domain Model Conversion (Dependency Order)

### 1. Product Model (God Node)
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003 (indirect)

Convert Product value object:
- Package: com.redhat.coolstore.model → com.demo.model
- No Spring dependencies to convert
- Verify Serializable contract is preserved
- Maintain JSON serialization compatibility

### 2. Promotion Model
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003 (indirect)

Convert Promotion model:
- Package: com.redhat.coolstore.model → com.demo.model
- Remove any Spring annotations (none present)
- Preserve constructor behavior

### 3. Application Bootstrap
**Class: rewrite**  
**Findings:** springboot-annotations-to-quarkus-00000, removed-javaee-modules-00020

Remove Spring Boot application class:
- Delete CartServiceApplication.java entirely
- Remove @SpringBootApplication and bootstrap model
- Handle javax.annotation.PostConstruct → Quarkus provided

### 4. ShoppingCartItem Model (God Node)
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003 (indirect)

Convert ShoppingCartItem entity:
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve Serializable contract
- Maintain Product reference compatibility
- Add characterization tests for serialization behavior

### 5. CatalogService (Feign Client)
**Class: infer**  
**Findings:** demo-env-integration-00001, springboot-di-to-quarkus-00003

Convert Feign client to Quarkus REST client:
- Replace @FeignClient with @RegisterRestClient
- Convert @GetMapping to JAX-RS annotations
- Implement environment-driven URL configuration: `${CATALOG_ENDPOINT:default}`
- Preserve CATALOG_ENDPOINT from migration.yaml:23

### 6. ShoppingCart Model (God Node)
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003 (indirect)

Convert ShoppingCart aggregate root:
- Package: com.redhat.coolstore.model → com.demo.model
- Preserve all pricing fields and calculation methods
- Maintain HashMap<String, ShoppingCart> persistence contract
- Add characterization tests for pricing orchestration

### 7. Service Interfaces
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003

Convert ShoppingCartService interface:
- Package: com.redhat.coolstore.service → com.demo.service
- Remove Spring annotations (none present)
- Maintain method signatures for endpoint compatibility

### 8. PromoService
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003

Convert PromoService:
- Package: com.redhat.coolstore.service → com.demo.service
- Convert field injection → constructor injection
- Preserve percentage discount calculation logic

### 9. ShippingService  
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003

Convert ShippingService:
- Package: com.redhat.coolstore.service → com.demo.service
- Convert field injection → constructor injection
- Preserve tiered shipping thresholds ($2.99, $4.99, $6.99, $8.99, $10.99)

## REST Endpoint Conversion

### 10. CartEndpoint (JAX-RS + Spring MVC Hybrid)
**Class: infer**  
**Findings:** jakarta-jaxrs-to-quarkus-00010, springboot-di-to-quarkus-00003, localhost-http-00001, springboot-metrics-to-quarkus-0200

Convert CartEndpoint:
- Package: com.redhat.coolstore.rest → com.demo.rest
- Convert @RestController + @Path hybrid → pure JAX-RS @Path
- Remove WebApplicationContext.SCOPE_SESSION (use Quarkus session management)
- Convert field injection → constructor injection
- Update MediaType.APPLICATION_JSON usage
- Preserve all 5 endpoint behaviors exactly

## Service Implementation

### 11. ShoppingCartServiceImpl (Primary Service)
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003, springboot-metrics-to-quarkus-0200

Convert main service implementation:
- Package: com.redhat.coolstore.service → com.demo.service
- Convert all field injections → constructor injection
- Preserve pricing orchestration workflow (ShoppingCartServiceImpl.java:66-85)
- Maintain HashMap<String, ShoppingCart> persistence
- Add metrics annotations if present
- Convert @PostConstruct to Quarkus lifecycle

### 12. JerseyConfig
**Class: infer**  
**Findings:** springboot-di-to-quarkus-00003

Convert Jersey configuration:
- Package: com.redhat.coolstore.rest → com.demo.rest
- Convert field injection → constructor injection or remove
- Update package scanning for new com.demo package

## Testing and Characterization

### Characterization Tests
**Class: infer**  
**Findings:** Contract gaps from brief

Add tests for behavioral gaps:
1. Item-level promotional discounts (PromoService percentage discounts)
2. Multi-quantity line items deduplication logic (ShoppingCartServiceImpl.java:200-221)
3. Temp cart → persistent cart transfer operation (ShoppingCartServiceImpl.java:179-198)

### Test Conversion
**Class: rewrite**  
**Findings:** springboot-plugins-to-quarkus-0000 (indirect)

Convert ShoppingCartServiceTest:
- Replace @RunWith(SpringRunner.class) with QuarkusTest
- Replace @SpringBootTest with @QuarkusTest
- Update CATALOG_ENDPOINT property handling
- Maintain all assertion values as behavioral pins

## Environment and Configuration

### Environment-Driven Configuration
**Class: infer**  
**Findings:** demo-env-integration-00001

Preserve CATALOG_ENDPOINT environment variable:
- Maintain ${CATALOG_ENDPOINT:default} resolution
- Update to quarkus.rest-client.<key>.url format
- Verify localhost hardcoded URLs are converted to env-driven config

### Metrics Conversion
**Class: infer**  
**Findings:** springboot-metrics-to-quarkus-0200

Convert Micrometer metrics to MicroProfile Metrics:
- Update @Timed, @Counted annotations if present
- Preserve metric names and dimensions
- Maintain health check behavior

## Version Compatibility

### Spring Boot Version Issues
**Class: infer**  
**Findings:** spring-components-00001, spring-components-00002

Address version incompatibility issues:
- Jakarta EE 9+ compatibility resolved through platform conversion
- Map to umbrella conversion tasks rather than individual fixes
- Verify no remaining Spring Boot 2.x specific code remains

## Build and Test Verification

### Build Configuration
**Class: rewrite**  
Ensure Maven build works with:
- Quarkus platform BOM
- Java 21 target
- All converted dependencies resolved
- Tests pass with behavioral pins preserved

### Integration Testing
**Class: infer**  
Verify endpoints serve correctly:
- GET /cart/{cartId}
- POST /cart/{cartId}/{itemId}/{quantity}
- POST /cart/{cartId}/{tmpId}
- DELETE /cart/{cartId}/{itemId}/{quantity}
- POST /checkout/{cartId}

## Migration Notes

- **Package Root Change**: com.redhat.coolstore → com.demo (project root)
- **God Nodes**: Product, ShoppingCart, ShoppingCartItem require characterization tests before conversion
- **Preserve Contract**: CATALOG_ENDPOINT environment variable from migration.yaml:23
- **Forbidden Patterns**: No mock/fallback code per migration.yaml:24-31