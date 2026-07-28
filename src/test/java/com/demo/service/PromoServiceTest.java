package com.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.demo.model.Promotion;
import com.demo.model.ShoppingCart;

/**
 * Test for PromoService coverage gaps.
 */
class PromoServiceTest {

    private PromoService promoService;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        promoService = new PromoService();
        cart = new ShoppingCart("test-cart");
    }

    @Test
    void testGetPromotionsWithNull() {
        // Test line 57: promotionSet == null
        promoService.setPromotions(null);
        Set<Promotion> promotions = promoService.getPromotions();
        assertThat(promotions).as("Promotions should not be null when set to null").isNotNull();
    }

    @Test
    void testSetPromotionsWithNull() {
        // Test line 67: promotionSet != null check
        promoService.setPromotions(null);
        assertThat(promoService.getPromotions()).as("Promotions should be empty when set to null").isEmpty();
    }

    @Test
    void testApplyCartItemPromotionsWithNullCart() {
        // Test line 32: cart != null
        assertThatCode(() -> promoService.applyCartItemPromotions(null))
            .doesNotThrowAnyException();
    }

    @Test
    void testApplyCartItemPromotionsWithEmptyCart() {
        // Test line 32: cart.getShoppingCartItemList().isEmpty()
        promoService.applyCartItemPromotions(cart);
        assertThat(cart.getShoppingCartItemList()).as("Cart should remain empty").isEmpty();
    }

    @Test
    void testApplyShippingPromotionsWithNullCart() {
        // Test line 50: cart != null
        assertThatCode(() -> promoService.applyShippingPromotions(null))
            .doesNotThrowAnyException();
    }

    @Test
    void testApplyShippingPromotionsBelowThreshold() {
        // Test line 50: cart.getCartItemTotal() < 75
        cart.setCartItemTotal(74.99);
        cart.setShippingTotal(4.99); // Set initial shipping before promo
        promoService.applyShippingPromotions(cart);
        assertThat(cart.getShippingTotal()).as("Shipping should remain unchanged below threshold").isEqualTo(4.99);
    }
}