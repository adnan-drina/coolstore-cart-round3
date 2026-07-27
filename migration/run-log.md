# Harness run log

Appended by the Hermes orchestrator after every task (see
`.hermes/skills/migration-harness/`). One line per task.

| Task | Class | Attempts | Result | Files |
|---|---|---|---|---|
| T-001 | rewrite | 1 | resolved-by-scaffold | pom.xml |
| T-002 | rewrite | 1 | completed | src/main/java/com/demo/model/ShoppingCart.java, src/main/java/com/demo/model/ShoppingCartItem.java |
| T-003 | infer | 1 | completed | src/main/java/com/demo/model/Product.java
| T-006 | infer | 1 | completed | migration/integrations/T-006-integration-contracts.md, migration/integrations/test-patterns-legacy-compatibility.md |
| T-004: Package Migration for ShoppingCartItem | Class: rewrite | Attempts: 1 | Result: SUCCESS | Files: src/main/java/com/demo/model/ShoppingCartItem.java |
| T-005: Package Migration for Promotion | Class: rewrite | Attempts: 1 | Result: SUCCESS | Files: src/main/java/com/demo/model/Promotion.java |
| T-007: Characterization Tests for ShoppingCart Pricing Behavior | Class: infer | Attempts: 1 | Result: ESCALATED | Files: src/test/java/com/demo/model/ShoppingCartPricingTest.java, pom.xml |
| T-009: Product Model Characterization Tests | Class: infer | Attempts: 1 | Result: ESCALATED | Files: src/test/java/com/demo/model/ProductTest.java |
