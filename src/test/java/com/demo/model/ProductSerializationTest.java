package com.demo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ProductSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesToJson() throws JsonProcessingException {
        Product product = new Product("2222", "Bike", "Super bike", 200);

        String json = mapper.writeValueAsString(product);

        assertTrue(json.contains("\"itemId\":\"2222\""));
        assertTrue(json.contains("\"name\":\"Bike\""));
        assertTrue(json.contains("\"desc\":\"Super bike\""));
        assertTrue(json.contains("\"price\":200.0"));
    }

    @Test
    void deserializesFromJson() throws IOException {
        String json = "{\"itemId\":\"1111\",\"name\":\"Car\",\"desc\":\"Super car\",\"price\":1000.0}";

        Product product = mapper.readValue(json, Product.class);

        assertEquals("1111", product.getItemId());
        assertEquals("Car", product.getName());
        assertEquals("Super car", product.getDesc());
        assertEquals(1000.0, product.getPrice());
    }

    @Test
    void javaSerializationRoundTrip() throws IOException, ClassNotFoundException {
        Product original = new Product("2222", "Bike", "Super bike", 200);

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

    @Test
    void defaultConstructorCreatesEmptyProduct() {
        Product product = new Product();

        assertNull(product.getItemId());
        assertNull(product.getName());
        assertNull(product.getDesc());
        assertEquals(0.0, product.getPrice());
    }

    @Test
    void settersUpdateFields() {
        Product product = new Product();
        product.setItemId("3333");
        product.setName("Boat");
        product.setDesc("Speed boat");
        product.setPrice(500);

        assertEquals("3333", product.getItemId());
        assertEquals("Boat", product.getName());
        assertEquals("Speed boat", product.getDesc());
        assertEquals(500.0, product.getPrice());
    }

    @Test
    void implementsSerializable() {
        Product product = new Product("2222", "Bike", "Super bike", 200);

        assertInstanceOf(java.io.Serializable.class, product);
    }

    @Test
    void toStringContainsFields() {
        Product product = new Product("2222", "Bike", "Super bike", 200);

        String str = product.toString();

        assertTrue(str.contains("2222"));
        assertTrue(str.contains("Bike"));
        assertTrue(str.contains("Super bike"));
        assertTrue(str.contains("200.0"));
    }
}
