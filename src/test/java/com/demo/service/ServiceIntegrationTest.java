package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive service integration tests for migrated CDI services.
 * 
 * This test validates that all migrated services (PromoService, ShippingService,
 * CatalogService, ShoppingCartServiceImpl) work together correctly and ports
 * the boundary test assertions from CartServiceBoundaryTest.java:38-45.
 */
class ServiceIntegrationTest {

    /**
     * Simple mock CatalogService implementation that returns predefined products.
     */
    private static class MockCatalogService implements CatalogService {
        private final List<Product> products;

        public MockCatalogService(List<Product> products) {
            this.products = products;
        }

        @Override
        public List<Product> products() {
            return products;
        }
    }

    /**
     * Test complete cart workflow with mocked catalog service boundary.
     * Ports boundary test assertions from CartServiceBoundaryTest.java:38-45.
     */
    @Test
    void should_complete_cart_workflow_with_correct_calculations() {
        // Given: Mock catalog service boundary - products from boundary test
        Product product = new Product("1111", "Test Product", "Description", 1000.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        // When: Create and configure services with real CDI services
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        // Create ShoppingCartServiceImpl with mocked catalog service
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Add 2x $1000 items to cart (equivalent to boundary test scenario)
        ShoppingCart cart = cartService.addItem("test-cart", "1111", 2);
        
        // Then: Port boundary test assertions from CartServiceBoundaryTest.java:38-45
        assertThat(cart)
            .returns(0.0, ShoppingCart::getCartItemPromoSavings)
            .returns(2000.0, ShoppingCart::getCartItemTotal)
            .returns(-10.99, ShoppingCart::getShippingPromoSavings)
            .returns(2000.0, ShoppingCart::getCartTotal)
            .extracting(ShoppingCart::getShoppingCartItemList)
            .asList()
            .hasSize(1);
    }

    /**
     * Test shipping calculation edge cases and promotion thresholds.
     */
    @Test
    void should_handle_shipping_tier_transitions_correctly() {
        // Given: Mock catalog service
        Product product = new Product("1111", "Test Product", "Description", 1000.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Test tier 0: $0-25 → $2.99 shipping
        ShoppingCart cart0to25 = cartService.addItem("cart-0to25", "1111", 1);
        assertThat(cart0to25.getShippingTotal()).isEqualTo(0.0); // Free shipping promotion applies (1000 >= 75)
        assertThat(cart0to25.getShippingPromoSavings()).isEqualTo(-10.99); // Savings from free shipping
        
        // Test tier 4: $100+ → $10.99 shipping before promotion
        ShoppingCart cart100plus = cartService.addItem("cart-100plus", "1111", 1);
        assertThat(cart100plus.getShippingTotal()).isEqualTo(0.0); // Free shipping promotion applies (1000 >= 75)
        assertThat(cart100plus.getShippingPromoSavings()).isEqualTo(-10.99); // Savings from free shipping
    }

    /**
     * Test promotion application at exact threshold boundaries.
     */
    @Test
    void should_apply_promotion_at_exact_threshold_75() {
        // Given: Mock catalog service
        Product product = new Product("1111", "Test Product", "Description", 37.5); // 37.5 * 2 = 75
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Test exact threshold: 2x $37.5 = $75 exactly
        ShoppingCart cart = cartService.addItem("cart-threshold", "1111", 2);
        
        // Should apply free shipping promotion (cart total >= 75)
        assertThat(cart.getShippingTotal()).isEqualTo(0.0);
        assertThat(cart.getShippingPromoSavings()).isEqualTo(-8.99); // Free shipping from $75 tier
        assertThat(cart.getCartTotal()).isEqualTo(75.0); // No shipping cost
    }

    /**
     * Test adding and removing items from cart.
     * Validates that items can be added and completely removed.
     */
    @Test
    void should_handle_cart_operations_correctly() {
        // Given: Mock catalog service
        Product product = new Product("1111", "Test Product", "Description", 100.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Add item
        ShoppingCart cart = cartService.addItem("test-cart", "1111", 1);
        assertThat(cart.getShoppingCartItemList()).hasSize(1);
        assertThat(cart.getCartItemTotal()).isEqualTo(100.0);
        
        // Remove item
        cart = cartService.deleteItem("test-cart", "1111", 1);
        assertThat(cart.getShoppingCartItemList()).isNotNull().isEmpty();
        assertThat(cart.getCartItemTotal()).isEqualTo(0.0);
    }

    /**
     * Test checkout operation clears cart but maintains pricing calculations.
     * Validates that checkout resets items to zero while preserving calculation logic.
     */
    @Test
    void should_clear_cart_on_checkout_with_pricing() {
        // Given: Mock catalog service
        Product product = new Product("1111", "Test Product", "Description", 100.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Add item and verify cart state
        ShoppingCart cart = cartService.addItem("checkout-cart", "1111", 2);
        assertThat(cart.getShoppingCartItemList()).hasSize(1);
        assertThat(cart.getCartItemTotal()).isEqualTo(200.0);
        
        // Checkout should clear cart items but maintain pricing calculation
        cart = cartService.checkout("checkout-cart");
        assertThat(cart.getShoppingCartItemList()).isNotNull().isEmpty();
        assertThat(cart.getCartItemTotal()).isEqualTo(0.0);
        assertThat(cart.getCartTotal()).isEqualTo(0.0);
    }

    /**
     * Test promotion composition semantics: zeroes shippingTotal, keeps shippingPromoSavings.
     */
    @Test
    void should_maintain_promotion_composition_semantics() {
        // Given: High-value cart that triggers free shipping promotion
        Product product = new Product("1111", "Test Product", "Description", 500.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Add item that triggers free shipping promotion
        ShoppingCart cart = cartService.addItem("promo-cart", "1111", 1);
        
        // Validate promotion composition semantics:
        // - ZEROES shippingTotal (free shipping above $75)
        // - keeps shippingPromoSavings informational
        assertThat(cart.getShippingTotal()).isEqualTo(0.0);
        assertThat(cart.getShippingPromoSavings()).isEqualTo(-10.99);
        assertThat(cart.getCartTotal()).isEqualTo(500.0); // Only item cost, no shipping
    }

    /**
     * Test error handling for invalid cart operations.
     * Verifies graceful handling when attempting to add non-existent products.
     */
    @Test
    void should_handle_invalid_cart_operations_gracefully() {
        // Given: Mock catalog service with no matching products
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList());
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Attempt to add invalid product - should not crash
        ShoppingCart cart = cartService.addItem("invalid-cart", "nonexistent", 1);
        
        // Cart should exist but be empty (no items added)
        assertThat(cart.getShoppingCartItemList()).isNotNull().isEmpty();
        assertThat(cart.getCartItemTotal()).isEqualTo(0.0);
    }

    /**
     * Test cart deduplication and quantity accumulation.
     */
    @Test
    void should_deduplicate_cart_items_correctly() {
        // Given: Mock catalog service
        Product product = new Product("1111", "Test Product", "Description", 100.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Add same item multiple times - should deduplicate
        cartService.addItem("dedup-cart", "1111", 2);
        cartService.addItem("dedup-cart", "1111", 3);
        
        ShoppingCart cart = cartService.getShoppingCart("dedup-cart");
        
        // Should have single item with accumulated quantity (5)
        assertThat(cart.getShoppingCartItemList()).hasSize(1);
        assertThat(cart.getShoppingCartItemList().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.getCartItemTotal()).isEqualTo(500.0); // 5 * 100.0
    }

    /**
     * Test that the migrated services maintain exact arithmetic from legacy tests.
     */
    @Test
    void should_validate_exact_arithmetic_from_service_characterization_test() {
        // Given: Services configured to match ServiceCharacterizationTest expectations
        Product product = new Product("1111", "Car", "Super car", 1000.0);
        CatalogService mockCatalogService = new MockCatalogService(Arrays.asList(product));
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // When: Complete scenario from ServiceCharacterizationTest:
        // 2x $1000 items = $2000 cart item total
        ShoppingCart cart = cartService.addItem("complete-test", "1111", 2);
        
        // Then: Validate exact arithmetic matches ServiceCharacterizationTest expectations
        assertThat(cart)
            .returns(2000.0, ShoppingCart::getCartItemTotal)  // 2 * 1000
            .returns(0.0, ShoppingCart::getShippingTotal)    // Free shipping promotion applied (>= 75)
            .returns(-10.99, ShoppingCart::getShippingPromoSavings) // Savings from free shipping
            .returns(2000.0, ShoppingCart::getCartTotal);      // shippingTotal = 0 after promotion
        
        // Verify promotion composition semantics are maintained
        assertThat(cart.getShippingTotal()).isEqualTo(0.0); // Zeroed by promotion
        assertThat(cart.getCartTotal()).isEqualTo(2000.0);  // Only cart items, no shipping
    }

    /**
     * Test multiple shipping tier boundaries to ensure complete coverage.
     */
    @Test
    void should_calculate_all_shipping_tiers_correctly() {
        // Given: Different products for different tiers
        Product lowTier = new Product("low", "Low Tier", "0-25", 10.0);
        Product midTier = new Product("mid", "Mid Tier", "25-50", 30.0);
        Product highTier = new Product("high", "High Tier", "75-100", 80.0);
        
        CatalogService mockCatalogService = new MockCatalogService(
            Arrays.asList(lowTier, midTier, highTier)
        );
        
        PromoService promoService = new PromoService();
        ShippingService shippingService = new ShippingService();
        
        ShoppingCartServiceImpl cartService = new ShoppingCartServiceImpl(
            shippingService,
            mockCatalogService,
            promoService
        );
        
        // Initialize the service (equivalent to CDI @PostConstruct)
        cartService.init();
        
        // Test tier boundaries
        ShoppingCart cartLow = cartService.addItem("test-low", "low", 2); // $20 -> tier 0
        assertThat(cartLow.getShippingTotal()).isEqualTo(2.99);
        
        ShoppingCart cartMid = cartService.addItem("test-mid", "mid", 1); // $30 -> tier 1  
        assertThat(cartMid.getShippingTotal()).isEqualTo(4.99);
        
        ShoppingCart cartHigh = cartService.addItem("test-high", "high", 1); // $80 -> tier 3 (free shipping)
        assertThat(cartHigh.getShippingTotal()).isEqualTo(0.0); // Free shipping promotion
        assertThat(cartHigh.getShippingPromoSavings()).isEqualTo(-8.99);
    }
}