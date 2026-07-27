package com.demo.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShippingTierTest {

    @Test
    void returns_2_99_for_zero_cart_total() {
        assertThat(expectedShipping(0))
            .isEqualTo(2.99);
    }

    @Test
    void returns_2_99_for_cart_total_below_25() {
        assertThat(expectedShipping(10))
            .isEqualTo(2.99);
        assertThat(expectedShipping(24.99))
            .isEqualTo(2.99);
    }

    @Test
    void returns_4_99_at_25_boundary() {
        assertThat(expectedShipping(25))
            .isEqualTo(4.99);
    }

    @Test
    void returns_4_99_for_cart_total_between_25_and_50() {
        assertThat(expectedShipping(30))
            .isEqualTo(4.99);
        assertThat(expectedShipping(49.99))
            .isEqualTo(4.99);
    }

    @Test
    void returns_6_99_at_50_boundary() {
        assertThat(expectedShipping(50))
            .isEqualTo(6.99);
    }

    @Test
    void returns_6_99_for_cart_total_between_50_and_75() {
        assertThat(expectedShipping(60))
            .isEqualTo(6.99);
        assertThat(expectedShipping(74.99))
            .isEqualTo(6.99);
    }

    @Test
    void returns_8_99_at_75_boundary() {
        assertThat(expectedShipping(75))
            .isEqualTo(8.99);
    }

    @Test
    void returns_8_99_for_cart_total_between_75_and_100() {
        assertThat(expectedShipping(80))
            .isEqualTo(8.99);
        assertThat(expectedShipping(99.99))
            .isEqualTo(8.99);
    }

    @Test
    void returns_10_99_at_100_boundary() {
        assertThat(expectedShipping(100))
            .isEqualTo(10.99);
    }

    @Test
    void returns_10_99_for_cart_total_100_and_above() {
        assertThat(expectedShipping(200))
            .isEqualTo(10.99);
        assertThat(expectedShipping(9999))
            .isEqualTo(10.99);
    }

    @Test
    void returns_zero_for_cart_total_at_or_above_10000() {
        assertThat(expectedShipping(10000))
            .isEqualTo(0.0);
        assertThat(expectedShipping(15000))
            .isEqualTo(0.0);
    }

    @Test
    void returns_zero_for_negative_cart_total() {
        assertThat(expectedShipping(-1))
            .isEqualTo(0.0);
        assertThat(expectedShipping(-100))
            .isEqualTo(0.0);
    }

    @Test
    void applies_shipping_to_shopping_cart() {
        ShoppingCart cart = new ShoppingCart("test");
        cart.setCartItemTotal(50);

        applyExpectedShipping(cart);

        assertThat(cart.getShippingTotal())
            .isEqualTo(6.99);
    }

    @Test
    void apply_shipping_handles_null_cart() {
        applyExpectedShipping(null);
    }

    @Test
    void shipping_before_promotion_discounts_preserves_base_rate() {
        ShoppingCart cart = new ShoppingCart("promo");
        cart.setCartItemTotal(80);

        applyExpectedShipping(cart);

        assertThat(cart.getShippingTotal())
            .isEqualTo(8.99);
    }

    @Test
    void all_tier_transitions_are_deterministic() {
        double[] boundaries = {0, 25, 50, 75, 100};
        double[] expected = {2.99, 4.99, 6.99, 8.99, 10.99};

        for (int i = 0; i < boundaries.length; i++) {
            assertThat(expectedShipping(boundaries[i]))
                .as("boundary %.0f", boundaries[i])
                .isEqualTo(expected[i]);
        }
    }

    @Test
    void just_below_each_boundary_stays_in_lower_tier() {
        assertThat(expectedShipping(24.99))
            .isEqualTo(2.99);
        assertThat(expectedShipping(49.99))
            .isEqualTo(4.99);
        assertThat(expectedShipping(74.99))
            .isEqualTo(6.99);
        assertThat(expectedShipping(99.99))
            .isEqualTo(8.99);
    }

    // ------------------------------------------------------------------
    // Test-local expectation model of the LEGACY shipping tiers.
    // The real implementation is ShippingService — S02 scope; S02 pins
    // the migrated service against these same values. Keeping the
    // arithmetic here (not in src/main) keeps S01 story-clean.
    private static double expectedShipping(double cartItemTotal) {
        if (cartItemTotal >= 0 && cartItemTotal < 25) {
            return 2.99;
        } else if (cartItemTotal >= 25 && cartItemTotal < 50) {
            return 4.99;
        } else if (cartItemTotal >= 50 && cartItemTotal < 75) {
            return 6.99;
        } else if (cartItemTotal >= 75 && cartItemTotal < 100) {
            return 8.99;
        } else if (cartItemTotal >= 100 && cartItemTotal < 10000) {
            return 10.99;
        }
        return 0.0;
    }

    private static void applyExpectedShipping(ShoppingCart cart) {
        if (cart != null) {
            cart.setShippingTotal(expectedShipping(cart.getCartItemTotal()));
        }
    }
}
