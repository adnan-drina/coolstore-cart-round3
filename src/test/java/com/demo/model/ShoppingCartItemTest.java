package com.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartItemTest {

    @Test
    void should_initialize_with_defaults() {
        ShoppingCartItem item = new ShoppingCartItem();

        assertEquals(0.0, item.getPrice());
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPromoSavings());
        assertNull(item.getProduct());
    }

    @Test
    void should_set_and_get_price() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(1000.0);

        assertEquals(1000.0, item.getPrice());
    }

    @Test
    void should_set_and_get_quantity() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setQuantity(3);

        assertEquals(3, item.getQuantity());
    }

    @Test
    void should_set_and_get_promoSavings() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPromoSavings(100.0);

        assertEquals(100.0, item.getPromoSavings());
    }

    @Test
    void should_set_and_get_product() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("2222", "Bike", "Mountain bike", 200.0);
        item.setProduct(product);

        assertEquals(product, item.getProduct());
        assertEquals("2222", item.getProduct().getItemId());
        assertEquals("Bike", item.getProduct().getName());
        assertEquals(200.0, item.getProduct().getPrice());
    }

    @Test
    void should_implement_serializable() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(500.0);
        item.setQuantity(2);
        item.setPromoSavings(50.0);
        item.setProduct(new Product("1111", "Car", "Super car", 1000.0));

        assertNotNull(item);
        assertInstanceOf(java.io.Serializable.class, item);
    }

    @Test
    void should_produce_toString_with_all_fields() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        item.setQuantity(1);
        item.setPromoSavings(10.0);
        item.setProduct(new Product("1111", "Car", "Super car", 1000.0));

        String result = item.toString();

        assertTrue(result.contains("price=100.0"));
        assertTrue(result.contains("quantity=1"));
        assertTrue(result.contains("promoSavings=10.0"));
        assertTrue(result.contains("product="));
    }

    @Test
    void should_maintain_product_reference_after_updates() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product("2222", "Bike", "Mountain bike", 200.0);
        item.setProduct(product);
        item.setPrice(200.0);
        item.setQuantity(5);

        assertEquals("2222", item.getProduct().getItemId());
        assertEquals(200.0, item.getPrice());
        assertEquals(5, item.getQuantity());
    }
}
