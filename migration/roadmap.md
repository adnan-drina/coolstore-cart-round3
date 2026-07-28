# Modernization roadmap

## S01: Platform modernization and project scaffolding
- scope: src/main/resources/application.properties, pom.xml
- findings: spring-components-00001, spring-components-00002, springboot-di-to-quarkus-00000, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-web-to-quarkus-00000, springboot-actuator-to-quarkus-0100, javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, removed-javaee-modules-00020
- depends: -
- deploy: false
- done: Quarkus BOM and plugins in place, Spring Boot artifacts removed, metrics/monitoring configured via Quarkus extensions
- rationale: Foundation layer must be established before any code changes; POM transformations are prerequisite for Quarkus build and dependency resolution

## S02: Core model entities and catalog integration
- scope: src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java, src/main/java/com/redhat/coolstore/model/ShoppingCart.java, src/main/java/com/redhat/coolstore/model/Promotion.java
- findings: -
- depends: S01
- deploy: false
- done: All entity classes compile with jakarta imports, characterization tests pass for god-node behavior
- rationale: Core data model stabilization (god nodes Product, ShoppingCart, ShoppingCartItem) required before dependent services can safely convert; cart pricing behavior must be pinned

## S03: Service interfaces and catalog client
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartService.java, src/main/java/com/redhat/coolstore/service/CatalogService.java
- findings: localhost-http-00001, demo-env-integration-00001
- depends: S02
- deploy: false
- done: Service interfaces preserved with jakarta imports, catalog service URL driven by ${CATALOG_ENDPOINT} environment variable
- rationale: Interface contracts must be established before implementations; catalog service integration point with environment-driven config must be secured

## S04: Service implementations and domain logic
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java, src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java
- findings: springboot-di-to-quarkus-00003
- depends: S03
- deploy: false
- done: All services converted to CDI with constructor injection, thread-safe state management implemented, business logic preserved
- rationale: Service implementations with state management (ShoppingCartServiceImpl HashMap) converted to thread-safe CDI pattern; promotion and shipping services migrated to constructor injection

## S05: REST endpoints and application bootstrap
- scope: src/main/java/com/redhat/coolstore/rest/CartEndpoint.java, src/main/java/com/redhat/coolstore/rest/JerseyConfig.java, src/main/java/com/redhat/coolstore/CartServiceApplication.java
- findings: jakarta-jaxrs-to-quarkus-00010, springboot-annotations-to-quarkus-00000
- depends: S04
- deploy: true
- done: JAX-RS converted to quarkus-rest, JerseyConfig removed (auto-discovery), @SpringBootApplication removed, GET returns 404 for missing carts, validation and error mapping implemented
- rationale: Surface layer transformation completing the migration; removing JerseyConfig and SpringBootApplication finalizes platform migration; deploy milestone proves API contract works end-to-end
