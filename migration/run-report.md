# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
success: shipped, route 200, 1 products. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 72 model sessions.

- Outcome: success: shipped, route 200, 1 products
- Supervisor version: 5f63476f; run base: 52597001b9e19d3b7746a2515d422565c5170d98
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
| retro | 52 | rc=0 |
| batch-T-001-T-002 | 156 | rc=0 |
| T-003-a1p0 | 356 | rc=0 |
| T-003-sfix | 902 | rc=124 |
| T-004-a1p0 | 225 | rc=0 |
| T-005-a1p0 | 491 | rc=0 |
| T-005-a1p1 | 69 | rc=0 |
| T-006-a1p0 | 757 | rc=0 |
| T-006-sfix | 862 | rc=0 |
| m5-evaluate-a1p0 | 86 | rc=0 |
| preflightfix-r1-a1p0 | 759 | rc=0 |
| retro | 108 | rc=0 |
| T-001-a1p0 | 1251 | rc=0 |
| T-002-a1p0 | 124 | rc=0 |
| T-003-a1p0 | 1195 | rc=0 |
| T-004-a1p0 | 1856 | rc=0 |
| T-005-a1p0 | 488 | rc=0 |
| T-005-a2p0 | 821 | rc=0 |
| T-005-sfix | 902 | rc=124 |
| T-006-a1p0 | 1923 | rc=0 |
| T-006-sfix | 835 | rc=0 |
| T-007-a1p0 | 298 | rc=0 |
| T-007-sfix | 903 | rc=124 |
| T-008-a1p0 | 806 | rc=0 |
| T-008-a1p1 | 333 | rc=0 |
| T-008-a2p1 | 1524 | rc=0 |
| T-009-a1p0 | 1071 | rc=0 |
| T-010-a1p0 | 485 | rc=0 |
| m5-evaluate-a1p0 | 99 | rc=0 |
| deployfix-r1-a1p0 | 887 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     43 success
     11 sensor_red_post_commit
      6 pipeline_succeeded
      5 sfix_committed_still_red
      5 no_commit
      5 debt_recorded
      4 style_autofix
      4 story_gate_pass
      4 scope_violation
      3 slow_session
      3 orphan_worker
      2 sensor_red_at_entry
      2 preflight_red
      1 later_story_class
      1 acceptance_pass
```
