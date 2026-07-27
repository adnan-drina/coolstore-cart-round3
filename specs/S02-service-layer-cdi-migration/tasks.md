# S02 Tasks: Service Layer CDI Migration and REST Endpoint Conversion

## T-001: Convert Application Bootstrap and Configuration

|**Class**: rewrite  
|**Finding References**: springboot-annotations-to-quarkus-00000, springboot-actuator-to-quarkus-0100  
|**Legacy Files**: `src/main/java/com/redhat/coolstore/CartServiceApplication.java:1-133`

Convert Spring Boot application to Quarkus bootstrap:

**File Paths Changed**: 
-  → DELETED (decided mapping: Quarkus has no main class; @SpringBootApplication bootstrap is removed, not converted)
- `src/main/resources/application.properties` → `src/main/resources/application.properties`

**Annotations**: 
- `@SpringBootApplication` → remove (Quarkus bootstrap without explicit main)
- Add: `quarkus.smallrye-health.enabled=true` to application.properties

1. OpenRewrite recipe execution on CartServiceApplication
2. Remove `@SpringBootApplication` annotation
   ```java
   public class CartServiceApplication {
       public static void main(String[] args) {
           Quarkus.run(args);
       }
   }
   ```
4. Alternative: Remove entirely if Quarkus can bootstrap without explicit main
5. Convert `application.properties` from Spring to Quarkus format:
   ```
   # Quarkus configuration
   quarkus.application.name=coolstore-cart
   quarkus.http.port=8080
   quarkus.smallrye-health.enabled=true
   # (catalog client config arrives at T-006 as quarkus.rest-client.catalogService.url — never catalog.endpoint) ${CATALOG_ENDPOINT:http://localhost:8081}
   ```

## T-002: Resolve POM Debt from S01

**Class**: rewrite  
**Finding References**: javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060  
**Legacy Files**: N/A (S01 carried debt)

Fix Maven POM conventions to resolve remaining Java EE to Quarkus BOM findings:

**File Paths Changed**: `pom.xml`

**Annotations**: `com.redhat.quarkus.platform:quarkus-bom:3.27.0`

1. OpenRewrite recipe execution on pom.xml
2. Ensure complete BOM alignment: `com.redhat.quarkus.platform:quarkus-bom:3.27.0`
3. Fix compiler conventions: Java 21 source/target, proper encoding
4. Fix failsafe plugin conventions: integration test patterns
5. Fix native profile conventions: proper executable generation
6. Ensure no Spring Boot dependencies remain (replaced by Quarkus equivalents)
7. Validate: All three findings (javaee-pom-to-quarkus-00030/00050/00060) resolved

## T-003: Characterize Service Behavior with S01 Test Contracts

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003, jakarta-jaxrs-to-quarkus-00010  
**Legacy Files**: `src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:27-64`

Port the expectation-helper pattern from S01 tests to characterize migrated service behavior:

**File Paths Changed**: `src/test/java/com/demo/service/ServiceCharacterizationTest.java`

1. Create `ServiceCharacterizationTest.java` in `src/test/java/com/demo/service/`
2. Port `ShoppingCartPricingTest.TestObjects.applyShippingCalculation()` and helper methods
3. Apply against migrated `ShippingService` and `PromoService` to pin arithmetic
4. Validate `ShoppingCartServiceImpl` satisfies pinned values:
   - 2x $1000 items = $2000 cart item total
   - Shipping promotion -$10.99 (free shipping above $75)
   - Final cart total = $2000 (shippingTotal = 0 after promotion)
5. Pin `ShippingTierTest.expectedShipping()` tier boundaries: 0, 25, 50, 75, 100
6. Validate exact tier rates: 2.99, 4.99, 6.99, 8.99, 10.99
7. Verify promotion composition semantics: ZEROES shippingTotal, keeps shippingPromoSavings informational

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=ServiceCharacterizationTest
```

## T-004: Convert Promotional Service to CDI

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003  
**Legacy Files**: `src/main/java/com/redhat/coolstore/service/PromoService.java:1-77`

Convert PromoService from Spring @Component to Quarkus CDI:

**File Paths Changed**: 
- `src/main/java/com/redhat/coolstore/service/PromoService.java` → `src/main/java/com/demo/service/PromoService.java`

**Annotations**: 
- `@Component` → `@ApplicationScoped`
- Add: `@Inject` constructor

**Method Signatures**:
- `public PromoService()` → `@Inject public PromoService()`
- `public Set<Promotion> getPromotionSet()` → preserved
- `public void setPromotionSet(Set<Promotion> promotionSet)` → preserved

1. Copy `PromoService.java` from `/projects/legacy/` to `src/main/java/com/demo/service/`
2. Replace `@Component` with `@ApplicationScoped`
3. Add `@Inject` constructor injection (no parameters for this service)
4. Preserve constructor logic: HashSet initialization with "329299", .25 promotion
5. Maintain serialVersionUID = 2088590587856645568L
6. Preserve all business methods: `applyCartItemPromotions()`, `applyShippingPromotions()`, etc.
7. Pin test contracts: ServiceCharacterizationTest must pass

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=ServiceCharacterizationTest#*
```

