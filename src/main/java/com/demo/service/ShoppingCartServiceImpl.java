package com.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

/**
 * In-memory shopping cart service.
 *
 * <p>Migrated from Spring Boot {@code @Service} to Quarkus CDI
 * {@code @ApplicationScoped} with constructor injection. Cart state lives in a
 * local {@link ConcurrentHashMap}; pricing uses {@link PromoService} and
 * {@link ShippingService}.</p>
 */
@ApplicationScoped
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger LOG = Logger.getLogger(ShoppingCartServiceImpl.class.getName());
    private static final String INVALID_PRODUCT_MSG = "Invalid product %s request to get added to the shopping cart. No product added";
    private static final long CATALOG_REFRESH_MS = 60_000;

    private final ShippingService shippingService;
    private final CatalogService catalogService;
    private final PromoService promoService;

    private ConcurrentHashMap<String, ShoppingCart> carts;
    private final ConcurrentHashMap<String, Product> productMap = new ConcurrentHashMap<>();
    private volatile long lastCatalogRefresh = 0;

    @Inject
    public ShoppingCartServiceImpl(
            ShippingService shippingService,
            @RestClient CatalogService catalogService,
            PromoService promoService) {
        this.shippingService = shippingService;
        this.catalogService = catalogService;
        this.promoService = promoService;
    }

    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<ShoppingCart> getShoppingCart(String cartId) {
        ShoppingCart existing = carts.get(cartId);
        if (existing == null) {
            return Optional.empty();
        }
        priceShoppingCart(existing);
        return Optional.of(existing);
    }

    @Override
    public void priceShoppingCart(ShoppingCart sc) {
        if (sc != null) {
            initShoppingCartForPricing(sc);

            if (sc.getShoppingCartItemList() != null && !sc.getShoppingCartItemList().isEmpty()) {
                promoService.applyCartItemPromotions(sc);

                for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
                    sc.setCartItemPromoSavings(sc.getCartItemPromoSavings() + sci.getPromoSavings() * sci.getQuantity());
                    sc.setCartItemTotal(sc.getCartItemTotal() + sci.getPrice() * sci.getQuantity());
                }

                shippingService.calculateShipping(sc);
            }

            promoService.applyShippingPromotions(sc);

            sc.setCartTotal(sc.getCartItemTotal() + sc.getShippingTotal());
        }
    }

    void initShoppingCartForPricing(ShoppingCart sc) {
        sc.setCartItemTotal(0);
        sc.setCartItemPromoSavings(0);
        sc.setShippingTotal(0);
        sc.setShippingPromoSavings(0);
        sc.setCartTotal(0);

        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            Product p = getProduct(sci.getProduct().getItemId());

            if (p != null) {
                sci.setProduct(new Product(p.getItemId(), p.getName(), p.getDesc(), p.getPrice()));
                sci.setPrice(p.getPrice());
            }

            sci.setPromoSavings(0);
        }
    }

    @Override
    public Product getProduct(String itemId) {
        if (!productMap.containsKey(itemId)) {
            long now = System.currentTimeMillis();
            if (now - lastCatalogRefresh < CATALOG_REFRESH_MS) {
                return null;
            }
            // Legacy contract: catalog failures propagate — never a
            // fabricated fallback (migration.yaml forbidden:).
            List<Product> products = catalogService.products();
            products.stream().collect(Collectors.toMap(Product::getItemId, Function.identity()))
                .forEach(productMap::putIfAbsent);
            lastCatalogRefresh = now;
        }

        return productMap.get(itemId);
    }

    @Override
    public ShoppingCart deleteItem(String cartId, String itemId, int quantity) {
        List<ShoppingCartItem> toRemoveList = new ArrayList<>();

        ShoppingCart cart = carts.compute(cartId, (id, existing) -> {
            if (existing == null) {
                return new ShoppingCart(cartId);
            }
            return existing;
        });

        cart.getShoppingCartItemList().stream()
                .filter(sci -> sci.getProduct().getItemId().equals(itemId))
                .forEach(sci -> {
                    if (quantity >= sci.getQuantity()) {
                        toRemoveList.add(sci);
                    } else {
                        sci.setQuantity(sci.getQuantity() - quantity);
                    }
                });

        toRemoveList.forEach(cart::removeShoppingCartItem);
        priceShoppingCart(cart);
        carts.compute(cartId, (id, existing) -> cart);
        return cart;
    }

    @Override
    public ShoppingCart checkout(String cartId) {
        ShoppingCart cart = carts.compute(cartId, (id, existing) -> {
            if (existing != null) {
                existing.resetShoppingCartItemList();
                return existing;
            }
            return null;
        });
        if (cart != null) {
            priceShoppingCart(cart);
        }
        return cart;
    }

    @Override
    public ShoppingCart addItem(String cartId, String itemId, int quantity) {
        ShoppingCart cart = carts.compute(cartId, (id, existing) -> {
            if (existing == null) {
                return new ShoppingCart(cartId);
            }
            return existing;
        });
        Product product = getProduct(itemId);

        if (product == null) {
            LOG.warning(() -> String.format(INVALID_PRODUCT_MSG, itemId));
            return cart;
        }

        ShoppingCartItem sci = new ShoppingCartItem();
        sci.setProduct(product);
        sci.setQuantity(quantity);
        sci.setPrice(product.getPrice());
        cart.addShoppingCartItem(sci);

        try {
            carts.compute(cartId, (id, existing) -> {
                cart.setShoppingCartItemList(dedupeCartItems(cart));
                priceShoppingCart(cart);
                return cart;
            });
        } catch (Exception ex) {
            cart.removeShoppingCartItem(sci);
            throw ex;
        }

        return cart;
    }

    @Override
    public ShoppingCart set(String cartId, String tmpId) {
        ShoppingCart tmpCart = carts.get(tmpId);
        if (tmpCart != null) {
            // Ensure target cart exists
            return carts.compute(cartId, (id, existing) -> {
                if (existing == null) {
                    existing = new ShoppingCart(cartId);
                }
                existing.resetShoppingCartItemList();
                existing.setShoppingCartItemList(new ArrayList<>(tmpCart.getShoppingCartItemList()));
                existing.setShoppingCartItemList(dedupeCartItems(existing));
                priceShoppingCart(existing);
                return existing;
            });
        } else {
            // Return existing cart or create new one if temp doesn't exist
            return carts.compute(cartId, (id, existing) -> {
                if (existing == null) {
                    existing = new ShoppingCart(cartId);
                }
                existing.setShoppingCartItemList(dedupeCartItems(existing));
                priceShoppingCart(existing);
                return existing;
            });
        }
    }

    List<ShoppingCartItem> dedupeCartItems(ShoppingCart sc) {
        Map<String, Integer> quantityMap = new HashMap<>();
        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            quantityMap.merge(sci.getProduct().getItemId(), sci.getQuantity(), Integer::sum);
        }

        return quantityMap.entrySet().stream()
            .map(entry -> {
                Product p = getProduct(entry.getKey());
                if (p != null) {
                    ShoppingCartItem newItem = new ShoppingCartItem();
                    newItem.setQuantity(entry.getValue());
                    newItem.setPrice(p.getPrice());
                    newItem.setProduct(p);
                    return newItem;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
