# T-035: Lint Preserve Verification Results

## CATALOG_ENDPOINT Preservation Verification ✓

**Migration.yaml Preserve Item**: `CATALOG_ENDPOINT`

**Verification Evidence**:

1. **Configuration File**: `/src/main/resources/application.properties`
   - Contains: `quarkus.rest-client.catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}`
   - Environment variable name preserved exactly as specified
   - Default fallback value maintained

2. **Test Coverage**: `/src/test/java/com/demo/service/ServiceInterfacesTest.java`
   - Line 104: `assertTrue(url.contains("CATALOG_ENDPOINT"), "CATALOG_ENDPOINT env var not referenced")`
   - Test verifies CATALOG_ENDPOINT is referenced in configuration
   - Test validates default fallback value preserved

3. **Service Interface Migration Tasks**:
   - T-030: ShoppingCartService interface harvested with preserve verification
   - T-031: application.properties migrated with CATALOG_ENDPOINT preservation
   - T-032: Maven dependencies updated supporting REST client configuration
   - T-033: CatalogService converted with environment-driven URL configuration
   - T-034: Interface contract tests validate CATALOG_ENDPOINT preservation

**CONCLUSION**: CATALOG_ENDPOINT environment variable is properly preserved and verified through multiple layers: configuration file, test coverage, and all related migration tasks.

## Forbidden Items Documentation

**Migration.yaml Forbidden Items**:
- `getMockProducts`
- `"mock products"`
- `"Mock products"`
- `"mock Products"`
- `"Fallback to mock"`

**Handler**: Forbidden tripwire sensors (as documented in migration.yaml lines 35-42)

**Status**: These items are explicitly marked as forbidden and handled by automated sensors that fail any commit introducing these patterns into `src/main`. This is the correct approach for handling non-preserve, prohibited items.

## Final Verification Status

✅ **CATALOG_ENDPOINT**: Properly preserved per migration.yaml contract  
✅ **Forbidden Items**: Documented and handled by tripwire sensors  
✅ **All Preserve Items**: Mapped and verified through migration tasks  
✅ **Sensor Coverage**: Green across all service interface tasks