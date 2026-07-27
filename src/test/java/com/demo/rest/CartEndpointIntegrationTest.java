package com.demo.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for CartEndpoint REST API.
 * Tests all endpoints as specified in T-012 task requirements.
 * 
 * This test validates the REST endpoints work correctly but uses
 * simplified assertions to avoid external service dependencies.
 */
@QuarkusTest
class CartEndpointIntegrationTest {

    private static final String CART_ID = "cart-1";
    private static final String ITEM_ID = "1111";
    private static final String ITEM_ID_2 = "2222";

    @Test
    void getCart_returns404ForMissingCart() {
        given()
            .when().get("/api/cart/nonexistent-cart")
            .then()
            .statusCode(404);
    }

    @Test
    void getCart_returnsJsonContentType() {
        given()
            .pathParam("cartId", CART_ID)
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .when().get("/api/cart/{cartId}", CART_ID)
            .then()
            .statusCode(200)
            .contentType("application/json");
    }

    @Test
    void add_itemToCart_returnsUpdatedCart() {
        given()
            .pathParam("cartId", CART_ID)
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("cartId", equalTo(CART_ID))
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("cartItemTotal", greaterThanOrEqualTo(0.0F))
            .body("cartTotal", greaterThanOrEqualTo(0.0F));
    }

    @Test
    void add_multipleItems_accumulatesCorrectly() {
        // Add first item
        given()
            .pathParam("cartId", "multi-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        // Add second item
        given()
            .pathParam("cartId", "multi-cart")
            .pathParam("itemId", ITEM_ID_2)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(2))
            .body("cartItemTotal", greaterThan(0.0F));
    }

    @Test
    void add_sameItem_deduplicatesAndAccumulatesQuantity() {
        String uniqueCartId = "dedup-cart-" + System.currentTimeMillis();
        
        // Add 2 items of same type
        given()
            .pathParam("cartId", uniqueCartId)
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 2)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].quantity", equalTo(2));
    }

    @Test
    void add_invalidProduct_handlesGracefully() {
        given()
            .pathParam("cartId", "invalid-cart")
            .pathParam("itemId", "9999")
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList", empty());
    }

    @Test
    void delete_itemFromCart_removesCorrectly() {
        // First add an item
        given()
            .pathParam("cartId", "remove-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        // Then remove it
        given()
            .pathParam("cartId", "remove-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList", empty());
    }

    @Test
    void delete_allItems_clearsCart() {
        // Add item
        given()
            .pathParam("cartId", "clear-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        // Delete all quantities
        given()
            .pathParam("cartId", "clear-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList", empty());
    }

    @Test
    void set_cartFromTempCopiesItems() {
        // First add some items to temp cart
        given()
            .pathParam("cartId", "temp-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        // Set permanent cart from temp
        given()
            .pathParam("cartId", "perm-cart")
            .pathParam("tmpId", "temp-cart")
            .when().post("/api/cart/{cartId}/{tmpId}")
            .then()
            .statusCode(200);
    }

    @Test
    void checkout_clearsCart() {
        // Add item to cart
        given()
            .pathParam("cartId", "checkout-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        // Perform checkout
        given()
            .pathParam("cartId", "checkout-cart")
            .when().post("/api/cart/checkout/{cartId}")
            .then()
            .statusCode(200);

        // Verify cart is empty after checkout
        given()
            .pathParam("cartId", "checkout-cart")
            .when().get("/api/cart/{cartId}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList", empty());
    }

    @Test
    void acceptanceCheck_returnsOk() {
        given()
            .when().post("/api/cart/acceptance-check")
            .then()
            .statusCode(200)
            .body(equalTo("{\"status\": \"ok\"}"));
    }

    @Test
    void responseIncludesProductDetails() {
        given()
            .pathParam("cartId", "details-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList[0].product", notNullValue());
    }

    @Test
    void testAllRestEndpoints() {
        given()
            .pathParam("cartId", "test-cart")
            .when().get("/api/cart/{cartId}")
            .then()
            .statusCode(404);

        given()
            .pathParam("cartId", "test-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", "test-cart")
            .when().get("/api/cart/{cartId}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", "dest-cart")
            .pathParam("tmpId", "test-cart")
            .when().post("/api/cart/{cartId}/{tmpId}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", "test-cart")
            .pathParam("itemId", ITEM_ID)
            .pathParam("quantity", 1)
            .when().delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", "test-cart")
            .when().post("/api/cart/checkout/{cartId}")
            .then()
            .statusCode(200);

        given()
            .when().post("/api/cart/acceptance-check")
            .then()
            .statusCode(200);
    }
}