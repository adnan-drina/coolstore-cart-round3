package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ShoppingCartItemTest {

    @Test
    void defaultConstructorCreatesDefaultValues() {
        ShoppingCartItem item = new ShoppingCartItem();

        assertNull(item.getProduct());
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPrice());
        assertEquals(0.0, item.getPromoSavings());
    }

    @Test
    void settersSetFields() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product p = new Product("1111", "Car", "Super car", 1000);

        item.setProduct(p);
        item.setQuantity(2);
        item.setPrice(2000);
        item.setPromoSavings(0);

        assertSame(p, item.getProduct());
        assertEquals(2, item.getQuantity());
        assertEquals(2000, item.getPrice());
        assertEquals(0, item.getPromoSavings());
    }

    @Test
    void toStringContainsAllFields() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(new Product("1111", "Car", "Super car", 1000));
        item.setQuantity(2);
        item.setPrice(2000);
        item.setPromoSavings(0);

        String s = item.toString();

        assertTrue(s.contains("price=2000.0"));
        assertTrue(s.contains("quantity=2"));
        assertTrue(s.contains("promoSavings=0.0"));
        assertTrue(s.contains("product="));
    }

    @Test
    void serializesAndDeserializes() throws Exception {
        ShoppingCartItem original = new ShoppingCartItem();
        original.setProduct(new Product("1111", "Car", "Super car", 1000));
        original.setQuantity(2);
        original.setPrice(2000);
        original.setPromoSavings(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ShoppingCartItem deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (ShoppingCartItem) ois.readObject();
        }

        assertEquals(original.getPrice(), deserialized.getPrice());
        assertEquals(original.getQuantity(), deserialized.getQuantity());
        assertEquals(original.getPromoSavings(), deserialized.getPromoSavings());
        assertEquals(original.getProduct().getItemId(), deserialized.getProduct().getItemId());
    }
}
