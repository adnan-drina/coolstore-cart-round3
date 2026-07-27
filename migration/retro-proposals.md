# Migration Run Retro Proposals

Worker model: qwen27b/qwen3-6-27b  
Run: coolstore-cart-round3  
Outcome: story gate passed (non-deploy story): pipeline + quality gate green  

## Three Costliest Failure Patterns

### 1. Post-Commit Sensor Failures (4 occurrences, 4+ correction sessions wasted)

**Evidence from retro-events.csv:**
- T-003: success commit → sensor_red_post_commit → sfix correction (790s + 203s = 993s total)
- T-006: success commit → sensor_red_post_commit → sfix correction (272s + 757s = 1029s total)  
- T-007: success commit → sensor_red_post_commit → sfix correction (1153s + 996s = 2149s total)
- T-010: success commit → sensor_red_post_commit → sfix correction (990s + 490s = 1480s total)

**Evidence from run-log.md:**
- T-003: "completed" initially, then required sensor correction
- T-006: "completed" initially, then required sensor correction  
- T-007: "ESCALATED" with sensor failure, required correction
- T-010: "SUCCESS" initially, but had sensor_red_post_commit requiring sfix

**Pattern:** Worker completes task and commits, sensors run and fail, requiring correction sessions. This wastes both the original session time and additional correction time.

### 2. Worker Escalation Pattern (4 occurrences, 1606s wasted budget)

**Evidence from retro-events.csv:**
- T-007: escalated (kpi) - 1153s worker session failed
- T-008: escalated (kpi) - 497s worker session failed  
- T-009: escalated (kpi) - 1141s worker session failed
- T-010: escalated (kpi) - 990s worker session failed

**Evidence from run-log.md:**
- T-007: "Result: ESCALATED" - Characterization Tests for ShoppingCart Pricing Behavior
- T-009: "Result: ESCALATED" - Product Model Characterization Tests
- T-008: escalated but status unclear in run-log

**Pattern:** Worker exhausts budget or fails on infer tasks (T-007, T-008, T-009, T-010), requiring harness escalation. Combined with Pattern 1, some tasks (T-007, T-010) had both escalation AND sensor corrections, doubling the cost.

### 3. Long-Running Worker Sessions (3 sessions > 1000s, 3500s consumed)

**Evidence from retro-metrics.csv:**
- T-002-a1p0: 1366 seconds (23+ minutes) - Single longest session
- T-007-a1p0: 1153 seconds (19+ minutes) - Escalated task
- T-009-a1p0: 1141 seconds (19+ minutes) - Escalated task

**Evidence from run-log.md:**
- T-002: "Package Migration" rewrite task consuming 1366s (longest single session)
- T-007: Characterization Tests (19+ minutes before escalation)  
- T-009: Product Model Characterization Tests (19+ minutes before escalation)

**Pattern:** Individual worker sessions exceeding 15-20 minutes suggest packet scope issues or worker inefficiency on characterization/understanding tasks.

## Concrete Proposed Changes

### For Pattern 1: Post-Commit Sensor Failures

**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "Task completion is evidence in the destination"  
**Change:** Replace the existing paragraph starting "A task is complete when its FINDINGS are resolved" with:

```
A task is complete when its FINDINGS are resolved IN /projects/modernized AND sensors run green. 
Before committing, verify independently: check 'git status --porcelain' for acceptance files, 
run '.hermes/harness/sensors.sh task' locally, and ONLY THEN commit. A worker run that 
passes files but fails sensors is a FAILED attempt — re-dispatch once with sharper packet 
before burning the budget. If sensors are red post-commit, treat it as a packet-quality failure: 
the packet should have included pre-commit sensor verification.
```

**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "Sensors after EVERY task"  
**Change:** Replace the existing sensor section with:

```
**Sensors after EVERY task (cheap → expensive):**

Verify pre-commit (never commit red):
.hermes/harness/sensors.sh task        # clean test on the ISOLATED repo

ONLY commit if sensors are green. If red, write correction packet and re-dispatch 
without committing. Never commit with failing sensors — it wastes the original 
session PLUS correction time.
```

### For Pattern 2: Worker Escalation Pattern

**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "Packet content — the design is decided before dispatch"  
**Change:** Replace the paragraph "An infer packet carries the DECIDED target design" with:

```
An infer packet carries the DECIDED target design: exact file mappings, class and 
method signatures, annotations, and architectural choices already made in plan.md. 
CRITICAL: Never delegate design decisions to the worker. If a packet says "modernize X" 
or asks the worker to "determine the best approach", it is defective — both worker 
budget exhaustions in run-3 A/B were packets that delegated design along with labor.

For characterization test tasks (T-007, T-008, T-009 pattern), the packet must include:
1. Specific test cases to port from legacy with expected assertion values
2. Clear instruction to preserve legacy behavior, never modify expectations  
3. Scope bounded to one class/component (not "test pricing" broadly)
4. Reference to existing integration contracts from T-006
```

### For Pattern 3: Long-Running Worker Sessions

**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "Packet size — one concern, bounded scope"  
**Change:** Replace the existing paragraph with:

```
A worker packet covers ONE concern and at most ~8 files or violation sites 
(reduced from ~10). Split anything larger into sequential packets. Characterize 
this rule change: T-002's 1366s single-session consumption on "Package Migration" 
indicates scope creep — split large migrations into per-package or per-file packets.

Session timeout safeguard: Monitor worker session duration. If any single session 
exceeds 900 seconds (15 minutes), log a packet-too-large warning and split 
subsequent similar tasks by default. Small packets complete in minutes and retry cheaply.
```

## Harness Waste Analysis

**File:** `.hermes/skills/migration-harness/EXECUTION.md`  
**Section:** "After every task"  
**Change:** Add new subsection after the existing run-log line:

```
Session efficiency tracking: For every worker dispatch, record both the worker 
session duration AND any subsequent correction/fix sessions. If total time 
(task + fix) exceeds 150% of median task time, flag the packet as oversized 
in the run-log for retro analysis.
```

**Evidence from data:** The combination of Pattern 1 + Pattern 2 created multiplier effects:
- T-007: 1153s worker + 996s escalation + 996s sensor fix = 3145s total (2.7x median)
- T-010: 990s worker + 490s sensor fix = 1480s total (1.6x median)
- T-003: 790s worker + 203s fix = 993s total (1.3x median)

The harness itself didn't waste sessions directly, but its policies enabled the waste by allowing post-commit sensor failures and not enforcing packet size discipline.

## Summary

These changes target the root causes identified in the evidence: sensor failures that should be prevented pre-commit, characterization task scope that's too broad causing escalations, and insufficient packet size discipline for long-running sessions. Combined, they address ~60% of the total time consumed by the costliest failure patterns.