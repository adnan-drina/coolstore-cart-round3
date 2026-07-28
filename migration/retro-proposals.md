# Phase F Retro - coolstore-cart-round3

## 1. Three Costliest Failure Patterns

### Pattern A: Post-Commit Sensor Detection (6 instances, 6 correction sessions)
**Evidence:**
- `retro-events.csv` lines 9, 17, 26, 35, 41, 51: `sensor_red_post_commit` events
- `retro-metrics.csv` shows 6 correction sessions with rc=124 (T-013-sfix: 903s, T-014-sfix: 902s, T-023-sfix: 902s, preflightfix-r1-a1p0: 903s, etc.)
- **Cost**: 6 sessions × average 850s = 5,100 seconds wasted + sensor-red commit contamination

**Root Cause**: EXECUTION.md states "run the task sensor EXACTLY ONCE, immediately before the commit" but workers commit first, then supervisor sensors detect issues. This creates a red commit that requires correction sessions.

### Pattern B: Worker No-Commit Failures (7 instances, 7 retries)
**Evidence:**
- `retro-events.csv` lines 3, 8, 16, 30, 41, 49, 54: `no_commit` events requiring retries
- Session durations show immediate retries: T-010-a1p0 (672s) followed by T-010-a2p0 (867s), T-016-a1p0 (63s) followed by T-016-a2p0 (447s)
- **Cost**: 7 wasted sessions + retry overhead

**Root Cause**: Task packets lack sufficient implementation detail. Workers complete work but fail to commit it, indicating unclear expectations about what constitutes task completion.

### Pattern C: Excessive Escalation Valve Usage (8 escalated events)
**Evidence:**
- `retro-events.csv` lines 21-22, 24, 28, 31, 34, 38, 42, 44: `escalated` and `escalated_untested` events
- T-019 row in run-log: "ESCALATED - Worker didn't create file; implemented directly per migration runbook escalation valve"
- **Cost**: 8 escalations bypassing the OpenCode contract

**Root Cause**: Worker frequently cannot complete bounded tasks within packet scope, indicating task boundaries are too large or implementation guidance is insufficient.

## 2. Concrete Proposed Changes

### Change A: Fix Sensor Timing (EXECUTION.md Section "Sensors after EVERY task")
**File**: `migration-harness/EXECUTION.md`
**Section**: Lines 171-175 ("Run the task sensor EXACTLY ONCE...")

**Replace**:
```
**Run the task sensor EXACTLY ONCE, immediately before the commit** — 
not after every edit (each run is a full Maven cycle; sessions were 
measured spending 2–4 of them). Edit until you believe the work is 
done, run the sensor once, fix only what it reports, commit.
```

**With**:
```
**MANDATORY PRE-COMMIT SENSOR CHECK**: Before ANY commit attempt, 
run `.hermes/harness/sensors.sh task` and verify GREEN output. 
NEVER commit red. If sensors fail, fix only the reported issues 
and re-run the sensor check - do not proceed to commit. This 
prevents the sensor_red_post_commit classification that triggers 
correction sessions.
```

### Change B: Strengthen Task Packet Completion Criteria (EXECUTION.md Section "Task packet schema")
**File**: `migration-harness/EXECUTION.md`
**Section**: Lines 10-21 (Task packet schema)

**Replace**:
```
Task ID:        T-014
Class:          infer
Goal:           <one sentence>
Findings:       <rule ids this task resolves>
Constraints:    follow AGENTS.md and the repo skills; no scope creep
Inputs:         attached via -f (spec.md, tasks.md, touched files)
Acceptance:     <files expected to change>; mvn -q clean test passes
Out of scope:   <explicitly excluded work>
```

**With**:
```
Task ID:        T-014
Class:          infer
Goal:           <one sentence>
Findings:       <rule ids this task resolves>
Constraints:    follow AGENTS.md and the repo skills; no scope creep
Inputs:         attached via -f (spec.md, tasks.md, touched files)
Acceptance:     <files expected to change>; mvn -q clean test passes
Out of scope:   <explicitly excluded work>

COMPLETION CRITERIA - ALL MUST BE MET BEFORE COMMIT:
1. All acceptance files exist and show the exact changes described
2. `.hermes/harness/sensors.sh task` returns GREEN status
3. Git status shows ONLY the expected acceptance files as modified
4. Commit message follows pattern: "T-XXX: <brief description>"

WORKER FAILURE PROTOCOL: If you cannot meet ALL criteria within 
the session, do NOT commit partial work. Report what prevented 
completion and request escalation rather than creating a no_commit event.
```

### Change C: Add Event Classification for No-Commit Prevention (EXECUTION.md Section "On sensor failure")
**File**: `migration-harness/EXECUTION.md`
**Section**: Lines 225-230 ("On sensor failure:")

**After existing content, add**:
```
NO-COMMIT EVENT PREVENTION: If a worker session ends without creating 
the expected commit, classify this as a PACKET DESIGN FAILURE, not a 
worker failure. Immediate actions:
1. Review the packet for missing implementation detail or overly broad scope
2. Split into smaller, more bounded tasks if needed
3. Add explicit file paths and expected changes to the packet
4. Re-dispatch with improved packet - do NOT retry the same packet

The no_commit classification indicates the packet failed to provide 
sufficient guidance for bounded completion, not that the worker failed.
```

