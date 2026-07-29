# S05: REST Endpoints and Application Bootstrap Specification

## Observed Legacy Behavior

### CartEndpoint (REDESIGN) - Session-Scoped REST Controller

**Legacy Implementation** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-70`):
- Uses `javax.ws.rs.*` imports for JAX-RS annotations (`@GET`, `@POST`, `@DELETE`, `@Path`, `@PathParam`, `@Produces`)
- Spring `@RestController` with session-scoped cart access via `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)`
- Field injection via `@Autowired private ShoppingCartService shoppingCartService`
- Produces `MediaType.APPLICATION_JSON` for all responses
- Throws generic `Exception` for service method failures (line 43, 52, 61)

**API Endpoints**:
1. `GET /cart/{cartId}` - retrieves cart with pricing calculation (line 34-36)
2. `POST /cart/{cartId}/{itemId}/{quantity}` - adds items to cart (line 38-45)
3. `POST /cart/{cartId}/{tmpId}` - replaces cart contents from temporary cart (line 47-53)
4. `DELETE /cart/{cartId}/{itemId}/{quantity}` - removes items from cart (line 55-62)
5. `POST /cart/checkout/{cartId}` - checkout cart and clear items (line 64-69)

**Legacy Cart Creation Behavior**:
- Legacy `getCart()` creates empty cart if not found (evidence from brief: "legacy would create empty cart")
- No input validation for negative quantities or empty/null itemIds
- No specific error mapping - service exceptions propagate as raw errors

### JerseyConfig (REMOVED) - Jersey Resource Registration

**Legacy Implementation** (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:1-11`):
- Extends `org.glassfish.jersey.server.ResourceConfig`
- Registers `CartEndpoint.class` in constructor (line 9)
- Marked with `@Component` for Spring DI (line 6)
- Purpose: Jersey framework resource discovery and configuration

### CartServiceApplication (REMOVED) - Spring Boot Bootstrap

**Legacy Implementation** (`/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:1-14`):
- `@SpringBootApplication` annotation (line 7)
- `@EnableFeignClients` for Feign client configuration (line 8)
- `main()` method with `SpringApplication.run(CartServiceApplication.class, args)` (line 12)
- Spring Boot application lifecycle and auto-configuration

## API Contract Requirements

### CartEndpoint Behavioral Contracts

**GET Idempotency Contract**: `GET /cart/{cartId}` must return **404 Not Found** for non-existent cartIds (deliberate departure from legacy behavior of creating empty cart)

**Input Validation Contract**: POST operations must reject with **400 Bad Request**:
- Negative quantities
- Null or empty itemIds
- Return problem-detail formatted responses per JAX-RS standards

**Error Mapping Contract**: Service failures (especially catalog service unavailable) must return **503 Service Unavailable** via JAX-RS ExceptionMapper, never raw 500 errors

**POST Idempotency Contract**: POST operations for same cartId/itemId/quantity combinations must be idempotent

### Quarkus Auto-Discovery Requirements

**JerseyConfig Removal**: Quarkus automatically discovers JAX-RS resources via `@Path` annotations - no manual ResourceConfig registration required

**Application Bootstrap**: Quarkus handles application startup and CDI container initialization - no Spring Boot main class required

## Integration Dependencies

**CartEndpoint Dependencies**:
- Depends on `ShoppingCartService` interface (already migrated in S04)
- Service injection via CDI constructor injection (not Spring @Autowired)
- Product catalog integration via Feign client (preserved configuration)

**Session Scope**: Cart operations require session-scoped cart access patterns maintained for user shopping cart persistence

**Catalog Service Integration**: Environment-driven `${CATALOG_ENDPOINT}` configuration preserved per migration.yaml requirements

## Evidence-Based Behavioral Mapping

**Primary Contract Source**: `ShoppingCartServiceTest` defines expected cart operations behavior (line references in brief)

**Pricing Integration**: Cart endpoints trigger pricing calculations through ShoppingCartService, preserving promotional discounts and shipping calculations

**Cart Lifecycle**: All endpoints maintain cart state consistency with in-memory HashMap storage (migration target uses ConcurrentHashMap for thread safety)