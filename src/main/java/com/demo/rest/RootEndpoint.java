package com.demo.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class RootEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String index() {
        return "{\"status\":\"ok\",\"service\":\"cart-service\",\"endpoints\":[\"/api/cart/{cartId}\",\"/api/cart/acceptance-check\"]}";
    }
}


