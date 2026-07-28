package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ShoppingCartTest {

    @Test
    void defaultConstructorInitializesZeroTotals() {
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
    void cartIdConstructorSetsId() {
        ShoppingCart cart = new ShoppingCart("1111");

        assertEquals("1111", cart.getCartId());
    }

    @Test
    void settersUpdatePricingFields() {
        ShoppingCart cart = new ShoppingCart();

        cart.setCartId("1");
        cart.setCartItemTotal(2000);
        cart.setCartItemPromoSavings(0);
        cart.setShippingTotal(10.99);
        cart.setShippingPromoSavings(-10.99);
        cart.setCartTotal(2000);

        assertEquals("1", cart.getCartId());
        assertEquals(2000, cart.getCartItemTotal());
        assertEquals(0, cart.getCartItemPromoSavings());
        assertEquals(10.99, cart.getShippingTotal());
        assertEquals(-10.99, cart.getShippingPromoSavings());
        assertEquals(2000, cart.getCartTotal());
    }

    @Test
    void addShoppingCartItemAddsNonNull() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();

        cart.addShoppingCartItem(item);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertSame(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void addShoppingCartItemIgnoresNull() {
        ShoppingCart cart = new ShoppingCart();

        cart.addShoppingCartItem(null);

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemRemovesNonNull() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);

        assertTrue(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemReturnsFalseForNull() {
        ShoppingCart cart = new ShoppingCart();

        boolean removed = cart.removeShoppingCartItem(null);

        assertFalse(removed);
    }

    @Test
    void removeShoppingCartItemReturnsFalseWhenNotPresent() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();

        boolean removed = cart.removeShoppingCartItem(item);

        assertFalse(removed);
    }

    @Test
    void resetShoppingCartItemListClearsItems() {
        ShoppingCart cart = new ShoppingCart();
        cart.addShoppingCartItem(new ShoppingCartItem());
        cart.addShoppingCartItem(new ShoppingCartItem());

        cart.resetShoppingCartItemList();

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void setShoppingCartItemListReplacesList() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();

        cart.setShoppingCartItemList(java.util.List.of(item));

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertSame(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void toStringContainsAllFields() {
        ShoppingCart cart = new ShoppingCart("1");
        cart.setCartItemTotal(2000);
        cart.setCartItemPromoSavings(0);
        cart.setShippingTotal(10.99);
        cart.setShippingPromoSavings(-10.99);
        cart.setCartTotal(2000);

        String s = cart.toString();

        assertTrue(s.contains("cartId=1"));
        assertTrue(s.contains("cartItemTotal=2000.0"));
        assertTrue(s.contains("cartItemPromoSavings=0.0"));
        assertTrue(s.contains("shippingTotal=10.99"));
        assertTrue(s.contains("shippingPromoSavings=-10.99"));
        assertTrue(s.contains("cartTotal=2000.0"));
    }

    @Test
    void serializesAndDeserializes() throws Exception {
        ShoppingCart original = new ShoppingCart("1");
        original.setCartItemTotal(2000);
        original.setCartTotal(2000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ShoppingCart deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (ShoppingCart) ois.readObject();
        }

        assertEquals(original.getCartId(), deserialized.getCartId());
        assertEquals(original.getCartItemTotal(), deserialized.getCartItemTotal());
        assertEquals(original.getCartTotal(), deserialized.getCartTotal());
    }
}
