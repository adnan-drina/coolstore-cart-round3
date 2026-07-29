package com.demo.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
public class RootEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        // Minimal index page returning service status
        return Response.ok("{\"status\":\"ok\",\"service\":\"cart-service\",\"version\":\"1.0.0\"}").build();
    }
}
