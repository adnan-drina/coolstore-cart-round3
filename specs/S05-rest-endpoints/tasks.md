# S05 REST Endpoints Migration Tasks

## Migration Tasks (Rewrite First, Then Infer)

#### T-001: Update JAX-RS dependency to quarkus-rest
**Class**: rewrite  
**Finding**: jakarta-jaxrs-to-quarkus-00010  
**Location**: `pom.xml`  
**Target**: Replace `jakarta.ws.rs:jakarta.ws.rs-api` with `io.quarkus:quarkus-resteasy-reactive` dependency

#### T-002: Remove JerseyConfig class entirely
**Class**: rewrite  
**Finding**: springboot-di-to-quarkus-00003  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`  
**Target**: Delete JerseyConfig.java - Quarkus auto-discovers JAX-RS resources via @Path annotations

#### T-003: Remove Spring Boot application bootstrap
**Class**: rewrite  
**Finding**: springboot-annotations-to-quarkus-00000  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/CartServiceApplication.java`  
**Target**: Delete CartServiceApplication.java and remove @SpringBootApplication/main method - Quarkus handles bootstrap

#### T-004: Convert CartEndpoint from Spring @RestController to Quarkus @Path
**Class**: infer  
**Finding**: springboot-di-to-quarkus-00003  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`  
**Target Design**:
- Convert `@RestController` to `@Path("/cart")` and `@ApplicationScoped`
- Convert `@Autowired private ShoppingCartService` to constructor injection with `@Inject`
- Replace `javax.ws.rs.*` imports with `jakarta.ws.rs.*` imports  
- Maintain session-scoped cart access through Quarkus REST configuration
- All five endpoints: GET /cart/{cartId}, POST /cart/{cartId}/{itemId}/{quantity}, POST /cart/{cartId}/{tmpId}, DELETE /cart/{cartId}/{itemId}/{quantity}, POST /cart/checkout/{cartId}

#### T-005: Implement GET idempotency contract (404 on missing cart)
**Class**: infer  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`  
**Target Design**:
- `getCart(String cartId)` returns `404 Not Found` when cartId not found in storage
- Deliberate departure from legacy create-empty-cart behavior
- GET operations must be read-only and never mutate state
- Contract traceable to architecture profile §7: "GET returns 404 on missing cartId (never creates cart for read operations)"

#### T-006: Add input validation for POST operations with 400 responses
**Class**: infer  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`  
**Target Design**:
- Validate `quantity` parameter with `@Min(1)` constraint - reject negative quantities with `400 Bad Request`
- Validate `itemId` parameter with `@NotNull @NotBlank` constraints - reject null/empty itemIds with `400 Bad Request`
- Return problem-detail formatted responses per JAX-RS standards for validation failures
- Apply validation to `add()`, `set()`, and `delete()` POST operations
- Contract traceable to architecture profile §7: "Input validation: reject with 400 (problem-detail) for negative quantities, null/empty itemId"

#### T-007: Implement error mapping via ExceptionMapper for 503 responses
**Class**: infer  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/ServiceExceptionMapper.java`  
**Target Design**:
- Create `ServiceExceptionMapper` implementing `ExceptionMapper<Exception>`
- Map catalog service failures to `503 Service Unavailable` HTTP status
- Map service-specific exceptions to appropriate HTTP status codes
- Never return raw `500 Internal Server Error` to clients
- Provide detailed error messages in response body for debugging
- Contract traceable to architecture profile §7: "Error mapping: 503 via JAX-RS ExceptionMapper for catalog service failures (never raw 500)"

#### T-008: Ensure POST operations are idempotent for same parameters
**Class**: infer  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`  
**Target Design**:
- `add()`, `set()`, `delete()` operations must be idempotent for same cartId/itemId/quantity combinations
- Cart operations should not create duplicate items for identical input parameters
- Consistent cart state management across multiple identical requests
- Contract traceable to architecture profile §7: "POST operations are idempotent for same cartId/itemId/quantity combinations"

#### T-009: Port REST endpoint characterization tests to Quarkus
**Class**: infer  
**Location**: `/projects/modernized/src/test/java/com/redhat/coolstore/rest/CartEndpointTest.java`  
**Target Design**:
- Convert Spring `@RestControllerTest` to Quarkus `@QuarkusTest`
- Test all five REST endpoints with proper validation assertions
- Assert target contracts: 404 for missing carts, 400 for invalid input, 503 for service failures
- Mock `ShoppingCartService` and `CatalogService` dependencies using `@InjectMock`
- Test GET idempotency (no cart creation on read), input validation, and error mapping
- Maintain existing cart operation behavior tests from legacy test suite

#### T-010: Preserve CATALOG_ENDPOINT environment-driven configuration
**Class**: infer  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/service/CatalogService.java` + `/projects/modernized/src/main/resources/application.properties`  
**Target Design**:
- Maintain environment-driven catalog service URL configuration as `${CATALOG_ENDPOINT}` substitution
- Preserve Feign client configuration with catalog service endpoint integration
- Ensure no hardcoded catalog URLs - use environment variable `${CATALOG_ENDPOINT}`
- Validate catalog service integration continues to work with environment configuration
- Contract traceable to migration.yaml preserve requirements and finding demo-env-integration-00001

## Legacy User-Facing Surface Coverage

All legacy REST API endpoints are covered:
- **GET /cart/{cartId}** → T-005 (GET idempotency), T-004 (endpoint conversion), T-009 (testing)
- **POST /cart/{cartId}/{itemId}/{quantity}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)  
- **POST /cart/{cartId}/{tmpId}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)
- **DELETE /cart/{cartId}/{itemId}/{quantity}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)
- **POST /cart/checkout/{cartId}** → T-004 (endpoint conversion), T-009 (testing)

## Findings Coverage

All mandatory findings mapped:
- **jakarta-jaxrs-to-quarkus-00010** → T-001 (dependency update)
- **springboot-di-to-quarkus-00003** → T-002 (JerseyConfig removal), T-003 (CartServiceApplication removal), T-004 (CartEndpoint conversion)
- **springboot-annotations-to-quarkus-00000** → T-003 (CartServiceApplication removal)
- **demo-env-integration-00001** → T-010 (CATALOG_ENDPOINT preservation)

## Story Dependencies

S05 depends on completion of S04 (service implementations) - all service interfaces and implementations are already migrated and available for injection.

## Legacy User-Facing Surface Coverage

All legacy REST API endpoints are covered:
- **GET /cart/{cartId}** → T-005 (GET idempotency), T-004 (endpoint conversion), T-009 (testing)
- **POST /cart/{cartId}/{itemId}/{quantity}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)  
- **POST /cart/{cartId}/{tmpId}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)
- **DELETE /cart/{cartId}/{itemId}/{quantity}** → T-006 (validation), T-008 (idempotency), T-004 (endpoint conversion), T-009 (testing)
- **POST /cart/checkout/{cartId}** → T-004 (endpoint conversion), T-009 (testing)

**UI Surface Waiver**: This is a pure REST API microservice with no HTML/JSP/thymeleaf frontend. The user-facing surface is exclusively the REST API contract defined above. No web UI exists to migrate or preserve. Contract traceable to service architecture: `CartEndpoint` is JAX-RS `@RestController` with no view resolution.