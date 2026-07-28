package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class ShoppingCartServiceCharacterizationTest {

    @Mock
    CatalogService catalogService;

    PromoService promoService;
    ShippingService shippingService;
    ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        promoService = new PromoService();
        shippingService = new ShippingService();
        shoppingCartService = new ShoppingCartServiceImpl(shippingService, promoService, catalogService);
        shoppingCartService.init();
    }

    @Test
    void appliesPromotionalDiscountAtItemLevel() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("329299", "Promo Item", "Has 25% discount", 100)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "329299", 1);

        assertEquals(1, cart.getShoppingCartItemList().size());

        assertEquals(-25.0, cart.getCartItemPromoSavings());
        assertEquals(75.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(-8.99, cart.getShippingPromoSavings());
        assertEquals(75.0, cart.getCartTotal());
    }

    @Test
    void appliesPromotionalDiscountWithMultipleQuantities() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("329299", "Promo Item", "Has 25% discount", 100)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "329299", 3);

        assertEquals(1, cart.getShoppingCartItemList().size());

        assertEquals(-75.0, cart.getCartItemPromoSavings());
        assertEquals(225.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(-10.99, cart.getShippingPromoSavings());
        assertEquals(225.0, cart.getCartTotal());
    }

    @Test
    void deduplicatesLineItemsWithSameProductId() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("1111", "Car", "Super car", 1000)
        ));

        shoppingCartService.addItem("cart1", "1111", 2);
        ShoppingCart cart = shoppingCartService.addItem("cart1", "1111", 3);

        assertEquals(1, cart.getShoppingCartItemList().size());
        ShoppingCartItem item = cart.getShoppingCartItemList().get(0);

        assertEquals(5, item.getQuantity());
        assertEquals(5000.0, cart.getCartItemTotal());
    }

    @Test
    void transfersItemsFromTempCartToPersistentCart() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("1111", "Car", "Super car", 1000),
                new Product("2222", "Bike", "Super bike", 200)
        ));

        shoppingCartService.addItem("tmp1", "1111", 1);
        shoppingCartService.addItem("tmp1", "2222", 2);

        ShoppingCart cart = shoppingCartService.set("persistent1", "tmp1");

        assertEquals("persistent1", cart.getCartId());
        assertEquals(2, cart.getShoppingCartItemList().size());
        assertEquals(1400.0, cart.getCartItemTotal());
    }

    @Test
    void transferResetsTargetCartBeforeMerging() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("1111", "Car", "Super car", 1000),
                new Product("2222", "Bike", "Super bike", 200)
        ));

        shoppingCartService.addItem("persistent1", "1111", 5);
        shoppingCartService.addItem("tmp1", "2222", 1);

        ShoppingCart cart = shoppingCartService.set("persistent1", "tmp1");

        assertEquals(1, cart.getShoppingCartItemList().size());
        ShoppingCartItem item = cart.getShoppingCartItemList().get(0);
        assertEquals("2222", item.getProduct().getItemId());
        assertEquals(200.0, cart.getCartItemTotal());
    }

    @Test
    void freeShippingPromotionAppliesForCartOverSeventyFive() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("1111", "Car", "Super car", 1000)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "1111", 1);

        assertEquals(1000.0, cart.getCartItemTotal());
        assertEquals(0.0, cart.getShippingTotal());
        assertEquals(-10.99, cart.getShippingPromoSavings());
        assertEquals(1000.0, cart.getCartTotal());
    }

    @Test
    void shippingChargesApplyForCartUnderSeventyFive() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("9999", "Cheap Item", "Low price", 10)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "9999", 1);

        assertEquals(10.0, cart.getCartItemTotal());
        assertEquals(2.99, cart.getShippingTotal());
        assertEquals(0.0, cart.getShippingPromoSavings());
        assertEquals(12.99, cart.getCartTotal());
    }

    @Test
    void shippingTierTwentyFiveToFifty() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("9999", "Item", "Mid range", 30)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "9999", 1);

        assertEquals(4.99, cart.getShippingTotal());
    }

    @Test
    void shippingTierFiftyToSeventyFive() {
        given(catalogService.products()).willReturn(Arrays.asList(
                new Product("9999", "Item", "Higher range", 60)
        ));

        ShoppingCart cart = shoppingCartService.addItem("cart1", "9999", 1);

        assertEquals(6.99, cart.getShippingTotal());
    }
}
