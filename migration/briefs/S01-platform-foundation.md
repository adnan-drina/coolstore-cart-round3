# S01: Platform foundation and POM conversion

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story establishes the Quarkus foundation layer. It replaces the Spring Boot parent POM with the Quarkus BOM, adds all necessary Quarkus plugins, and removes Spring Boot bootstrap components. This is the prerequisite for all subsequent class-level modernization — the POM must be Quarkus-native before any Java classes can compile.

Position: **First story** in the roadmap, no dependencies. It unlocks the entire migration by providing the correct dependency tree and build configuration.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `pom.xml` — Spring Boot parent and dependencies
  ```xml
  <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.7.18</version>
      <relativePath/>
  </parent>
  
  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-actuator</artifactId>
      </dependency>
      <!-- 20+ Spring Boot dependencies -->
  </dependencies>
  
  <build>
      <plugins>
          <plugin>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-maven-plugin</artifactId>
          </plugin>
      </plugins>
  </build>
  ```

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — Spring Boot bootstrap
  ```java
  @SpringBootApplication
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — Jersey configuration
  ```java
  @Configuration
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(CartEndpoint.class);
      }
  }
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **All Java classes under `src/main/java/com/redhat/coolstore/`** — remain unchanged until their respective stories (S02-S05)
- **Test files** — unchanged until S06
- **Resources files** — application.properties unchanged until S05
- **Legacy Spring annotations** — will be converted in class-specific stories

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `CartServiceApplication` — **REMOVED**
  - Target: Deleted entirely; Quarkus provides native CDI bootstrap and discovery
  
- `JerseyConfig` — **REMOVED**
  - Target: Deleted entirely; Quarkus auto-discovers JAX-RS resources without explicit registration

- `pom.xml` — **PLATFORM CONVERSION**
  - Target: Quarkus platform BOM, pinned quarkus/compiler/surefire/failsafe plugins, native profile, quarkus junit

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `javaee-pom-to-quarkus-00010/20/30/40/50/60/80` — Adopt Quarkus BOM, plugins, and native profile
- `springboot-parent-pom-to-quarkus-00000` — Replace Spring Parent POM with Quarkus BOM
- `springboot-plugins-to-quarkus-0000` — Replace spring-boot-maven-plugin with quarkus-maven-plugin
- `springboot-properties-to-quarkus-00000` — Quarkus keys in application.properties (plain pass-throughs)
- `springboot-annotations-to-quarkus-00000` — Delete `@SpringBootApplication` + main class
- `jakarta-jaxrs-to-quarkus-00010` — Replace jakarta JAX-RS dependency with quarkus-rest
- `springboot-actuator-to-quarkus-0100` — Replace with `quarkus-smallrye-health` (`/q/health`)
- `springboot-metrics-to-quarkus-0100` — Replace Micrometer dependency with `quarkus-smallrye-metrics`
- `removed-javaee-modules-00020` — JEE modules provided by Quarkus platform dependencies

## Contracts owned by this story

- **Findings**: All pom.xml-related findings (javaee-pom-to-quarkus-*, springboot-*-to-quarkus-*) plus springboot-annotations-to-quarkus-00000
- **Preserve**: None directly — CATALOG_ENDPOINT preservation happens in S05
- **Behavioral pins**: None — this is pure platform configuration
- **Forbidden**: None specific to this story

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- Maven builds successfully with Quarkus BOM and all plugins configured
- Quarkus dev mode starts without errors
- Health endpoint `/q/health` responds
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- deploy story only: factory pipeline green, deployed, acceptance path
  serving

**NOTE**: This story does NOT deploy — deploy marker is false. Platform foundation must be solid before considering deployment.
