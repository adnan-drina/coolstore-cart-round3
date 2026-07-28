# S01 Platform Modernization Plan

## Quarkus Mapping (Findings → Target)

### Build Configuration Transformation

**Rewrite Tasks** (mechanical transformations):

1. **Parent POM Replacement**
   - Rule: `springboot-parent-pom-to-quarkus-00000` → `javaee-pom-to-quarkus-00010`
   - Target: Replace Spring Boot parent with Quarkus BOM (`com.redhat.quarkus.platform`)
   - Evidence: `/projects/legacy/pom.xml:18-26` → `pom.xml`

2. **Maven Plugin Replacement**
   - Rule: `springboot-plugins-to-quarkus-0000` → `javaee-pom-to-quarkus-00020`
   - Target: Replace `spring-boot-maven-plugin` with `quarkus-maven-plugin` (pinned, `${quarkus.platform.group-id}`)
   - Evidence: `/projects/legacy/pom.xml:104-106` → `pom.xml`

3. **Spring Boot Plugin Set**
   - Rules: `javaee-pom-to-quarkus-00030`, `javaee-pom-to-quarkus-00040`, `javaee-pom-to-quarkus-00050`, `javaee-pom-to-quarkus-00060`
   - Target: Add Maven Compiler, Surefire, Failsafe plugins; native build profile
   - Evidence: `/projects/legacy/pom.xml` → `pom.xml`

4. **Actuator Replacement**
   - Rule: `springboot-actuator-to-quarkus-0100`
   - Target: Replace `spring-boot-starter-actuator` → `quarkus-smallrye-health` (`/q/health`)
   - Evidence: `/projects/legacy/pom.xml:65` → `pom.xml`

5. **Metrics Dependencies**
   - Rule: `springboot-metrics-to-quarkus-0100`
   - Target: Replace Micrometer → `quarkus-smallrye-metrics`
   - Evidence: `/projects/legacy/pom.xml:65` → `pom.xml`

6. **Spring Boot Dependencies**
   - Rules: `springboot-properties-to-quarkus-00000`, `springboot-web-to-quarkus-00000`, `springboot-di-to-quarkus-00000`
   - Target: Remove spring-boot-properties, spring-web, spring-di extensions (use native Quarkus CDI/JAX-RS instead - S02-S05)
   - Evidence: `/projects/legacy/pom.xml:55` → removed from `pom.xml`

7. **JAX-RS Dependency**
   - Rule: `jakarta-jaxrs-to-quarkus-00010`
   - Target: Replace Jersey dependency → `quarkus-rest` dependency
   - Evidence: `/projects/legacy/pom.xml:60` → `pom.xml`

8. **JUnit Integration**
   - Rule: `javaee-pom-to-quarkus-00080`
   - Target: Add Quarkus JUnit dependency
   - Evidence: `/projects/legacy/pom.xml:82` → `pom.xml`

**Infer Tasks** (design decisions):

1. **Version Incompatibility Resolution**
   - Rules: `spring-components-00001`, `spring-components-00002`
   - Target: Platform upgrade resolves Spring Boot 2.7 → Jakarta EE 9+ incompatibility
   - Decision: Umbrella resolution via BOM replacement, not individual version bumps

2. **Metrics Code Modernization**
   - Rule: `springboot-metrics-to-quarkus-0200`
   - Target: Replace Micrometer code → MicroProfile Metrics annotations
   - Decision: Future task - metrics call sites identified, code patterns to be determined

3. **Jakarta EE Modules**
   - Rule: `removed-javaee-modules-00020`
   - Target: Java EE modules removed from OpenJDK 11 → provided by Quarkus platform dependencies
   - Decision: Resolved automatically via BOM conversion

### Class Roles & Target Contract

**pom.xml** (INFRASTRUCTURE → HARVEST):
- Current: Spring Boot 2.7.18 parent with starter dependencies
- Target: Quarkus 3.27 platform BOM (`com.redhat.quarkus.platform`) with scoped dependencies
- Contract: Maintains compilation capability; enables Quarkus dev mode and native compilation

**application.properties** (PRESERVED):
- Current: Environment variable `${CATALOG_ENDPOINT}` for catalog service URL
- Target: Preserved unchanged per migration.yaml `preserve:` contract
- Contract: Catalog service integration maintained via environment-driven configuration

### Out-of-Scope (Future Stories)

- **Java Source Files**: Remain Spring Boot (S02-S04 handle CDI migration)
- **Test Files**: Remain Spring Boot test patterns (S02-S04 handle test modernization)
- **REST Endpoints**: Currently JAX-RS with Jersey (S05 converts to Quarkus JAX-RS)
- **Dependency Injection**: Currently Spring `@Autowired` (S02-S04 convert to CDI)
- **Metrics Code**: Micrometer call sites (future story after S01 platform foundation)

## Platform Modernization Scope

This story establishes the Quarkus foundation layer:
1. Quarkus platform BOM replaces Spring Boot parent
2. Quarkus Maven plugin replaces Spring Boot plugin
3. Quarkus operational extensions replace Spring Boot Actuator/Micrometer
4. Native Quarkus CDI/JAX-RS (not spring-di/spring-web extensions)

This enables Quarkus compilation, CDI support, and JAX-RS migration for subsequent stories.
