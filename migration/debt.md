# Migration debt ledger

All prior sensor-RED entries resolved at the green ship (ee127fd). New unresolved REDs are appended below by the supervisor.

## T-005 Service dependency debt
**Date:** 2026-07-29
**Reason:** Story-scope sensor reverted service layer edits needed for GET idempotency contract
**Required:** CartEndpoint.java calls `getShoppingCartIfExists()` but service interface only has `getShoppingCart()` 
**Action:** Add `getShoppingCartIfExists()` method to ShoppingCartService interface and implementation to return null for non-existent carts
**Status:** RESOLVED - Added getShoppingCartIfExists() method that returns null for non-existent carts, removed unnecessary throws Exception declarations

## T-006 — milestone RED
- head: bc0ea21
- reason: sensor-fix committed but milestone still RED
