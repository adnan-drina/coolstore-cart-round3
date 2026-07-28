# S06: Final integration and validation

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

This story provides final integration and comprehensive validation. It ensures all pieces work together harmoniously, handles any remaining compatibility issues, and validates the complete migration through the sensor framework. It also addresses the remaining Spring compatibility findings that couldn't be resolved until all components were modernized.

Position: **Final story** (depends on S05). This produces a production-ready application with complete test coverage and sensor validation.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- **Test files** — Comprehensive validation
  ```java
  // ShoppingCartServiceTest.java - existing tests must all pass
  @Test
  public void testShoppingCartTotal() {
      // Existing assertion: cartItemTotal = 2000.0 for 2×1000-priced items
      // Existing assertion: shippingPromoSavings = -10.99, shippingTotal = 0 for ≥ $75
      // Existing assertion: cartTotal = 2000.0
  }
  
  // New characterization tests for contract gaps identified in M1:
  // - Session-scoped REST behavior validation
  // - Concurrent access to shared cart map testing
  // - Error handling for invalid catalog responses
  // - Catalog service failure handling
  ```

- **Remaining Spring compatibility** — Final cleanup
  ```xml
  <!-- pom.xml - any remaining Spring dependencies -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId> <!-- REMOVE IF PRESENT -->
  </dependency>
  ```

- **Sensor validation** — Complete migration verification
  ```bash
  # All mandatory findings should be resolved
  # No recipe-executed rules should appear in task scope
  # All preserve contracts validated
  ```

## Out of scope

What neighboring code this story must NOT touch, and which story owns
it. (The tree must stay buildable: name any temporary seams — e.g. a
dependent class that keeps compiling against the old shape until its
own story.)

- **All application code** — Already modernized in S01-S05
- **Configuration files** — Already modernized in S05
- **Deployment manifests** — Not in scope for this migration

## Class roles & target contract (from architecture-profile §7)

This story is primarily **VALIDATION** rather than modernization. It ensures:

- All previously modernized classes maintain their target contracts
- Complete test coverage validates behavioral compatibility
- Sensor framework confirms all mandatory findings resolved
- No regressions introduced during integration

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

- `spring-components-00001` (infer) — Version incompatibility resolved:
  - All Spring Boot version conflicts eliminated through Quarkus conversion
  - Jakarta EE 9+ compatibility achieved

- `spring-components-00002` (infer) — Spring version incompatibility resolved:
  - All Spring framework dependencies properly replaced or removed
  - Jakarta EE 9+ compatibility achieved

- `springboot-di-to-quarkus-00000` (infer) — Spring DI replacement:
  - Any remaining spring-di references eliminated
  - Native CDI used throughout (confirmed in S03-S05)

## Contracts owned by this story

- **Findings**: spring-components-00001, spring-components-00002, springboot-di-to-quarkus-00000 (final resolution)
- **Preserve**: Complete validation that all preserve contracts (CATALOG_ENDPOINT) still function
- **Behavioral pins**: 
  - All existing ShoppingCartServiceTest assertions pass
  - New characterization tests validate contract gaps
  - No behavioral regressions from S01-S05 changes
- **Forbidden**: Complete validation that no forbidden patterns introduced

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All existing tests pass with identical results
- New characterization tests validate all identified contract gaps
- Complete sensor validation: all mandatory findings resolved
- <story-specific criteria: which tests exist and pass, which endpoint
  serves, which findings ids no longer fire on re-analysis>
- deploy story only: factory pipeline green, deployed, acceptance path
  serving

**FINAL VALIDATION STORY**: This story confirms the migration is complete and production-ready. All modernization work done in S01-S05 is validated to work together without regressions.

**Complete Coverage Requirements:**
- Unit tests for all business logic
- Integration tests for REST endpoints  
- Characterization tests for identified contract gaps
- Sensor validation for all mandatory findings
- Migration acceptance criteria met
