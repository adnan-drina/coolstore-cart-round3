# S01 findings delta (harness-run after-analysis, Java 21 kantra)

Before (legacy): 24 rules / 47 incidents. After (destination, S01
state): 8 rules / 12 incidents.

Remaining, by disposition:
- javaee-pom-to-quarkus-00030/00050/00060: STILL FIRE on the scaffold
  pom — T-001s resolved-by-scaffold was PARTIALLY wrong (compiler/
  failsafe/native-profile conventions incomplete). Owned by S01;
  carried as story debt to close with S02s pom work.
- localhost-http-00001, demo-env-integration-00001: fire on scaffold
  config surfaces — resolve with S02s real configuration.
- jakarta-jaxrs-to-quarkus-00010 + 2 others: S02-scope surfaces.

Phase D session could not run the re-analysis (kantra needs the Java 21
export sessions do not have) — harness ran it. Fix queued: make the
after-analysis a supervisor SCRIPT step like Phase A (harness owns
analysis, both ends).
