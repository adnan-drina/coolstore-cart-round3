# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 8 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: 3d0a1342; run base: aed2130f404421523ca91274527d74a6a5964095
- Orchestrator: custom:maas-m2/minimax-m2; worker: qwen27b/qwen3-6-27b

## Sessions

| session | seconds | rc |
|---|---|---|
| batch-T-001-T-002-T-003 | 879 | rc=0 |
| batch-T-004-T-005-T-006 | 776 | rc=0 |
| T-007-a1p0 | 508 | rc=0 |
| T-008-a1p0 | 123 | rc=0 |
| T-009-a1p0 | 111 | rc=0 |
| T-010-a1p0 | 233 | rc=0 |
| T-011-a1p0 | 204 | rc=0 |
| m5-evaluate-a1p0 | 80 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
      6 success
      1 story_gate_pass
      1 pipeline_succeeded
```
