# S01: Platform modernization and project scaffolding

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story establishes the Quarkus foundation layer required for all subsequent modernization work. It replaces Spring Boot dependencies and build configuration with Quarkus platform BOM and Maven plugins. This is the prerequisite that enables Quarkus compilation, CDI support, and JAX-RS migration.

Position: First story (S01) establishes dependency graph roots; no dependencies, all subsequent stories depend on S01 completion.

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
  </parent>
  
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  
  <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
  </plugin>
  ```

- `pom.xml` — Micrometer metrics dependencies
  ```xml
  <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- Java source files remain unchanged (S02-S05 handle source code)
- Test files remain unchanged (behavior preserved for now)
- The `CATALOG_ENDPOINT` environment configuration in application.properties is preserved (S03 handles integration points)

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- **pom.xml** — INFRASTRUCTURE
  - HARVEST (build configuration, not application code)
  - Target: Quarkus platform BOM replaces Spring Boot parent, quarkus-maven-plugin replaces spring-boot-maven-plugin

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- **javaee-pom-to-quarkus-00010**: Adopt Quarkus BOM → `com.redhat.quarkus.platform` BOM with pinned quarkus/compiler/surefire/failsafe plugins
- **javaee-pom-to-quarkus-00020**: Adopt Quarkus Maven plugin → `quarkus-maven-plugin` with platform group-id
- **springboot-parent-pom-to-quarkus-00000**: Replace Spring Parent POM with Quarkus BOM
- **springboot-plugins-to-quarkus-0000**: Replace spring-boot-maven-plugin with quarkus-maven-plugin
- **springboot-actuator-to-quarkus-0100**: Replace Spring Boot Actuator → `quarkus-smallrye-health` for `/q/health`
- **springboot-metrics-to-quarkus-0100**: Replace Micrometer → `quarkus-smallrye-metrics`
- **springboot-metrics-to-quarkus-0200**: Replace Micrometer code → MP Metrics annotations (future task)
- **springboot-properties-to-quarkus-00000**: Remove spring-boot-properties extension, use Quarkus keys directly
- **springboot-web-to-quarkus-00000**: Remove spring-web extension, use native JAX-RS (S05)
- **springboot-di-to-quarkus-00000**: Remove spring-di extension, use native CDI (S02-S04)

## Contracts owned by this story

- **Findings**: spring-components-00001, spring-components-00002, springboot-di-to-quarkus-00000, springboot-metrics-to-quarkus-0100, springboot-metrics-to-quarkus-0200, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-properties-to-quarkus-00000, springboot-web-to-quarkus-00000, springboot-actuator-0100
- **Preserve**: CATALOG_ENDPOINT environment variable preserved in application.properties (handled later in S03)
- **Behavioral pins**: None - this is infrastructure only, no application behavior changes
- **Forbidden**: None applicable

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- `mvn clean compile` succeeds with Quarkus BOM and plugins
- All Spring Boot parent/plugin references removed from pom.xml
- Quarkus dependencies added: quarkus-smallrye-health, quarkus-smallrye-metrics
- No changes to Java source files or tests
- Deploy story only: factory pipeline green, deployed, acceptance path
  serving
