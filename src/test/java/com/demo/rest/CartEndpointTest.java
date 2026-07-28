package com.demo.rest;

import com.demo.model.ShoppingCart;
import com.demo.service.ShoppingCartService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CartEndpointTest {

    @Inject
    ShoppingCartService shoppingCartService;

    @Test
    void should_return_acceptance_check() {
        given()
            .when().get("/api/cart/acceptance-check")
            .then()
            .statusCode(200)
            .body(containsString("status"), containsString("healthy"));
    }

    @Test
    void should_get_cart_by_id() {
        String cartId = "test-cart-123";
        
        ShoppingCart cart = given()
            .when().get("/api/cart/{cartId}", cartId)
            .then()
            .statusCode(200)
            .extract()
            .as(ShoppingCart.class);
        
        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }

    @Test
    void should_add_item_to_cart() {
        String cartId = "test-cart-456";
        String itemId = "329299";
        int quantity = 2;

        ShoppingCart cart = given()
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}", cartId, itemId, quantity)
            .then()
            .statusCode(200)
            .extract()
            .as(ShoppingCart.class);

        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }

    @Test
    void should_delete_item_from_cart() {
        String cartId = "test-cart-789";
        String itemId = "329299";
        int quantity = 1;

        ShoppingCart cart = given()
            .when().delete("/api/cart/{cartId}/{itemId}/{quantity}", cartId, itemId, quantity)
            .then()
            .statusCode(200)
            .extract()
            .as(ShoppingCart.class);

        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }

    @Test
    void should_checkout_cart() {
        String cartId = "test-cart-checkout";

        ShoppingCart cart = given()
            .when().post("/api/cart/checkout/{cartId}", cartId)
            .then()
            .statusCode(200)
            .extract()
            .as(ShoppingCart.class);

        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }

    @Test
    void should_set_cart_contents() {
        String cartId = "target-cart";
        String tmpId = "source-cart";

        ShoppingCart cart = given()
            .when().post("/api/cart/{cartId}/{tmpId}", cartId, tmpId)
            .then()
            .statusCode(200)
            .extract()
            .as(ShoppingCart.class);

        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
    }
}