## T-005: Convert Shipping Service to CDI

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003  
**Legacy Files**: `src/main/java/com/redhat/coolstore/service/ShippingService.java:1-25`

Convert ShippingService from Spring @Component to Quarkus CDI:

**File Paths Changed**: 
- `src/main/java/com/redhat/coolstore/service/ShippingService.java` → `src/main/java/com/demo/service/ShippingService.java`

**Annotations**: 
- `@Component` → `@ApplicationScoped`
- Add: `@Inject` constructor

**Method Signatures**:
- `public void calculateShipping(ShoppingCart sc)` → preserved

1. Copy `ShippingService.java` from `/projects/legacy/` to `src/main/java/com/demo/service/`
2. Replace `@Component` with `@ApplicationScoped`
3. Add `@Inject` constructor injection (no parameters for this service)
4. Preserve exact shipping tier logic:
   - $0-25: $2.99
   - $25-50: $4.99
   - $50-75: $6.99
   - $75-100: $8.99
   - $100+: $10.99
5. Maintain `calculateShipping(ShoppingCart sc)` method signature
6. Pin test contracts: ServiceCharacterizationTest must pass with exact tier arithmetic

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=ServiceCharacterizationTest#*
```

## T-006: Convert Catalog Service to Quarkus REST Client

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003  
**Legacy Files**: `src/main/java/com/redhat/coolstore/service/CatalogService.java:1-92`

Convert CatalogService from Spring Feign Client to Quarkus REST Client:

**File Paths Changed**: 
- `src/main/java/com/redhat/coolstore/service/CatalogService.java` → `src/main/java/com/demo/service/CatalogService.java`
- `src/main/resources/application.properties`

**Annotations**: 
- `@FeignClient` → `@RegisterRestClient`
- Add: `org.eclipse.microprofile.rest.client.inject.RegisterRestClient`

**Method Signatures**:
- `@GetMapping("/api/products") List<Product> products()` → `@GET @Path("/api/products") List<Product> products()`

1. Copy `CatalogService.java` from `/projects/legacy/` to `src/main/java/com/demo/service/`
2. Replace `@FeignClient` with `@RegisterRestClient`
3. Replace `url = "${CATALOG_ENDPOINT}"` with `baseUri = "${catalog.endpoint}"`
4. Replace `@GetMapping("/api/products")` with `@GET @Path("/api/products")`
5. Add required imports: `org.eclipse.microprofile.rest.client.inject.RegisterRestClient`
6. Create `application.properties` entry:
   ```
   catalog.endpoint=${CATALOG_ENDPOINT:http://localhost:8081}
   ```
7. Preserve `List<Product> products()` method signature and behavior
8. Add REST client configuration to `application.properties`

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean compile
```

## T-007: Convert ShoppingCartServiceImpl to CDI with Constructor Injection

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003  
**Legacy Files**: `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:1-222`

Convert ShoppingCartServiceImpl from Spring @Service to Quarkus CDI:

**File Paths Changed**: 
- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` → `src/main/java/com/demo/service/ShoppingCartServiceImpl.java`

**Annotations**: 
- `@Service` → `@ApplicationScoped`
- Remove: `@Autowired` fields
- Add: `@Inject` constructor

**Method Signatures**:
- `public ShoppingCartServiceImpl(ShippingService ss, CatalogService catalogServie, PromoService ps)` → constructor injection with `final` fields
- All business methods preserved: `getShoppingCart()`, `addItem()`, `priceShoppingCart()`, etc.

1. Copy `ShoppingCartServiceImpl.java` from `/projects/legacy/` to `src/main/java/com/demo/service/`
2. Replace `@Service` with `@ApplicationScoped`
3. Replace all `@Autowired` field injection with constructor injection:
   ```java
   private final ShippingService shippingService;
   private final CatalogService catalogService;
   private final PromoService promoService;

   @Inject
   public ShoppingCartServiceImpl(
           ShippingService shippingService,
           CatalogService catalogService,
           PromoService promoService) {
       this.shippingService = shippingService;
       this.catalogService = catalogService;
       this.promoService = promoService;
   }
   ```
4. Replace `@Autowired` on individual fields with constructor parameters
5. Preserve `@PostConstruct init()` method and HashMap initialization
6. Maintain all business methods: `getShoppingCart()`, `addItem()`, `priceShoppingCart()`, etc.
7. Preserve in-memory `Map<String, ShoppingCart>` and `Map<String, Product>` storage
8. Pin test contracts: ServiceCharacterizationTest must pass with migrated service integration

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=ServiceCharacterizationTest#*
```

## T-008: Convert CartEndpoint to JAX-RS Resource

**Class**: infer  
**Finding References**: jakarta-jaxrs-to-quarkus-00010, springboot-di-to-quarkus-00003  
**Legacy Files**: `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:1-123`

Convert CartEndpoint from Spring @RestController to JAX-RS resource:

**File Paths Changed**: 
- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` → `src/main/java/com/demo/rest/CartEndpoint.java`

**Annotations**: 
- `@RestController` → `@ApplicationScoped @Path("/api/cart")`
- Remove: `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)`
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- Add: `@Inject` constructor

**Method Signatures**:
- `public ShoppingCart getCart(@PathParam("cartId") String cartId)` → preserved
- `public ShoppingCart add(@PathParam("cartId") String cartId, @PathParam("itemId") String itemId, @PathParam("quantity") int quantity)` → preserved
- All endpoints preserved: GET, POST, DELETE operations

1. Copy `CartEndpoint.java` from `/projects/legacy/` to `src/main/java/com/demo/rest/`
2. Replace `@RestController` with `@ApplicationScoped` and `@Path("/api/cart")`
3. Remove `@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)` - stateless approach
4. Replace `@Autowired` field injection with constructor injection:
   ```java
   private final ShoppingCartService shoppingCartService;

   @Inject
   public CartEndpoint(ShoppingCartService shoppingCartService) {
       this.shoppingCartService = shoppingCartService;
   }
   ```
5. Replace `javax.ws.rs.*` imports with `jakarta.ws.rs.*` imports
6. Maintain all endpoint methods:
   - `GET /{cartId}` → Returns shopping cart with current pricing
   - `POST /{cartId}/{itemId}/{quantity}` → Adds items to cart
   - `POST /{cartId}/{tmpId}` → Sets cart contents from temporary cart
   - `DELETE /{cartId}/{itemId}/{quantity}` → Removes items from cart
   - `POST /checkout/{cartId}` → Processes checkout and clears cart
7. Add JAX-RS annotations: `@GET`, `@POST`, `@DELETE`, `@Produces(MediaType.APPLICATION_JSON)`
8. Preserve Content-Type: application/json and path structure

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean compile
```

