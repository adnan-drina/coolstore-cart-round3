package com.demo.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {

    @Test
    void should_initialize_with_defaults() {
        ShoppingCart cart = new ShoppingCart();

        assertNull(cart.getCartId());
        assertEquals(0.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getCartItemPromoSavings());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(0.0, cart.getShippingPromoSavings());
        assertEquals(0.0, cart.getCartTotal());
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void should_initialize_with_cartId() {
        ShoppingCart cart = new ShoppingCart("cart-123");

        assertEquals("cart-123", cart.getCartId());
    }

    @Test
    void should_set_and_get_cartId() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId("cart-456");

        assertEquals("cart-456", cart.getCartId());
    }

    @Test
    void should_set_and_get_cartItemTotal() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartItemTotal(500.0);

        assertEquals(500.0, cart.getCartItemTotal());
    }

    @Test
    void should_set_and_get_cartItemPromoSavings() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartItemPromoSavings(50.0);

        assertEquals(50.0, cart.getCartItemPromoSavings());
    }

    @Test
    void should_set_and_get_shippingTotal() {
        ShoppingCart cart = new ShoppingCart();
        cart.setShippingTotal(25.0);

        assertEquals(25.0, cart.getShippingTotal());
    }

    @Test
    void should_set_and_get_shippingPromoSavings() {
        ShoppingCart cart = new ShoppingCart();
        cart.setShippingPromoSavings(5.0);

        assertEquals(5.0, cart.getShippingPromoSavings());
    }

    @Test
    void should_set_and_get_cartTotal() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartTotal(475.0);

        assertEquals(475.0, cart.getCartTotal());
    }

    @Test
    void should_add_shopping_cart_item() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        item.setQuantity(2);

        cart.addShoppingCartItem(item);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertSame(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void should_ignore_null_when_adding_shopping_cart_item() {
        ShoppingCart cart = new ShoppingCart();

        cart.addShoppingCartItem(null);

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void should_add_multiple_shopping_cart_items() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(100.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(200.0);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        assertEquals(2, cart.getShoppingCartItemList().size());
    }

    @Test
    void should_remove_shopping_cart_item() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);

        assertTrue(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void should_return_false_when_removing_null_item() {
        ShoppingCart cart = new ShoppingCart();

        boolean removed = cart.removeShoppingCartItem(null);

        assertFalse(removed);
    }

    @Test
    void should_return_false_when_removing_item_not_in_cart() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);

        boolean removed = cart.removeShoppingCartItem(item);

        assertFalse(removed);
    }

    @Test
    void should_reset_shopping_cart_item_list() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        cart.addShoppingCartItem(item);

        cart.resetShoppingCartItemList();

        assertTrue(cart.getShoppingCartItemList().isEmpty());
        assertNotSame(cart.getShoppingCartItemList(), new ArrayList<>());
    }

    @Test
    void should_set_shopping_cart_item_list() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        List<ShoppingCartItem> items = new ArrayList<>();
        items.add(item);

        cart.setShoppingCartItemList(items);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertSame(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void should_implement_serializable() {
        ShoppingCart cart = new ShoppingCart("cart-1");
        cart.setCartItemTotal(100.0);
        cart.setCartItemPromoSavings(10.0);
        cart.setShippingTotal(20.0);
        cart.setShippingPromoSavings(2.0);
        cart.setCartTotal(108.0);

        assertNotNull(cart);
        assertInstanceOf(java.io.Serializable.class, cart);
    }

    @Test
    void should_produce_toString_with_all_fields() {
        ShoppingCart cart = new ShoppingCart("cart-1");
        cart.setCartItemTotal(100.0);
        cart.setCartItemPromoSavings(10.0);
        cart.setShippingTotal(20.0);
        cart.setShippingPromoSavings(2.0);
        cart.setCartTotal(108.0);

        String result = cart.toString();

        assertTrue(result.contains("cartId=cart-1"));
        assertTrue(result.contains("cartItemTotal=100.0"));
        assertTrue(result.contains("cartItemPromoSavings=10.0"));
        assertTrue(result.contains("shippingTotal=20.0"));
        assertTrue(result.contains("shippingPromoSavings=2.0"));
        assertTrue(result.contains("cartTotal=108.0"));
        assertTrue(result.contains("shoppingCartItemList="));
    }

    @Test
    void should_maintain_pricing_fields_independently() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartItemTotal(500.0);
        cart.setCartItemPromoSavings(50.0);
        cart.setShippingTotal(25.0);
        cart.setShippingPromoSavings(5.0);
        cart.setCartTotal(470.0);

        assertEquals(500.0, cart.getCartItemTotal());
        assertEquals(50.0, cart.getCartItemPromoSavings());
        assertEquals(25.0, cart.getShippingTotal());
        assertEquals(5.0, cart.getShippingPromoSavings());
        assertEquals(470.0, cart.getCartTotal());
    }

    @Test
    void should_preserve_serialVersionUID() {
        ShoppingCart cart = new ShoppingCart();
        java.lang.reflect.Field field = null;
        try {
            field = ShoppingCart.class.getDeclaredField("serialVersionUID");
        } catch (NoSuchFieldException e) {
            fail("serialVersionUID field not found", e);
        }

        field.setAccessible(true);
        try {
            assertEquals(-1108043957592113528L, field.getLong(cart));
        } catch (IllegalAccessException e) {
            fail("Cannot read serialVersionUID", e);
        }
    }
}
