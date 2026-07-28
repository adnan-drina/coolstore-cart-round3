package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ShoppingCartTest {

    @Test
    void defaultConstructorInitializesDefaults() {
        ShoppingCart cart = new ShoppingCart();

        assertNull(cart.getCartId());
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
        assertEquals(0.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getCartItemPromoSavings());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(0.0, cart.getShippingPromoSavings());
        assertEquals(0.0, cart.getCartTotal());
    }

    @Test
    void parameterizedConstructorSetsCartId() {
        ShoppingCart cart = new ShoppingCart("CART-001");

        assertEquals("CART-001", cart.getCartId());
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void cartIdSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertNull(cart.getCartId());

        cart.setCartId("CART-002");
        assertEquals("CART-002", cart.getCartId());
    }

    @Test
    void addShoppingCartItemAddsNonNullItem() {
        ShoppingCart cart = new ShoppingCart("CART-003");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        item.setQuantity(1);

        cart.addShoppingCartItem(item);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void addShoppingCartItemIgnoresNull() {
        ShoppingCart cart = new ShoppingCart("CART-004");

        cart.addShoppingCartItem(null);

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void addShoppingCartItemMultipleItems() {
        ShoppingCart cart = new ShoppingCart("CART-005");
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(10.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(20.0);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        assertEquals(2, cart.getShoppingCartItemList().size());
        assertEquals(item1, cart.getShoppingCartItemList().get(0));
        assertEquals(item2, cart.getShoppingCartItemList().get(1));
    }

    @Test
    void removeShoppingCartItemRemovesExistingItem() {
        ShoppingCart cart = new ShoppingCart("CART-006");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(15.0);
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);

        assertTrue(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemReturnsFalseForNotFound() {
        ShoppingCart cart = new ShoppingCart("CART-007");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(15.0);

        boolean removed = cart.removeShoppingCartItem(item);

        assertFalse(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemIgnoresNull() {
        ShoppingCart cart = new ShoppingCart("CART-008");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(15.0);
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(null);

        assertFalse(removed);
        assertEquals(1, cart.getShoppingCartItemList().size());
    }

    @Test
    void removeShoppingCartItemRemovesOneOfMultiple() {
        ShoppingCart cart = new ShoppingCart("CART-009");
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(10.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(20.0);
        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        boolean removed = cart.removeShoppingCartItem(item1);

        assertTrue(removed);
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(item2, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void resetShoppingCartItemListClearsAllItems() {
        ShoppingCart cart = new ShoppingCart("CART-010");
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(10.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(20.0);
        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        cart.resetShoppingCartItemList();

        assertTrue(cart.getShoppingCartItemList().isEmpty());
        assertNotSame(cart.getShoppingCartItemList(), new ArrayList<>());
    }

    @Test
    void resetShoppingCartItemListOnEmptyCart() {
        ShoppingCart cart = new ShoppingCart("CART-011");

        cart.resetShoppingCartItemList();

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void setShoppingCartItemListReplacesList() {
        ShoppingCart cart = new ShoppingCart("CART-012");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        cart.addShoppingCartItem(item);

        List<ShoppingCartItem> newList = new ArrayList<>();
        ShoppingCartItem newItem = new ShoppingCartItem();
        newItem.setPrice(30.0);
        newList.add(newItem);
        cart.setShoppingCartItemList(newList);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(newItem, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void cartItemTotalSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertEquals(0.0, cart.getCartItemTotal());

        cart.setCartItemTotal(100.0);
        assertEquals(100.0, cart.getCartItemTotal());
    }

    @Test
    void cartItemPromoSavingsSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertEquals(0.0, cart.getCartItemPromoSavings());

        cart.setCartItemPromoSavings(10.0);
        assertEquals(10.0, cart.getCartItemPromoSavings());
    }

    @Test
    void shippingTotalSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertEquals(0.0, cart.getShippingTotal());

        cart.setShippingTotal(5.99);
        assertEquals(5.99, cart.getShippingTotal());
    }

    @Test
    void shippingPromoSavingsSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertEquals(0.0, cart.getShippingPromoSavings());

        cart.setShippingPromoSavings(2.0);
        assertEquals(2.0, cart.getShippingPromoSavings());
    }

    @Test
    void cartTotalSetterAndGetter() {
        ShoppingCart cart = new ShoppingCart();

        assertEquals(0.0, cart.getCartTotal());

        cart.setCartTotal(111.99);
        assertEquals(111.99, cart.getCartTotal());
    }

    @Test
    void allPricingFieldsInitializedToZero() {
        ShoppingCart cart = new ShoppingCart("CART-013");

        assertEquals(0.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getCartItemPromoSavings());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(0.0, cart.getShippingPromoSavings());
        assertEquals(0.0, cart.getCartTotal());
    }

    @Test
    void allPricingFieldsCanBeSetIndependently() {
        ShoppingCart cart = new ShoppingCart("CART-014");

        cart.setCartItemTotal(100.0);
        cart.setCartItemPromoSavings(10.0);
        cart.setShippingTotal(5.99);
        cart.setShippingPromoSavings(1.0);
        cart.setCartTotal(94.99);

        assertEquals(100.0, cart.getCartItemTotal());
        assertEquals(10.0, cart.getCartItemPromoSavings());
        assertEquals(5.99, cart.getShippingTotal());
        assertEquals(1.0, cart.getShippingPromoSavings());
        assertEquals(94.99, cart.getCartTotal());
    }

    @Test
    void toStringIncludesAllProperties() {
        ShoppingCart cart = new ShoppingCart("CART-015");
        cart.setCartItemTotal(100.0);
        cart.setCartItemPromoSavings(10.0);
        cart.setShippingTotal(5.99);
        cart.setShippingPromoSavings(1.0);
        cart.setCartTotal(94.99);

        String result = cart.toString();

        assertTrue(result.contains("cartId=CART-015"));
        assertTrue(result.contains("cartItemTotal=100.0"));
        assertTrue(result.contains("cartItemPromoSavings=10.0"));
        assertTrue(result.contains("shippingTotal=5.99"));
        assertTrue(result.contains("shippingPromoSavings=1.0"));
        assertTrue(result.contains("cartTotal=94.99"));
        assertTrue(result.contains("shoppingCartItemList="));
    }

    @Test
    void toStringWithDefaults() {
        ShoppingCart cart = new ShoppingCart();
        String result = cart.toString();

        assertTrue(result.contains("cartId=null"));
        assertTrue(result.contains("cartItemTotal=0.0"));
        assertTrue(result.contains("cartItemPromoSavings=0.0"));
        assertTrue(result.contains("shippingTotal=0.0"));
        assertTrue(result.contains("shippingPromoSavings=0.0"));
        assertTrue(result.contains("cartTotal=0.0"));
    }

    @Test
    void toStringWithItems() {
        ShoppingCart cart = new ShoppingCart("CART-016");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        item.setQuantity(2);
        cart.addShoppingCartItem(item);

        String result = cart.toString();

        assertTrue(result.contains("shoppingCartItemList="));
        assertTrue(result.contains("price=10.0"));
    }

    @Test
    void serialVersionUIDPreserved() throws Exception {
        java.lang.reflect.Field field = ShoppingCart.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        assertEquals(-1108043957592113528L, field.getLong(null));
    }

    @Test
    void serializationRoundTripPreservesState() throws Exception {
        Product product = new Product("ITEM-1", "Widget", "A widget", 19.99);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setId(1L);
        item.setPrice(19.99);
        item.setQuantity(2);
        item.setPromoSavings(3.0);
        item.setProduct(product);

        ShoppingCart original = new ShoppingCart("CART-017");
        original.addShoppingCartItem(item);
        original.setCartItemTotal(39.98);
        original.setCartItemPromoSavings(3.0);
        original.setShippingTotal(5.99);
        original.setShippingPromoSavings(0.0);
        original.setCartTotal(42.97);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCart deserialized = (ShoppingCart) ois.readObject();

            assertEquals("CART-017", deserialized.getCartId());
            assertEquals(1, deserialized.getShoppingCartItemList().size());
            assertEquals(39.98, deserialized.getCartItemTotal());
            assertEquals(3.0, deserialized.getCartItemPromoSavings());
            assertEquals(5.99, deserialized.getShippingTotal());
            assertEquals(0.0, deserialized.getShippingPromoSavings());
            assertEquals(42.97, deserialized.getCartTotal());
        }
    }

    @Test
    void addItemThenRemoveThenAddMaintainsCorrectState() {
        ShoppingCart cart = new ShoppingCart("CART-018");
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(10.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(20.0);
        ShoppingCartItem item3 = new ShoppingCartItem();
        item3.setPrice(30.0);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);
        assertEquals(2, cart.getShoppingCartItemList().size());

        cart.removeShoppingCartItem(item1);
        assertEquals(1, cart.getShoppingCartItemList().size());

        cart.addShoppingCartItem(item3);
        assertEquals(2, cart.getShoppingCartItemList().size());
        assertEquals(item2, cart.getShoppingCartItemList().get(0));
        assertEquals(item3, cart.getShoppingCartItemList().get(1));
    }

    @Test
    void cartItemListIsModifiableViaGetter() {
        ShoppingCart cart = new ShoppingCart("CART-019");
        List<ShoppingCartItem> items = cart.getShoppingCartItemList();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        items.add(item);

        assertEquals(1, cart.getShoppingCartItemList().size());
    }

    @Test
    void implementsSerializable() {
        ShoppingCart cart = new ShoppingCart();
        assertTrue(cart instanceof java.io.Serializable);
    }
}
