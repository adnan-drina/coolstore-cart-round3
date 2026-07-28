package com.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PromotionTest {

  @Test
  void defaultConstructorInitializesNullItemIdAndZeroPercentOff() {
    Promotion promotion = new Promotion();

    assertAll(
      () -> assertNull(promotion.getItemId()),
      () -> assertEquals(0.0, promotion.getPercentOff())
    );
  }

  @Test
  void parameterizedConstructorSetsAllProperties() {
    Promotion promotion = new Promotion("item-1", 0.25);

    assertAll(
      () -> assertEquals("item-1", promotion.getItemId()),
      () -> assertEquals(0.25, promotion.getPercentOff())
    );
  }

  @Test
  void settersUpdateProperties() {
    Promotion promotion = new Promotion();
    promotion.setItemId("item-2");
    promotion.setPercentOff(0.5);

    assertAll(
      () -> assertEquals("item-2", promotion.getItemId()),
      () -> assertEquals(0.5, promotion.getPercentOff())
    );
  }

  @Test
  void toStringContainsItemIdAndPercentOff() {
    Promotion promotion = new Promotion("item-3", 0.1);

    assertEquals("Promotion [itemId=item-3, percentOff=0.1]", promotion.toString());
  }
}
