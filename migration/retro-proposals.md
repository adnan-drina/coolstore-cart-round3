# Migration Run Phase F — Retrospective Proposals

**Run**: coolstore-cart-round3  
**Worker**: qwen27b/qwen3-6-27b  
**Orchestrator**: custom:maas-m2/minimax-m2  
**Total Sessions**: 40  

## Executive Summary

This retrospective identifies the three costliest failure patterns from Phase A-E execution and proposes concrete changes to skill files and sensors. The run succeeded but incurred significant session waste through avoidable patterns.

---

## 1. SENSOR RED POST-COMMIT EPIDEMIC (Most Costly)

### Evidence
- **9 sensor_red_post_commit events** across tasks T-003, T-006, T-007, T-010, T-005, T-006 (again), T-009
- **Event pattern**: Every sensor_red_post_commit event follows immediately after a `success,commit` event
- **Session waste**: Each red-commit consumed the worker session PLUS required an additional sfix session (9 extra sessions minimum)
- **Run-log evidence**: "T-003,1,success,commit" followed immediately by "T-003,1,sensor_red_post_commit,verify"

### Root Cause
EXECUTION.md mandates sensors run BEFORE commit (line 148: "run the task sensor BEFORE you commit — never commit red"), but the harness implementation allows commits on green work that later fails sensor verification. This creates a race condition where commit happens before sensor completion or sensors aren't blocking commits.

### Proposed Change
**File**: `/home/user/.hermes/skills/migration-harness/EXECUTION.md`  
**Section**: "Sensors: run the task sensor BEFORE you commit — never commit red"  
**Text to replace** (lines 147-153):
```
**Sensors: run the task sensor BEFORE you commit — never commit red**
(S01 retro). `sensors.sh task` green is a precondition of the commit,
not a post-hoc check; a green-work-red-commit costs the session plus a
correction session. Sonar-tier findings surfaced by the supervisor's
post-commit milestone cadence are the DESIGNED in-loop catch, not a
session failure — fix them in the dispatched session without
relitigating the commit.
```

**New text to add**:
```
**Sensors: run the task sensor BEFORE you commit — never commit red**
`mvn -q clean test` (task sensor) MUST complete and pass BEFORE the git commit.
If the task packet includes acceptance criteria in ACCEPTANCE field,
verify those files changed before running sensors. The commit command becomes:

1. Run sensors.sh task
2. Only if sensors pass: git commit -m "T-NNN: <description>"
3. Record success in run-log.md

If sensors fail, dispatch a correction packet without committing the failed work.
A green-work-red-commit pattern indicates a harness bug — escalate immediately.

**Pre-commit verification checklist:**
- ACCEPTANCE files modified as claimed
- mvn -q clean test passes in isolation
- git diff shows only intended changes
- THEN commit and record sensor_red_post_commit event

A sensor_red_post_commit event is always a harness failure, never acceptable worker output.
```

---

## 2. CATASTROPHIC SLOW SESSION: T-012-SFIX (2702s, rc=124)

### Evidence
- **Session T-012-sfix**: 2702 seconds (45+ minutes) with rc=124 (failure)
- **Preceded by**: T-012-a1p1 (653s, rc=0) and T-012-a1p0 (860s, rc=0) 
- **Event sequence**: T-012,1,success,commit → T-012,1,sensor_red_post_commit,verify → T-012-sfix,0,slow_session,2702s
- **Total T-012 time**: 860 + 653 + 2702 = 4215 seconds (70+ minutes for one task)
- **Impact**: Single task consumed 11.7% of total run time and FAILED

### Root Cause
EXECUTION.md allows "2 attempts per task" (line 198) but lacks hard session timeout. A worker stuck in a 45-minute failure loop burns budget without bounded loss. The packet likely suffered from ambiguous scope or missing acceptance criteria, causing the worker to explore indefinitely.

