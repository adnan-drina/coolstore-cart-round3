package com.demo.model;

/**
 * Pure utility for shipping tier calculation logic.
 * Mirrors the legacy ShippingService.calculateShipping() behavior.
 */
public final class ShippingTierCalculator {

    private ShippingTierCalculator() {
    }

    public static double calculateShipping(double cartItemTotal) {
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

    public static void applyShipping(ShoppingCart cart) {
        if (cart != null) {
            cart.setShippingTotal(calculateShipping(cart.getCartItemTotal()));
        }
    }
}
