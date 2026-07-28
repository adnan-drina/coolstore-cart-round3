package com.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {

    @Test
    void should_create_promotion_with_default_constructor() {
        Promotion promotion = new Promotion();
        
        assertNull(promotion.getItemId());
        assertEquals(0.0, promotion.getPercentOff(), 0.001);
    }

    @Test
    void should_create_promotion_with_parameters() {
        String itemId = "test-item-123";
        double percentOff = 0.25;
        
        Promotion promotion = new Promotion(itemId, percentOff);
        
        assertEquals(itemId, promotion.getItemId());
        assertEquals(percentOff, promotion.getPercentOff(), 0.001);
    }

    @Test
    void should_set_and_get_item_id() {
        Promotion promotion = new Promotion();
        String itemId = "test-item-456";
        
        promotion.setItemId(itemId);
        
        assertEquals(itemId, promotion.getItemId());
    }

    @Test
    void should_set_and_get_percent_off() {
        Promotion promotion = new Promotion();
        double percentOff = 0.30;
        
        promotion.setPercentOff(percentOff);
        
        assertEquals(percentOff, promotion.getPercentOff(), 0.001);
    }

    @Test
    void should_generate_to_string_with_all_fields() {
        String itemId = "test-item-789";
        double percentOff = 0.20;
        
        Promotion promotion = new Promotion(itemId, percentOff);
        String result = promotion.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("itemId=" + itemId));
        assertTrue(result.contains("percentOff=" + percentOff));
    }

    @Test
    void should_handle_null_values_gracefully() {
        Promotion promotion = new Promotion();
        
        promotion.setItemId(null);
        promotion.setPercentOff(0.0);
        
        assertNull(promotion.getItemId());
        assertEquals(0.0, promotion.getPercentOff(), 0.001);
    }

    @Test
    void should_handle_edge_case_percentages() {
        Promotion zeroPromo = new Promotion("item1", 0.0);
        Promotion fullPromo = new Promotion("item2", 1.0);
        Promotion hundredPromo = new Promotion("item3", 100.0);
        
        assertEquals(0.0, zeroPromo.getPercentOff(), 0.001);
        assertEquals(1.0, fullPromo.getPercentOff(), 0.001);
        assertEquals(100.0, hundredPromo.getPercentOff(), 0.001);
    }
}