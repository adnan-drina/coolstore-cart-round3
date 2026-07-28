package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    private final ShippingService shippingService;
    private final PromoService promoService;
    private final CatalogService catalogService;

    private Map<String, ShoppingCart> carts;
    private Map<String, Product> productMap = new HashMap<>();

    @Inject
    public ShoppingCartServiceImpl(
            ShippingService shippingService,
            PromoService promoService,
            @RestClient CatalogService catalogService) {
        this.shippingService = shippingService;
        this.promoService = promoService;
        this.catalogService = catalogService;
    }

    @PostConstruct
    public void init() {
        LOG.info("Using local in-memory cache for cart data");
        carts = new HashMap<>();
    }

    @Override
    public ShoppingCart getShoppingCart(String cartId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        ShoppingCart cart = carts.get(cartId);
        if (Objects.isNull(cart)) {
            cart = new ShoppingCart(cartId);
            carts.put(cartId, cart);
        } else {
            priceShoppingCart(cart);
            carts.put(cartId, cart);
        }
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

            if (p != null) {
                sci.setProduct(new Product(p.getItemId(), p.getName(), p.getDesc(), p.getPrice()));
                // Only reset price if no promotional discount is already applied
                if (sci.getPromoSavings() == 0) {
                    sci.setPrice(p.getPrice());
                }
            }

            // Only reset promo savings if no discount is already applied
            if (sci.getPromoSavings() == 0) {
                sci.setPromoSavings(0);
            }
        }
    }

    @Override
    public Product getProduct(String itemId) {
        if (!productMap.containsKey(itemId)) {
            try {
                List<Product> products = catalogService.products();
                productMap = products.stream().collect(Collectors.toMap(Product::getItemId, Function.identity()));
            } catch (Exception e) {
                // Catalog service not available - return null to indicate product lookup failed
                LOG.warn("Catalog service not available for product lookup: {}", e.getMessage());
                return null;
            }
        }

        return productMap.get(itemId);
    }

    @Override
    public ShoppingCart deleteItem(String cartId, String itemId, int quantity) {
        List<ShoppingCartItem> toRemoveList = new ArrayList<>();

        ShoppingCart cart = getShoppingCart(cartId);

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
        carts.put(cartId, cart);

        return cart;
    }

    @Override
    public ShoppingCart checkout(String cartId) {
        ShoppingCart cart = getShoppingCart(cartId);
        cart.resetShoppingCartItemList();
        priceShoppingCart(cart);
        carts.put(cartId, cart);
        return cart;
    }

    @Override
    public ShoppingCart addItem(String cartId, String itemId, int quantity) {
        ShoppingCart cart = getShoppingCart(cartId);
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

        carts.put(cartId, cart);
        return cart;
    }

    @Override
    public ShoppingCart set(String cartId, String tmpId) {
        ShoppingCart cart = getShoppingCart(cartId);
        ShoppingCart tmpCart = getShoppingCart(tmpId);

        if (tmpCart != null) {
            cart.resetShoppingCartItemList();
            cart.setShoppingCartItemList(tmpCart.getShoppingCartItemList());
        }

        priceShoppingCart(cart);
        cart.setShoppingCartItemList(dedupeCartItems(cart));

        carts.put(cartId, cart);
        return cart;
    }

    List<ShoppingCartItem> dedupeCartItems(ShoppingCart sc) {
        List<ShoppingCartItem> result = new ArrayList<>();
        Map<String, Integer> quantityMap = new HashMap<>();
        Map<String, Product> existingProducts = new HashMap<>();
        Map<String, Double> itemPrices = new HashMap<>();
        Map<String, Double> itemPromoSavings = new HashMap<>();
        
        // First pass: collect quantities, preserve existing products, and keep promotional pricing
        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            String productId = sci.getProduct().getItemId();
            if (quantityMap.containsKey(productId)) {
                quantityMap.put(productId, quantityMap.get(productId) + sci.getQuantity());
            } else {
                quantityMap.put(productId, sci.getQuantity());
                existingProducts.put(productId, sci.getProduct());
                itemPrices.put(productId, sci.getPrice());
                itemPromoSavings.put(productId, sci.getPromoSavings());
            }
        }

        // Second pass: create new items with preserved products and promotional pricing
        for (Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
            String productId = entry.getKey();
            Product existingProduct = existingProducts.get(productId);
            ShoppingCartItem newItem = new ShoppingCartItem();
            newItem.setQuantity(entry.getValue());
            newItem.setPrice(itemPrices.get(productId));
            newItem.setPromoSavings(itemPromoSavings.get(productId));
            newItem.setProduct(existingProduct);
            result.add(newItem);
        }

        return result;
    }
}