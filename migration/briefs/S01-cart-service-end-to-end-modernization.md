# S01: Cart service end-to-end modernization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Complete modernization of the shopping cart service from Spring Boot to Quarkus 3.27 platform. This is the ONLY story in the roadmap because the dependency analysis (dependency-order.md:130-138) shows a single bounded context with tight pricing orchestration coupling across all 12 classes. The ShoppingCartServiceImpl coordinates pricing workflow between PromoService, ShippingService, and CatalogService (architecture-profile.md:53-54) — they cannot function independently during migration.

This story unblocks: the ability to deploy a production-grade Quarkus cart service with preserved behavioral contracts and environment-driven configuration.

## In scope

The exact legacy classes/files this story modernizes. For each, quote the load-bearing legacy code (the lines being transformed — imports, annotations, key methods), so the story never starts from a blank read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — god node: Product value object referenced by ShoppingCartItem; fan-in=4 (dependency-order.md:10); serialization contract affects REST boundary (architecture-profile.md:53)
  ```java
  public class Product implements Serializable {
      private static final long serialVersionUID = -7304814269819778382L;
      private String itemId;
      private String name;
      private String desc;
      private double price;
  }
  ```
- `src/main/java/com/redhat/coolstore/model/Promotion.java` — discount rules applied at item and cart level; referenced by PromoService (architecture-profile.md:9)
  ```java
  public class Promotion {
      private String itemId;
      private double percentOff;
      public Promotion() {}
      public Promotion(String itemId, double percentOff) {
          this.itemId = itemId;
          this.percentOff = percentOff;
      }
  }
  ```
- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot main class with @SpringBootApplication (architecture-profile.md:113); line 7 must be deleted per recipe-log.md:7
  ```java
  @SpringBootApplication
  @EnableFeignClients
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```
- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — god node: ShoppingCart aggregate constituent; fan-in=3 (dependency-order.md:12); referenced by ShoppingCartServiceImpl (dependency-order.md:20-21)
  ```java
  public class ShoppingCartItem implements Serializable {
      private static final long serialVersionUID = 6964558044240061049L;
      private double price;
      private int quantity;
      private double promoSavings;
      private Product product;
  }
  ```
- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — Feign client to external product catalog (architecture-profile.md:57-58); @FeignClient line 10 needs REST client migration
  ```java
  @FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
  interface CatalogService {
      @GetMapping("/api/products")
      List<Product> products();
  }
  ```
- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — god node: aggregate root with highest fan-in=5 (dependency-order.md:8); contains pricing orchestration coordination (dependency-order.md:29)
  ```java
  public class ShoppingCart implements Serializable {
      private static final long serialVersionUID = 8803105703919108037L;
      private String cartId;
      private List<ShoppingCartItem> shoppingCartItem;
      // ... pricing fields and calculations
  }
  ```
- `src/main/java/com/redhat/coolstore/service/ShoppingCartService.java` — service interface defining cart operations; implementation coordinates pricing workflow
- `src/main/java/com/redhat/coolstore/service/PromoService.java` — applies percentage discounts to specific product IDs (architecture-profile.md:9); field injection line 15
- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — calculates shipping costs using tiered thresholds (architecture-profile.md:7); field injection line 7
- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REST controller with JAX-RS + Spring MVC hybrid (architecture-profile.md:19-22); field injection line 28; javax imports lines 5-11 (already migrated per recipe-log.md:7)
  ```java
  @RestController
  @Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
  @Path("/cart")
  public class CartEndpoint implements Serializable {
      @Autowired
      private ShoppingCartService shoppingCartService;
  }
  ```
- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — primary service implementing pricing orchestration (architecture-profile.md:53-54); field injection lines 28,33,36,39
- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — Jersey configuration class; field injection line 6
- `pom.xml` — Spring Boot platform dependencies (architecture-profile.md:109-121); lines 4,17,55,60,65,70,76,82 require Quarkus conversion

## Out of scope

