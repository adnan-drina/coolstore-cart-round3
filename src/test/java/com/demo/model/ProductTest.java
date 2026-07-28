package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void defaultConstructorInitializesNullsAndZeroPrice() {
        Product product = new Product();

        assertNull(product.getItemId());
        assertNull(product.getName());
        assertNull(product.getDesc());
        assertEquals(0.0, product.getPrice());
    }

    @Test
    void parameterizedConstructorSetsAllProperties() {
        Product product = new Product("ITEM-1", "Widget", "A fine widget", 19.99);

        assertEquals("ITEM-1", product.getItemId());
        assertEquals("Widget", product.getName());
        assertEquals("A fine widget", product.getDesc());
        assertEquals(19.99, product.getPrice());
    }

    @Test
    void settersUpdateProperties() {
        Product product = new Product();

        product.setItemId("ITEM-2");
        product.setName("Gadget");
        product.setDesc("A useful gadget");
        product.setPrice(29.99);

        assertEquals("ITEM-2", product.getItemId());
        assertEquals("Gadget", product.getName());
        assertEquals("A useful gadget", product.getDesc());
        assertEquals(29.99, product.getPrice());
    }

    @Test
    void toStringIncludesAllProperties() {
        Product product = new Product("ITEM-3", "Doohickey", "Description", 9.99);
        String result = product.toString();

        assertTrue(result.contains("itemId=ITEM-3"));
        assertTrue(result.contains("name=Doohickey"));
        assertTrue(result.contains("desc=Description"));
        assertTrue(result.contains("price=9.99"));
    }

    @Test
    void toStringWithNullProperties() {
        Product product = new Product();
        String result = product.toString();

        assertTrue(result.contains("itemId=null"));
        assertTrue(result.contains("name=null"));
        assertTrue(result.contains("desc=null"));
        assertTrue(result.contains("price=0.0"));
    }

    @Test
    void serialVersionUIDPreserved() throws Exception {
        java.lang.reflect.Field field = Product.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        assertEquals(-7304814269819778382L, field.getLong(null));
    }

    @Test
    void serializationRoundTripPreservesState() throws Exception {
        Product original = new Product("ITEM-4", "Thingamajig", "A thing", 49.99);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            Product deserialized = (Product) ois.readObject();

            assertEquals("ITEM-4", deserialized.getItemId());
            assertEquals("Thingamajig", deserialized.getName());
            assertEquals("A thing", deserialized.getDesc());
            assertEquals(49.99, deserialized.getPrice());
        }
    }
}
