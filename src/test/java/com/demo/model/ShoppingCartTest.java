package com.demo.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartTest {

  @Test
  void defaultConstructorInitializesDefaults() {
    ShoppingCart cart = new ShoppingCart();

    assertAll(
      () -> assertEquals(0.0, cart.getCartItemTotal()),
      () -> assertEquals(0.0, cart.getCartItemPromoSavings()),
      () -> assertEquals(0.0, cart.getShippingTotal()),
      () -> assertEquals(0.0, cart.getShippingPromoSavings()),
      () -> assertEquals(0.0, cart.getCartTotal()),
      () -> assertNull(cart.getCartId()),
      () -> assertNotNull(cart.getShoppingCartItemList()),
      () -> assertTrue(cart.getShoppingCartItemList().isEmpty())
    );
  }

  @Test
  void parameterizedConstructorSetsCartId() {
    ShoppingCart cart = new ShoppingCart("cart-abc");

    assertEquals("cart-abc", cart.getCartId());
  }

  @Test
  void settersUpdateProperties() {
    ShoppingCart cart = new ShoppingCart();

    cart.setCartId("cart-1");
    cart.setCartItemTotal(100.0);
    cart.setCartItemPromoSavings(10.0);
    cart.setShippingTotal(5.0);
    cart.setShippingPromoSavings(1.0);
    cart.setCartTotal(94.0);

    assertAll(
      () -> assertEquals("cart-1", cart.getCartId()),
      () -> assertEquals(100.0, cart.getCartItemTotal()),
      () -> assertEquals(10.0, cart.getCartItemPromoSavings()),
      () -> assertEquals(5.0, cart.getShippingTotal()),
      () -> assertEquals(1.0, cart.getShippingPromoSavings()),
      () -> assertEquals(94.0, cart.getCartTotal())
    );
  }

  @Test
  void gettersReturnReferencedValues() {
    ShoppingCart cart = new ShoppingCart();
    String cartId = "cart-ref";
    cart.setCartId(cartId);

    assertSame(cartId, cart.getCartId());
  }

  @Test
  void addShoppingCartItemAddsNonNullItem() {
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
  void addShoppingCartItemAccumulatesMultipleItems() {
    ShoppingCart cart = new ShoppingCart();

    ShoppingCartItem item1 = new ShoppingCartItem();
    ShoppingCartItem item2 = new ShoppingCartItem();

    cart.addShoppingCartItem(item1);
    cart.addShoppingCartItem(item2);

    assertEquals(2, cart.getShoppingCartItemList().size());
    assertSame(item1, cart.getShoppingCartItemList().get(0));
    assertSame(item2, cart.getShoppingCartItemList().get(1));
  }

  @Test
  void removeShoppingCartItemRemovesExistingItem() {
    ShoppingCart cart = new ShoppingCart();
    ShoppingCartItem item = new ShoppingCartItem();
    cart.addShoppingCartItem(item);

    boolean removed = cart.removeShoppingCartItem(item);

    assertTrue(removed);
    assertTrue(cart.getShoppingCartItemList().isEmpty());
  }

  @Test
  void removeShoppingCartItemReturnsFalseForMissingItem() {
    ShoppingCart cart = new ShoppingCart();
    ShoppingCartItem item = new ShoppingCartItem();

    boolean removed = cart.removeShoppingCartItem(item);

    assertFalse(removed);
  }

  @Test
  void removeShoppingCartItemHandlesNull() {
    ShoppingCart cart = new ShoppingCart();
    ShoppingCartItem item = new ShoppingCartItem();
    cart.addShoppingCartItem(item);

    boolean removed = cart.removeShoppingCartItem(null);

    assertFalse(removed);
    assertEquals(1, cart.getShoppingCartItemList().size());
  }

  @Test
  void resetShoppingCartItemListClearsAndReplacesList() {
    ShoppingCart cart = new ShoppingCart();
    cart.addShoppingCartItem(new ShoppingCartItem());
    cart.addShoppingCartItem(new ShoppingCartItem());

    cart.resetShoppingCartItemList();

    assertTrue(cart.getShoppingCartItemList().isEmpty());
  }

  @Test
  void setShoppingCartItemListReplacesList() {
    ShoppingCart cart = new ShoppingCart();
    List<ShoppingCartItem> newList = new ArrayList<>();
    ShoppingCartItem item = new ShoppingCartItem();
    newList.add(item);

    cart.setShoppingCartItemList(newList);

    assertSame(newList, cart.getShoppingCartItemList());
    assertEquals(1, cart.getShoppingCartItemList().size());
  }

  @Test
  void toStringIncludesAllProperties() {
    ShoppingCart cart = new ShoppingCart("cart-tostring");
    cart.setCartItemTotal(50.0);
    cart.setCartItemPromoSavings(5.0);
    cart.setShippingTotal(3.0);
    cart.setShippingPromoSavings(0.5);
    cart.setCartTotal(47.5);
    cart.addShoppingCartItem(new ShoppingCartItem());

    String str = cart.toString();

    assertTrue(str.contains("cartId=cart-tostring"));
    assertTrue(str.contains("cartItemTotal=50.0"));
    assertTrue(str.contains("cartItemPromoSavings=5.0"));
    assertTrue(str.contains("shippingTotal=3.0"));
    assertTrue(str.contains("shippingPromoSavings=0.5"));
    assertTrue(str.contains("cartTotal=47.5"));
    assertTrue(str.contains("shoppingCartItemList="));
  }

  @Test
  void toStringHandlesNullAndEmptyDefaults() {
    ShoppingCart cart = new ShoppingCart();
    String str = cart.toString();

    assertTrue(str.contains("cartId=null"));
    assertTrue(str.contains("cartItemTotal=0.0"));
    assertTrue(str.contains("cartItemPromoSavings=0.0"));
    assertTrue(str.contains("shippingTotal=0.0"));
    assertTrue(str.contains("shippingPromoSavings=0.0"));
    assertTrue(str.contains("cartTotal=0.0"));
    assertTrue(str.contains("shoppingCartItemList=[]"));
  }

  @Test
  void implementsSerializable() {
    ShoppingCart cart = new ShoppingCart();
    assertTrue(cart instanceof Serializable);
  }

  @Test
  void serializableRoundTripPreservesState() throws Exception {
    ShoppingCart original = new ShoppingCart("cart-ser");
    original.setCartItemTotal(200.0);
    original.setCartItemPromoSavings(20.0);
    original.setShippingTotal(10.0);
    original.setShippingPromoSavings(2.0);
    original.setCartTotal(188.0);

    ShoppingCartItem item = new ShoppingCartItem();
    Product product = new Product("item-1", "Widget", "A widget", 25.0);
    item.setPrice(25.0);
    item.setQuantity(4);
    item.setProduct(product);
    original.addShoppingCartItem(item);

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    ShoppingCart deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (ShoppingCart) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertEquals(original.getCartId(), deserialized.getCartId()),
      () -> assertEquals(original.getCartItemTotal(), deserialized.getCartItemTotal()),
      () -> assertEquals(original.getCartItemPromoSavings(), deserialized.getCartItemPromoSavings()),
      () -> assertEquals(original.getShippingTotal(), deserialized.getShippingTotal()),
      () -> assertEquals(original.getShippingPromoSavings(), deserialized.getShippingPromoSavings()),
      () -> assertEquals(original.getCartTotal(), deserialized.getCartTotal()),
      () -> assertEquals(1, deserialized.getShoppingCartItemList().size()),
      () -> assertEquals(25.0, deserialized.getShoppingCartItemList().get(0).getPrice()),
      () -> assertEquals(4, deserialized.getShoppingCartItemList().get(0).getQuantity())
    );
  }

  @Test
  void serializableRoundTripWithEmptyCart() throws Exception {
    ShoppingCart original = new ShoppingCart();

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    ShoppingCart deserialized;
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      deserialized = (ShoppingCart) ois.readObject();
    }

    assertAll(
      () -> assertNotNull(deserialized),
      () -> assertNull(deserialized.getCartId()),
      () -> assertEquals(0.0, deserialized.getCartItemTotal()),
      () -> assertEquals(0.0, deserialized.getCartItemPromoSavings()),
      () -> assertEquals(0.0, deserialized.getShippingTotal()),
      () -> assertEquals(0.0, deserialized.getShippingPromoSavings()),
      () -> assertEquals(0.0, deserialized.getCartTotal()),
      () -> assertNotNull(deserialized.getShoppingCartItemList()),
      () -> assertTrue(deserialized.getShoppingCartItemList().isEmpty())
    );
  }

  @Test
  void serialVersionUIDMatchesLegacy() throws Exception {
    var field = ShoppingCart.class.getDeclaredField("serialVersionUID");
    field.setAccessible(true);
    assertEquals(-1108043957592113528L, field.getLong(null));
  }
}
