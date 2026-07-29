package com.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * In-memory shopping cart service.
 *
 * <p>Trimmed from the Coolstore microservice cart-service for the Spring Boot
 * to Quarkus migration demo: remote JBoss Data Grid (Infinispan Hot Rod) and the
 * Drools/KIE Decision Server pricing path were removed. Cart state lives in a
 * local {@link ConcurrentHashMap}; pricing uses {@link PromoService} and
 * {@link ShippingService}.</p>
 */
@ApplicationScoped
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    private final ShippingService shippingService;
    private final CatalogService catalogService;
    private final PromoService promoService;

    private final ConcurrentHashMap<String, ShoppingCart> carts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Product> productMap = new ConcurrentHashMap<>();

    public ShoppingCartServiceImpl(ShippingService shippingService, @RestClient CatalogService catalogService, PromoService promoService) {
        this.shippingService = shippingService;
        this.catalogService = catalogService;
        this.promoService = promoService;
        LOG.info("Using local in-memory cache for cart data");
    }

    @Override
    public ShoppingCart getShoppingCart(String cartId) {
        ShoppingCart cart = carts.compute(cartId, (key, existing) -> {
            if (existing == null) {
                return new ShoppingCart(cartId);
            }
            return existing;
        });

        priceShoppingCart(cart);
        return cart;
    }

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

            // if product exist, create new product to reset price
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
            // Fetch and cache products
            List<Product> products = catalogService.products();
            productMap.putAll(products.stream().collect(Collectors.toMap(Product::getItemId, Function.identity())));
        }

        return productMap.get(itemId);
    }

    @Override
    public ShoppingCart deleteItem(String cartId, String itemId, int quantity) {
        return carts.compute(cartId, (key, cart) -> {
            if (cart == null) {
                cart = new ShoppingCart(cartId);
            }

            priceShoppingCart(cart);

            List<ShoppingCartItem> toRemoveList = new ArrayList<>();

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

            return cart;
        });
    }

    @Override
    public ShoppingCart checkout(String cartId) {
        return carts.compute(cartId, (key, cart) -> {
            if (cart == null) {
                cart = new ShoppingCart(cartId);
            }

            priceShoppingCart(cart);
            cart.resetShoppingCartItemList();
            priceShoppingCart(cart);
            return cart;
        });
    }

    @Override
    public ShoppingCart addItem(String cartId, String itemId, int quantity) {
        return carts.compute(cartId, (key, cart) -> {
            if (cart == null) {
                cart = new ShoppingCart(cartId);
            }

            priceShoppingCart(cart);

            Product product = getProduct(itemId);

            if (product == null) {
                LOG.warn("Invalid product {} request to get added to the shopping cart. No product added", itemId);
                return cart;
            }

            ShoppingCartItem sci = new ShoppingCartItem();
            sci.setProduct(product);
            sci.setQuantity(quantity);
            sci.setPrice(product.getPrice());
            cart.addShoppingCartItem(sci);

            try {
                priceShoppingCart(cart);
                cart.setShoppingCartItemList(dedupeCartItems(cart));
            } catch (Exception ex) {
                cart.removeShoppingCartItem(sci);
                throw ex;
            }

            return cart;
        });
    }

    @Override
    public ShoppingCart set(String cartId, String tmpId) {
        return carts.compute(cartId, (key, cart) -> {
            if (cart == null) {
                cart = new ShoppingCart(cartId);
            }

            ShoppingCart tmpCart = getShoppingCart(tmpId);

            if (tmpCart != null) {
                cart.resetShoppingCartItemList();
                cart.setShoppingCartItemList(tmpCart.getShoppingCartItemList());
            }

            priceShoppingCart(cart);
            cart.setShoppingCartItemList(dedupeCartItems(cart));

            return cart;
        });
    }

    List<ShoppingCartItem> dedupeCartItems(ShoppingCart sc) {
        List<ShoppingCartItem> result = new ArrayList<>();
        Map<String, Integer> quantityMap = new HashMap<>();
        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            if (quantityMap.containsKey(sci.getProduct().getItemId())) {
                quantityMap.put(sci.getProduct().getItemId(), quantityMap.get(sci.getProduct().getItemId()) + sci.getQuantity());
            } else {
                quantityMap.put(sci.getProduct().getItemId(), sci.getQuantity());
            }
        }

        for (Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
            Product p = getProduct(entry.getKey());
            ShoppingCartItem newItem = new ShoppingCartItem();
            newItem.setQuantity(entry.getValue());
            newItem.setPrice(p.getPrice());
            newItem.setProduct(p);
            result.add(newItem);
        }

        return result;
    }
}
