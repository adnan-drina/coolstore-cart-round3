package com.demo.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class IndexResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok(
            "<html>" +
            "<head><title>Coolstore Cart Service</title></head>" +
            "<body>" +
            "<h1>Coolstore Cart Service</h1>" +
            "<ul>" +
            "<li><a href=\"/api/cart\">Cart API</a></li>" +
            "<li><a href=\"/q/health\">Health</a></li>" +
            "</ul>" +
            "</body>" +
            "</html>")
            .build();
    }
}
