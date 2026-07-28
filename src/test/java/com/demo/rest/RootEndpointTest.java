package com.demo.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RootEndpointTest {

    @Test
    void should_return_index_page() {
        String response = given()
            .when().get("/")
            .then()
            .statusCode(200)
            .extract()
            .asString();
        
        assertNotNull(response);
        assertTrue(response.contains("status"));
        assertTrue(response.contains("ok"));
        assertTrue(response.contains("cart-service"));
        assertTrue(response.contains("/api/cart/"));
        assertTrue(response.contains("/api/cart/acceptance-check"));
    }
}