# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 27 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: 3b9b38f2; run base: c2c0456598ac19f1d00ed46406a6bb3a366752ee
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
| retro | 39 | rc=0 |
| batch-T-001-T-002-T-003 | 852 | rc=0 |
| T-004-a1p0 | 81 | rc=0 |
| T-005-a1p0 | 325 | rc=0 |
| T-005-sfix | 365 | rc=0 |
| T-006-a1p0 | 222 | rc=0 |
| T-007-a1p0 | 286 | rc=0 |
| T-007-a1p1 | 66 | rc=0 |
| m5-evaluate-a1p0 | 217 | rc=0 |
| m5-evaluate-a2p0 | 665 | rc=0 |
| treefix | 428 | rc=0 |
| batch-T-001-T-002-T-003 | 2703 | rc=124 |
| T-004-a1p0 | 586 | rc=0 |
| T-005-a1p0 | 231 | rc=0 |
| T-005-a1p0 | 987 | rc=0 |
| T-006-a1p0 | 317 | rc=0 |
| T-007-a1p0 | 335 | rc=0 |
| T-007-sfix | 902 | rc=124 |
| m5-evaluate-a1p0 | 428 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     17 success
      4 sensor_red_post_commit
      2 story_gate_pass
      2 sfix_committed_still_red
      2 sensor_red_at_entry
      2 scope_violation
      2 pipeline_succeeded
      1 slow_session
      1 orphan_worker
      1 no_commit
      1 debt_recorded
```
