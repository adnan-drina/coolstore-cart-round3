package com.demo.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.demo.model.ShoppingCart;
import com.demo.service.ShoppingCartService;

@Path("/cart")
public class CartEndpoint {

    private static final long serialVersionUID = -7227732980791688773L;

    private final ShoppingCartService shoppingCartService;

    @Inject
    public CartEndpoint(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GET
    @Path("/acceptance-check")
    @Produces(MediaType.APPLICATION_JSON)
    public String acceptanceCheck() {
        // Get real service state - test that the service can create and return a cart
        ShoppingCart cart = shoppingCartService.getShoppingCart("acceptance-check");
        
        return String.format(
            "{\"status\":\"healthy\",\"service\":\"cart-service\",\"timestamp\":\"%s\"," +
            "\"cartId\":\"%s\",\"cartTotal\":%.2f," +
            "\"message\":\"Acceptance check passed - service operational\"}",
            java.time.Instant.now(),
            cart.getCartId(),
            cart.getCartTotal()
        );
    }

    @GET
    @Path("/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart getCart(@PathParam("cartId") String cartId) {
        return shoppingCartService.getShoppingCart(cartId);
    }

    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart add(@PathParam("cartId") String cartId,
                            @PathParam("itemId") String itemId,
                            @PathParam("quantity") int quantity) {
        return shoppingCartService.addItem(cartId, itemId, quantity);
    }

    @POST
    @Path("/{cartId}/{tmpId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart set(@PathParam("cartId") String cartId,
                            @PathParam("tmpId") String tmpId) {
        return shoppingCartService.set(cartId, tmpId);
    }

    @DELETE
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart delete(@PathParam("cartId") String cartId,
                               @PathParam("itemId") String itemId,
                               @PathParam("quantity") int quantity) {
        return shoppingCartService.deleteItem(cartId, itemId, quantity);
    }

    @POST
    @Path("/checkout/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShoppingCart checkout(@PathParam("cartId") String cartId) {
        return shoppingCartService.checkout(cartId);
    }
}