## T-009: Create Acceptance Check Endpoint

**Class**: infer  
**Finding References**: localhost-http-00001  
**Legacy Files**: N/A (ship surface requirement)

Create acceptance check endpoint per migration.yaml acceptance.path:

**File Paths Changed**: `src/main/java/com/demo/rest/CartEndpoint.java`

**Annotations**: 
- Add: `@POST @Path("/acceptance-check")` to CartEndpoint

**Method Signatures**:
- `public Response acceptanceCheck()` → new method in CartEndpoint

1. Add new endpoint method to CartEndpoint:
   ```java
   @POST
   @Path("/acceptance-check")
   public Response acceptanceCheck() {
       return Response.ok().entity("{\"status\": \"ok\"}").build();
   }
   ```
2. Ensure endpoint returns 200 OK with JSON response
3. Validate endpoint path: `/api/cart/acceptance-check`
4. Test endpoint accessibility and response format

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn quarkus:dev &
sleep 5
curl -X POST http://localhost:8080/api/cart/acceptance-check
pkill -f quarkus
```

## T-010: Create Root Index Page

**Class**: infer  
**Finding References**: localhost-http-00001  
**Legacy Files**: N/A (ship surface requirement)

Create minimal root path index page:

**File Paths Changed**: `src/main/java/com/demo/rest/IndexResource.java`

**Annotations**: 
- `@Path("/")`
- `@ApplicationScoped`
- `@GET`

**Method Signatures**:
- `public Response index()` → root endpoint

1. Add new JAX-RS resource `IndexResource.java` in `src/main/java/com/demo/rest/`
2. Create root endpoint:
   ```java
   @Path("/")
   @ApplicationScoped
   public class IndexResource {
       @GET
       public Response index() {
           return Response.ok().entity("<html><body><h1>Coolstore Cart Service</h1></body></html>")
                          .type(MediaType.TEXT_HTML).build();
       }
   }
   ```
3. Ensure root path `/` returns 200 with minimal HTML content
4. Validate quarkus.http.root-path stays DEFAULT

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn quarkus:dev &
sleep 5
curl http://localhost:8080/
pkill -f quarkus
```

## T-011: Service Integration Testing

**Class**: infer  
**Finding References**: N/A (quality gate)  
**Legacy Files**: `src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java:34-46`

