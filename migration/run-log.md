# Harness run log

Appended by the Hermes orchestrator after every task (see
`.hermes/skills/migration-harness/`). One line per task.

|| Task | Class | Attempts | Result | Files |
||---|---|---|---|---|
|| T-001 | rewrite | 1 | resolved-by-scaffold | pom.xml |
|| T-002 | rewrite | 1 | completed | src/main/java/com/demo/model/ShoppingCart.java, src/main/java/com/demo/model/ShoppingCartItem.java |
|T-003 | infer | 1 | completed | src/main/java/com/demo/model/Product.java
T-004: Package Migration for ShoppingCartItem | Class: rewrite | Attempts: 1 | Result: SUCCESS | Files: src/main/java/com/demo/model/ShoppingCartItem.java
T-005: Package Migration for Promotion | Class: rewrite | Attempts: 1 | Result: SUCCESS | Files: src/main/java/com/demo/model/Promotion.java
