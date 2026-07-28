# S01 Platform Foundation - Behavioral Contract

## Legacy Platform Configuration

The legacy application uses Spring Boot 2.7.18 as its foundation with the following platform characteristics:

### POM Structure
- **Parent POM**: `spring-boot-starter-parent:2.7.18` (src/main/java/com/redhat/coolstore/CartServiceApplication.java:17)
- **Web Framework**: Spring Boot Starter Web with embedded Tomcat
- **JAX-RS Implementation**: Spring Boot Starter Jersey (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:3)
- **Health/Monitoring**: Spring Boot Actuator for `/actuator/health` endpoint
- **Dependency Management**: Spring Cloud Dependencies for Feign client support
- **Build Plugin**: `spring-boot-maven-plugin` for executable JAR creation

### Bootstrap Architecture
- **Main Class**: `CartServiceApplication` (src/main/java/com/redhat/coolstore/CartServiceApplication.java:7-13)
  - Annotated with `@SpringBootApplication` for component scanning and auto-configuration
  - Uses `SpringApplication.run()` for application startup
  - Feign client enabled via `@EnableFeignClients`
- **JAX-RS Configuration**: `JerseyConfig` (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:7-10)
  - Extends `ResourceConfig` for Jersey configuration
  - Manually registers `CartEndpoint` class for JAX-RS resource discovery
  - Spring `@Component` for dependency injection

### Integration Surfaces
- **Health Endpoint**: `/actuator/health` provides application health status
- **Management Endpoints**: Additional Actuator endpoints available via Spring Boot
- **External Catalog**: Configured via `CATALOG_ENDPOINT` environment property

### Dependency Tree
The legacy POM declares 20+ Spring Boot dependencies including:
- `spring-boot-starter-web` - Web MVC and embedded Tomcat
- `spring-boot-starter-jersey` - JAX-RS implementation via Jersey
- `spring-boot-starter-actuator` - Health and management endpoints
- `spring-cloud-starter-openfeign` - Declarative REST client

## API Contract Surface

This story does not modify any user-facing APIs. The REST endpoints (`GET /cart/{cartId}`, `POST /cart/{cartId}/{itemId}/{quantity}`, etc.) remain unchanged and will be addressed in subsequent stories (S02-S05).

## Platform Assumptions
- Java 11 compatibility (java.version: 11 property)
- In-memory cart storage (HashMap-based)
- Environment-driven configuration via `application.properties`
- Red Hat GA repository for dependency resolution

## Evidence Files
- `/projects/legacy/pom.xml` - Complete Spring Boot platform configuration
- `/projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java` - Bootstrap main class
- `/projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` - JAX-RS configuration
- `/projects/legacy/src/main/resources/application.properties` - Configuration properties

## Out of Scope
All Java source files under `src/main/java/com/redhat/coolstore/` remain unchanged until their respective migration stories. This story is purely platform-level conversion.