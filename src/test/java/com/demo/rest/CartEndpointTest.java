package com.demo.rest;

import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import com.demo.model.ShoppingCart;
import com.demo.service.ShoppingCartService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class CartEndpointTest {

    @InjectMock
    ShoppingCartService shoppingCartService;

    @Test
    void shouldReturn404ForNonExistentCart() {
        // Given cart service returns null for non-existent cart
        when(shoppingCartService.getShoppingCartIfExists(anyString())).thenReturn(null);

        // When making GET request to /cart/non-existent-cart
        given()
            .when()
            .get("/cart/non-existent-cart")
            .then()
            .statusCode(404);
    }

    @Test
    void shouldReturnCartForExistingCart() {
        // Given cart service returns a cart for existing cart
        ShoppingCart existingCart = new ShoppingCart("existing-cart");
        when(shoppingCartService.getShoppingCartIfExists("existing-cart")).thenReturn(existingCart);

        // When making GET request to /cart/existing-cart
        given()
            .when()
            .get("/cart/existing-cart")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cartId", equalTo("existing-cart"));
    }
}
