package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import com.demo.ProductsObjectMother;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ShoppingCartServiceTest {

    @Inject
    ShoppingCartService shoppingCartService;

    @Test
    public void should_get_initialized_shopping_cart_in_case_of_not_exists() {
        final ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1111");

        assertEquals(0.0, shoppingCart.getCartItemPromoSavings());
        assertEquals(0.0, shoppingCart.getCartItemTotal());
        assertEquals(0.0, shoppingCart.getShippingPromoSavings());
        assertEquals(0.0, shoppingCart.getCartTotal());
    }

    @Test
    public void should_calculate_price_of_cart() {
        final ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1");
        ShoppingCartItem sci = new ShoppingCartItem();
        sci.setProduct(new Product("1111", "Car", "Super car", 1000));
        sci.setQuantity(2);
        sci.setPrice(1000);
        shoppingCart.addShoppingCartItem(sci);

        shoppingCartService.priceShoppingCart(shoppingCart);

        assertEquals(0.0, shoppingCart.getCartItemPromoSavings());
        assertEquals(2000.0, shoppingCart.getCartItemTotal());
        assertEquals(-10.99, shoppingCart.getShippingPromoSavings());
        assertEquals(2000.0, shoppingCart.getCartTotal());
    }
}
