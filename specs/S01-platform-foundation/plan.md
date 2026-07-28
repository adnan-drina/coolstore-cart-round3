# S01 Platform Foundation - Migration Plan

## POM Conversion Tasks

### Parent and BOM Management
- **T-001: Replace Spring Boot parent with Quarkus platform BOM** (rewrite)
  - Rule: `springboot-parent-pom-to-quarkus-00000`
  - Action: Replace `spring-boot-starter-parent:2.7.18` with `com.redhat.quarkus.platform` BOM
  - Target: Quarkus 3.27 platform coordinates with managed dependencies

### Plugin Conversion
- **T-002: Replace Spring Boot Maven plugin with Quarkus Maven plugin** (rewrite)
  - Rule: `springboot-plugins-to-quarkus-0000`
  - Action: Replace `spring-boot-maven-plugin` with `quarkus-maven-plugin`
  - Target: Pinned plugin with platform group ID

- **T-003: Adopt Quarkus Maven plugin ecosystem** (rewrite)
  - Rules: `javaee-pom-to-quarkus-00020`, `javaee-pom-to-quarkus-00030`, `javaee-pom-to-quarkus-00040`, `javaee-pom-to-quarkus-00050`, `javaee-pom-to-quarkus-00060`
  - Action: Add Maven Compiler, Surefire, Failsafe plugins with Quarkus configurations
  - Target: Platform-conformant plugin versions with native profile

### Dependency Conversion
- **T-004: Replace Spring Boot web stack with Quarkus REST** (rewrite)
  - Rule: `jakarta-jaxrs-to-quarkus-00010`
  - Action: Replace `spring-boot-starter-web` + `spring-boot-starter-jersey` with `quarkus-rest`
  - Target: Quarkus RESTEasy implementation for JAX-RS

- **T-005: Replace Spring Boot Actuator with Quarkus health** (rewrite)
  - Rule: `springboot-actuator-to-quarkus-0100`
  - Action: Replace `spring-boot-starter-actuator` with `quarkus-smallrye-health`
  - Target: `/q/health` endpoint replacing `/actuator/health`

- **T-006: Add Quarkus testing support** (rewrite)
  - Rule: `javaee-pom-to-quarkus-00080`
  - Action: Add `quarkus-junit5` test dependency
  - Target: Native Quarkus testing framework

- **T-007: Remove Spring Cloud dependencies** (rewrite)
  - Rule: `removed-javaee-modules-00020`
  - Action: Remove `spring-cloud-dependencies` and Feign client dependencies
  - Target: Clean separation - Feign client will be replaced with Quarkus REST client in S05

### Java EE Platform Conversion
- **T-008: Update Java EE to Jakarta EE imports** (rewrite)
  - Rule: `javax-to-jakarta-import-00001` (recipe-executed)
  - Action: Package name changes from `javax.*` to `jakarta.*`
  - Target: Jakarta EE 9+ compatible imports

### Bootstrap Application Conversion
- **T-009: Remove Spring Boot bootstrap artifacts** (rewrite)
  - Rule: `springboot-annotations-to-quarkus-00000`
  - Action: Delete `CartServiceApplication.java` and `JerseyConfig.java`
  - Target: Quarkus auto-discovery replaces manual configuration

### Build Profile Configuration
- **T-010: Configure Quarkus native build profile** (rewrite)
  - Rule: `javaee-pom-to-quarkus-00060`
  - Action: Add native build profile with GraalVM configuration
  - Target: Production-ready native image build capability

### Configuration Migration
- **T-011: Migrate application configuration keys** (rewrite)
  - Rule: `springboot-properties-to-quarkus-00000`
  - Action: Review `application.properties` for Quarkus key migration
  - Target: Quarkus-native configuration keys where applicable

## Dependency Ordering Constraints

All POM conversion tasks must complete before any Java class conversion can begin. The dependency order follows:
1. Platform foundation (this story) → Model classes (S02) → Services (S03) → REST endpoints (S04) → External integrations (S05)

## Target Contract

The platform conversion establishes:
- **Package root**: Changes from `com.redhat.coolstore` to `com.demo`
- **Health endpoint**: `/q/health` (Quarkus SmallRye Health)
- **Build system**: Maven with Quarkus plugins and native profile
- **Dependency management**: Quarkus platform BOM with managed versions
- **Bootstrap**: CDI-based auto-discovery (no manual configuration classes)

## Build Verification

After platform conversion:
- Maven builds must succeed with Quarkus BOM
- Dev mode must start without Spring Boot artifacts
- Health endpoint must respond at `/q/health`
- All dependency conflicts must be resolved