Create comprehensive service integration tests:

**File Paths Changed**: `src/test/java/com/demo/service/ServiceIntegrationTest.java`

1. Create `ServiceIntegrationTest.java` in `src/test/java/com/demo/service/`
2. Port boundary test assertions from `CartServiceBoundaryTest.java:38-45`
3. Test complete cart workflow:
   - Initialize empty cart
   - Add items via catalog service integration
   - Apply pricing calculations (shipping tiers + promotions)
   - Verify cart totals match ServiceCharacterizationTest pinned values
   - Process checkout and cart clearing
4. Validate shipping calculation edge cases
5. Test promotion application thresholds
6. Test external catalog service integration (with mocked responses if needed)
7. Validate error handling for invalid cart operations

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=ServiceIntegrationTest
```

## T-012: REST Endpoint Integration Testing

**Class**: infer  
**Finding References**: N/A (quality gate)  
**Legacy Files**: `src/test/java/com/redhat/coolstore/rest/CartEndpointTest.java`

Create REST endpoint integration tests:

**File Paths Changed**: `src/test/java/com/demo/rest/CartEndpointIntegrationTest.java`

1. Create `CartEndpointIntegrationTest.java` in `src/test/java/com/demo/rest/`
2. Test all CartEndpoint REST endpoints:
   - GET `/api/cart/{cartId}` → Returns shopping cart JSON
   - POST `/api/cart/{cartId}/{itemId}/{quantity}` → Adds items, returns updated cart
   - POST `/api/cart/{cartId}/{tmpId}` → Sets cart contents, returns updated cart
   - DELETE `/api/cart/{cartId}/{itemId}/{quantity}` → Removes items, returns updated cart
   - POST `/api/cart/checkout/{cartId}` → Processes checkout, clears cart
   - POST `/api/cart/acceptance-check` → Health check endpoint
3. Validate JSON serialization contracts match S01 model tests
4. Test HTTP status codes and Content-Type headers
5. Validate complete cart workflow through REST API
6. Test stateless session management (no session state persistence)

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=CartEndpointIntegrationTest
```

## T-013: Forbidden Mock Behavior Verification

**Class**: infer  
**Finding References**: springboot-di-to-quarkus-00003 (preserved behavior)  
**Legacy Files**: `migration.yaml:27-28`

Verify forbidden mock behavior is not introduced:

**File Paths Changed**: All service files (`src/main/java/com/demo/service/*.java`)

**Method Signatures**:
- No `getMockProducts()` methods in any service
- No "Fallback to mock" logic in CatalogService integration

1. Search all service files for `getMockProducts` calls
2. Verify no mock product fallback logic exists
3. Ensure CatalogService integration calls external service only
4. Validate error handling for external service failures (not fallback to mocks)
5. Document forbidden behaviors clearly in service code comments

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
grep -r "getMockProducts" src/main/java/com/demo/service/
grep -r "Fallback to mock" src/main/java/com/demo/service/
```

## T-014: Complete Integration Testing

**Class**: infer  
**Finding References**: N/A (factory gate validation)  
**Legacy Files**: N/A

Create end-to-end integration test suite:

**File Paths Changed**: `src/test/java/com/demo/CompleteIntegrationTest.java`

1. Create `CompleteIntegrationTest.java` in `src/test/java/com/demo/`
2. Test complete cart service lifecycle:
   - Start application
   - Create new cart via POST `/api/cart/{cartId}/{itemId}/{quantity}`
   - Verify cart pricing matches ServiceCharacterizationTest expectations
   - Add multiple items and verify cumulative pricing
   - Test shipping tier transitions
   - Test promotion application (free shipping above $75)
   - Process checkout and verify cart clearing
3. Test acceptance check endpoint
4. Test root index page
5. Test health endpoint
6. Validate all findings resolved:
   - springboot-di-to-quarkus-00003 (all service instances)
   - jakarta-jaxrs-to-quarkus-00010
   - springboot-actuator-to-quarkus-0100
   - springboot-annotations-to-quarkus-00000
   - demo-env-integration-00001
   - localhost-http-00001

**Test Commands**:
```bash
export JAVA_HOME="${JAVA_HOME_21}" && export PATH="${JAVA_HOME}/bin:${PATH}"
mvn -q clean test -Dtest=CompleteIntegrationTest
```

## Execution Order

**Rewrite Tasks First** (per plan lint): T-001, T-002  
**Characterization Tests First** (per brief): T-003  
**CDI Conversion** (dependency order): T-004, T-005, T-006, T-007  
**REST Endpoint**: T-008  
**Ship Surface** (brief requirement): T-009, T-010  
**Integration Testing**: T-011, T-012, T-013, T-014