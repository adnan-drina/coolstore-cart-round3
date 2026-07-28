package com.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.demo.model.ShoppingCart;

/**
 * Test for ShippingService coverage gaps.
 */
class ShippingServiceTest {

    private ShippingService shippingService;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        shippingService = new ShippingService();
        cart = new ShoppingCart("test-cart");
    }

    @Test
    void testCalculateShippingWithNullCart() {
        // Test null cart handling (line 17)
        shippingService.calculateShipping(null);
        // Should not throw exception - test passes if no exception is thrown
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 10.0, 24.99})
    void testCalculateShippingTier0(double cartTotal) {
        // Test tier 0: 0 - 24.99 (line 18)
        cart.setCartItemTotal(cartTotal);
        shippingService.calculateShipping(cart);
        assertThat(cart.getShippingTotal()).as("Shipping for tier 0 should be $2.99").isEqualTo(2.99);
    }

    @ParameterizedTest
    @ValueSource(doubles = {25.0, 30.0, 49.99})
    void testCalculateShippingTier1(double cartTotal) {
        // Test tier 1: 25 - 49.99 (line 20)
        cart.setCartItemTotal(cartTotal);
        shippingService.calculateShipping(cart);
        assertThat(cart.getShippingTotal()).as("Shipping for tier 1 should be $4.99").isEqualTo(4.99);
    }

    @ParameterizedTest
    @ValueSource(doubles = {50.0, 60.0, 74.99})
    void testCalculateShippingTier2(double cartTotal) {
        // Test tier 2: 50 - 74.99 (line 22)
        cart.setCartItemTotal(cartTotal);
        shippingService.calculateShipping(cart);
        assertThat(cart.getShippingTotal()).as("Shipping for tier 2 should be $6.99").isEqualTo(6.99);
    }

    @ParameterizedTest
    @ValueSource(doubles = {75.0, 80.0, 99.99})
    void testCalculateShippingTier3(double cartTotal) {
        // Test tier 3: 75 - 99.99 (line 24)
        cart.setCartItemTotal(cartTotal);
        shippingService.calculateShipping(cart);
        assertThat(cart.getShippingTotal()).as("Shipping for tier 3 should be $8.99").isEqualTo(8.99);
    }

    @ParameterizedTest
    @ValueSource(doubles = {100.0, 500.0, 9999.99})
    void testCalculateShippingTier4(double cartTotal) {
        // Test tier 4: 100 - 9999.99 (line 26)
        cart.setCartItemTotal(cartTotal);
        shippingService.calculateShipping(cart);
        assertThat(cart.getShippingTotal()).as("Shipping for tier 4 should be $10.99").isEqualTo(10.99);
    }
}
