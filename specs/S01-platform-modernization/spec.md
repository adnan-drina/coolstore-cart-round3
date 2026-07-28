# S01 Platform Modernization Spec

## Observed Legacy Behavior

### Build Configuration Contract

The legacy application uses Spring Boot 2.7.18 as its parent POM with the following key dependencies and plugins:

**Spring Boot Parent POM** (`pom.xml:18-26`):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>
```

**Spring Boot Dependencies** (`pom.xml:55-72`):
- `spring-boot-starter-web`: Web framework and REST support
- `spring-boot-starter-jersey`: JAX-RS implementation via Jersey
- `spring-boot-starter-actuator`: Health checks and operational endpoints
- `spring-cloud-starter-openfeign`: Feign client for REST communication

**Spring Boot Maven Plugin** (`pom.xml:104-106`):
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

**Micrometer Metrics** (`pom.xml:65`):
Spring Boot Actuator includes Micrometer for metrics collection.

### Application Behavior

No application-level behavior changes occur in this story - this is purely a platform migration affecting build configuration and dependencies.

**Environment Configuration** (`/projects/legacy/src/main/resources/application.properties:6`):
- CATALOG_ENDPOINT environment variable configuration preserved from migration.yaml

**Integration Points**:
- Feign client to catalog service endpoint `${CATALOG_ENDPOINT}/api/products` remains unchanged (S03 handles integration points)

## Legacy API Contract

**REST Endpoints** remain unchanged (handled in S05):
- `GET /cart/{cartId}` - retrieve cart
- `POST /cart/{cartId}/{itemId}/{quantity}` - add items
- `POST /cart/{cartId}/{tmpId}` - replace cart contents
- `DELETE /cart/{cartId}/{itemId}/{quantity}` - remove items
- `POST /cart/checkout/{cartId}` - checkout

**Health Endpoints** currently provided by Spring Boot Actuator at `/actuator/health` (replaced with Quarkus SmallRye Health at `/q/health`)

## Legacy File Paths (Evidence)

- `/projects/legacy/pom.xml` - Spring Boot parent and dependencies (lines 18-26, 55-72, 104-106)
- `/projects/legacy/src/main/resources/application.properties` - Environment configuration (line 6)

## In-Scope Legacy Components

**pom.xml** (INFRASTRUCTURE):
- HARVEST class - build configuration transformed mechanically
- Parent POM reference
- Spring Boot starter dependencies
- Spring Boot Maven plugin
- Spring Cloud dependencies (preserved)

**application.properties** (PRESERVED):
- CATALOG_ENDPOINT environment configuration preserved per migration.yaml

## UI Surface Waiver

**Legacy user-facing surface**: N/A - this is infrastructure-only modernization
**Waiver reason**: S01 platform modernization affects only build configuration (pom.xml). No application behavior changes, REST endpoints, or user-facing functionality is modified. Java source files and tests remain unchanged (handled in S02-S05).