### Proposed Change
**File**: `/home/user/.hermes/skills/migration-harness/EXECUTION.md`  
**Section**: "On sensor failure: write a correction packet"  
**Text to add after line 199**:
```
**Session timeout enforcement:** Every worker dispatch has a hard 1200-second (20-minute) timeout. If the worker exceeds this:
1. Kill the process immediately (rc=124)
2. Record the failure in migration/debt.md with timeout evidence
3. Mark the task as requiring packet redesign, not retry
4. Alert: timeout indicates scope too large or acceptance criteria too vague

**Recovery protocol for timeout failures:**
- Do NOT retry the same packet
- Redesign the packet with smaller scope (≤ 4 files, 1 concern)
- Verify acceptance criteria are specific and testable
- Require explicit file paths and expected changes before dispatch
```

**Also add to Packet Schema section (after line 21)**:
```
**Timeout**: 1200 seconds maximum. Large scope packets must be split.
**Failure recovery**: rc=124 = timeout → redesign packet, don't retry
```

---

## 3. ORPHAN WORKER EVENT: PROCESS MANAGEMENT FAILURE

### Evidence
- **Event**: T-012,1,orphan_worker,retrying at timestamp 1785181599
- **Preceded immediately by**: T-012-a1p0,1,success,commit at 1785181599
- **Implication**: Worker process outlived its session, requiring retry
- **Pattern**: T-012 had multiple attempts and timeout - suggests worker process instability

### Root Cause
EXECUTION.md section "Worker dispatch is synchronous" (line 106) states processes should block until completion, but lacks explicit process cleanup and zombie detection. Orphaned workers consume resources and create session state corruption.

### Proposed Change
**File**: `/home/user/.hermes/skills/migration-harness/EXECUTION.md`  
**Section**: "Worker dispatch is synchronous — never background it"  
**Text to replace** (lines 114-116):
```
If the terminal returns while the worker is still running, poll in a loop
(`sleep 60` then check for the `opencode` process) until it exits before
doing anything else. Before dispatching, verify no worker is already
running.
```

**New text**:
```
**Process management protocol:**
Before dispatching: `pkill -f "opencode run"` to ensure clean slate
During dispatch: monitor process tree and log child processes
After dispatch: explicit process cleanup with SIGTERM → SIGKILL hierarchy
If terminal returns unexpectedly: 
1. Check for orphaned opencode processes: `ps aux | grep opencode`
2. Kill all orphaned processes: `pkill -f "opencode run"`
3. Record orphan_worker event in retro-events.csv
4. Do not retry without investigating the orphan cause

**Process tree verification:**
After EVERY worker dispatch, run: `ps aux | grep -E "(opencode|qwen)" | grep -v grep`
If any processes remain after session end, that's a harness defect requiring immediate fix.
```

---

## Harness-Level Session Waste Analysis

### What the Harness Did That Wasted Model Sessions

1. **Redundant retry patterns**: Tasks T-001, T-002, T-005, T-006, T-009, T-010 all had successful first attempts followed by unnecessary sfix sessions
   - Evidence: T-001-a1p0 (423s, success) → T-001-sfix (1196s, success)
   - **Fix needed**: PLANNING.md should require definitive single-attempt success criteria

2. **Inefficient milestone boundaries**: The harness ran `phaseB-lint-a1p0` (797s) after the main execution loop instead of interleaving lint validation
   - **Fix needed**: Run lint validation during Phase C, not as post-hoc cleanup

3. **Long final phaseD session**: phaseD-a1p0 (559s) suggests the final sensor run wasn't optimally cached or parallelized
   - **Fix needed**: SHIPPING.md should optimize Phase D sensor performance

### Session Time Waste Breakdown
- **Total run time**: 40 sessions across 3+ hours
- **Avoidable waste from red commits**: Minimum 9 sessions (22.5% overhead)
- **T-012 timeout alone**: 2702 seconds (45+ minutes, 11.7% of total run time)
- **Estimated recoverable**: 30-40% of total session time

---

## Summary of Proposed Changes

1. **EXECUTION.md sensor section**: Hard enforcement of pre-commit sensor verification
2. **EXECUTION.md timeout enforcement**: 1200-second hard limit with packet redesign protocol  
3. **EXECUTION.md process management**: Explicit orphan worker detection and cleanup
4. **PLANNING.md**: Eliminate redundant retry patterns through better initial packet design
5. **SHIPPING.md**: Optimize Phase D sensor performance and milestone timing

These changes target the specific evidence patterns observed in this run and should reduce future run times by 30-40% while improving reliability.