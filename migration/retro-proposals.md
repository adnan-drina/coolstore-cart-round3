# Migration Run Retro — coolstore-cart-round3

## Brief updates (auto-applicable)

**No brief updates required.** All story briefs (S01-S05) are complete and require no modifications. The migration run successfully completed with all planned stories finishing within scope.

## Skill / harness proposals (human-only)

### Three costliest failure patterns of this run

**1. Sensor-red-post-commit cycle waste (8 occurrences)**
Evidence: retro-events.csv shows "sensor_red_post_commit" occurs 8 times across the run. Each occurrence follows the pattern: task commits successfully → post-commit sensors run → verification fails → correction session required. This represents the most frequent failure pattern and wasted sessions fixing issues that should have been caught pre-commit.

**2. Correction-session red-commit waste (4 occurrences)**  
Evidence: retro-events.csv shows "sfix_committed_still_red" occurs 4 times. Despite running a correction session to fix a sensor failure, the fix session itself commits and remains red (rc=124), requiring a third session. This indicates correction packets fail to isolate the exact failure scope.

**3. Slow-session budget burn (1 occurrence)**
Evidence: retro-metrics.csv shows "batch-T-001-T-002-T-003" session runs for 2703 seconds (45+ minutes) and returns rc=124 (failure). This represents the single largest time budget waste in the run, indicating packet sizing or worker efficiency issues.

### Concrete proposed skill changes

**1. For sensor-red-post-commit waste**

**File: .hermes/skills/migration-harness/EXECUTION.md**
**Section: "Sensors: run the task sensor BEFORE you commit"**
**Current text:**
> Run the task sensor EXACTLY ONCE, immediately before the commit — not after every edit (each run is a full Maven cycle; sessions were measured spending 2–4 of them). Edit until you believe the work is done, run the sensor once, fix only what it reports, commit.

**Proposed change:**
> Run the task sensor EXACTLY ONCE, immediately before the commit — not after every edit (each run is a full Maven cycle; sessions were measured spending 2–4 of them). Edit until you believe the work is done, run the sensor once, fix only what it reports, commit. **MANDATORY: sensors.sh task must return GREEN before any commit attempt. If sensors.sh task returns RED, do NOT commit — record the failure evidence and dispatch a correction packet immediately. Never commit red regardless of budget pressure.**

**Rationale:** The 8 sensor-red-post-commit events prove workers commit despite red sensors. Making sensor-green a hard precondition prevents red commits entirely, eliminating this waste pattern.

**2. For correction-session red-commit waste**

**File: .hermes/skills/migration-harness/EXECUTION.md**  
**Section: "On sensor failure"**
**Current text:**
> On sensor failure: write a correction packet — the original packet plus the exact failure output and the instruction "fix only this failure; change nothing else" — and re-delegate. Iteration budget: 2 attempts per task (original + one correction).

**Proposed change:**
> On sensor failure: write a correction packet — the original packet plus the exact failure output and the instruction "fix only this failure; change only what the failure evidence reports; change nothing else" — and re-delegate. **CRITICAL: the correction session MUST run sensors.sh task locally before committing. If sensors.sh task still returns RED after the correction attempt, do NOT commit — record debt with the evidence of persistent failure and escalate to the harness orchestrator.** Iteration budget: 2 attempts per task (original + one correction), with hard no-commit-on-red enforcement.

**Rationale:** The 4 sfix_committed_still_red events show correction sessions fail to properly scope their fixes. Adding mandatory pre-commit sensor verification prevents second-level waste.

**3. For slow-session budget burn**

**File: .hermes/skills/migration-harness/EXECUTION.md**
**Section: "Packet size — one concern, bounded scope"**
**Current text:**
> A worker packet covers ONE concern and at most ~8 files or violation sites. Split anything larger into sequential packets. Large single packets push the worker (and you) into planning generations that outlast client timeouts; small packets complete in minutes and retry cheaply.

**Proposed change:**
> A worker packet covers ONE concern and at most ~8 files or violation sites. Split anything larger into sequential packets. Large single packets push the worker (and you) into planning generations that outlast client timeouts; small packets complete in minutes and retry cheaply. **HARD LIMIT: individual packets must complete within 1800 seconds (30 minutes). If a packet requires longer, split it regardless of the ~8 file guideline. Long-running batch sessions (T-001-T-002-T-003) are prohibited.**

**Rationale:** The 2703-second batch session represents massive budget waste. A hard time limit prevents oversized packets from burning sessions without delivering value.

### Artifact review of this run's commits

**Harvest fidelity:** HIGH (31 successful commits vs 8 sensor failures)
Evidence: The 31 success events and the 75% violation reduction (24→6 violations) show strong harvest fidelity. Most classes converted successfully on first attempt without fabrication.

**Story-scope compliance:** STRONG (2 scope_violation events only)
Evidence: Only 2 scope_violation events occurred across 53 sessions, with 1 "orphan_worker" and 1 "later_story_class" indicating good scope discipline. The brief-based story separation worked effectively.

**Fabrication quality:** LOW RISK (limited debt recorded)
Evidence: Only 4 debt_recorded events total across the run, and debt.md shows "All prior sensor-RED entries resolved at the green ship" indicating minimal lasting fabrication issues.

### Harness waste

**Session efficiency:** MODERATE WASTE (53 sessions for 5 stories)
53 model sessions across 5 stories averages ~10.6 sessions per story. The three failure patterns above represent identifiable waste that could be reduced through the proposed skill changes.

**Budget utilization:** ACCEPTABLE (mostly green outcomes)
Despite the failure patterns, the run achieved story gate passed status with pipeline + quality gate green. The 75% violation reduction validates that session investment produced meaningful migration progress.

**Supervisor effectiveness:** STRONG (clean debt slate)
The supervisor successfully maintained a clean debt ledger with all prior sensor-RED entries resolved before the retro, indicating effective correction tracking and resolution management.