# Legacy dependency analysis (scripted, Phase A)

- Classes: 12; intra-project import edges: 12
- Explicit imports only — same-package implicit references are not resolved; treat same-package groups as coupled.

## God nodes (highest fan-in — pin behavior with characterization tests BEFORE converting)

| class | fan-in | fan-out |
|---|---|---|
| com.redhat.coolstore.model.ShoppingCart | 5 | 0 |
| com.redhat.coolstore.model.Product | 3 | 0 |
| com.redhat.coolstore.model.ShoppingCartItem | 2 | 0 |
| com.redhat.coolstore.model.Promotion | 1 | 0 |
| com.redhat.coolstore.service.ShoppingCartService | 1 | 2 |

## Conversion order (dependencies first — the tree must compile at every commit)

1. com.redhat.coolstore.model.ShoppingCart (src/main/java/com/redhat/coolstore/model/ShoppingCart.java) — god-node: characterization tests first
2. com.redhat.coolstore.model.Product (src/main/java/com/redhat/coolstore/model/Product.java) — god-node: characterization tests first
3. com.redhat.coolstore.model.ShoppingCartItem (src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java)
4. com.redhat.coolstore.model.Promotion (src/main/java/com/redhat/coolstore/model/Promotion.java)
5. com.redhat.coolstore.CartServiceApplication (src/main/java/com/redhat/coolstore/CartServiceApplication.java)
6. com.redhat.coolstore.rest.JerseyConfig (src/main/java/com/redhat/coolstore/rest/JerseyConfig.java)
7. com.redhat.coolstore.service.ShoppingCartService (src/main/java/com/redhat/coolstore/service/ShoppingCartService.java)
8. com.redhat.coolstore.service.CatalogService (src/main/java/com/redhat/coolstore/service/CatalogService.java)
9. com.redhat.coolstore.service.PromoService (src/main/java/com/redhat/coolstore/service/PromoService.java)
10. com.redhat.coolstore.service.ShippingService (src/main/java/com/redhat/coolstore/service/ShippingService.java)
11. com.redhat.coolstore.service.ShoppingCartServiceImpl (src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java)
12. com.redhat.coolstore.rest.CartEndpoint (src/main/java/com/redhat/coolstore/rest/CartEndpoint.java)
