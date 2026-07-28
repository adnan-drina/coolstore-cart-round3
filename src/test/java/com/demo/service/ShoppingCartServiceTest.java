package com.demo.service;

import com.demo.ProductsObjectMother;
import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ShoppingCartServiceTest {

    private CatalogService catalogService;
    private ShoppingCartService shoppingCartService;

    @BeforeEach
    public void setUp() {
        catalogService = mock(CatalogService.class);
        shoppingCartService = new ShoppingCartServiceImpl();
    }

    @Test
    public void should_get_initialized_shopping_cart_in_case_of_not_exists() {
        final ShoppingCart shoppingCart = shoppingCartService.getShoppingCart("1111");

        assertThat(shoppingCart)
            .returns(0.0, ShoppingCart::getCartItemPromoSavings)
            .returns(0.0, ShoppingCart::getCartItemTotal)
            .returns(0.0, ShoppingCart::getShippingPromoSavings)
            .returns(0.0, ShoppingCart::getCartTotal);
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

        assertThat(shoppingCart)
            .returns(0.0, ShoppingCart::getCartItemPromoSavings)
            .returns(2000.0, ShoppingCart::getCartItemTotal)
            .returns(-10.99, ShoppingCart::getShippingPromoSavings)
            .returns(2000.0, ShoppingCart::getCartTotal);
    }

    @Test
    public void should_get_product_id() {
        given(this.catalogService.products()).willReturn(ProductsObjectMother.createVehicleProducts());

        final Product product = shoppingCartService.getProduct("2222");
        assertThat(product)
            .isEqualToIgnoringNullFields(new Product("2222", "Bike", "Super bike", 200));
    }
}