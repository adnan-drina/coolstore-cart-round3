# Modernization roadmap

## S01: Domain model migration
- scope: src/main/java/com/redhat/coolstore/model/ShoppingCart.java, src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java, src/main/java/com/redhat/coolstore/model/Promotion.java, pom.xml
- findings: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, removed-javaee-modules-00020, spring-components-00001, spring-components-00002, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-di-to-quarkus-00000, springboot-web-to-quarkus-00000
- depends: -
- deploy: false
- done: All domain models converted to Jakarta namespace with Quarkus-compatible annotations; POJOs maintain serialization compatibility; characterization tests validate pricing behavior; Maven project configured with Quarkus BOM and plugins; all project-level modernization completed
- rationale: God nodes with highest fan-in (ShoppingCart:5, Product:3) must be converted first per dependency-order.md to enable downstream service layer migration without compilation issues; project configuration foundation established first

## S02: Service layer CDI migration and REST endpoint conversion
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java, src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java, src/main/java/com/redhat/coolstore/service/CatalogService.java, src/main/java/com/redhat/coolstore/rest/CartEndpoint.java, src/main/java/com/redhat/coolstore/CartServiceApplication.java, src/main/resources/application.properties
- findings: springboot-di-to-quarkus-00003, demo-env-integration-00001, localhost-http-00001, jakarta-jaxrs-to-quarkus-00010, springboot-actuator-to-quarkus-0100, springboot-annotations-to-quarkus-00000
- depends: S01
- deploy: true
- done: All service classes converted to CDI with constructor injection; ShoppingCartServiceImpl uses @ApplicationScoped; PromoService and ShippingService use CDI beans; CartEndpoint converted to native JAX-RS with jakarta.* imports and constructor injection; @Scope(SCOPE_SESSION) replaced with appropriate session management strategy; @SpringBootApplication removed; JAX-RS endpoints serve /api/cart paths; quarkus-smallrye-health provides /q/health; service accepts and returns JSON properly; CATALOG_ENDPOINT preserved via environment-driven configuration; Feign client converted to Quarkus REST client
- rationale: Complete modernization in one comprehensive story that handles both service layer conversion and endpoint modernization; creates first deployable milestone where application serves its complete API surface; session scope requires special attention for stateless Quarkus deployment

## S03: Post-ship hardening — concurrency, API correctness, cache policy
- scope: src/main/java/com/demo/service/ShoppingCartServiceImpl.java, src/main/java/com/demo/rest/CartEndpoint.java, src/test/java/com/demo/service/, src/test/java/com/demo/rest/
- findings: -
- depends: S02
- deploy: true
- done: pinned contracts green; concurrency/cache/validation/error-mapping defect classes closed with tests; deployed with GET acceptance 200
- rationale: hardening story from the post-ship semantic review (six findings the fidelity contract carried from legacy plus two S02-authored API defects); see the brief and rhoai3 docs/DRYRUN-M-PROCESS.md
