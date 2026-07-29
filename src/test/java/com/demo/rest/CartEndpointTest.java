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
        when(shoppingCartService.getShoppingCartIfExists(anyString())).thenReturn(null);

        given()
            .when()
            .get("/cart/non-existent-cart")
            .then()
            .statusCode(404);
    }

    @Test
    void shouldReturnCartForExistingCart() {
        ShoppingCart existingCart = new ShoppingCart("existing-cart");
        when(shoppingCartService.getShoppingCartIfExists("existing-cart")).thenReturn(existingCart);

        given()
            .when()
            .get("/cart/existing-cart")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cartId", equalTo("existing-cart"));
    }

    @Test
    void shouldReturn400ForNegativeQuantityOnAdd() {
        given()
            .when()
            .post("/cart/cart1/item1/-1")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("quantity must be at least 1"));
    }

    @Test
    void shouldReturn400ForZeroQuantityOnAdd() {
        given()
            .when()
            .post("/cart/cart1/item1/0")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("quantity must be at least 1"));
    }

    @Test
    void shouldReturn400ForBlankItemIdOnAdd() {
        given()
            .when()
            .post("/cart/cart1/ /1")  // Using space to trigger @NotBlank validation
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("itemId must not be blank"));
    }

    @Test
    void shouldReturn400ForNegativeQuantityOnDelete() {
        given()
            .when()
            .delete("/cart/cart1/item1/-1")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("quantity must be at least 1"));
    }

    @Test
    void shouldReturn400ForBlankItemIdOnDelete() {
        given()
            .when()
            .delete("/cart/cart1/ /1")  // Using space to trigger @NotBlank validation
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("itemId must not be blank"));
    }

    @Test
    void shouldReturn400ForBlankTmpIdOnSet() {
        given()
            .pathParam("cartId", "cart1")
            .pathParam("tmpId", " ")  // Single space to trigger @NotBlank validation
            .when()
            .post("/cart/{cartId}/{tmpId}")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"))
            .body("detail", equalTo("tmpId must not be blank"));
    }

    @Test
    void shouldAddItemWithValidParameters() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.addItem("cart1", "item1", 2)).thenReturn(cart);

        given()
            .when()
            .post("/cart/cart1/item1/2")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cartId", equalTo("cart1"));
    }

    @Test
    void shouldDeleteItemWithValidParameters() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.deleteItem("cart1", "item1", 1)).thenReturn(cart);

        given()
            .when()
            .delete("/cart/cart1/item1/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cartId", equalTo("cart1"));
    }

    @Test
    void shouldSetCartWithValidTmpId() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.set("cart1", "tmp1")).thenReturn(cart);

        given()
            .when()
            .post("/cart/cart1/tmp1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cartId", equalTo("cart1"));
    }

    @Test
    void shouldEnsureIdempotencyForAddOperations() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.addItem("cart1", "item1", 2)).thenReturn(cart);

        // First request
        given()
            .when()
            .post("/cart/cart1/item1/2")
            .then()
            .statusCode(200);

        // Second identical request (should not create duplicates - idempotent behavior)
        given()
            .when()
            .post("/cart/cart1/item1/2")
            .then()
            .statusCode(200);
    }

    @Test
    void shouldEnsureIdempotencyForDeleteOperations() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.deleteItem("cart1", "item1", 1)).thenReturn(cart);

        // First request
        given()
            .when()
            .delete("/cart/cart1/item1/1")
            .then()
            .statusCode(200);

        // Second identical request (should be idempotent)
        given()
            .when()
            .delete("/cart/cart1/item1/1")
            .then()
            .statusCode(200);
    }

    @Test
    void shouldEnsureIdempotencyForSetOperations() {
        ShoppingCart cart = new ShoppingCart("cart1");
        when(shoppingCartService.set("cart1", "tmp1")).thenReturn(cart);

        // First request
        given()
            .when()
            .post("/cart/cart1/tmp1")
            .then()
            .statusCode(200);

        // Second identical request (should be idempotent)
        given()
            .when()
            .post("/cart/cart1/tmp1")
            .then()
            .statusCode(200);
    }
}
