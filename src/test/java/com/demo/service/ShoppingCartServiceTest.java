package com.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

class ShoppingCartServiceTest {

    @Mock
    CatalogService catalogService;

    ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shoppingCartService = new ShoppingCartServiceImpl(
            new ShippingService(),
            catalogService,
            new PromoService());
    }

    @Test
    void should_get_initialized_shopping_cart_in_case_of_not_exists() {
        ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1111");

        assertEquals(0.0, shoppingCart.getCartItemPromoSavings(), 0.001);
        assertEquals(0.0, shoppingCart.getCartItemTotal(), 0.001);
        assertEquals(0.0, shoppingCart.getShippingPromoSavings(), 0.001);
        assertEquals(0.0, shoppingCart.getCartTotal(), 0.001);
    }

    @Test
    void should_calculate_price_of_cart() {
        ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1");
        ShoppingCartItem sci = new ShoppingCartItem();
        sci.setProduct(new Product("1111", "Car", "Super car", 1000));
        sci.setQuantity(2);
        sci.setPrice(1000);
        shoppingCart.addShoppingCartItem(sci);

        shoppingCartService.priceShoppingCart(shoppingCart);

        assertEquals(0.0, shoppingCart.getCartItemPromoSavings(), 0.001);
        assertEquals(2000.0, shoppingCart.getCartItemTotal(), 0.001);
        assertEquals(-10.99, shoppingCart.getShippingPromoSavings(), 0.001);
        assertEquals(2000.0, shoppingCart.getCartTotal(), 0.001);
    }

    @Test
    void should_get_product_id() {
        when(catalogService.products()).thenReturn(ProductsObjectMother.createVehicleProducts());

        Product product = shoppingCartService.getProduct("2222");
        assertEquals("2222", product.getItemId());
        assertEquals("Bike", product.getName());
        assertEquals("Super bike", product.getDesc());
        assertEquals(200, product.getPrice(), 0.001);
    }
}