What neighboring code this story must NOT touch, and which story owns it. (The tree must stay buildable: name any temporary seams — e.g. a dependent class that keeps compiling against the old shape until its own story.)

This is the ONLY migration story. All cart service components modernized together to maintain pricing contract integrity (dependency-order.md:137-139). No separate stories exist for individual components due to the tight coupling and single bounded context nature of this application.

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't re-decide). Recipe-executed rules already handled: reference `migration/recipe-log.md` and `migration/staging/` where applicable.

- **javax→jakarta imports**: Already executed by recipe (recipe-log.md:7-9); remaining javax.annotation import in ShoppingCartServiceImpl.java:11 handled by removed-javaee-modules-00020
- **Platform conversion**: Spring Boot → Quarkus platform BOM with quarkus-maven-plugin, quarkus-rest, quarkus-smallrye-health dependencies (findings-inventory.md:53-93)
- **Constructor injection**: Field injection → native CDI constructor injection for all @Component/@Service classes (findings-inventory.md:12-21)
- **Feign client**: @FeignClient → quarkus-rest-client with @RegisterRestClient annotation and environment-driven URL configuration
- **Properties**: Spring Boot application.properties → Quarkus application.properties with quarkus.rest-client.<key>.url format
- **Health endpoints**: Spring Boot Actuator → SmallRye Health (/q/health)

## Contracts owned by this story

- **Findings**: All mandatory rule ids from findings-inventory.md except javax-to-jakarta-import-00001 (already recipe-executed): springboot-di-to-quarkus-00003, spring-components-00001, spring-components-00002, localhost-http-00001, demo-env-integration-00001, jakarta-jaxrs-to-quarkus-00010, javaee-pom-to-quarkus-00010/00020/00030/00040/00050/00060/00080, removed-javaee-modules-00020, springboot-actuator-to-quarkus-0100, springboot-annotations-to-quarkus-00000, springboot-di-to-quarkus-00000, springboot-metrics-to-quarkus-0100/0200, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-web-to-quarkus-00000

- **Preserve**: CATALOG_ENDPOINT environment variable from migration.yaml:23; must keep ${CATALOG_ENDPOINT:default} resolution and env-driven configuration (findings-inventory.md:42-46)

- **Behavioral pins**: The legacy assertion values that must hold after this story (quote numbers/strings and their test source), and the contract GAPS this story closes with characterization tests:
  - Cart initialization: cartItemPromoSavings=0.0, cartItemTotal=0.0, shippingPromoSavings=0.0, cartTotal=0.0 (ShoppingCartServiceTest:28-36)
  - Pricing calculation: 2 items × $1000 = $2000 cartItemTotal; shipping calculation triggers for carts ≥$100 (shippingTotal=10.99); free shipping promotion applies for carts ≥$75 (shippingPromoSavings=-10.99, shippingTotal=0.0); final cartTotal = cartItemTotal + shippingTotal = $2000.00 (ShoppingCartServiceTest:38-54)
  - Product lookup: getProduct("2222") returns Bike with price $200.00 (ShoppingCartServiceTest:56-63)
  - Contract gaps: Add characterization tests for promotional discounts at item level, multi-quantity line items deduplication logic (ShoppingCartServiceImpl:200-221), temp cart → persistent cart transfer operation

- **Forbidden**: The fabrication tripwires from migration.yaml:24-31 must not appear in src/main: getMockProducts, "mock products", "Mock products", "mock Products", "Fallback to mock"

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at story end
- All endpoints serve correctly: GET /cart/{cartId}, POST /cart/{cartId}/{itemId}/{quantity}, POST /cart/{cartId}/{tmpId}, DELETE /cart/{cartId}/{itemId}/{quantity}, POST /checkout/{cartId}
- All findings ids from roadmap entry no longer fire on re-analysis (except demo-env-integration-00001 which fires on pristine scaffold)
- Characterization tests added for contract gaps: item-level promotional discounts, multi-quantity line items, temp cart transfer
- deploy story only: factory pipeline green, deployed, acceptance path serving
