# Modernization roadmap

## S01: Platform foundation and POM conversion
- scope: pom.xml, CartServiceApplication.java, JerseyConfig.java
- findings: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-annotations-to-quarkus-00000, removed-javaee-modules-00020, jakarta-jaxrs-to-quarkus-00010, springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100, springboot-di-to-quarkus-00003
- depends: -
- deploy: false
- done: Quarkus BOM and plugins applied, Spring Boot bootstrap removed, Quarkus health/metrics available
- rationale: Foundation layer - POM must be Quarkus-native before any classes can compile. Removes Spring Boot parent, adds Quarkus BOM, plugins, and health/metrics capabilities.

## S02: Core domain models (HARVEST)
- scope: src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/Promotion.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java
- findings: -
- depends: S01
- deploy: false
- done: All model classes compile with jakarta imports, no behavior changes
- rationale: HARVEST classes preserved as-is per architecture-profile §7. Models are god nodes (Product fan-in: 4, ShoppingCartItem fan-in: 3) and must be stable before services reference them.

## S03: Core services modernization (REDESIGN)  
- scope: src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java
- findings: -
- depends: S02
- deploy: false
- done: Services converted to CDI @ApplicationScoped with constructor injection, thread-safe
- rationale: Core pricing services converted to Quarkus-native CDI with thread-safe design per architecture-profile §7. Stateless services - inherently thread-safe.

## S04: Cart aggregate and service (REDESIGN)
- scope: src/main/java/com/redhat/coolstore/model/ShoppingCart.java, src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java
- findings: -
- depends: S03
- deploy: false
- done: ShoppingCart with enhanced constructors/validation; ShoppingCartServiceImpl as CDI @ApplicationScoped with concurrent state management
- rationale: ShoppingCart is the central aggregate (fan-in: 5) containing all business logic. Redesign implements thread-safe HashMap operations and synchronized pricing per §7 target contract.

## S05: Catalog service and REST endpoint (REDESIGN)
- scope: src/main/java/com/redhat/coolstore/service/CatalogService.java, src/main/java/com/redhat/coolstore/rest/CartEndpoint.java, src/main/resources/application.properties
- findings: springboot-web-to-quarkus-00000, localhost-http-00001, demo-env-integration-00001, springboot-metrics-to-quarkus-0200
- depends: S04
- deploy: true
- done: CatalogService as Quarkus REST client with env-driven config; CartEndpoint as JAX-RS resource with Quarkus REST, all endpoints functional
- rationale: Integration boundary - external catalog service and REST API surface. Behavior-changing redesign (request-scoped vs session-scoped) explicitly documented per §7. Cloud-ready config preserves CATALOG_ENDPOINT.

## S06: Final integration and validation
- scope: All test files under src/test/, sensor validation, complete integration
- findings: spring-components-00001, spring-components-00002, springboot-di-to-quarkus-00000
- depends: S05
- deploy: true
- done: All mandatory findings resolved, full test suite passes, sensors green, production-ready deployment validated
- rationale: Final integration story ensures all pieces work together. Handles any remaining compatibility issues and validates the complete migration through sensors.
