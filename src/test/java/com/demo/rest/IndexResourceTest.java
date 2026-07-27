package com.demo.rest;

import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class IndexResourceTest {

    @org.junit.jupiter.api.Test
    void returnsOkWithHtml() {
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .header("Content-Type", containsString("text/html"))
            .body(containsString("Coolstore Cart Service"));
    }
}
