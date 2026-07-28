# S05: Configuration and bootstrap cleanup

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story performs final configuration cleanup, removing Spring-specific bootstrap components and enabling Quarkus auto-discovery. CartServiceApplication (Spring Boot bootstrap) and JerseyConfig (JAX-RS application configuration) are removed entirely as Quarkus provides auto-configuration. POM conversion finalizes the platform migration to Quarkus.

This story follows S04 because it depends on the converted services and endpoints, and is the final cleanup step that removes legacy Spring bootstrap artifacts. Architecture-profile §7 designates these as removed components with Quarkus auto-discovery replacing their functionality.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot bootstrap and Feign client enablement
  ```java
  // Spring Boot main class and Feign client configuration
  @SpringBootApplication
  @EnableFeignClients
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — JAX-RS application configuration
  ```java
  // Jersey configuration for JAX-RS resource discovery
  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          packages("com.redhat.coolstore.rest");
          register(JacksonFeature.class);
      }
  }
  ```

- **POM conversion** — platform migration from Spring Boot to Quarkus
  - Spring Boot parent BOM → Quarkus platform BOM
  - Spring dependencies → Quarkus extensions
  - Spring Maven plugins → Quarkus Maven plugin

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- All service implementations remain as converted in S03-S04
- All data models remain as converted in S01-S02
- REST endpoints remain as converted in S04
- Application functionality remains exactly the same

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `CartServiceApplication` — REDESIGN
  - Role: Spring Boot bootstrap and Feign client enablement
  - Target: **REMOVED** — Quarkus auto-configuration handles bootstrap and REST client discovery
  - Target contract: Application starts with Quarkus bootstrap, REST clients auto-configured via annotations

- `JerseyConfig` — REDESIGN  
  - Role: JAX-RS application configuration
  - Target: **REMOVED** — auto-discovery by Quarkus (`springboot-annotations-to-quarkus-00000`)
  - Target contract: JAX-RS resources auto-discovered by Quarkus without explicit configuration

- **POM** — REDESIGN
  - Role: Maven build configuration and dependency management
  - Target: Quarkus platform BOM with all required extensions and plugins

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **springboot-annotations-to-quarkus-00000 [rewrite]**: Replace SpringBootApplication bootstrap model with Quarkus bootstrap and CDI
  - Target: delete `@SpringBootApplication` + main class
- **springboot-di-to-quarkus-00003 [infer]**: Apply Quarkus Spring DI conversion guidance for common Spring DI annotations
  - Target: native CDI constructor injection (NOT the spring-di extension)
- **springboot-parent-pom-to-quarkus-00000 [rewrite]**: Replace the Spring Parent POM with Quarkus BOM
  - Target: Quarkus platform BOM replaces the Spring parent
- **javaee-pom-to-quarkus-00010/00020/00030/00040/00050/00060 [rewrite]**: Adopt Quarkus platform conventions
  - Target: scaffold pom conventions: platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit
- **springboot-plugins-to-quarkus-0000 [rewrite]**: Replace the spring-boot-maven-plugin dependency
  - Target: `quarkus-maven-plugin` (pinned, `${quarkus.platform.group-id}`)
- **springboot-properties-to-quarkus-00000 [rewrite]**: Replace the SpringBoot artifact with Quarkus 'spring-boot-properties' extension
  - Target: Quarkus keys in application.properties (plain pass-throughs keep working; NOT the spring-boot-properties extension)
- **jakarta-jaxrs-to-quarkus-00010 [rewrite]**: Replace jakarta JAX-RS dependency
  - Target: `quarkus-rest` dependency
- **springboot-actuator-to-quarkus-0100 [rewrite]**: Replace Spring Boot Actuator with Quarkus health, metrics, info and management interface capabilities
  - Target: `quarkus-smallrye-health` (`/q/health`)
- **springboot-metrics-to-quarkus-0100 [rewrite]**: Replace the Micrometer dependency with Quarkus Microprofile 'metrics' extension
  - Target: Micrometer dependency → `quarkus-smallrye-metrics`

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-annotations-to-quarkus-00000
  - springboot-di-to-quarkus-00003

- **Preserve**: the `preserve:` items whose surfaces live in scope —
  spell out the env var names/values mechanism to keep.
  - **CATALOG_ENDPOINT**: Environment-driven external configuration preserved via Quarkus REST Client config

- **Behavioral pins**: the assertion values that must hold after this
  story (quote numbers/strings and their test source). Harvest classes
  and behavior-preserving redesign pin LEGACY values; behavior-changing
  redesign pins the §7 TARGET (e.g. 404, not create-on-GET). Name the
  contract GAPS this story closes with characterization tests.
  - Application starts successfully without Spring Boot main class
  - JAX-RS resources auto-discovered without JerseyConfig
  - REST Client auto-configuration works without @EnableFeignClients
  - All cart operations continue to work exactly as in S04
  - Health endpoint `/q/health` provides application status
  - Contract GAPS: All bootstrap and configuration gaps closed with Quarkus auto-discovery

- **Forbidden**: the fabrication tripwires relevant here.
  - None specific to configuration cleanup

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
  - CartServiceApplication.java deleted (Quarkus auto-bootstrap replaces it)
  - JerseyConfig.java deleted (Quarkus auto-discovery replaces it)
  - pom.xml converted to Quarkus platform BOM with all required extensions
  - All Spring dependencies replaced with Quarkus equivalents
  - Application starts successfully without legacy bootstrap classes
  - All cart endpoints continue to work exactly as before
  - Health endpoint `/q/health` operational
  - Maven build uses Quarkus plugin and conventions
  - No remaining Spring Boot or Spring Web dependencies
- deploy story only: factory pipeline green, deployed, acceptance path
  serving
  - Not applicable for S05 - final cleanup, S04 already deployed