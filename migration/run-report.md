# Autonomous run report

## Executive summary

Autonomous migration of coolstore-cart-round3:
success: shipped, route 200, 1 products. Findings delta and per-task detail: migration/run-log.md;
debt: migration/debt.md. Orchestrator custom:maas-m2/minimax-m2,
worker qwen27b/qwen3-6-27b, 40 model sessions.

- Outcome: success: shipped, route 200, 1 products
- Supervisor version: bb3cb4c2; run base: c8cee14f9e1e5621036d02b5a4b801e5cd23e631
- Orchestrator: custom:maas-m2/minimax-m2; worker: qwen27b/qwen3-6-27b

## Sessions

| session | seconds | rc |
|---|---|---|
| batch-T-001-T-002-T-003 | 1156 | rc=0 |
| batch-T-004-T-005-T-006 | 1478 | rc=0 |
| batch-T-007-T-008-T-009 | 731 | rc=0 |
| T-010-a1p0 | 672 | rc=0 |
| T-010-a2p0 | 867 | rc=0 |
| T-010-sfix | 202 | rc=0 |
| T-011-a1p0 | 513 | rc=0 |
| T-012-a1p0 | 89 | rc=0 |
| T-013-a1p0 | 152 | rc=0 |
| T-013-a2p0 | 58 | rc=0 |
| T-013-sfix | 903 | rc=124 |
| T-014-a1p0 | 932 | rc=0 |
| T-014-sfix | 902 | rc=124 |
| T-015-a1p0 | 727 | rc=0 |
| T-016-a1p0 | 63 | rc=0 |
| T-016-a2p0 | 447 | rc=0 |
| T-017-a1p0 | 1164 | rc=0 |
| T-017-sfix | 235 | rc=0 |
| T-018-a1p0 | 456 | rc=0 |
| T-019-a1p0 | 1149 | rc=0 |
| T-020-a1p0 | 253 | rc=0 |
| T-020-sfix | 630 | rc=0 |
| T-021-a1p0 | 212 | rc=0 |
| T-022-a1p0 | 445 | rc=0 |
| T-022-a2p0 | 48 | rc=0 |
| T-023-a1p0 | 892 | rc=0 |
| T-023-a1p1 | 59 | rc=0 |
| T-023-sfix | 902 | rc=124 |
| T-024-a1p0 | 267 | rc=0 |
| T-024-a1p1 | 551 | rc=0 |
| T-025-a1p0 | 224 | rc=0 |
| T-025-a2p0 | 64 | rc=0 |
| T-026-a1p0 | 523 | rc=0 |
| T-027-a1p0 | 564 | rc=0 |
| phaseD-a1p0 | 63 | rc=0 |
| deployfix-r1-a1p0 | 930 | rc=0 |
| deployfix-r1-a2p0 | 469 | rc=0 |
| preflightfix-r1-a1p0 | 903 | rc=124 |
| deployfix-r2-a1p0 | 931 | rc=0 |
| deployfix-r2-a2p0 | 482 | rc=0 |

- Escalations (KPI, from supervisor events): 0 (untested: 0)

## Classified events

```
     22 success
      8 escalated
      7 no_commit
      6 sensor_red_post_commit
      3 pipeline_succeeded
      2 quota
      2 mechanical_commit
      2 escalated_untested
      1 preflight_red
      1 acceptance_pass
```
