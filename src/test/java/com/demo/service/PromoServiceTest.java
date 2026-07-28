package com.demo.service;

import com.demo.model.Promotion;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PromoServiceTest {

    @Inject
    PromoService promoService;

    @Test
    void should_apply_cart_item_promotions() {
        ShoppingCart cart = new ShoppingCart("test-cart");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(new com.demo.model.Product("329299", "Test Product", "Description", 100.0));
        item.setQuantity(1);
        item.setPrice(100.0);
        cart.addShoppingCartItem(item);

        promoService.applyCartItemPromotions(cart);

        assertEquals(-25.0, item.getPromoSavings(), 0.001);
        assertEquals(75.0, item.getPrice(), 0.001);
    }

    @Test
    void should_apply_cart_item_promotions_with_no_promotion() {
        ShoppingCart cart = new ShoppingCart("test-cart");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(new com.demo.model.Product("999999", "No Promo Product", "Description", 100.0));
        item.setQuantity(1);
        item.setPrice(100.0);
        cart.addShoppingCartItem(item);

        promoService.applyCartItemPromotions(cart);

        assertEquals(0.0, item.getPromoSavings(), 0.001);
        assertEquals(100.0, item.getPrice(), 0.001);
    }

    @Test
    void should_not_apply_cart_item_promotions_when_cart_is_null() {
        assertDoesNotThrow(() -> {
            promoService.applyCartItemPromotions(null);
        });
    }

    @Test
    void should_not_apply_cart_item_promotions_when_cart_has_no_items() {
        ShoppingCart cart = new ShoppingCart("empty-cart");

        promoService.applyCartItemPromotions(cart);

        assertEquals(0.0, cart.getCartItemTotal(), 0.001);
    }

    @Test
    void should_apply_shipping_promotions() {
        ShoppingCart cart = new ShoppingCart("test-cart");
        cart.setCartItemTotal(100.0);
        cart.setShippingTotal(10.99);

        promoService.applyShippingPromotions(cart);

        assertEquals(-10.99, cart.getShippingPromoSavings(), 0.001);
        assertEquals(0.0, cart.getShippingTotal(), 0.001);
    }

    @Test
    void should_not_apply_shipping_promotions_below_threshold() {
        ShoppingCart cart = new ShoppingCart("test-cart");
        cart.setCartItemTotal(50.0);
        cart.setShippingTotal(10.99);

        promoService.applyShippingPromotions(cart);

        assertEquals(0.0, cart.getShippingPromoSavings(), 0.001);
        assertEquals(10.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void should_not_apply_shipping_promotions_when_cart_is_null() {
        assertDoesNotThrow(() -> {
            promoService.applyShippingPromotions(null);
        });
    }

    @Test
    void should_get_default_promotions() {
        Set<Promotion> promotions = promoService.getPromotions();

        assertNotNull(promotions);
        assertEquals(1, promotions.size());
        
        Promotion promo = promotions.iterator().next();
        assertEquals("329299", promo.getItemId());
        assertEquals(0.25, promo.getPercentOff(), 0.001);
    }

    @Test
    void should_set_custom_promotions() {
        Set<Promotion> customPromos = new HashSet<>();
        customPromos.add(new Promotion("item1", 0.30));
        customPromos.add(new Promotion("item2", 0.50));

        promoService.setPromotions(customPromos);
        Set<Promotion> result = promoService.getPromotions();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> "item1".equals(p.getItemId()) && p.getPercentOff() == 0.30));
        assertTrue(result.stream().anyMatch(p -> "item2".equals(p.getItemId()) && p.getPercentOff() == 0.50));
    }

    @Test
    void should_set_null_promotions_to_empty_set() {
        promoService.setPromotions(null);
        Set<Promotion> result = promoService.getPromotions();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void should_generate_to_string() {
        String result = promoService.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("PromoService"));
        assertTrue(result.contains("promotionSet"));
    }
}