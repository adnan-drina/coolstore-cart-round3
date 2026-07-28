package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ShoppingCartServiceImplTest {

    @Inject
    ShoppingCartServiceImpl shoppingCartService;

    @Test
    void should_handle_null_cart_id() {
        assertThrows(NullPointerException.class, () -> {
            shoppingCartService.getShoppingCart(null);
        });
    }

    @Test
    void should_create_new_cart_when_not_exists() {
        String cartId = "non-existent-cart";
        
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        
        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void should_return_existing_cart() {
        String cartId = "existing-cart";
        
        // Create cart first
        ShoppingCart firstCart = shoppingCartService.getShoppingCart(cartId);
        firstCart.addShoppingCartItem(createTestItem("product1", 100.0, 2));
        
        // Get the same cart again
        ShoppingCart secondCart = shoppingCartService.getShoppingCart(cartId);
        
        assertNotNull(secondCart);
        assertEquals(cartId, secondCart.getCartId());
        assertEquals(1, secondCart.getShoppingCartItemList().size());
    }

    @Test
    void should_handle_null_product_lookup() {
        // Try to add item with product that doesn't exist in catalog
        Product result = shoppingCartService.getProduct("non-existent-product");
        
        assertNull(result);
    }

    @Test
    void should_handle_catalog_service_exception() {
        String cartId = "test-cart-with-bad-product";
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        ShoppingCartItem item = createTestItem("bad-product", 100.0, 1);
        cart.addShoppingCartItem(item);
        
        // This should not throw an exception but handle it gracefully
        assertDoesNotThrow(() -> {
            shoppingCartService.priceShoppingCart(cart);
        });
    }

    @Test
    void should_handle_empty_cart_item_list() {
        ShoppingCart cart = new ShoppingCart("empty-cart");
        
        assertDoesNotThrow(() -> {
            shoppingCartService.priceShoppingCart(cart);
        });
        
        assertEquals(0.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void should_handle_null_cart_item_list() {
        ShoppingCart cart = new ShoppingCart("null-list-cart");
        cart.setShoppingCartItemList(null);
        
        shoppingCartService.priceShoppingCart(cart);
        
        assertEquals(0.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void should_remove_items_with_sufficient_quantity() {
        String cartId = "remove-cart";
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        ShoppingCartItem item = createTestItem("product1", 100.0, 2);
        cart.addShoppingCartItem(item);
        
        ShoppingCart result = shoppingCartService.deleteItem(cartId, "product1", 2);
        
        assertEquals(0, result.getShoppingCartItemList().size());
    }

    @Test
    void should_reduce_quantity_when_insufficient_removal() {
        String cartId = "partial-remove-cart";
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        ShoppingCartItem item = createTestItem("product1", 100.0, 5);
        cart.addShoppingCartItem(item);
        
        ShoppingCart result = shoppingCartService.deleteItem(cartId, "product1", 2);
        
        assertEquals(1, result.getShoppingCartItemList().size());
        assertEquals(3, result.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void should_handle_delete_with_non_existent_item() {
        String cartId = "delete-non-existent";
        
        ShoppingCart result = shoppingCartService.deleteItem(cartId, "non-existent-product", 1);
        
        assertEquals(0, result.getShoppingCartItemList().size());
    }

    @Test
    void should_checkout_and_clear_cart() {
        String cartId = "checkout-cart";
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        cart.addShoppingCartItem(createTestItem("product1", 100.0, 2));
        
        ShoppingCart result = shoppingCartService.checkout(cartId);
        
        assertEquals(0, result.getShoppingCartItemList().size());
        assertEquals(cartId, result.getCartId());
    }

    @Test
    void should_add_item_and_handle_exceptions() {
        String cartId = "exception-test-cart";
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        
        // Add a valid item first
        ShoppingCartItem item = createTestItem("329299", 100.0, 1);
        cart.addShoppingCartItem(item);
        
        ShoppingCart result = shoppingCartService.addItem(cartId, "329299", 1);
        
        assertNotNull(result);
        // Should have processed the item without exceptions
    }

    @Test
    void should_set_cart_contents_from_temp_cart() {
        String targetCartId = "target-cart";
        String tempCartId = "temp-cart";
        
        // Setup temp cart with items
        ShoppingCart tempCart = shoppingCartService.getShoppingCart(tempCartId);
        tempCart.addShoppingCartItem(createTestItem("product1", 100.0, 2));
        tempCart.addShoppingCartItem(createTestItem("product2", 200.0, 1));
        
        // Set target cart contents from temp cart
        ShoppingCart result = shoppingCartService.set(targetCartId, tempCartId);
        
        assertEquals(2, result.getShoppingCartItemList().size());
        assertEquals(targetCartId, result.getCartId());
    }

    @Test
    void should_handle_set_with_null_temp_cart() {
        String targetCartId = "target-from-null";
        
        ShoppingCart result = shoppingCartService.set(targetCartId, "non-existent-temp");
        
        assertEquals(0, result.getShoppingCartItemList().size());
        assertEquals(targetCartId, result.getCartId());
    }

    @Test
    void should_dedupe_cart_items_correctly() {
        ShoppingCart cart = new ShoppingCart("dedupe-test");
        
        // Add same product twice with different quantities
        ShoppingCartItem item1 = createTestItem("product1", 100.0, 2);
        ShoppingCartItem item2 = createTestItem("product1", 100.0, 3);
        ShoppingCartItem item3 = createTestItem("product2", 200.0, 1);
        
        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);
        cart.addShoppingCartItem(item3);
        
        // Call the package-private method via reflection
        try {
            java.lang.reflect.Method dedupeMethod = ShoppingCartServiceImpl.class.getDeclaredMethod("dedupeCartItems", ShoppingCart.class);
            dedupeMethod.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            List<ShoppingCartItem> result = (List<ShoppingCartItem>) dedupeMethod.invoke(shoppingCartService, cart);
            
            assertEquals(2, result.size());
            // Should have combined quantities for product1
            ShoppingCartItem product1Item = result.stream()
                .filter(item -> "product1".equals(item.getProduct().getItemId()))
                .findFirst()
                .orElse(null);
            
            assertNotNull(product1Item);
            assertEquals(5, product1Item.getQuantity()); // 2 + 3
        } catch (Exception e) {
            fail("Failed to invoke dedupe method: " + e.getMessage());
        }
    }

    @Test
    void should_initialize_shopping_cart_for_pricing() {
        ShoppingCart cart = new ShoppingCart("pricing-test");
        cart.setCartItemTotal(100.0);
        cart.setCartItemPromoSavings(10.0);
        cart.setShippingTotal(20.0);
        cart.setShippingPromoSavings(5.0);
        cart.setCartTotal(105.0);
        
        // Call the package-private method via reflection
        try {
            java.lang.reflect.Method initMethod = ShoppingCartServiceImpl.class.getDeclaredMethod("initShoppingCartForPricing", ShoppingCart.class);
            initMethod.setAccessible(true);
            
            initMethod.invoke(shoppingCartService, cart);
            
            assertEquals(0.0, cart.getCartItemTotal(), 0.001);
            assertEquals(0.0, cart.getCartItemPromoSavings(), 0.001);
            assertEquals(0.0, cart.getShippingTotal(), 0.001);
            assertEquals(0.0, cart.getShippingPromoSavings(), 0.001);
            assertEquals(0.0, cart.getCartTotal(), 0.001);
        } catch (Exception e) {
            fail("Failed to invoke init method: " + e.getMessage());
        }
    }

    private ShoppingCartItem createTestItem(String productId, double price, int quantity) {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(new Product(productId, "Test Product", "Description", price));
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }
}