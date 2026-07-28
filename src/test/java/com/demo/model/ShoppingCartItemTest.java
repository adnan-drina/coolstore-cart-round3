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

class ShoppingCartItemTest {

  @Test
  void defaultConstructorInitializesDefaults() {
    ShoppingCartItem item = new ShoppingCartItem();

    assertAll(
      () -> assertEquals(0.0, item.getPrice()),
      () -> assertEquals(0, item.getQuantity()),
      () -> assertEquals(0.0, item.getPromoSavings()),
      () -> assertNull(item.getProduct())
    );
  }

  @Test
  void settersUpdateProperties() {
    ShoppingCartItem item = new ShoppingCartItem();

    item.setPrice(15.5);
    item.setQuantity(3);
    item.setPromoSavings(2.0);

    Product product = new Product("item-1", "Widget", "A widget", 15.5);
    item.setProduct(product);

    assertAll(
      () -> assertEquals(15.5, item.getPrice()),
      () -> assertEquals(3, item.getQuantity()),
      () -> assertEquals(2.0, item.getPromoSavings()),
      () -> assertEquals(product, item.getProduct())
    );
  }

  @Test
  void gettersReturnReferencedValues() {
    ShoppingCartItem item = new ShoppingCartItem();

    Product product = new Product("item-2", "Gadget", "A gadget", 29.99);
    item.setProduct(product);

    assertSame(product, item.getProduct());
  }

  @Test
  void productAssociationCanBeReplaced() {
    ShoppingCartItem item = new ShoppingCartItem();

    Product first = new Product("item-1", "Widget", "A widget", 10.0);
    item.setProduct(first);
    assertSame(first, item.getProduct());

    Product second = new Product("item-2", "Gadget", "A gadget", 20.0);
    item.setProduct(second);
    assertSame(second, item.getProduct());
  }

  @Test
  void productAssociationCanBeCleared() {
    ShoppingCartItem item = new ShoppingCartItem();

    Product product = new Product("item-1", "Widget", "A widget", 10.0);
    item.setProduct(product);
    item.setProduct(null);

    assertNull(item.getProduct());
  }

  @Test
  void toStringIncludesAllProperties() {
    Product product = new Product("item-1", "Widget", "A widget", 15.5);
    ShoppingCartItem item = new ShoppingCartItem();
    item.setPrice(15.5);
    item.setQuantity(2);
    item.setPromoSavings(1.5);
    item.setProduct(product);

    String str = item.toString();

    assertTrue(str.contains("price=15.5"));
    assertTrue(str.contains("quantity=2"));
    assertTrue(str.contains("promoSavings=1.5"));
    assertTrue(str.contains("product="));
  }

  @Test
  void toStringHandlesNullProduct() {
    ShoppingCartItem item = new ShoppingCartItem();
    String str = item.toString();

    assertTrue(str.contains("price=0.0"));
    assertTrue(str.contains("quantity=0"));
    assertTrue(str.contains("promoSavings=0.0"));
    assertTrue(str.contains("product=null"));
  }

  @Test
  void implementsSerializable() {
    ShoppingCartItem item = new ShoppingCartItem();
    assertTrue(item instanceof Serializable);
  }

  @Test
  void serializableRoundTripPreservesState() throws Exception {
    Product product = new Product("item-1", "Widget", "A widget", 15.5);
    ShoppingCartItem original = new ShoppingCartItem();
    original.setPrice(15.5);
    original.setQuantity(3);
    original.setPromoSavings(2.0);
    original.setProduct(product);

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    ShoppingCartItem deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (ShoppingCartItem) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertEquals(original.getPrice(), deserialized.getPrice()),
      () -> assertEquals(original.getQuantity(), deserialized.getQuantity()),
      () -> assertEquals(original.getPromoSavings(), deserialized.getPromoSavings()),
      () -> assertNotNull(deserialized.getProduct()),
      () -> assertEquals(original.getProduct().getItemId(), deserialized.getProduct().getItemId()),
      () -> assertEquals(original.getProduct().getName(), deserialized.getProduct().getName())
    );
  }

  @Test
  void serializableRoundTripWithNullProduct() throws Exception {
    ShoppingCartItem original = new ShoppingCartItem();
    original.setPrice(10.0);
    original.setQuantity(1);

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    ShoppingCartItem deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (ShoppingCartItem) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertEquals(10.0, deserialized.getPrice()),
      () -> assertEquals(1, deserialized.getQuantity()),
      () -> assertEquals(0.0, deserialized.getPromoSavings()),
      () -> assertNull(deserialized.getProduct())
    );
  }

  @Test
  void serialVersionUIDMatchesLegacy() throws Exception {
    var field = ShoppingCartItem.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    assertEquals(6964558044240061049L, field.getLong(null));
  }
}
