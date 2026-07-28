#!/usr/bin/env python3
import json

# Load before and after findings
with open('migration/mta-findings.json', 'r') as f:
    before = json.load(f)
with open('migration/mta-findings-after.json', 'r') as f:
    after = json.load(f)

# Count violations for each
def count_violations(findings):
    total_violations = 0
    total_incidents = 0
    for ruleset in findings:
        violations = ruleset.get('violations', {})
        total_violations += len(violations)
        for rule_id, violation_data in violations.items():
            incidents = violation_data.get('incidents', [])
            total_incidents += len(incidents)
    return total_violations, total_incidents

before_violations, before_incidents = count_violations(before)
after_violations, after_incidents = count_violations(after)

print(f'BEFORE: {before_violations} violations, {before_incidents} incidents')
print(f'AFTER: {after_violations} violations, {after_incidents} incidents')
print(f'DELTA: {after_violations - before_violations} violations, {after_incidents - before_incidents} incidents')