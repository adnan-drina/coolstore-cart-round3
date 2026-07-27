package com.demo.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShippingTierTest {

    @Test
    void returns_2_99_for_zero_cart_total() {
        assertThat(ShippingTierCalculator.calculateShipping(0))
            .isEqualTo(2.99);
    }

    @Test
    void returns_2_99_for_cart_total_below_25() {
        assertThat(ShippingTierCalculator.calculateShipping(10))
            .isEqualTo(2.99);
        assertThat(ShippingTierCalculator.calculateShipping(24.99))
            .isEqualTo(2.99);
    }

    @Test
    void returns_4_99_at_25_boundary() {
        assertThat(ShippingTierCalculator.calculateShipping(25))
            .isEqualTo(4.99);
    }

    @Test
    void returns_4_99_for_cart_total_between_25_and_50() {
        assertThat(ShippingTierCalculator.calculateShipping(30))
            .isEqualTo(4.99);
        assertThat(ShippingTierCalculator.calculateShipping(49.99))
            .isEqualTo(4.99);
    }

    @Test
    void returns_6_99_at_50_boundary() {
        assertThat(ShippingTierCalculator.calculateShipping(50))
            .isEqualTo(6.99);
    }

    @Test
    void returns_6_99_for_cart_total_between_50_and_75() {
        assertThat(ShippingTierCalculator.calculateShipping(60))
            .isEqualTo(6.99);
        assertThat(ShippingTierCalculator.calculateShipping(74.99))
            .isEqualTo(6.99);
    }

    @Test
    void returns_8_99_at_75_boundary() {
        assertThat(ShippingTierCalculator.calculateShipping(75))
            .isEqualTo(8.99);
    }

    @Test
    void returns_8_99_for_cart_total_between_75_and_100() {
        assertThat(ShippingTierCalculator.calculateShipping(80))
            .isEqualTo(8.99);
        assertThat(ShippingTierCalculator.calculateShipping(99.99))
            .isEqualTo(8.99);
    }

    @Test
    void returns_10_99_at_100_boundary() {
        assertThat(ShippingTierCalculator.calculateShipping(100))
            .isEqualTo(10.99);
    }

    @Test
    void returns_10_99_for_cart_total_100_and_above() {
        assertThat(ShippingTierCalculator.calculateShipping(200))
            .isEqualTo(10.99);
        assertThat(ShippingTierCalculator.calculateShipping(9999))
            .isEqualTo(10.99);
    }

    @Test
    void returns_zero_for_cart_total_at_or_above_10000() {
        assertThat(ShippingTierCalculator.calculateShipping(10000))
            .isEqualTo(0.0);
        assertThat(ShippingTierCalculator.calculateShipping(15000))
            .isEqualTo(0.0);
    }

    @Test
    void returns_zero_for_negative_cart_total() {
        assertThat(ShippingTierCalculator.calculateShipping(-1))
            .isEqualTo(0.0);
        assertThat(ShippingTierCalculator.calculateShipping(-100))
            .isEqualTo(0.0);
    }

    @Test
    void applies_shipping_to_shopping_cart() {
        ShoppingCart cart = new ShoppingCart("test");
        cart.setCartItemTotal(50);

        ShippingTierCalculator.applyShipping(cart);

        assertThat(cart.getShippingTotal())
            .isEqualTo(6.99);
    }

    @Test
    void apply_shipping_handles_null_cart() {
        ShippingTierCalculator.applyShipping(null);
    }

    @Test
    void shipping_before_promotion_discounts_preserves_base_rate() {
        ShoppingCart cart = new ShoppingCart("promo");
        cart.setCartItemTotal(80);

        ShippingTierCalculator.applyShipping(cart);

        assertThat(cart.getShippingTotal())
            .isEqualTo(8.99);
    }

    @Test
    void all_tier_transitions_are_deterministic() {
        double[] boundaries = {0, 25, 50, 75, 100};
        double[] expected = {2.99, 4.99, 6.99, 8.99, 10.99};

        for (int i = 0; i < boundaries.length; i++) {
            assertThat(ShippingTierCalculator.calculateShipping(boundaries[i]))
                .as("boundary %.0f", boundaries[i])
                .isEqualTo(expected[i]);
        }
    }

    @Test
    void just_below_each_boundary_stays_in_lower_tier() {
        assertThat(ShippingTierCalculator.calculateShipping(24.99))
            .isEqualTo(2.99);
        assertThat(ShippingTierCalculator.calculateShipping(49.99))
            .isEqualTo(4.99);
        assertThat(ShippingTierCalculator.calculateShipping(74.99))
            .isEqualTo(6.99);
        assertThat(ShippingTierCalculator.calculateShipping(99.99))
            .isEqualTo(8.99);
    }
}
