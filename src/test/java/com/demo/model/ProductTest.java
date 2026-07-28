package com.demo.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

  @Test
  void defaultConstructorInitializesNullStringsAndZeroPrice() {
    Product product = new Product();

    assertAll(
      () -> assertNull(product.getItemId()),
      () -> assertNull(product.getName()),
      () -> assertNull(product.getDesc()),
      () -> assertEquals(0.0, product.getPrice())
    );
  }

  @Test
  void parameterizedConstructorSetsAllProperties() {
    Product product = new Product("item-1", "Widget", "A fine widget", 19.99);

    assertAll(
      () -> assertEquals("item-1", product.getItemId()),
      () -> assertEquals("Widget", product.getName()),
      () -> assertEquals("A fine widget", product.getDesc()),
      () -> assertEquals(19.99, product.getPrice())
    );
  }

  @Test
  void settersUpdateProperties() {
    Product product = new Product();

    product.setItemId("item-2");
    product.setName("Gadget");
    product.setDesc("A fine gadget");
    product.setPrice(29.99);

    assertAll(
      () -> assertEquals("item-2", product.getItemId()),
      () -> assertEquals("Gadget", product.getName()),
      () -> assertEquals("A fine gadget", product.getDesc()),
      () -> assertEquals(29.99, product.getPrice())
    );
  }

  @Test
  void gettersReturnReferencedValues() {
    String itemId = "item-3";
    String name = "Doohickey";

    Product product = new Product();
    product.setItemId(itemId);
    product.setName(name);

    assertSame(itemId, product.getItemId());
    assertSame(name, product.getName());
  }

  @Test
  void toStringIncludesAllProperties() {
    Product product = new Product("item-4", "Thingamajig", "A thing", 9.99);
    String str = product.toString();

    assertTrue(str.contains("itemId=item-4"));
    assertTrue(str.contains("name=Thingamajig"));
    assertTrue(str.contains("desc=A thing"));
    assertTrue(str.contains("price=9.99"));
  }

  @Test
  void toStringHandlesNullProperties() {
    Product product = new Product();
    String str = product.toString();

    assertTrue(str.contains("itemId=null"));
    assertTrue(str.contains("name=null"));
    assertTrue(str.contains("desc=null"));
    assertTrue(str.contains("price=0.0"));
  }

  @Test
  void implementsSerializable() {
    Product product = new Product();
    assertTrue(product instanceof Serializable);
  }

  @Test
  void serializableRoundTripPreservesState() throws Exception {
    Product original = new Product("item-5", "Gizmo", "A gizmo", 49.99);

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    Product deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (Product) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertEquals(original.getItemId(), deserialized.getItemId()),
      () -> assertEquals(original.getName(), deserialized.getName()),
      () -> assertEquals(original.getDesc(), deserialized.getDesc()),
      () -> assertEquals(original.getPrice(), deserialized.getPrice())
    );
  }

  @Test
  void serializableRoundTripWithNullFields() throws Exception {
    Product original = new Product();

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    Product deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (Product) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertNull(deserialized.getItemId()),
      () -> assertNull(deserialized.getName()),
      () -> assertNull(deserialized.getDesc()),
      () -> assertEquals(0.0, deserialized.getPrice())
    );
  }

  @Test
  void serialVersionUIDMatchesLegacy() throws Exception {
    var field = Product.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    assertEquals(-7304814269819778382L, field.getLong(null));
  }
}
