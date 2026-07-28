package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ShoppingCartItemTest {

    @Test
    void defaultConstructorInitializesDefaults() {
        ShoppingCartItem item = new ShoppingCartItem();

        assertNull(item.getId());
        assertEquals(0.0, item.getPrice());
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPromoSavings());
        assertNull(item.getProduct());
    }

    @Test
    void settersUpdateAllProperties() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("ITEM-1", "Widget", "A widget", 19.99);

        item.setId(1L);
        item.setPrice(19.99);
        item.setQuantity(3);
        item.setPromoSavings(5.0);
        item.setProduct(product);

        assertEquals(1L, item.getId());
        assertEquals(19.99, item.getPrice());
        assertEquals(3, item.getQuantity());
        assertEquals(5.0, item.getPromoSavings());
        assertEquals(product, item.getProduct());
    }

    @Test
    void pricingCalculationQuantityTimesPriceMinusPromoSavings() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(25.0);
        item.setQuantity(4);
        item.setPromoSavings(10.0);

        double expected = item.getQuantity() * item.getPrice() - item.getPromoSavings();
        assertEquals(90.0, expected);
    }

    @Test
    void pricingCalculationZeroPromoSavings() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        item.setQuantity(5);
        item.setPromoSavings(0.0);

        double expected = item.getQuantity() * item.getPrice() - item.getPromoSavings();
        assertEquals(50.0, expected);
    }

    @Test
    void pricingCalculationZeroQuantity() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(15.0);
        item.setQuantity(0);
        item.setPromoSavings(0.0);

        double expected = item.getQuantity() * item.getPrice() - item.getPromoSavings();
        assertEquals(0.0, expected);
    }

    @Test
    void productAssociationRetrievedCorrectly() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("ITEM-2", "Gadget", "A gadget", 29.99);
        item.setProduct(product);

        assertEquals(product, item.getProduct());
        assertEquals("ITEM-2", item.getProduct().getItemId());
        assertEquals("Gadget", item.getProduct().getName());
        assertEquals(29.99, item.getProduct().getPrice());
    }

    @Test
    void productAssociationCanBeReplaced() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product1 = new Product("ITEM-1", "Widget", "A widget", 19.99);
        Product product2 = new Product("ITEM-2", "Gadget", "A gadget", 29.99);

        item.setProduct(product1);
        assertEquals("ITEM-1", item.getProduct().getItemId());

        item.setProduct(product2);
        assertEquals("ITEM-2", item.getProduct().getItemId());
    }

    @Test
    void productAssociationCanBeCleared() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("ITEM-1", "Widget", "A widget", 19.99);
        item.setProduct(product);

        item.setProduct(null);
        assertNull(item.getProduct());
    }

    @Test
    void toStringIncludesAllProperties() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(19.99);
        item.setQuantity(2);
        item.setPromoSavings(3.0);
        Product product = new Product("ITEM-1", "Widget", "A widget", 19.99);
        item.setProduct(product);

        String result = item.toString();

        assertTrue(result.contains("price=19.99"));
        assertTrue(result.contains("quantity=2"));
        assertTrue(result.contains("promoSavings=3.0"));
        assertTrue(result.contains("product="));
    }

    @Test
    void toStringWithDefaults() {
        ShoppingCartItem item = new ShoppingCartItem();
        String result = item.toString();

        assertTrue(result.contains("price=0.0"));
        assertTrue(result.contains("quantity=0"));
        assertTrue(result.contains("promoSavings=0.0"));
        assertTrue(result.contains("product=null"));
    }

    @Test
    void serialVersionUIDPreserved() throws Exception {
        java.lang.reflect.Field field = ShoppingCartItem.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        assertEquals(6964558044240061049L, field.getLong(null));
    }

    @Test
    void serializationRoundTripPreservesState() throws Exception {
        Product product = new Product("ITEM-3", "Doohickey", "A doohickey", 14.99);
        ShoppingCartItem original = new ShoppingCartItem();
        original.setId(42L);
        original.setPrice(14.99);
        original.setQuantity(2);
        original.setPromoSavings(2.5);
        original.setProduct(product);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCartItem deserialized = (ShoppingCartItem) ois.readObject();

            assertEquals(42L, deserialized.getId());
            assertEquals(14.99, deserialized.getPrice());
            assertEquals(2, deserialized.getQuantity());
            assertEquals(2.5, deserialized.getPromoSavings());
            assertNotNull(deserialized.getProduct());
            assertEquals("ITEM-3", deserialized.getProduct().getItemId());
            assertEquals("Doohickey", deserialized.getProduct().getName());
            assertEquals(14.99, deserialized.getProduct().getPrice());
        }
    }

    @Test
    void idSetterAndGetter() {
        ShoppingCartItem item = new ShoppingCartItem();

        assertNull(item.getId());

        item.setId(100L);
        assertEquals(100L, item.getId());
    }

    @Test
    void priceWithDoublePrecision() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(0.1);

        assertEquals(0.1, item.getPrice());
    }

    @Test
    void quantityWithLargeValue() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setQuantity(1000);

        assertEquals(1000, item.getQuantity());
    }

    @Test
    void promoSavingsWithFullDiscount() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        item.setQuantity(1);
        item.setPromoSavings(10.0);

        double expected = item.getQuantity() * item.getPrice() - item.getPromoSavings();
        assertEquals(0.0, expected);
    }
}
