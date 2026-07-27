# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 15 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: d33a290a; run base: 8afd2da4b6b252dc61096ff86072f1fa7f6f549f
- Orchestrator: custom:maas-m2/minimax-m2; worker: qwen27b/qwen3-6-27b

## Sessions

| session | seconds | rc |
|---|---|---|
| T-001-a1p0 | 423 | rc=0 |
| T-002-a1p0 | 1366 | rc=0 |
| T-003-a1p0 | 790 | rc=0 |
| T-003-sfix | 203 | rc=0 |
| T-004-a1p0 | 696 | rc=0 |
| T-005-a1p0 | 730 | rc=0 |
| T-006-a1p0 | 272 | rc=0 |
| T-006-sfix | 757 | rc=0 |
| T-007-a1p0 | 1153 | rc=0 |
| T-007-sfix | 996 | rc=0 |
| T-008-a1p0 | 497 | rc=0 |
| T-009-a1p0 | 1141 | rc=0 |
| T-010-a1p0 | 990 | rc=0 |
| T-010-sfix | 490 | rc=0 |
| phaseD-a1p0 | 219 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     11 success
      4 sensor_red_post_commit
      4 escalated
      1 story_gate_pass
      1 pipeline_succeeded
```
