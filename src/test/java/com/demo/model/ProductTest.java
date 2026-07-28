package com.demo.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void defaultConstructorCreatesNullFields() {
        Product p = new Product();

        assertNull(p.getItemId());
        assertNull(p.getName());
        assertNull(p.getDesc());
        assertEquals(0.0, p.getPrice());
    }

    @Test
    void fullConstructorSetsAllFields() {
        Product p = new Product("1111", "Car", "Super car", 1000);

        assertEquals("1111", p.getItemId());
        assertEquals("Car", p.getName());
        assertEquals("Super car", p.getDesc());
        assertEquals(1000, p.getPrice());
    }

    @Test
    void settersOverrideFields() {
        Product p = new Product();

        p.setItemId("2222");
        p.setName("Bike");
        p.setDesc("Super bike");
        p.setPrice(200);

        assertEquals("2222", p.getItemId());
        assertEquals("Bike", p.getName());
        assertEquals("Super bike", p.getDesc());
        assertEquals(200, p.getPrice());
    }

    @Test
    void toStringContainsAllFields() {
        Product p = new Product("1111", "Car", "Super car", 1000);
        String s = p.toString();

        assertTrue(s.contains("itemId=1111"));
        assertTrue(s.contains("name=Car"));
        assertTrue(s.contains("desc=Super car"));
        assertTrue(s.contains("price=1000.0"));
    }

    @Test
    void serializesAndDeserializes() throws Exception {
        Product original = new Product("1111", "Car", "Super car", 1000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        Product deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (Product) ois.readObject();
        }

        assertEquals(original.getItemId(), deserialized.getItemId());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getDesc(), deserialized.getDesc());
        assertEquals(original.getPrice(), deserialized.getPrice());
    }
}
