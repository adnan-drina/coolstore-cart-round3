# Modernization roadmap

## S01: Foundation data models with god-node characterization
- scope: src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/ShoppingCart.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java
- findings: removed-javaee-modules-00020
- depends: - 
- deploy: false
- done: HARVEST classes converted with javax→jakarta imports; god-node behavior characterized and preserved
- rationale: God nodes (ShoppingCart, Product, ShoppingCartItem) are the foundation data structures with highest fan-in. Architecture-profile §7 requires characterization tests before converting these central classes to establish behavior baselines.

## S02: Service interfaces and external integration
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartService.java, src/main/java/com/redhat/coolstore/service/CatalogService.java, src/main/java/com/redhat/coolstore/model/Promotion.java
- findings: demo-env-integration-00001, localhost-http-00001, springboot-di-to-quarkus-00003
- depends: S01
- deploy: false
- done: Service interfaces converted to CDI; CatalogService REST Client with env-driven config; javax→jakarta imports
- rationale: Service interfaces and external integration layer conversion enables dependency injection for downstream service implementations. Catalog service conversion establishes the REST client pattern needed by the core service.

## S03: Core business logic services
- scope: src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java, pom.xml
- findings: springboot-di-to-quarkus-00000, springboot-web-to-quarkus-00000, springboot-properties-to-quarkus-00000, javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200, spring-components-00001, spring-components-00002
- depends: S02
- deploy: false
- done: Business logic services converted to CDI with constructor injection; pom.xml converted to Quarkus platform; thread-safe implementations
- rationale: Core business logic services (promotion and shipping calculations) provide pricing behavior that ShoppingCartService depends on. POM conversion happens here to establish Quarkus platform before service implementations.

## S04: Cart operations and REST endpoints
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java, src/main/java/com/redhat/coolstore/rest/CartEndpoint.java
- findings: jakarta-jaxrs-to-quarkus-00010, springboot-actuator-to-quarkus-0100
- depends: S03
- deploy: true
- done: ShoppingCartServiceImpl converted to thread-safe CDI bean with ConcurrentHashMap; CartEndpoint converted to JAX-RS with constructor injection; /api/cart endpoints operational
- rationale: Main cart orchestration and REST surface conversion delivers the first deployable milestone. The service implements the core cart behavior and the endpoint exposes the API contract. This story pins the target contract from architecture-profile §7.

## S05: Configuration and bootstrap cleanup
- scope: src/main/java/com/redhat/coolstore/CartServiceApplication.java, src/main/java/com/redhat/coolstore/rest/JerseyConfig.java
- findings: springboot-annotations-to-quarkus-00000
- depends: S04
- deploy: true
- done: Spring Boot bootstrap removed; JerseyConfig eliminated (auto-discovery); final Quarkus platform configuration
- rationale: Final configuration cleanup removes Spring-specific bootstrap and enables Quarkus auto-discovery. Final deployable milestone with complete Quarkus-native application.