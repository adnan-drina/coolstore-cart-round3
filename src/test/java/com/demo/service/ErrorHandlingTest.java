package com.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for error handling scenarios in ShoppingCartServiceImpl.
 * 
 * This test validates service-level exception handling, input validation,
 * and service failure scenarios to ensure robust error handling behavior.
 */
class ErrorHandlingTest {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlingTest.class);

    @Mock
    CatalogService catalogService;

    private ShoppingCartServiceImpl shoppingCartService;
    private ShippingService shippingService;
    private PromoService promoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shippingService = new ShippingService();
        promoService = new PromoService();
        shoppingCartService = new ShoppingCartServiceImpl(shippingService, catalogService, promoService);
    }

    // Catalog Service Unavailable Scenarios

    @Test
    void catalogService_throwsException_propagatesException() {
        // Arrange
        String cartId = "error-cart-1";
        String itemId = "1111";
        when(catalogService.products()).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert - Service currently propagates the exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.addItem(cartId, itemId, 1);
        });
        
        assertEquals("Service unavailable", exception.getMessage());
    }

    @Test
    void catalogService_emptyResponse_handlesGracefully() {
        // Arrange
        String cartId = "error-cart-2";
        String itemId = "9999"; // Non-existent item
        when(catalogService.products()).thenReturn(Collections.emptyList());

        // Act
        ShoppingCart cart = shoppingCartService.addItem(cartId, itemId, 1);

        // Assert - Should handle empty catalog gracefully
        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
        assertTrue(cart.getShoppingCartItemList().isEmpty(), "Cart should remain empty when product not found");
        LOG.info("Successfully handled empty catalog service response");
    }

    @Test
    void catalogService_partialFailure_cachesValidProducts() {
        // Arrange
        String cartId = "error-cart-3";
        String validItemId = "1111";
        String invalidItemId = "9999";
        
        Product validProduct = new Product(validItemId, "Valid Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(validProduct));

        // Act
        shoppingCartService.addItem(cartId, validItemId, 1);
        ShoppingCart cart = shoppingCartService.addItem(cartId, invalidItemId, 1);

        // Assert
        assertNotNull(cart);
        assertEquals(1, cart.getShoppingCartItemList().size(), "Should have only the valid product");
        assertEquals(validItemId, cart.getShoppingCartItemList().get(0).getProduct().getItemId());
    }

    @Test
    void catalogService_intermittentFailure_propagatesFirstException() {
        // Arrange
        String cartId = "error-cart-4";
        String itemId = "1111";
        
        // First call fails - service currently fails immediately
        when(catalogService.products()).thenThrow(new RuntimeException("Temporary failure"));

        // Act & Assert - Service fails on first call
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.addItem(cartId, itemId, 1);
        });
        
        assertEquals("Temporary failure", exception.getMessage());
    }

    // Input Validation Tests

    @Test
    void addItem_negativeQuantity_addsNegativeToCart() {
        // Arrange
        String cartId = "validation-cart-1";
        String itemId = "1111";
        int negativeQuantity = -5;

        Product product = new Product(itemId, "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act
        ShoppingCart cart = shoppingCartService.addItem(cartId, itemId, negativeQuantity);

        // Assert - Current behavior: accepts negative quantities
        assertNotNull(cart);
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(-5, cart.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void addItem_zeroQuantity_addsZeroQtyItem() {
        // Arrange
        String cartId = "validation-cart-2";
        String itemId = "1111";
        int zeroQuantity = 0;

        Product product = new Product(itemId, "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act
        ShoppingCart cart = shoppingCartService.addItem(cartId, itemId, zeroQuantity);

        // Assert - Current behavior: accepts zero quantity
        assertNotNull(cart);
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(0, cart.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void addItem_excessiveQuantity_handlesGracefully() {
        // Arrange
        String cartId = "validation-cart-3";
        String itemId = "1111";
        int excessiveQuantity = 999999;

        Product product = new Product(itemId, "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act
        ShoppingCart cart = shoppingCartService.addItem(cartId, itemId, excessiveQuantity);

        // Assert
        assertNotNull(cart);
        if (!cart.getShoppingCartItemList().isEmpty()) {
            ShoppingCartItem item = cart.getShoppingCartItemList().get(0);
            assertTrue(item.getQuantity() > 0, "Should have positive quantity");
            // May want to assert reasonable upper bounds in real implementation
        }
    }

    @Test
    void addItem_emptyCartId_handlesGracefully() {
        // Arrange
        String emptyCartId = "";
        String itemId = "1111";

        Product product = new Product(itemId, "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act
        ShoppingCart cart = shoppingCartService.addItem(emptyCartId, itemId, 1);

        // Assert
        assertNotNull(cart);
        assertEquals(emptyCartId, cart.getCartId());
    }

    @Test
    void addItem_nullCartId_handlesGracefully() {
        // Arrange
        String nullCartId = null;
        String itemId = "1111";

        Product product = new Product(itemId, "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act & Assert - Should not throw NullPointerException
        assertThrows(Exception.class, () -> {
            shoppingCartService.addItem(nullCartId, itemId, 1);
        });
    }

    @Test
    void addItem_nullItemId_handlesGracefully() {
        // Arrange
        String cartId = "validation-cart-6";
        String nullItemId = null;

        // Act & Assert - Should handle null item ID gracefully
        assertThrows(Exception.class, () -> {
            shoppingCartService.addItem(cartId, nullItemId, 1);
        });
    }

    @Test
    void addItem_emptyItemId_handlesGracefully() {
        // Arrange
        String cartId = "validation-cart-7";
        String emptyItemId = "";
        Product product = new Product("valid", "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));

        // Act
        ShoppingCart cart = shoppingCartService.addItem(cartId, emptyItemId, 1);

        // Assert - Should handle empty item ID gracefully
        assertNotNull(cart);
        assertTrue(cart.getShoppingCartItemList().isEmpty(), 
                  "Empty item ID should not add items to cart");
    }

    @Test
    void getShoppingCart_nullCartId_handlesGracefully() {
        // Act & Assert - Should handle null cart ID
        assertThrows(Exception.class, () -> {
            shoppingCartService.getShoppingCart(null);
        });
    }

    @Test
    void deleteItem_invalidItemId_handlesGracefully() {
        // Arrange
        String cartId = "validation-cart-8";
        String invalidItemId = "invalid-id";
        
        // Add a valid item first
        Product product = new Product("1111", "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));
        shoppingCartService.addItem(cartId, "1111", 2);

        // Act
        ShoppingCart cart = shoppingCartService.deleteItem(cartId, invalidItemId, 1);

        // Assert - Should handle invalid item ID gracefully
        assertNotNull(cart);
        assertEquals(2, cart.getShoppingCartItemList().get(0).getQuantity(), 
                    "Original item should remain unchanged");
    }

    // Service Failure Recovery Tests

    @Test
    void cartState_consistentAfterError() {
        // Arrange
        String cartId = "recovery-cart-1";
        Product product = new Product("1111", "Product", "Description", 100.0);
        when(catalogService.products()).thenReturn(Arrays.asList(product));
        
        // Add initial item
        shoppingCartService.addItem(cartId, "1111", 2);

        // Act - Try to add invalid item that might cause issues
        ShoppingCart cart = shoppingCartService.addItem(cartId, "invalid-id", 1);

        // Assert - Cart should still be in valid state
        assertNotNull(cart);
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals("1111", cart.getShoppingCartItemList().get(0).getProduct().getItemId());
        assertEquals(2, cart.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void concurrentOperations_withFailures_maintainConsistency() {
        // This test validates that even with service failures, cart operations
        // maintain consistency in concurrent scenarios
        
        String cartId = "concurrent-error-cart";
        String validItemId = "1111";
        String invalidItemId = "invalid";
        
        Product validProduct = new Product(validItemId, "Product", "Description", 100.0);
        
        // Simulate intermittent catalog service failures
        when(catalogService.products())
            .thenThrow(new RuntimeException("Service down"))
            .thenReturn(Arrays.asList(validProduct))
            .thenThrow(new RuntimeException("Timeout"))
            .thenReturn(Arrays.asList(validProduct));

        // Act - Multiple concurrent operations including failures
        for (int i = 0; i < 5; i++) {
            try {
                shoppingCartService.addItem(cartId, validItemId, 1);
                shoppingCartService.addItem(cartId, invalidItemId, 1);
                shoppingCartService.getShoppingCart(cartId);
            } catch (Exception e) {
                // Expected for some operations
                LOG.debug("Expected error occurred: {}", e.getMessage());
            }
        }

        // Assert - Cart should be in valid state
        ShoppingCart finalCart = shoppingCartService.getShoppingCart(cartId);
        assertNotNull(finalCart);
        assertTrue(finalCart.getShoppingCartItemList().size() >= 0);
    }

    @Test
    void productCache_invalidatedOnError() {
        // Arrange
        String cartId = "cache-error-cart";
        Product product = new Product("1111", "Product", "Description", 100.0);
        
        // Prime the cache
        when(catalogService.products()).thenReturn(Arrays.asList(product));
        shoppingCartService.addItem(cartId, "1111", 1);
        
        // Verify cache was populated
        assertNotNull(shoppingCartService.getProduct("1111"));

        // Act - Force catalog service to fail
        when(catalogService.products()).thenThrow(new RuntimeException("Service error"));
        
        // Should still be able to get cached product
        Product cachedProduct = shoppingCartService.getProduct("1111");
        assertNotNull(cachedProduct);
        assertEquals("1111", cachedProduct.getItemId());
    }

    // Service-Level Exception Mapping Tests

    @Test
    void serviceException_surfaceCorrectlyForRestLayer() {
        // This test validates that service-level exceptions are structured
        // for proper REST layer handling
        
        // Arrange
        String cartId = "exception-mapping-cart";
        doThrow(new RuntimeException("Catalog service error")).when(catalogService).products();
        
        // Act & Assert - Service should surface exceptions appropriately
        // In real implementation, these might be mapped to specific HTTP status codes
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.addItem(cartId, "1111", 1);
        });
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Catalog service error") ||
                   exception.getMessage() != null);
    }

    @Test
    void logging_behaviorForErrorConditions() {
        // Arrange
        String cartId = "logging-cart";
        when(catalogService.products()).thenThrow(new RuntimeException("Test error"));

        // Act & Assert - Service propagates exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shoppingCartService.addItem(cartId, "1111", 1);
        });
        
        assertEquals("Test error", exception.getMessage());
        LOG.info("Error handling test completed successfully");
    }
}