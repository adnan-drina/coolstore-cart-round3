# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
success: shipped, route 200, 1 products. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 58 model sessions.

- Outcome: success: shipped, route 200, 1 products
- Supervisor version: 4afd671a; run base: 4c9a112b4c6ef4c1b9718a41acef75b6115be5dd
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
| phaseF | 38 | rc=0 |
| phaseB-lint-a1p0 | 797 | rc=0 |
| treefix | 92 | rc=0 |
| T-002-a1p0 | 151 | rc=0 |
| T-001-a1p0 | 1416 | rc=0 |
| T-001-sfix | 1196 | rc=0 |
| T-002-a1p0 | 1053 | rc=0 |
| T-003-a1p0 | 161 | rc=0 |
| T-004-a1p0 | 687 | rc=0 |
| T-005-a1p0 | 196 | rc=0 |
| T-005-sfix | 628 | rc=0 |
| T-006-a1p0 | 546 | rc=0 |
| T-006-sfix | 797 | rc=0 |
| T-007-a1p0 | 298 | rc=0 |
| T-008-a1p0 | 878 | rc=0 |
| T-009-a1p0 | 446 | rc=0 |
| T-009-sfix | 1400 | rc=0 |
| T-010-a1p0 | 585 | rc=0 |
| T-011-a1p0 | 1277 | rc=0 |
| T-012-a1p0 | 860 | rc=0 |
| T-012-a1p1 | 653 | rc=0 |
| T-012-sfix | 2702 | rc=124 |
| T-013-a1p0 | 1412 | rc=0 |
| T-014-a1p0 | 1970 | rc=0 |
| phaseD-a1p0 | 559 | rc=0 |
| phaseF | 62 | rc=0 |
| T-001-a1p0 | 1123 | rc=0 |
| T-002-a1p0 | 539 | rc=0 |
| T-003-a1p0 | 568 | rc=0 |
| T-003-sfix | 903 | rc=124 |
| T-004-a1p0 | 878 | rc=0 |
| T-005-a1p0 | 674 | rc=0 |
| T-005-a1p1 | 445 | rc=0 |
| T-005-sfix | 903 | rc=124 |
| T-006-a1p0 | 1111 | rc=0 |
| T-007-a1p0 | 865 | rc=0 |
| T-007-a2p0 | 1544 | rc=0 |
| phaseD-a1p0 | 843 | rc=0 |
| phaseD-sfix | 903 | rc=124 |
| preflightfix-r1-a1p0 | 902 | rc=124 |
| preflightfix-r1-a2p0 | 902 | rc=124 |
| preflightfix-r2-a1p0 | 514 | rc=0 |
| preflightfix-r2-a2p0 | 198 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     37 success
     12 sensor_red_post_commit
      4 escalated
      3 pipeline_succeeded
      2 timeout
      2 slow_session
      2 preflight_red
      2 orphan_worker
      2 no_commit
      2 mechanical_commit
      2 acceptance_pass
      1 story_gate_pass
      1 sensor_red_at_entry
```