### Change D: Implement Task Size Budget Sensor (new sensor in SHIPPING.md)
**File**: `migration-harness/SHIPPING.md`
**Section**: After Phase D procedures (line 30)

**Add**:
```
TASK SIZE GOVERNANCE: During execution, monitor task session durations.
If a single infer task exceeds 600 seconds (10 minutes), this indicates:
- Packet scope is too large
- Implementation guidance is insufficient  
- Task should be split into smaller units

Flag sessions exceeding 600s as TASK_SCOPE_VIOLATION and add to 
retropective analysis for future packet size improvements.
```

## 3. Artifact Review - Commit Analysis

### T-019 (2931b0f): CartEndpoint Conversion
**Harvest Fidelity**: GOOD - Created new file with complete conversion from Spring MVC to JAX-RS
- Package migration: com.redhat.coolstore.rest → com.demo.rest ✓
- Constructor injection replacing @Autowired field injection ✓
- All 5 endpoint behaviors preserved exactly ✓
- Proper jakarta imports and annotations ✓

**Story-Scope Discipline**: EXCELLENT - Modified ONLY CartEndpoint.java as expected
- No drive-by changes to other files ✓
- Clear task completion with expected file creation ✓

**Fabrication Patterns**: NONE DETECTED - Real migration, no stubs or mocks

### T-020 Escalation (ed0f30e): Resolved-by-Scaffold 
**Harvest Fidelity**: POOR - This should not have required a session
- ShoppingCartServiceImpl was already converted in previous work
- Resolution should have been identified in planning phase
- Worker didn't need to touch this file

**Story-Scope Discipline**: N/A - No actual work performed

**Fabrication Patterns**: N/A - No code changes

### Mechanical Commit (825d5d3): Sensor-Fix Pattern
**Harvest Fidelity**: MIXED - Several good fixes but formatting changes look automated
- Removed unnecessary Serializable implementations ✓
- Cleaned up generic type declarations ✓  
- Removed unnecessary throws clauses ✓
- BUT: Wholesale formatting changes suggest automated rather than semantic fixes

**Story-Scope Discipline**: GOOD - Targeted specific sensor violations
- Fixed S3824 null check violations ✓
- Removed unused imports ✓
- No scope creep ✓

**Fabrication Patterns**: NONE - All changes were legitimate sensor fixes

### Final Phase D Commit (3e4a9f2): Re-analysis Results
**Harvest Fidelity**: EXCELLENT - Proper re-analysis workflow
- Generated new findings file showing 71% violation reduction ✓
- Updated debt classification properly ✓
- Clear documentation of what was resolved vs. deferred ✓

**Story-Scope Discipline**: EXCELLENT - Only migration analysis files changed
- No src/ modifications in this commit ✓
- Proper documentation approach ✓

**Fabrication Patterns**: NONE - This was analysis/documentation work

### Overall Artifact Assessment
**What telemetry missed**: The V3 pattern where every artifact-level defect was missed by the quality gate. In this run:
- No fabricated platform stubs detected (telemetry would miss this)
- No mock/fallback fabrication patterns found (T-022 specifically scanned for this)
- All endpoint conversions were real migrations, not compatibility layers
- The only "defect" was T-020 being unnecessarily dispatched when already resolved

**Verification**: The commit diffs show real behavior preservation:
- CartEndpoint maintains exact 5-endpoint contract
- All pricing calculations preserved in characterization tests  
- Constructor injection properly implemented throughout
- No compatibility shims or temporary stubs

## 4. Harness Behavior That Wasted Model Sessions

### Session Count Waste
- **40 total sessions** for 27 core tasks + 3 deploy fixes = 1.48 sessions per task
- 6 correction sessions for sensor_red_post_commit = 15% overhead
- 7 retry sessions for no_commit events = 17.5% overhead  
- **Net efficiency**: Only 67% of sessions produced new commits

### Long-Running Session Pattern
Several sessions exceeded 900 seconds (15+ minutes):
- T-017-a1p0: 1164s (19.4 minutes)
- T-014-a1p0: 932s (15.5 minutes) 
- T-014-sfix: 902s (15.0 minutes)
- deployfix-r1-a1p0: 930s (15.5 minutes)
- deployfix-r2-a1p0: 931s (15.5 minutes)

This indicates packets were too large or lacked sufficient implementation guidance.

### Deploy Fix Iteration Waste
Two deploy fix rounds (r1 and r2) suggest:
- Phase E gate loop ran multiple times unnecessarily
- Each round burned 2 sessions (930s + 469s for r1, 931s + 482s for r2)
- Total deploy-fix waste: ~2,812 seconds across 4 sessions

## Summary

The telemetry captured the cost patterns accurately, but the harness procedures need strengthening to prevent them. The three proposed changes to EXECUTION.md directly address the root causes: sensor timing, packet completion clarity, and no-commit prevention. The artifact review shows good harvest fidelity when tasks were properly scoped, but several sessions were wasted on unclear packets or already-resolved items.

**Costliest Impact**: Post-commit sensor detection (Pattern A) caused 6 correction sessions totaling ~5,100 seconds - over 1.4 hours of compute time that could have been prevented with pre-commit sensor checks.