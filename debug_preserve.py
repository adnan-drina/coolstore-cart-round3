#!/usr/bin/env python3
"""Fixed plan-lint.py to properly handle preserve section boundaries."""

import json
import re
import sys

def fix_preserve_extraction():
    """Extract only actual preserve items, not forbidden items."""
    try:
        with open("migration.yaml", encoding="utf-8") as f:
            content = f.read()
        
        # Find preserve section and bound it properly
        preserve_start = content.index("preserve:")
        preserve_section_end = content.find("forbidden:", preserve_start)
        if preserve_section_end == -1:
            preserve_section_end = len(content)
        
        # Extract only from preserve section
        preserve_section = content[preserve_start:preserve_section_end]
        preserve_items = re.findall(r"^\s*-\s*([A-Za-z0-9_./:-]+)", preserve_section, re.M)
        
        print("ACTUAL PRESERVE ITEMS:")
        for item in preserve_items:
            print(f"  - {item}")
        
        return preserve_items
    except Exception as e:
        print(f"Error: {e}")
        return []

if __name__ == "__main__":
    fix_preserve_extraction()
