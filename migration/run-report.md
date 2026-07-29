# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
story gate passed (non-deploy story): pipeline + quality gate green. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 42 model sessions.

- Outcome: story gate passed (non-deploy story): pipeline + quality gate green
- Supervisor version: 5f63476f; run base: 1129322b005ac0c07c94621a25eafa14ce887881
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
| retro | 94 | rc=0 |
| batch-T-030-T-031-T-032 | 132 | rc=0 |
| T-033-a1p0 | 304 | rc=0 |
| T-034-a1p0 | 312 | rc=0 |
| T-034-sfix | 194 | rc=0 |
| T-035-a1p0 | 125 | rc=0 |
| T-035-a2p0 | 278 | rc=0 |
| T-035-sfix | 903 | rc=124 |
| m5-evaluate-a1p0 | 53 | rc=0 |
| batch-T-030-T-031-T-032 | 138 | rc=0 |
| T-033-a1p0 | 181 | rc=0 |
| T-033-a2p0 | 49 | rc=0 |
| T-034-a1p0 | 96 | rc=0 |
| T-035-a1p0 | 238 | rc=0 |
| m5-evaluate-a1p0 | 62 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     25 success
      6 sensor_red_post_commit
      3 story_gate_pass
      3 sfix_committed_still_red
      3 pipeline_succeeded
      3 no_commit
      3 debt_recorded
      2 sensor_red_at_entry
      2 scope_violation
      1 style_autofix
      1 slow_session
      1 preflight_red
      1 orphan_worker
      1 later_story_class
```
