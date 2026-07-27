# S01 Domain Model Migration Plan

## Migration Strategy

This plan migrates the domain model layer as the foundation for downstream modernization. Following the dependency order, model classes are converted first to enable service layer migration without compilation issues.

## Mappings to Quarkus

### Package Restructuring
- **Source**: `com.redhat.coolstore.model.*`
- **Target**: `com.demo.model.*`
- **Rationale**: Project root is `com.demo` per architecture-profile.md:82

### File Harvest Strategy
- **Source**: `migration/staging/src/main/java/com/redhat/coolstore/model/` (recipe-transformed)
- **Target**: `src/main/java/com/demo/model/`
- **Transformation**: Jakarta imports already applied via recipe execution

### Build Dependencies
- **Parent POM**: Spring Boot → Quarkus platform BOM
- **Dependencies**: javax.* → jakarta.* (recipe-executed)
- **Source Layout**: Maintain Maven standard layout

## Task Classification

### Rewrite Tasks (Mechanical Transforms)
1. **Package Migration**: Update imports and package declarations from `com.redhat.coolstore.model` to `com.demo.model`
2. **File Relocation**: Copy recipe-transformed model files from staging to modernized project
3. **POM Integration**: Add model classes to Quarkus project structure

### Infer Tasks (Design Decisions)
1. **Characterization Tests**: Design test suite to pin legacy behavior from ShoppingCartServiceTest
2. **Serialization Contract**: Ensure JSON compatibility for external API consumers
3. **God Node Coverage**: Prioritize ShoppingCart and Product with early characterization tests

## Findings Coverage

### Recipe-Executed (No Tasks Required)
- `javax-to-jakarta-import-00001`: Already transformed in staging (recipe-log.md:7)

### Mandatory Findings to Address
- `springboot-di-to-quarkus-00003`: DI annotations on model classes (verify no @Component/@Service on POJOs)
- `javaee-pom-to-quarkus-*`: POM migration for domain model dependencies
- `springboot-parent-pom-to-quarkus-00000`: Parent POM migration
- `springboot-properties-to-quarkus-00000`: Configuration migration
- `springboot-plugins-to-quarkus-0000`: Build plugin migration

### Preserved Behavior
- **ShoppingCart pricing assertion**: 2x $1000 items = $2000 cart total with -$10.99 shipping promotion
- **Cart operations**: add/remove/reset behavior preserved for service layer
- **Serialization contract**: Field names/types unchanged for REST API compatibility

## Dependency Order Compliance

Following dependency-order.md:16-29:
1. **ShoppingCart** first (god node, fan-in: 5) - characterization tests early
2. **Product** second (god node, fan-in: 3) - characterization tests early  
3. **ShoppingCartItem** third
4. **Promotion** fourth
5. Application and config classes (CartServiceApplication, JerseyConfig) - owned by S02

## Test Strategy

### Characterization Tests Placement
- **Timing**: Immediately after mechanical rewrite tasks per PLANNING.md:111-117
- **Coverage**: ShoppingCartServiceTest assertions (pricing logic, shipping promotions)
- **Boundary Tests**: CartServiceBoundaryTest integration validation
- **Gap Coverage**: Shipping tier calculations ($0-25=$2.99, $25-50=$4.99, etc.)

### Quality Gate Compliance
- Tests sized for 80% new-code coverage requirement
- Every migrated class covered by characterization tests
- Integration validation between model layer and service contracts

## Risk Mitigation

### Compilation Safety
- Convert dependencies before dependents to maintain buildability
- Model classes first, then services, then endpoints per dependency order
- Single task per circular dependency group (ShoppingCart + Product)

### Behavior Preservation
- Characterization tests pin legacy assertion values before conversion
- Serialization compatibility maintained for external consumers
- No business logic changes in POJO layer
