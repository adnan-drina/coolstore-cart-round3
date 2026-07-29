# S05 REST Endpoints Migration Plan

## Migration Mapping Summary

This plan maps the mandatory findings to Quarkus-native equivalents per MAPPINGS.md catalog, implementing the target contracts from architecture profile §7.

## Jakarta JAX-RS Conversion

#### T-001: Update JAX-RS dependency to quarkus-rest
**Finding**: `jakarta-jaxrs-to-quarkus-00010`  
**Class**: `rewrite`  
**Location**: `pom.xml`

Replace `jakarta.ws.rs:jakarta.ws.rs-api` dependency with `io.quarkus:quarkus-resteasy-reactive` dependency to enable Quarkus-native JAX-RS support.

## CartEndpoint Conversion

#### T-002: Convert CartEndpoint annotations from Spring to Quarkus JAX-RS
**Finding**: `springboot-di-to-quarkus-00003`  
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- `@RestController` → `@Path("/cart")` and `@ApplicationScoped`
- `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)` → maintain session-scoped behavior through Quarkus REST configuration
- `@Autowired private ShoppingCartService` → constructor injection with `@Inject` and `@ApplicationScoped`
- `javax.ws.rs.*` imports → `jakarta.ws.rs.*` imports (handled in staging)

#### T-003: Implement GET idempotency contract (404 on missing cart)
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- `getCart(String cartId)` returns `404 Not Found` when cartId not found
- Deliberate departure from legacy create-empty-cart behavior
- Contract: GET operations must not mutate state

#### T-004: Add input validation for POST operations
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- Validate negative quantities with `400 Bad Request` response
- Validate null/empty itemIds with `400 Bad Request` response  
- Use JAX-RS constraint annotations: `@Min(1)`, `@NotNull`, `@NotBlank`
- Return problem-detail formatted responses per JAX-RS standards

#### T-005: Implement error mapping via ExceptionMapper
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- Create `ServiceExceptionMapper` implementing `ExceptionMapper<Exception>`
- Map catalog service failures to `503 Service Unavailable`
- Map service-specific exceptions to appropriate HTTP status codes
- Never return raw `500 Internal Server Error` to clients

## JerseyConfig Elimination

#### T-006: Remove JerseyConfig class
**Finding**: `springboot-di-to-quarkus-00003`  
**Class**: `rewrite`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`

Target Design:
- Delete entire `JerseyConfig.java` file
- Quarkus auto-discovers JAX-RS resources via `@Path` annotations
- No manual ResourceConfig registration needed

## Spring Boot Bootstrap Elimination

#### T-007: Remove Spring Boot application bootstrap
**Finding**: `springboot-annotations-to-quarkus-00000`  
**Class**: `rewrite`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/CartServiceApplication.java`

Target Design:
- Delete entire `CartServiceApplication.java` file
- Remove `@SpringBootApplication` and `main()` method
- Quarkus handles application bootstrap and CDI container initialization

## POST Idempotency Contract

#### T-008: Ensure POST operations are idempotent
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- `add()`, `set()`, `delete()` operations are idempotent for same cartId/itemId/quantity
- Cart operations should not create duplicate items for same input parameters
- Consistent cart state management across multiple identical requests

## Session Management

#### T-009: Maintain session-scoped cart access
**Class**: `infer`  
**Location**: `/projects/modernized/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`

Target Design:
- Preserve session-scoped cart persistence through Quarkus REST session management
- User cart state maintained across multiple HTTP requests in same session
- Cart data consistency per user session

## Integration Testing

#### T-010: Port characterization tests for REST endpoints
**Class**: `infer`  
**Location**: `/projects/modernized/src/test/java/com/redhat/coolstore/rest/CartEndpointTest.java`

Target Design:
- Convert Spring `@RestControllerTest` to Quarkus `@QuarkusTest`
- Test all five REST endpoints with validation
- Assert target contracts: 404 for missing carts, 400 for invalid input, 503 for service failures
- Mock `ShoppingCartService` and `CatalogService` dependencies using `@InjectMock`

## Dependency Order Compliance

This plan follows `migration/dependency-order.md` requirements:
- CartEndpoint conversion after service layer completion (S04)
- All endpoint tasks before final integration tests
- Characterization tests before deployment validation

## Target Contracts Summary

Per architecture profile §7, this migration implements:
- **Stateless endpoint** with thread-safe session-scoped service injection
- **GET 404 policy** for missing cartId (never creates cart on read)
- **Input validation** rejecting negative quantities/empty itemIds with 400
- **Error mapping** via ExceptionMapper to 503 for catalog failures
- **POST idempotency** for same cartId/itemId/quantity combinations
- **Quarkus auto-discovery** eliminating JerseyConfig
- **Quarkus bootstrap** replacing Spring Boot application