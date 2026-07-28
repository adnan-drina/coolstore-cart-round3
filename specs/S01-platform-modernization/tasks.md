# S01 Platform Modernization Tasks

#### T-001: Replace Spring Boot Parent with Quarkus BOM
**Class**: rewrite
**Findings**: springboot-parent-pom-to-quarkus-00000 (1), javaee-pom-to-quarkus-00010 (1)
**Goal**: Adopt Quarkus platform BOM to enable Quarkus compilation and dependency management
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Spring Boot parent POM → Quarkus BOM (`com.redhat.quarkus.platform`)
**Acceptance**: `mvn clean compile` succeeds with Quarkus BOM; Spring Boot parent references removed

#### T-002: Replace Spring Boot Maven Plugin with Quarkus Maven Plugin
**Class**: rewrite
**Findings**: springboot-plugins-to-quarkus-0000 (1), javaee-pom-to-quarkus-00020 (1)
**Goal**: Enable Quarkus build and dev mode capabilities
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- spring-boot-maven-plugin → quarkus-maven-plugin (pinned with `${quarkus.platform.group-id}`)
**Acceptance**: Quarkus Maven plugin configured; spring-boot-maven-plugin removed

#### T-003: Add Quarkus Platform Plugin Set
**Class**: rewrite
**Findings**: javaee-pom-to-quarkus-00030 (1), javaee-pom-to-quarkus-00040 (1), javaee-pom-to-quarkus-00050 (1), javaee-pom-to-quarkus-00060 (1), javaee-pom-to-quarkus-00080 (1)
**Goal**: Configure Maven plugins for Quarkus development and testing
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Add Maven Compiler, Surefire, Failsafe plugins
- Add native build profile
- Add Quarkus JUnit dependency for testing
**Acceptance**: All required Maven plugins configured; native build profile available; Quarkus JUnit available

#### T-004: Replace Spring Boot Actuator with Quarkus Health
**Class**: rewrite
**Findings**: springboot-actuator-to-quarkus-0100 (1)
**Goal**: Provide operational health endpoints via Quarkus SmallRye Health
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- spring-boot-starter-actuator → quarkus-smallrye-health
- Health endpoint: `/q/health` (Quarkus default)
**Acceptance**: quarkus-smallrye-health dependency added; spring-boot-starter-actuator removed

#### T-005: Replace Micrometer with Quarkus Metrics
**Class**: rewrite
**Findings**: springboot-metrics-to-quarkus-0100 (1)
**Goal**: Provide metrics collection via Quarkus SmallRye Metrics
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Micrometer registry → quarkus-smallrye-metrics
**Acceptance**: quarkus-smallrye-metrics dependency added; Micrometer dependency removed

#### T-006: Replace JAX-RS Dependency with Quarkus REST
**Class**: rewrite
**Findings**: jakarta-jaxrs-to-quarkus-00010 (1)
**Goal**: Use Quarkus REST client instead of Jersey
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- spring-boot-starter-jersey → quarkus-rest dependency
**Acceptance**: quarkus-rest dependency added; spring-boot-starter-jersey removed

#### T-007: Remove Spring Boot Extension Dependencies
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000 (1), springboot-web-to-quarkus-00000 (1), springboot-di-to-quarkus-00000 (1)
**Goal**: Remove Spring extension artifacts in favor of native Quarkus CDI/JAX-RS
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Remove spring-boot-properties, spring-web, spring-di extension references
- Preserve Spring Cloud OpenFeign (catalog service integration)
**Acceptance**: Spring Boot extension dependencies removed; Spring Cloud dependency preserved

#### T-008: Resolve Spring Version Incompatibility via Platform Upgrade
**Class**: infer
**Findings**: spring-components-00001 (5), spring-components-00002 (5)
**Goal**: Address Spring Boot 2.7 / Spring 5 Jakarta EE 9+ incompatibility through platform modernization
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Decision: BOM replacement resolves version incompatibility as umbrella solution
- No individual version bumps required; Quarkus 3.27 provides Jakarta EE 9+ compatible foundation
**Acceptance**: Spring version compatibility findings resolved via platform conversion; project compiles successfully

#### T-009: Establish Metrics Migration Foundation
**Class**: infer
**Findings**: springboot-metrics-to-quarkus-0200 (1)
**Goal**: Prepare for Micrometer to MicroProfile Metrics conversion
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Decision: Infrastructure established via quarkus-smallrye-metrics dependency; code migration deferred to future story
**Acceptance**: Metrics dependency in place; Micrometer code migration deferred to dedicated story

#### T-010: Resolve Java EE Module Dependencies
**Class**: infer
**Findings**: removed-javaee-modules-00020 (1)
**Goal**: Ensure Java EE modules removed from OpenJDK 11 are provided by Quarkus platform
**Target design**:
- `/projects/legacy/pom.xml` → `pom.xml`
- Decision: Automatic resolution via Quarkus BOM - JEE annotations provided by platform dependencies
**Acceptance**: Compilation succeeds with OpenJDK 11; javax.annotation references satisfied

#### T-011: Verify Platform Modernization Build
**Class**: infer
**Findings**: All S01 findings mapped above
**Goal**: Ensure complete platform migration builds and runs with Quarkus
**Target design**:
- Build: `mvn clean compile` succeeds with Java 21
- Dev mode: Quarkus dev mode starts successfully
- Health: `/q/health` endpoint available
- No application behavior changes (preserved contract)
- UI Surface: infrastructure-only modernization with no user-facing changes (waived per spec.md:74-76)
- Forbidden items: `getMockProducts` preserved per migration.yaml:37 (no test doubles or mock fallbacks introduced)
- REDESIGN classes traced to future stories with target contract references:
  * CartEndpoint: stateless endpoint with thread-safe injection, GET 404 on missing cart, 400 for validation, 503 for catalog failures, idempotent POST operations
  * CartServiceApplication: REMOVED - Quarkus bootstrap replaces Spring Boot startup
  * Concurrency: thread-safe state management for endpoint and service layers
  * ExceptionMapper: JAX-RS error mapping for 503 catalog service failures
  * JerseyConfig: REMOVED - Quarkus auto-discovers JAX-RS resources
  * PromoService: thread-safe promotion data via ConcurrentHashMap cache
  * ShippingService: stateless tiered shipping calculation preserved
  * ShoppingCartServiceImpl: ConcurrentHashMap with compute() operations for thread-safe access
**Acceptance**: Clean build with Quarkus platform; all Spring Boot references removed from pom.xml; CATALOG_ENDPOINT preserved in application.properties; UI surface waiver acknowledged; forbidden items preserved; REDESIGN classes traced with target contracts
